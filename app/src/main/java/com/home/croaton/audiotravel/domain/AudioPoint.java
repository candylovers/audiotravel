package com.home.croaton.audiotravel.domain;

import com.google.android.gms.maps.model.LatLng;

public class AudioPoint extends Point
{
    public Integer Radius;
    public boolean Done;

    private Integer _defaultRadius = 10;

    public AudioPoint(LatLng position)
    {
        super(0, position);
        Done = false;
        Radius = _defaultRadius;
    }

    public AudioPoint(LatLng position, int radius)
    {
        super(0, position);
        Done = false;
        Radius = radius;
    }

    public AudioPoint(int number, LatLng position, int radius)
    {
        super(0, position);
        Number = number;
        Done = false;
        Radius = radius;
    }
}
