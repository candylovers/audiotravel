package com.home.croaton.audiotravel.audio;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

import com.home.croaton.audiotravel.R;
import com.home.croaton.audiotravel.activities.MapsActivity;
import com.home.croaton.audiotravel.instrumentation.IObserver;

public class AudioPlayerUI
{
    public AudioPlayerUI(MapsActivity mapsActivity)
    {
        final Button pause = (Button) mapsActivity.findViewById(R.id.button_pause);
        final Context activity = mapsActivity;

        pause.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent startingIntent = new Intent(activity, AudioService.class);
                startingIntent.putExtra(AudioService.Command, AudioServiceCommand.PlayPause);
                activity.startService(startingIntent);
            }
        });

        AudioService.State.subscribe(new IObserver<PlayerState>() {
            @Override
            public void notify(PlayerState state) {
                if (state == PlayerState.NotPlaying)
                    pause.setText("Play");
                else
                    pause.setText("Pause");
            }
        });
    }
}
