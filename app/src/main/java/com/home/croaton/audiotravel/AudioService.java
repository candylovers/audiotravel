package com.home.croaton.audiotravel;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.Releasable;

import java.io.IOException;

public class AudioService extends android.app.Service implements Releasable,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener
{
    public static final String ACTION_STOP="xxx.yyy.zzz.ACTION_STOP";

    private static Uri _url;
    private static String ServiceName = "Audio Service";
    private static Context _context;
    private static MediaPlayer _mediaPlayer;

    private Notification.Builder _notificationBuilder;
    private Notification _notification;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (_mediaPlayer != null)
            _mediaPlayer.release();

        _mediaPlayer = new MediaPlayer();
        _mediaPlayer.setOnPreparedListener(this);
        _mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        _mediaPlayer.setOnErrorListener(this);
        prepareMediaPlayer();

        return START_STICKY;
    }

    // ToDo: show notification during audio is playing
    void setUpAsForeground(String text)
    {
        if (_notificationBuilder == null)
        {
            Intent notIntent = new Intent(this, MapsActivity.class);
            notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                    notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            _notificationBuilder = new Notification.Builder(this)
                    .setContentIntent(pendInt)
                    .setOngoing(true)
                    .setTicker(text)
                    .setContentTitle("Audio guide notification")
                    .setSmallIcon(R.drawable.play);
        }
        _notificationBuilder.setContentText(text);
        _notification = _notificationBuilder.build();

        startForeground(1, _notification);
    }

    private void prepareMediaPlayer()
    {
        try
        {
            _mediaPlayer.setDataSource(_context, _url);
        } catch (IOException e)
        {
            Log.e(ServiceName, e.getMessage());
        }

        try
        {
            _mediaPlayer.prepareAsync();
        } catch (IllegalStateException e)
        {
            Log.e(ServiceName, e.getMessage());
        }
    }

    public static void setTrack(Context context, Uri url)
    {
        _context = context;
        _url = url;
    }

    public void onPrepared(MediaPlayer player)
    {
        setUpAsForeground("Audio + "+ _url.getLastPathSegment());
        player.start();
    }

    public static boolean isPlaying()
    {
        if (_mediaPlayer == null)
            return false;

        return _mediaPlayer.isPlaying();
    }

    @Override
    public void release()
    {
        _mediaPlayer.release();
        _mediaPlayer = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }
}
