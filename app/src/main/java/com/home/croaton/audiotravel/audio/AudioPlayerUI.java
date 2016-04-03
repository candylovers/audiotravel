package com.home.croaton.audiotravel.audio;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.home.croaton.audiotravel.R;
import com.home.croaton.audiotravel.activities.MapsActivity;
import com.home.croaton.audiotravel.domain.RouteSerializer;
import com.home.croaton.audiotravel.instrumentation.IObserver;

import java.util.HashMap;

public class AudioPlayerUI implements SeekBar.OnSeekBarChangeListener {

    private final MapsActivity _context;
    private HashMap<String, HashMap<String, String>> _audioPointNames;

    public AudioPlayerUI(MapsActivity mapsActivity, String excursionName)
    {
        _context = mapsActivity;
        final Button pause = (Button) mapsActivity.findViewById(R.id.button_pause);
        final Context activity = mapsActivity;
        final SeekBar seekBar = (SeekBar) mapsActivity.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);

        readAudioPointNames(excursionName);
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startingIntent = new Intent(activity, AudioService.class);
                startingIntent.putExtra(AudioService.Command, AudioServiceCommand.ToggleState);
                activity.startService(startingIntent);
            }
        });
        AudioService.State.subscribe(new IObserver<PlayerState>() {
            @Override
            public void notify(PlayerState state) {
                if (state == PlayerState.Paused || state == PlayerState.PlaybackCompleted) {
                    pause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_arrow_black_48dp, 0, 0, 0);
                }
                if (state == PlayerState.Playing) {
                    pause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause_black_48dp, 0, 0, 0);
                }
            }
        });

        AudioService.Position.subscribe(new IObserver<Integer>() {
            @Override
            public void notify(Integer progress) {
                seekBar.setProgress(progress);
            }
        });

        AudioService.TrackName.subscribe(new IObserver<String>() {
            @Override
            public void notify(String trackName) {
                changeTrackCaption(_audioPointNames.get(_context.getLanguage()).get(trackName));
            }
        });
    }

    private void readAudioPointNames(String excursionName) {
        if (excursionName.equals("Gamlastan")) {
            deserializeAudioPointNames(R.raw.demo_point_names);
        }
        if (excursionName.equals("Abrahamsberg")) {
            deserializeAudioPointNames(R.raw.abrahamsberg_point_names);
        } else {
            throw new IllegalArgumentException("Unsupported excursion");
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

    private void changeTrackCaption(String caption)
    {
        TextView textView = (TextView)_context.findViewById(R.id.textViewSongName);
        textView.setText(caption);
    }

    private void deserializeAudioPointNames(int fileResId) {
        _audioPointNames =  RouteSerializer.deserializeAudioPointNames(_context.getResources(), fileResId);
    }
}
