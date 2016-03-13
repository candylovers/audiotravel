package com.home.croaton.audiotravel.domain;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Hashtable;

public class Route
{
    private ArrayList<AudioPoint> _audioPoints;
    private ArrayList<Point> _geoPoints;
    private Hashtable<Point, ArrayList<String>> _pointTrackMapper;

    public Route()
    {
        _pointTrackMapper = new Hashtable<>();
        _geoPoints = new ArrayList<>();
        _audioPoints = new ArrayList<>();
    }

    public void addGeoPoint(GeoPoint position)
    {
        _geoPoints.add(new Point(_geoPoints.size() + 1, position));
    }

    public void addGeoPoint(int number, GeoPoint position)
    {
        _geoPoints.add(new Point(number, position));
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

    public void markAudioPointDone(int pointNumber)
    {
        _audioPoints.get(pointNumber).Done = true;
    }

    public ArrayList<String> getAudiosForPoint(AudioPoint audioPoint)
    {
        return _pointTrackMapper.get(audioPoint);
    }

    public void addAudioTrack(AudioPoint point, String fileName)
    {
        if (_pointTrackMapper.containsKey(point))
        {
            _pointTrackMapper.get(point).add(fileName);
            return;
        }

        ArrayList<String> audioFilesIds = new ArrayList<>();
        audioFilesIds.add(fileName);
        _pointTrackMapper.put(point, audioFilesIds);
    }

    public void updateAudioPoints(ArrayList<Polygon> circles, ArrayList<Marker> pointMarkers)
    {
        for(int i = 0; i < _audioPoints.size(); i++)
        {
            //_audioPoints.get(i).Radius = (int)circles.get(i).getRadius();
            //_audioPoints.get(i).Position = pointMarkers.get(i).getPosition();
        }
    }
}
