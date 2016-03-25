package com.home.croaton.audiotravel.domain;

import org.osmdroid.util.GeoPoint;

// ToDo: move done out from the audio point
public class AudioPoint extends Point implements Cloneable
{
    public Integer Radius;
    public boolean Done;

    private Integer _defaultRadius = 10;

    public AudioPoint(int number, GeoPoint position, int radius)
    {
        super(0, position);
        Number = number;
        Done = false;
        Radius = radius;
    }

    public AudioPoint(int number, GeoPoint position, int radius, boolean done)
    {
        super(0, position);
        Number = number;
        Done = done;
        Radius = radius;
    }

    @Override
    public Object clone() {
        return new AudioPoint(Number, Position.clone(), Radius, Done);
    }
}
