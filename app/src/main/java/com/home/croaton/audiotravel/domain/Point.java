package com.home.croaton.audiotravel.domain;

import org.osmdroid.util.GeoPoint;

public class Point
{
    public Integer Number;
    public GeoPoint Position;

    public Point(int number, GeoPoint position)
    {
        Number = number;
        Position = position;
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) {
            return true;
        }

        if (!(o instanceof Point)) {
            return false;
        }

        Point point = (Point) o;

        return point.Position == Position;
    }

    @Override
    public int hashCode()
    {
        return Position.hashCode();
    }
}
