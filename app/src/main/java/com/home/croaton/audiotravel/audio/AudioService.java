package com.home.croaton.audiotravel.audio;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.home.croaton.audiotravel.R;
import com.home.croaton.audiotravel.activities.MapsActivity;
import com.home.croaton.audiotravel.instrumentation.IObservable;
import com.home.croaton.audiotravel.instrumentation.MyObservable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

// Stack overflow: There can only be one instance of a given Service.
public class AudioService extends android.app.Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener
{
    public static final String NewUris = "Uris";
    public static final String Command = "Command";
    public static final String Progress = "Progress";
    private static final String ServiceName = "Audio Service";

    private volatile MediaPlayer _mediaPlayer;
    private Queue<Uri> _uriQueue = new LinkedList<>();
    private final int _positionPollTime = 500;
    private int FullProgress = 100;

    private Notification.Builder _notificationBuilder;
    private Notification _notification;
    private volatile ReentrantLock _playerLock = new ReentrantLock();
    private Thread _positionPoller;

    private static MyObservable<PlayerState> _innerState = new MyObservable<>();
    public static IObservable<PlayerState> State = _innerState;

    private static MyObservable<Integer> _innerPosition = new MyObservable<>();
    public static IObservable<Integer> Position = _innerPosition;

    private volatile int _position;
    private volatile boolean _playbackFinished = false;

    @Override
    // OOP cries. Me too.
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (intent == null)
            return START_STICKY;

        AudioServiceCommand command = (AudioServiceCommand)intent.getSerializableExtra(Command);

        if (command == AudioServiceCommand.ReverseState)
        {
            if (_mediaPlayer == null)
                return START_STICKY;

            command = _mediaPlayer.isPlaying()
                    ? AudioServiceCommand.Pause
                    : AudioServiceCommand.Play;
        }
        if (command == AudioServiceCommand.LoadTracks)
        {
            ArrayList<Uri> newUris = (ArrayList<Uri>)intent.getSerializableExtra(NewUris);

            RenewPlayer();

            _uriQueue.clear();
            _uriQueue.addAll(newUris);

            setPlayerListeners();

            preparePlayerWithNextTrack();
        }
        if (command == AudioServiceCommand.Pause)
        {
            _playerLock.lock();
            if (_mediaPlayer != null)
            {
                _mediaPlayer.pause();
                _innerState.notifyObservers(PlayerState.Paused);
            }
            _playerLock.unlock();
        }
        if (command == AudioServiceCommand.Play)
        {
            _playerLock.lock();
            if (_mediaPlayer != null)
            {
                _playbackFinished = false;
                _mediaPlayer.start();
                _innerState.notifyObservers(PlayerState.Playing);
            }
            _playerLock.unlock();
        }
        if (command == AudioServiceCommand.Rewind)
        {
            int progress = intent.getIntExtra(Progress, 0);
            _playerLock.lock();
            if (_mediaPlayer != null)
                _mediaPlayer.seekTo(_mediaPlayer.getDuration() * progress / 100);
            _playerLock.unlock();
        }

        return START_STICKY;
    }

    private void setPlayerListeners() {
        _mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        _mediaPlayer.setOnErrorListener(this);
        _mediaPlayer.setOnPreparedListener(this);
        _mediaPlayer.setOnCompletionListener(this);
    }

    private void RenewPlayer()
    {
        _playerLock.lock();

        if (_mediaPlayer != null)
        {
            _mediaPlayer.release();
        }
        _mediaPlayer = new MediaPlayer();

        _playerLock.unlock();

        _innerState.notifyObservers(PlayerState.NotPrepared);

        if (_positionPoller == null) {
            _positionPoller = new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean onPrevStepWasPlaying = false;
                    while (true)
                    {
                        _playerLock.lock();
                        boolean isPlaying = _mediaPlayer.isPlaying();
                        if (isPlaying)
                        {
                            int total = _mediaPlayer.getDuration();
                            int currentPosition = _mediaPlayer.getCurrentPosition();
                            int newPosition = (int)((double) currentPosition / (double)total * FullProgress);
                            if (newPosition != _position) {
                                _position = newPosition;
                                _innerPosition.notifyObservers(_position);
                            }
                            onPrevStepWasPlaying = isPlaying;
                        }
                        else if (onPrevStepWasPlaying)
                        {
                            onPrevStepWasPlaying = false;
                            if (_playbackFinished)
                                _innerPosition.notifyObservers(FullProgress);
                        }
                        _playerLock.unlock();

                        try
                        {
                            Thread.sleep(_positionPollTime);
                        }
                        catch (InterruptedException e)
                        {
                            return;
                        }
                    }
                }
            });
            _positionPoller.start();
        }

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

    private void preparePlayerWithNextTrack()
    {
        try
        {
            setUpAsForeground("Audio + " + _uriQueue.peek().getLastPathSegment());
            _mediaPlayer.setDataSource(this, _uriQueue.poll());
        } catch (Exception e)
        {
            Log.e(ServiceName, e.toString());
        }

        try
        {
            _mediaPlayer.prepareAsync();
        } catch (IllegalStateException e)
        {
            Log.e(ServiceName, e.getMessage());
        }
    }

    private boolean hasNextTrack() {
        return !_uriQueue.isEmpty();
    }

    public void onPrepared(MediaPlayer player)
    {
        _playerLock.lock();

        if (playerIsActual(player))
        {
            player.start();
            _playbackFinished = false;
            _innerState.notifyObservers(PlayerState.Playing);
        }

        _playerLock.unlock();
    }

    private boolean playerIsActual(MediaPlayer player)
    {
        return _mediaPlayer != null && _mediaPlayer.hashCode() == player.hashCode();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra)
    {
        return false;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (_mediaPlayer != null)
        {
            _mediaPlayer.release();
            _mediaPlayer = null;
        }
    }

    @Override
    public void onCompletion(MediaPlayer player)
    {
        _playerLock.lock();
        _playbackFinished = true;
        _innerState.notifyObservers(PlayerState.PlaybackCompleted);

        if (playerIsActual(player) && hasNextTrack())
        {
            _mediaPlayer.reset();
            setPlayerListeners();
            preparePlayerWithNextTrack();
        }

        _playerLock.unlock();
    }
}