package com.home.croaton.followme.maps;

import android.content.Context;
import android.util.Pair;

import com.home.croaton.followme.audio.AudioPlaybackController;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.views.MapView;

import java.util.ArrayList;

public class OnMarkerClick implements Marker.OnMarkerClickListener {

    AudioPlaybackController _audioController;
    Context _context;
    String _language;

    public OnMarkerClick(Context context, AudioPlaybackController audioController, String language)
    {
        _audioController = audioController;
        _context = context;
        _language = language;
    }

    @Override
    public boolean onMarkerClick(Marker marker, MapView mapView) {
        Pair<Integer, ArrayList<String>> audioAtPoint = _audioController
                .getResourceToPlay(marker.getPosition(), true);

        _audioController.startPlaying(_context, audioAtPoint.second);
        return true;
    }
}
