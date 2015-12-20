package com.home.croaton.audiotravel;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.android.gms.common.api.Releasable;

import java.io.IOException;

public class AudioService extends android.app.Service implements Releasable,
        MediaPlayer.OnPreparedListener
{
    //private static final String ACTION_PLAY = "com.example.action.PLAY";
    private static String _url;
    private MediaPlayer mMediaPlayer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (mMediaPlayer != null)
            mMediaPlayer.release();

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //prepareMediaPlayer();
        int id = getResources().getIdentifier("a1", "raw", getPackageName());
        AssetFileDescriptor afd = getResources().openRawResourceFd(id);
        if (afd == null)
            return 0;

        //Uri uri = Uri.parse("android.resource://com.home.croaton.audiotravel/" + R.raw.a1);
        try
        {
            mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        mMediaPlayer.prepareAsync();

        return START_STICKY;
    }

    private void prepareMediaPlayer() {

    }

    public static void setTrack(String url) {
        _url = url;
    }

    public void onPrepared(MediaPlayer player) {
        player.start();
    }

    @Override
    public void release()
    {
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
