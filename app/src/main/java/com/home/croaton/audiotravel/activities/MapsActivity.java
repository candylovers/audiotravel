package com.home.croaton.audiotravel.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Pair;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.home.croaton.audiotravel.audio.AudioPlaybackController;
import com.home.croaton.audiotravel.audio.AudioPlayerUI;
import com.home.croaton.audiotravel.audio.AudioServiceCommand;
import com.home.croaton.audiotravel.domain.AudioPoint;
import com.home.croaton.audiotravel.audio.AudioService;
import com.home.croaton.audiotravel.LocationTracker;
import com.home.croaton.audiotravel.MapHelper;
import com.home.croaton.audiotravel.domain.Point;
import com.home.croaton.audiotravel.R;
import com.home.croaton.audiotravel.TestTracker;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String WAKE_LOCK_NAME = "MyWakeLock";
    private GoogleMap _map;
    private GoogleApiClient _googleApiClient;
    private LocationTracker _tracker;
    private AudioPlaybackController _audioPlaybackController;
    private PowerManager.WakeLock _wakeLock;
    private boolean _fakeLocation;
    private int _currentRouteId = -1;
    private AudioPlayerUI _audioPlayerUi;

    @Override
    // ToDo: save _route and load after activity destroyed
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        PowerManager mgr = (PowerManager)getSystemService(Context.POWER_SERVICE);
        _wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_NAME);
        _wakeLock.acquire();

        loadState(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (!_fakeLocation)
            startLocationTracking();

        _audioPlayerUi = new AudioPlayerUI(this);
    }

    private void loadState(Bundle savedInstanceState)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        _fakeLocation = sharedPref.getBoolean(getString(R.string.settings_fake_location_id), false);

        if (savedInstanceState != null)
        {
            _currentRouteId = savedInstanceState.getInt(getString(R.string.route_name));
            _audioPlaybackController = new AudioPlaybackController(getResources(), _currentRouteId);

            boolean[] done = savedInstanceState.getBooleanArray(getString(R.string.audio_point_state));
            if (done != null)
            {
                int i=0;
                for(AudioPoint p : _audioPlaybackController.audioPoints())
                    p.Done = done[i++];
            }
            _fakeLocationStarted = savedInstanceState.getBoolean(getString(R.string.fake_location_started));
        }
        else
        {
            Intent  intent = getIntent();
            _currentRouteId = intent.getIntExtra(getString(R.string.route_name), R.id.route_demo);
            _audioPlaybackController = new AudioPlaybackController(getResources(), _currentRouteId);
        }
    }

    public void locationChanged(LatLng point)
    {
        Pair<Integer, ArrayList<Uri>> audioAtPoint = _audioPlaybackController.getResourceToPlay(point);

        if (audioAtPoint == null)
            return;

        Intent startingIntent = new Intent(this, AudioService.class);
        startingIntent.putExtra(AudioService.Command, AudioServiceCommand.LoadTracks);
        startingIntent.putExtra(AudioService.NewUris, audioAtPoint.second);

        _audioPlaybackController.doneAudioPoint(audioAtPoint.first);
        startService(startingIntent);
    }

    private synchronized void startLocationTracking()
    {
        _tracker = new LocationTracker(this);

        _googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(_tracker)
                .addOnConnectionFailedListener(_tracker)
                .addApi(LocationServices.API)
                .build();

        _googleApiClient.connect();

        _tracker.setGogleApiClient(_googleApiClient);
    }

    private boolean _fakeLocationStarted = false;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        _map = googleMap;
        _map.setMyLocationEnabled(true);
        if (_fakeLocation && !_fakeLocationStarted)
        {
            TestTracker.startFakeLocationTracking(this, _audioPlaybackController.geoPoints(), _map);
            _fakeLocationStarted = true;
        }

        PolylineOptions route = new PolylineOptions()
                .width(25)
                .color(0x7F0000FF)
                .geodesic(true);

        ArrayList<Point> points = _audioPlaybackController.geoPoints();
        for(Point point : points)
        {
            route.add(point.Position);
        }

        MapHelper.putMarker(_map, points.get(0).Position, R.drawable.start);
        MapHelper.putMarker(_map, points.get(points.size() - 1).Position, R.drawable.finish);

        for(AudioPoint point : _audioPlaybackController.audioPoints())
        {
            MapHelper.putMarker(_map, point.Position, R.drawable.play);
            MapHelper.addCircle(_map, point.Position, point.Radius);
        }

        _map.addPolyline(route);
        _map.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(
                route.getPoints().get(0), 16)));
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if (_googleApiClient == null)
            return;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        savedInstanceState.putBooleanArray(getString(R.string.audio_point_state),
                _audioPlaybackController.GetDoneArray());
        savedInstanceState.putBoolean(getString(R.string.fake_location_started), _fakeLocationStarted);
        savedInstanceState.putInt(getString(R.string.route_name), _currentRouteId);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (_tracker == null)
            return;

        _tracker.resume();
    }

    @Override
    protected void onStop()
    {
        if (_wakeLock.isHeld())
            _wakeLock.release();

        super.onStop();
        TestTracker.stop();
    }
}