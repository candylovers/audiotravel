package com.home.croaton.audiotravel;

import android.net.Uri;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Hashtable;

public class RouteController
{
    // ToDo: this should be audio factory
    private Hashtable<Point, ArrayList<Integer>> _pointTrackMapper;
    private Route _route;
    public RouteController()
    {
        _pointTrackMapper = new Hashtable<>();

        FillDemoRoute();
    }

    private void FillDemoRoute()
    {
        _route = new Route();
        _route.addGeoPoint(new LatLng(59.32312, 18.06767));
        _route.addGeoPoint(new LatLng(59.3228, 18.06668));
        _route.addGeoPoint(new LatLng(59.32449, 18.06564));
        _route.addGeoPoint(new LatLng(59.32508, 18.06496));
        _route.addGeoPoint(new LatLng(59.32491, 18.06382));
        _route.addGeoPoint(new LatLng(59.32465, 18.06215));
        _route.addGeoPoint(new LatLng(59.32536, 18.06185));
        _route.addGeoPoint(new LatLng(59.32587, 18.06138));

        AudioPoint point = new AudioPoint(new LatLng(59.32303, 18.06742), 5);
        _route.addAudioPoint(point);
        AddAudioTrack(point, R.raw.a0_1);
        AddAudioTrack(point, R.raw.a0_2);

        point = new AudioPoint(new LatLng(59.3228, 18.06668), 5);
        _route.addAudioPoint(point);
        AddAudioTrack(point, R.raw.a0_3);

        point = new AudioPoint(new LatLng(59.32302, 18.06654), 5);
        _route.addAudioPoint(point);
        AddAudioTrack(point, R.raw.a0_3_2);

        point = new AudioPoint(new LatLng(59.32382, 18.06607), 5);
        _route.addAudioPoint(point);
        AddAudioTrack(point, R.raw.a1_0);
        AddAudioTrack(point, R.raw.a1_1);

        point = new AudioPoint(new LatLng(59.3249, 18.06383), 10);
        _route.addAudioPoint(point);
        AddAudioTrack(point, R.raw.a2_0);
        AddAudioTrack(point, R.raw.a2_1);

        point = new AudioPoint(new LatLng(59.32463, 18.06216), 5);
        _route.addAudioPoint(point);
        AddAudioTrack(point, R.raw.a3_0);

    }

    private void AddAudioTrack(AudioPoint point, int fileId)
    {
        if (_pointTrackMapper.containsKey(point))
        {
            _pointTrackMapper.get(point).add(fileId);
            return;
        }

        ArrayList<Integer> audioFilesIds = new ArrayList<>();
        audioFilesIds.add(fileId);
        _pointTrackMapper.put(point, audioFilesIds);
    }

    // ToDo: according to user choose files
    public Pair<Integer, ArrayList<Uri>> getResourceToPlay(LatLng position)
    {
        float min = Integer.MAX_VALUE;
        AudioPoint closestPoint = null;

        for (AudioPoint point : _route.audioPoints())
        {
            if (point.Done)
                continue;

            float distance = LocationTracker.GetDistance(position, point.Position);
            if (distance < min && distance <= point.Radius)
            {
                min = distance;
                closestPoint = point;
            }
        }

        if (closestPoint == null)
            return null;

        ArrayList<Uri> uris = new ArrayList<>();
        Pair<Integer, ArrayList<Uri>> result = new Pair<>(closestPoint.Number, uris);
        for(int resourceId : _pointTrackMapper.get(closestPoint))
        {
            uris.add(Uri.parse("android.resource://com.home.croaton.audiotravel/" + resourceId));
        }

        return result;
    }

    public void doneAudioPoint(int pointNumber)
    {
        _route.markAudioPointDone(pointNumber);
    }

    public ArrayList<Point> geoPoints() {
        return _route.geoPoints();
    }

    public ArrayList<AudioPoint> audioPoints() {
        return _route.audioPoints();
    }
}
