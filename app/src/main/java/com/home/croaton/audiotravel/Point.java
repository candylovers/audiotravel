package com.home.croaton.audiotravel;

import com.google.android.gms.maps.model.LatLng;

public class Point
{
    public Integer Number;
    public LatLng Position;

    public Point(int number, LatLng position)
    {
        Number = number;
        Position = position;
    }
}
