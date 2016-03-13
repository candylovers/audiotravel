package com.home.croaton.audiotravel.maps;

import android.content.Context;
import android.net.Uri;
import android.util.Pair;

import com.home.croaton.audiotravel.audio.AudioPlaybackController;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.views.MapView;

import java.util.ArrayList;

public class OnMarkerClick implements Marker.OnMarkerClickListener {

    AudioPlaybackController _audioController;
    Context _context;

    public OnMarkerClick(Context context, AudioPlaybackController audioController)
    {
        _audioController = audioController;
        _context = context;
    }

    @Override
    public boolean onMarkerClick(Marker marker, MapView mapView) {
        Pair<Integer, ArrayList<Uri>> audioAtPoint = _audioController
                .getResourceToPlay(_context, marker.getPosition(), true);

        _audioController.startPlaying(_context, audioAtPoint.second);
        return true;
    }
}
