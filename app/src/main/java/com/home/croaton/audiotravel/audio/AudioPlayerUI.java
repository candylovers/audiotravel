package com.home.croaton.audiotravel.audio;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.home.croaton.audiotravel.R;
import com.home.croaton.audiotravel.activities.MapsActivity;
import com.home.croaton.audiotravel.instrumentation.IObserver;

public class AudioPlayerUI implements SeekBar.OnSeekBarChangeListener {

    private final Context _context;
    private int _miliSecondsPlayed;

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
                startingIntent.putExtra(AudioService.Command, AudioServiceCommand.ReverseState);
                activity.startService(startingIntent);
            }
        });
        AudioService.State.subscribe(new IObserver<PlayerState>() {
            @Override
            public void notify(PlayerState state) {
                if (state == PlayerState.Paused) {
                    pause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_arrow_black_48dp, 0, 0, 0);
                } else {
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
        AudioService.SecondsPlayed.subscribe(new IObserver<Integer>() {
            @Override
            public void notify(Integer value) {
                _miliSecondsPlayed = value;
            }
        });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Integer value = seekBar.getProgress();
        Integer min = (int)((double)_miliSecondsPlayed / 1000d / 60d);
        Integer sec = _miliSecondsPlayed / 1000 - min * 60;
        String valueString = min + ":" + sec;

        seekBar.setThumb(writeOnDrawable(R.drawable.seekbar_thumb_dark_grey, valueString));
    }

    private Drawable writeOnDrawable(int resourceId, String text) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bm = BitmapFactory.decodeResource(_context.getResources(), resourceId, options)
                .copy(Bitmap.Config.ARGB_8888, true);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(_context.getResources().getColor(R.color.background_player));
        paint.setTextSize(16);

        Canvas canvas = new Canvas(bm);
        canvas.drawText(text, 0, (bm.getHeight() / 6f)*5f, paint);

        return new BitmapDrawable(_context.getResources(), bm);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
