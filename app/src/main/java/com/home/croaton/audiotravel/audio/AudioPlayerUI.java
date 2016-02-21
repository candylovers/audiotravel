package com.home.croaton.audiotravel.audio;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.home.croaton.audiotravel.R;
import com.home.croaton.audiotravel.activities.MapsActivity;
import com.home.croaton.audiotravel.instrumentation.IObserver;

public class AudioPlayerUI
{
    public AudioPlayerUI(MapsActivity mapsActivity)
    {
        final Button pause = (Button) mapsActivity.findViewById(R.id.button_pause);
        final Context activity = mapsActivity;
        final SeekBar seekBar = (SeekBar) mapsActivity.findViewById(R.id.seekBar);

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startingIntent = new Intent(activity, AudioService.class);
                startingIntent.putExtra(AudioService.Command, AudioServiceCommand.ReverseState);
                activity.startService(startingIntent);
            }
        });
        final Context context = mapsActivity;
        AudioService.State.subscribe(new IObserver<PlayerState>() {
            @Override
            public void notify(PlayerState state) {
                if (state == PlayerState.Paused)
                {
                    Drawable img = context.getResources().getDrawable(R.drawable.ic_play_arrow_black_48dp);
                    pause.setCompoundDrawables(img, null, null, null);
                }
                else
                {
                    Drawable img = context.getResources().getDrawable(R.drawable.ic_pause_black_48dp);
                    pause.setCompoundDrawables(img, null, null, null);
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
}
