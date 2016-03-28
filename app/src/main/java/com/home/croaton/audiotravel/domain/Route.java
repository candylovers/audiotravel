package com.home.croaton.audiotravel.domain;

import com.home.croaton.audiotravel.maps.Circle;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Hashtable;

public class Route
{
    private ArrayList<AudioPoint> _audioPoints;
    private ArrayList<Point> _geoPoints;
    private ArrayList<Boolean> _passedPoints;
    private Hashtable<Integer, ArrayList<String>> _pointTrackMapper;

    public Route()
    {
        _pointTrackMapper = new Hashtable<>();
        _geoPoints = new ArrayList<>();
        _audioPoints = new ArrayList<>();
        _passedPoints = new ArrayList<>();
    }

    public void addGeoPoint(int number, GeoPoint position)
    {
        _geoPoints.add(new Point(number, position));
    }

    public void addAudioPoint(AudioPoint audioPoint)
    {
        _audioPoints.add(audioPoint);
        _passedPoints.add(false);
    }

    public ArrayList<Point> geoPoints()
    {
        return (ArrayList<Point>)_geoPoints.clone();
    }

    public ArrayList<AudioPoint> audioPoints()
    {
        return (ArrayList<AudioPoint>)_audioPoints.clone();
    }

    public void markAudioPoint(int pointNumber, boolean passed)
    {
        if (_passedPoints == null) {

        }

        _passedPoints.set(pointNumber, passed);
    }

    public ArrayList<String> getAudiosForPoint(AudioPoint audioPoint)
    {
        return _pointTrackMapper.get(audioPoint.Number);
    }

    public void addAudioTrack(AudioPoint point, String fileName)
    {
        if (_pointTrackMapper.containsKey(point.Number))
        {
            _pointTrackMapper.get(point.Number).add(fileName);
            return;
        }

        ArrayList<String> audioFilesIds = new ArrayList<>();
        audioFilesIds.add(fileName);
        _pointTrackMapper.put(point.Number, audioFilesIds);
    }

    public void updateAudioPoints(ArrayList<Circle> circles, ArrayList<Marker> pointMarkers)
    {
        for(int i = 0; i < _audioPoints.size(); i++)
        {
            _audioPoints.get(i).Radius = circles.get(i).getRadius();
            _audioPoints.get(i).Position = pointMarkers.get(i).getPosition();
        }
    }

    public boolean isAudioPointPassed(Integer number) {
        return _passedPoints.get(number);
    }
}
