package com.home.croaton.audiotravel.maps;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapHelper
{
    public static Marker putMarker(GoogleMap map, LatLng position, Integer resourceId)
    {
        return map.addMarker(new MarkerOptions()
                .position(position)
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(resourceId))
                .draggable(true));
    }

    public static Circle addCircle(GoogleMap map, LatLng position, Integer radius)
    {
        return map.addCircle(new CircleOptions()
                .center(position)
                .radius(radius)
                .strokeColor(0xFFFF0050));
    }

    public static void changeIcon(ArrayList<Marker> audioPointMarkers, Integer index, int resId)
    {
        if (audioPointMarkers.size() > index)
            audioPointMarkers.get(index).setIcon(BitmapDescriptorFactory.fromResource(resId));
    }
}
