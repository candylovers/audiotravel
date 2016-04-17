package com.home.croaton.followme.domain;

import org.osmdroid.util.GeoPoint;

public class AudioPoint extends Point implements Cloneable
{
    public Integer Radius;

    public AudioPoint(int number, GeoPoint position, int radius)
    {
        super(0, position);
        Number = number;
        Radius = radius;
    }

    @Override
    public Object clone() {
        return new AudioPoint(Number, Position.clone(), Radius);
    }
}
