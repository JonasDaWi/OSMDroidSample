package com.example.administrator.osmdroidsample;

import android.location.Location;

import com.example.administrator.osmdroidsample.ui.map.MapFragment;

import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;


public class MyLocationProvider implements IMyLocationProvider {

    private IMyLocationConsumer listener = null;

    private final Location lastLoc = new Location("Test Location");

    @Override
    public boolean startLocationProvider(IMyLocationConsumer locationListener) {
        listener = locationListener;
        lastLoc.setLatitude(MapFragment.centerLatitude);
        lastLoc.setLatitude(MapFragment.centerLongitude);
        return true;
    }


    @Override
    public Location getLastKnownLocation() {
        return lastLoc;
    }


    public void updateLocation(final Location newLoc) {
        lastLoc.setLatitude(newLoc.getLatitude());
        lastLoc.setLongitude(newLoc.getLongitude());
            if (listener != null) {
                listener.onLocationChanged(newLoc, this);
            }
    }

    @Override
    public void stopLocationProvider() {
    }

    @Override
    public void destroy() {
    }
}