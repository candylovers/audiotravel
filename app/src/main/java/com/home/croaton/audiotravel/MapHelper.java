package com.home.croaton.audiotravel;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
}
