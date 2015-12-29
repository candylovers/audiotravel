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

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class AudioService extends android.app.Service implements Releasable,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener
{
    public static final String ACTION_STOP="xxx.yyy.zzz.ACTION_STOP";

    private static Queue<Uri> _uriQueue;
    private static String ServiceName = "Audio Service";
    private static Context _context;
    private static MediaPlayer _mediaPlayer;

    private boolean _prepared;
    private Notification.Builder _notificationBuilder;
    private Notification _notification;
    private static Thread _playQueueThread;
    private ReentrantLock lock = new ReentrantLock();
    private ReentrantLock _playerLock = new ReentrantLock();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        final AudioService me = this;
        _playQueueThread = new Thread()
        {
            public void run()
            {
                while (_uriQueue != null && !_uriQueue.isEmpty()) {
                    _playerLock.lock();
                    if (_mediaPlayer != null)
                        _mediaPlayer.release();

                    _mediaPlayer = new MediaPlayer();
                    _mediaPlayer.setOnPreparedListener(me);
                    _mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    _mediaPlayer.setOnErrorListener(me);
                    _prepared = false;
                    prepareMediaPlayer();
                    _playerLock.unlock();

                    while (!_prepared || (_mediaPlayer != null && _mediaPlayer.isPlaying())) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        _playQueueThread.start();

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
            lock.lock();
            _playerLock.lock();
            setUpAsForeground("Audio + " + _uriQueue.peek().getLastPathSegment());
            _mediaPlayer.setDataSource(_context, _uriQueue.poll());
            _playerLock.unlock();
            lock.unlock();
        } catch (Exception e)
        {
            Log.e(ServiceName, e.getMessage());
        }

        try
        {
            _playerLock.lock();
            _mediaPlayer.prepareAsync();
            _playerLock.unlock();
        } catch (IllegalStateException e)
        {
            //Log.e(ServiceName, e.getMessage());
        }
    }

    public static void setTrackQueue(Context context, ArrayList<Uri> trackUris)
    {
        _context = context;
        _uriQueue = new ArrayBlockingQueue<Uri>(trackUris.size(), false, trackUris);
    }

    public void onPrepared(MediaPlayer player)
    {
        _prepared = true;
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
