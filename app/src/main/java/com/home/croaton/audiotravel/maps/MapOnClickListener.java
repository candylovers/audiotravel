package com.home.croaton.audiotravel.maps;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.home.croaton.audiotravel.LocationTracker;

import java.util.ArrayList;

public class MapOnClickListener implements GoogleMap.OnMapClickListener
{

    private final ArrayList<Circle> _circles;

    public MapOnClickListener(ArrayList<Circle> circles)
    {
        _circles = circles;
    }

    @Override
    public void onMapClick(LatLng clickPosition)
    {
        for(Circle c : _circles)
        {
            if (LocationTracker.GetDistance(c.getCenter(), clickPosition) <= c.getRadius())
            {
                if (clickPosition.latitude >= c.getCenter().latitude)
                    c.setRadius(c.getRadius() + 1);
                else
                    c.setRadius(c.getRadius() - 1);
            }
        }
    }
}
