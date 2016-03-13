package com.home.croaton.audiotravel.domain;

import org.osmdroid.util.GeoPoint;

public class AudioPoint extends Point
{
    public Integer Radius;
    public boolean Done;

    private Integer _defaultRadius = 10;

    public AudioPoint(GeoPoint position)
    {
        super(0, position);
        Done = false;
        Radius = _defaultRadius;
    }

    public AudioPoint(GeoPoint position, int radius)
    {
        super(0, position);
        Done = false;
        Radius = radius;
    }

    public AudioPoint(int number, GeoPoint position, int radius)
    {
        super(0, position);
        Number = number;
        Done = false;
        Radius = radius;
    }
}
