package com.home.croaton.audiotravel.audio;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

import com.home.croaton.audiotravel.R;
import com.home.croaton.audiotravel.activities.MapsActivity;

public class AudioPlayerUI
{
    private View.OnClickListener _playOnClickListener;
    private View.OnClickListener _pauseOnClickListener;

    public AudioPlayerUI(MapsActivity mapsActivity)
    {
        final Button pause = (Button) mapsActivity.findViewById(R.id.button_pause);
        final Context activity = mapsActivity;

        _pauseOnClickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent startingIntent = new Intent(activity, AudioService.class);
                if (AudioService.)
                startingIntent.putExtra(AudioService.Command, AudioServiceCommand.Pause);
                activity.startService(startingIntent);
                pause.setText("Play");
            }
        };
        pause.setOnClickListener();
    }
}
