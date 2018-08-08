package com.example.administrator.osmdroidsample.ui.map;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


import com.example.administrator.osmdroidsample.MyLocationProvider;
import com.example.administrator.osmdroidsample.MyMapTileProviderBasic;
import com.example.administrator.osmdroidsample.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MapFragment extends Fragment {

    private MapView mMapView;
    private Timer positionRotationTimer;

    public static final double centerLatitude = 50.257689;
    public static final double centerLongitude = 14.5149403;


    public static MapFragment newInstance() {
        return new MapFragment();
    }

    static boolean rotate = true;
    static boolean position= true;

    @Nullable
    @Override
    public View onCreateView(final @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        copyMapFiles();

        final View v = inflater.inflate(R.layout.map_fragment, container, false);

        Configuration.getInstance().setOsmdroidBasePath(new File(getContext().getFilesDir() + File.separator + "osmdroid"));
        Configuration.getInstance().setOsmdroidTileCache(new File(getContext().getCacheDir() + File.separator + "osmdroid" + File.separator + "tiles"));
        Configuration.getInstance().setDebugMode(false);


        final ITileSource mapTileSource = new XYTileSource("4uMaps", 13, 15, 256, ".png", new String[]{""});
        final MapTileFileArchiveProvider archiveProvider = new MapTileFileArchiveProvider(new SimpleRegisterReceiver(inflater.getContext()), mapTileSource);
        mMapView = new MapView(inflater.getContext(),
                new MapTileProviderArray(
                        mapTileSource,
                        new SimpleRegisterReceiver(inflater.getContext()),
                        new MapTileModuleProviderBase[]{archiveProvider}));
        final LinearLayout mapLayout = v.findViewById(R.id.mapLayout);
        mapLayout.addView(mMapView);


        v.findViewById(R.id.tileadd).setOnClickListener(new View.OnClickListener() {

            boolean toggle = true;
            @Override
            public void onClick(View v) {
                mMapView.getTileProvider().detach();
                toggle ^= true;
                if (toggle) {
                    Log.d("db", "using MapTileFileArchiveProvider");
                    final MapTileFileArchiveProvider archiveProvider = new MapTileFileArchiveProvider(new SimpleRegisterReceiver(inflater.getContext()), mapTileSource);
                    mMapView.setTileProvider(new MapTileProviderArray(
                            mapTileSource,
                            new SimpleRegisterReceiver(getActivity()),
                            new MapTileModuleProviderBase[]{archiveProvider}));
                } else {
                    Log.d("db", "using MapTileProviderBasic");
                    mMapView.setTileProvider(new MyMapTileProviderBasic(getContext(), mapTileSource) );
                }
            }
        });

        v.findViewById(R.id.position).setOnClickListener(new View.OnClickListener() {

            boolean toggle;

            @Override
            public void onClick(View v) {
                position = toggle;
                toggle ^= true;
                if (toggle)
                    removeMyLocationOverlay();
                else
                    addMyLocationOverlay();

            }
        });

        v.findViewById(R.id.rotate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotate ^= true;
            }
        });


        // mMapView.setTileSource(TileSourceFactory.MAPNIK);
        // mMapView.setTileSource(tileSource);
      /*  MapTileProviderBasic outdoorProvider = new MapTileProviderBasic(inflater.getContext(), outdoorSource);
        final TilesOverlay outdoorTilesOverlay = new TilesOverlay(outdoorProvider, inflater.getContext());
        outdoorTilesOverlay.setLoadingBackgroundColor(Color.BLACK);
        mMapView.getOverlays().add(outdoorTilesOverlay);*/


        mMapView.setUseDataConnection(false);
        mMapView.getTileProvider().setUseDataConnection(false);
        mMapView.setBuiltInZoomControls(true);
        mMapView.setMultiTouchControls(true);
        mMapView.setMaxZoomLevel(15d);
        mMapView.setMinZoomLevel(5d);
        mMapView.setBackgroundColor(Color.WHITE);

        final IMapController mapController = mMapView.getController();
        mapController.setZoom(15d);
        mapController.setCenter(new GeoPoint(centerLatitude, centerLongitude));




         addMyLocationOverlay();

        positionRotationTimer = new Timer();
        positionRotationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    getActivity().runOnUiThread(new Runnable() {

                        Location currentUserAndroidLocation = new Location("myLocation");
                        boolean plus;

                        @Override
                        public void run() {

                            final double newLatitude = centerLatitude + (plus ? new Random().nextDouble() : -new Random().nextDouble() / 1000d);
                            final double newLongitude = centerLongitude + (plus ? new Random().nextDouble() : -new Random().nextDouble() / 1000d);
                            plus ^= true;

                            if (rotate)
                                rotateMap(mMapView.getMapOrientation() + 3);


                            if (position && myLocationProvider != null) {
                                currentUserAndroidLocation.setAccuracy(new Random().nextFloat() * 10);
                                currentUserAndroidLocation.setBearing(-mMapView.getMapOrientation() / 2);
                                currentUserAndroidLocation.setLatitude(newLatitude);
                                currentUserAndroidLocation.setLongitude(newLongitude);
                                myLocationProvider.updateLocation(currentUserAndroidLocation);
                            }

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 30, 30);


        return v;
    }

    void removeMyLocationOverlay() {
        mMapView.getOverlays().remove(mLocationOverlay);
    }


    void rotateMap(final float bearing) {
        mMapView.setMapOrientation(bearing);
    }


    MyLocationProvider myLocationProvider;
    MyLocationNewOverlay mLocationOverlay;

    void addMyLocationOverlay() {
        myLocationProvider = new MyLocationProvider();

        mLocationOverlay = new MyLocationNewOverlay(myLocationProvider, mMapView);

        if (!mMapView.getOverlays().contains(mLocationOverlay)) {
            mMapView.getOverlays().add(mLocationOverlay);
        }
        mLocationOverlay.enableMyLocation(myLocationProvider);
        mLocationOverlay.enableFollowLocation();

        mLocationOverlay.setDrawAccuracyEnabled(true);


    }


    void copyMapFiles() {
        new Thread() {
            @Override
            public void run() {
                setName("copyMapFilesThread");
                final File folder = new File(getActivity().getFilesDir() + File.separator + "osmdroid");

                if (folder.exists() || folder.mkdirs()) {
                    p("copying mapfiles");
                    final AssetManager assetManager = getActivity().getAssets();
                    String[] files = null;
                    try {
                        files = assetManager.list("osmdroid");
                    } catch (IOException e) {
                        Log.e("db", "Failed to get asset file list.", e);
                    }
                    if (files != null) {
                        for (String filename : files) {
                            InputStream in = null;
                            OutputStream out = null;
                            try {
                                in = assetManager.open("osmdroid" + File.separator + filename);
                                File outFile = new File(folder, filename);
                                out = new FileOutputStream(outFile);
                                copyFile(in, out);
                                Log.d("db", "successfully copied " + filename + " to " + outFile.toString());
                            } catch (IOException e) {
                                Log.d("db", "Failed to copy asset file: " + filename);
                                e.printStackTrace();
                            } finally {
                                if (in != null) {
                                    try {
                                        in.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (out != null) {
                                    try {
                                        out.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }.run();
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        final byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }


    @Override
    public void onPause() {
        if (mMapView != null) {
            mMapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        if (mMapView != null) {
            mMapView.onResume();
        }
        super.onResume();
    }

    void p(String p) {
        Log.d("db", p);
    }


}
