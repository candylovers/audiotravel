package com.home.croaton.audiotravel;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;

public class Route
{
    private ArrayList<AudioPoint> _audioPoints;
    private ArrayList<Point> _geoPoints;

    public Route()
    {
        _geoPoints = new ArrayList<>();
        _audioPoints = new ArrayList<>();
    }

    public void addGeoPoint(LatLng position)
    {
        _geoPoints.add(new Point(_geoPoints.size() + 1, position));
    }

    public void addAudioPoint(AudioPoint audioPoint)
    {
        _audioPoints.add(audioPoint);
    }

    public Point getGeoPoint(int index)
    {
        return _geoPoints.get(index);
    }

    public ArrayList<Point> geoPoints()
    {
        return (ArrayList<Point>)_geoPoints.clone();
    }

    public ArrayList<AudioPoint> audioPoints()
    {
        return (ArrayList<AudioPoint>)_audioPoints.clone();
    }
}
