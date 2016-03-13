package com.home.croaton.audiotravel.maps;


import com.home.croaton.audiotravel.LocationTracker;

import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class MapOnClickListener implements MapEventsReceiver
{

    private final ArrayList<Circle> _circles;
    Callable<Void> _callback;

    public MapOnClickListener(Callable<Void> callback, ArrayList<Circle> circles)
    {
        _circles = circles;
        _callback = callback;
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        boolean shouldCallCallback = false;
        for(Circle c : _circles)
        {
            if (LocationTracker.GetDistance(c.getCenter(), p) <= c.getRadius())
            {
                shouldCallCallback = true;
                if (p.getLatitude() >= c.getCenter().getLatitude())
                    c.setRadius(c.getRadius() + 1);
                else
                    c.setRadius(c.getRadius() - 1);
            }
        }

        if (shouldCallCallback) {
            try {
                _callback.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        return false;
    }
}
