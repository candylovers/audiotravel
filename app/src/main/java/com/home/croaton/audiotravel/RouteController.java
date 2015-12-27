package com.home.croaton.audiotravel;

import android.net.Uri;

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

        AudioPoint point = new AudioPoint(new LatLng(59.32303, 18.06742), 5);
        _route.addAudioPoint(point);
        AddAudioTrack(point, R.raw.a0_1);

        point = new AudioPoint(new LatLng(59.32303, 18.06742), 5);
        AddAudioTrack(point, R.raw.a0_2);
        _route.addAudioPoint(point);

        point = new AudioPoint(new LatLng(59.32296, 18.06657), 5);
        AddAudioTrack(point, R.raw.a0_3);
        _route.addAudioPoint(point);

        /*_route.addGeoPoint(new LatLng(59.32505, 18.0651));
        _route.addGeoPoint(new LatLng(59.32491, 18.06382));
        _route.addGeoPoint(new LatLng(59.32465, 18.06215));
        _route.addGeoPoint(new LatLng(59.32536, 18.06185));
        _route.addGeoPoint(new LatLng(59.32587, 18.06138));
        */
    }

    private void AddAudioTrack(AudioPoint point, int fileId) {
        ArrayList<Integer> audioFilesIds = new ArrayList<>();
        audioFilesIds.add(fileId);
        _pointTrackMapper.put(point, audioFilesIds);
    }

    public Route getDemoRoute()
    {
        return _route;
    }

    // ToDo: according to user choose files
    public Uri getResourceToPlay(LatLng position)
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
            return Uri.EMPTY;

        // Assumes that we have 1 audio at each point
        return Uri.parse("android.resource://com.home.croaton.audiotravel/" + _pointTrackMapper.get(closestPoint).get(0));
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
