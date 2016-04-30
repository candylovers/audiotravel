package com.home.croaton.followme.audio;

import android.content.Context;
import android.content.Intent;
import android.util.Pair;

import com.home.croaton.followme.domain.AudioPoint;
import com.home.croaton.followme.domain.Point;
import com.home.croaton.followme.domain.Route;
import com.home.croaton.followme.domain.RouteSerializer;
import com.home.croaton.followme.download.ExcursionDownloadManager;
import com.home.croaton.followme.location.LocationHelper;
import com.home.croaton.followme.maps.Circle;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class AudioPlaybackController {
    private static final String MP3_EXTENSION = ".mp3";
    private static final CharSequence FOLDER_SEPARATOR = "/";
    private ExcursionDownloadManager downloadManager;
    private Route _route;

    public AudioPlaybackController(Context context, ExcursionDownloadManager downloadManager) {
        this.downloadManager = downloadManager;
        deserializeFromFile(context);
    }

    public static void stopAnyPlayback(Context context) {
        context.stopService(new Intent(context, AudioService.class));
    }

    private void deserializeFromFile(Context context) {
        try {
            _route = RouteSerializer.deserializeFromFile(new FileInputStream(downloadManager.getRouteFileName()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Pair<Integer, ArrayList<String>> getResourceToPlay(GeoPoint position) {
        return getResourceToPlay(position, false);
    }

    public Pair<Integer, ArrayList<String>> getResourceToPlay(GeoPoint position, boolean ignorePassed) {
        float min = Integer.MAX_VALUE;
        AudioPoint closestPoint = null;

        for (AudioPoint point : _route.audioPoints()) {
            if (!ignorePassed && _route.isAudioPointPassed(point.Number))
                continue;

            float distance = LocationHelper.GetDistance(position, point.Position);
            if (distance < min && distance <= point.Radius) {
                min = distance;
                closestPoint = point;
            }
        }

        if (closestPoint == null)
            return null;

        ArrayList<String> fullNames = new ArrayList<>();
        for(String fileName : _route.getAudiosForPoint(closestPoint))
            fullNames.add(downloadManager.getAudioLocalDir() + FOLDER_SEPARATOR+ fileName + MP3_EXTENSION);

        return new Pair<>(closestPoint.Number, fullNames);
    }

    public void markAudioPoint(int pointNumber, boolean passed) {
        _route.markAudioPoint(pointNumber, passed);
    }

    public ArrayList<Point> geoPoints() {
        return _route.geoPoints();
    }

    public ArrayList<AudioPoint> audioPoints() {
        return _route.audioPoints();
    }

    public boolean[] getDoneArray() {
        ArrayList<AudioPoint> audioPoints = _route.audioPoints();
        boolean[] doneIndicators = new boolean[audioPoints.size()];

        for (int i = 0; i < audioPoints.size(); i++)
            doneIndicators[i] = _route.isAudioPointPassed(i);

        return doneIndicators;
    }

    public void specialSaveRouteToDisc(ArrayList<Circle> circles, ArrayList<Marker> pointMarkers,
                                       Context context) {
        if (circles.size() > 0 && pointMarkers.size() > 0)
            _route.updateAudioPoints(circles, pointMarkers);

        FileOutputStream fs = null;
        try {
            fs = new FileOutputStream(downloadManager.getRouteFileName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        RouteSerializer.serialize(_route, fs);
    }

    public void startPlaying(Context context, ArrayList<String> audioToPlay) {
        Intent startingIntent = new Intent(context, AudioService.class);
        startingIntent.putExtra(AudioService.Command, AudioServiceCommand.LoadTracks);
        startingIntent.putExtra(AudioService.NewTracks, audioToPlay);

        context.startService(startingIntent);
    }

    public boolean isAudioPointPassed(Integer number) {
        return _route.isAudioPointPassed(number);
    }

    public AudioPoint getFirstNotDoneAudioPoint() {
        for (AudioPoint audioPoint : _route.audioPoints()) {
            if (!_route.isAudioPointPassed(audioPoint.Number))
                return (AudioPoint) audioPoint.clone();
        }

        return null;
    }
}
