package com.home.croaton.audiotravel.audio;

import android.content.res.Resources;
import android.net.Uri;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;
import com.home.croaton.audiotravel.LocationTracker;
import com.home.croaton.audiotravel.R;
import com.home.croaton.audiotravel.domain.AudioPoint;
import com.home.croaton.audiotravel.domain.Point;
import com.home.croaton.audiotravel.domain.Route;
import com.home.croaton.audiotravel.domain.RouteSerializer;

import java.util.ArrayList;

public class AudioPlaybackController
{
    private static final String RESOURCE_FOLDER = "android.resource://com.home.croaton.audiotravel/";
    private Route _route;

    public AudioPlaybackController(Resources resources, int routeId)
    {
        switch (routeId)
        {
            case R.id.route_demo:
                _route = RouteSerializer.deserialize(resources, R.raw.demo);
                break;
            case R.id.route_abrahamsberg:
                _route = RouteSerializer.deserialize(resources, R.raw.abrahamsberg);
                break;
            default:
                throw new IllegalArgumentException("Unsupported route id");
        }
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
        for(int resourceId : _route.getAudiosForPoint(closestPoint))
        {
            uris.add(Uri.parse(RESOURCE_FOLDER + resourceId));
        }

        return result;
    }

    public void doneAudioPoint(int pointNumber)
    {
        _route.markAudioPointDone(pointNumber);
    }

    public ArrayList<Point> geoPoints()
    {
        return _route.geoPoints();
    }

    public ArrayList<AudioPoint> audioPoints()
    {
        return _route.audioPoints();
    }

    public boolean[] GetDoneArray()
    {
        ArrayList<AudioPoint> audioPoints = _route.audioPoints();
        boolean[] doneIndicators = new boolean[audioPoints.size()];

        for(int i = 0; i < audioPoints.size(); i++)
            doneIndicators[i] = audioPoints.get(i).Done;

        return doneIndicators;
    }
}
