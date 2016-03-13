package com.home.croaton.audiotravel.maps;


import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class MapOnClickListener implements MapEventsReceiver
{

    private final ArrayList<Polygon> _circles;

    public MapOnClickListener(ArrayList<Polygon> circles)
    {
        _circles = circles;
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
//        for(Polygon c : _circles)
//        {
//            if (LocationTracker.GetDistance(c., clickPosition) <= c.getRadius())
//            {
//                if (clickPosition.latitude >= c.getCenter().latitude)
//                    c.setRadius(c.getRadius() + 1);
//                else
//                    c.setRadius(c.getRadius() - 1);
//            }
//        }
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        return false;
    }
}
