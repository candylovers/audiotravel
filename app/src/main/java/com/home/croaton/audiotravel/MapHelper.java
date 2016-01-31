package com.home.croaton.audiotravel;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapHelper
{
    public static void putMarker(GoogleMap map, LatLng position, int resourceId)
    {
        map.addMarker(new MarkerOptions()
                .position(position)
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(resourceId)));
    }

    public static void addCircle(GoogleMap map, LatLng position, Integer radius)
    {
        map.addCircle(new CircleOptions()
            .center(position)
            .radius(radius)
            .strokeColor(0xFFFF0050));
    }
}
