package com.home.croaton.audiotravel.audio;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.home.croaton.audiotravel.R;
import com.home.croaton.audiotravel.activities.MapsActivity;
import com.home.croaton.audiotravel.instrumentation.IObserver;

public class AudioPlayerUI implements SeekBar.OnSeekBarChangeListener {

    private final Activity _context;

    public AudioPlayerUI(MapsActivity mapsActivity)
    {
        _context = mapsActivity;
        final Button pause = (Button) mapsActivity.findViewById(R.id.button_pause);
        final Context activity = mapsActivity;
        final SeekBar seekBar = (SeekBar) mapsActivity.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);

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
    }

    public void changeTrackCaption(String caption)
    {
        TextView textView = (TextView)_context.findViewById(R.id.textViewSongName);
        textView.setText(caption);
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
}
