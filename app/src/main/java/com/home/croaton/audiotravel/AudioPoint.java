package com.home.croaton.audiotravel;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

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
}
