package com.home.croaton.followme.audio;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.home.croaton.followme.R;
import com.home.croaton.followme.activities.IntentNames;
import com.home.croaton.followme.activities.MapsActivity;
import com.home.croaton.followme.domain.ExcursionBrief;
import com.home.croaton.followme.domain.RouteSerializer;
import com.home.croaton.followme.download.ExcursionDownloadManager;
import com.home.croaton.followme.instrumentation.IObserver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class AudioPlayerUI implements SeekBar.OnSeekBarChangeListener, AutoCloseable {

    private final MapsActivity _context;
    private final ExcursionBrief excursion;
    private HashMap<String, HashMap<String, String>> _audioPointNames;
    private IObserver<PlayerState> _stateListener;
    private IObserver<String> _trackNameListener;
    private IObserver<Integer> _positionListener;

    public AudioPlayerUI(MapsActivity mapsActivity, ExcursionBrief excursionBrief,  ExcursionDownloadManager downloadManager)
    {
        this.excursion = excursionBrief;
        _context = mapsActivity;

        final FloatingActionButton pause = (FloatingActionButton) mapsActivity.findViewById(R.id.button_pause);
        final Context activity = mapsActivity;
        final SeekBar seekBar = (SeekBar) mapsActivity.findViewById(R.id.seekBar);
        int color = ContextCompat.getColor(_context, R.color.blue_light);
        seekBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        seekBar.getThumb().setColorFilter(color, PorterDuff.Mode.MULTIPLY);

        seekBar.setOnSeekBarChangeListener(this);

        readAudioPointNames(downloadManager);
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startingIntent = new Intent(activity, AudioService.class);
                startingIntent.putExtra(AudioService.Command, AudioServiceCommand.ToggleState);
                activity.startService(startingIntent);
            }
        });

        _stateListener = new IObserver<PlayerState>() {
            @Override
            public void notify(PlayerState state) {
                if (state == PlayerState.Paused || state == PlayerState.PlaybackCompleted) {
                    pause.setImageDrawable(ContextCompat.getDrawable(_context, android.R.drawable.ic_media_play));
                }
                if (state == PlayerState.Playing) {
                    pause.setImageDrawable(ContextCompat.getDrawable(_context, android.R.drawable.ic_media_pause));
                }
            }
        };
        AudioService.State.subscribe(_stateListener);
        _stateListener.notify(AudioService.getCurrentState());

        _positionListener = new IObserver<Integer>() {
            @Override
            public void notify(Integer progress) {
                seekBar.setProgress(progress);
            }
        };
        AudioService.Position.subscribe(_positionListener);
        _positionListener.notify(AudioService.getCurrentPosition());

        _trackNameListener = new IObserver<String>() {
            @Override
            public void notify(String trackName) {
                String caption = changeAndGetTrackCaption(trackName);

                if (trackName == "")
                    return;

                Intent startingIntent = new Intent(_context, AudioService.class);
                startingIntent.putExtra(AudioService.Command, AudioServiceCommand.StartForeground);
                startingIntent.putExtra(AudioService.TrackCaption, caption);
                startingIntent.putExtra(IntentNames.SELECTED_EXCURSION_BRIEF, excursion);


                _context.startService(startingIntent);
            }
        };
        AudioService.TrackName.subscribe(_trackNameListener);
        _trackNameListener.notify(AudioService.getLastTrackName());
    }

    private void readAudioPointNames(ExcursionDownloadManager downloadManager) {
        try {
            InputStream stream = new FileInputStream(downloadManager.getPointNamesFileName());
            _audioPointNames =  RouteSerializer.deserializeAudioPointNames(stream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Intent startingIntent = new Intent(_context, AudioService.class);
        startingIntent.putExtra(AudioService.Command, AudioServiceCommand.Rewind);
        startingIntent.putExtra(AudioService.Progress, seekBar.getProgress());

        _context.startService(startingIntent);
    }

    private String changeAndGetTrackCaption(String trackName)
    {
        String caption = trackName.equals("")
            ? _context.getString(R.string.audio_choose_tack)
            : _audioPointNames.get(_context.getLanguage()).get(trackName);

        TextView textView = (TextView)_context.findViewById(R.id.textViewSongName);
        textView.setText(caption);

        return caption;
    }

    @Override
    public void close() throws Exception {
        AudioService.State.unSubscribe(_stateListener);
        AudioService.TrackName.unSubscribe(_trackNameListener);
        AudioService.Position.unSubscribe(_positionListener);
    }
}
