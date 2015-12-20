package com.home.croaton.audiotravel;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Iterator;

public class Route implements Iterable<Point>
{
    private ArrayList<Point> _points;

    public Route()
    {
        _points = new ArrayList<>();
    }

    public void addPoint(LatLng position)
    {
        _points.add(new Point(_points.size() + 1, position));
    }

    public Point get(int index)
    {
        return _points.get(index);
    }

    @Override
    public Iterator<Point> iterator() {
        return _points.iterator();
    }
}
