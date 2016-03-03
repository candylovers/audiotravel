package com.home.croaton.audiotravel.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Pair;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;
import com.home.croaton.audiotravel.audio.AudioPlaybackController;
import com.home.croaton.audiotravel.audio.AudioPlayerUI;
import com.home.croaton.audiotravel.audio.AudioServiceCommand;
import com.home.croaton.audiotravel.domain.AudioPoint;
import com.home.croaton.audiotravel.audio.AudioService;
import com.home.croaton.audiotravel.LocationTracker;
import com.home.croaton.audiotravel.instrumentation.IObserver;
import com.home.croaton.audiotravel.maps.MapOnClickListener;
import com.home.croaton.audiotravel.maps.MapHelper;
import com.home.croaton.audiotravel.domain.Point;
import com.home.croaton.audiotravel.R;
import com.home.croaton.audiotravel.TestTracker;
import com.home.croaton.audiotravel.security.PermissionChecker;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String WAKE_LOCK_NAME = "MyWakeLock";
    private GoogleMap _map;
    private LocationTracker _tracker;
    private AudioPlaybackController _audioPlaybackController;
    private PowerManager.WakeLock _wakeLock;
    private boolean _fakeLocation;
    private int _currentRouteId = -1;
    private AudioPlayerUI _audioPlayerUi;
    private ArrayList<Marker> _audioPointMarkers = new ArrayList<>();
    ArrayList<Circle> _circles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PowerManager mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
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

    private void loadState(Bundle savedInstanceState) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        _fakeLocation = sharedPref.getBoolean(getString(R.string.settings_fake_location_id), false);

        if (savedInstanceState != null) {
            _currentRouteId = savedInstanceState.getInt(getString(R.string.route_name));
            _audioPlaybackController = new AudioPlaybackController(this, _currentRouteId);

            boolean[] done = savedInstanceState.getBooleanArray(getString(R.string.audio_point_state));
            if (done != null) {
                int i = 0;
                for (AudioPoint p : _audioPlaybackController.audioPoints())
                    p.Done = done[i++];
            }
            _fakeLocationStarted = savedInstanceState.getBoolean(getString(R.string.fake_location_started));
        } else {
            Intent intent = getIntent();
            _currentRouteId = intent.getIntExtra(getString(R.string.route_name), R.id.route_demo);
            _audioPlaybackController = new AudioPlaybackController(this, _currentRouteId);
        }
    }

    public void locationChanged(LatLng point) {

        Pair<Integer, ArrayList<Uri>> audioAtPoint = _audioPlaybackController
                .getResourceToPlay(this, point, false);

        if (audioAtPoint == null)
            return;

        _audioPlaybackController.startPlaying(this, audioAtPoint.second);
        _audioPlaybackController.doneAudioPoint(audioAtPoint.first);

        MapHelper.changeIcon(_audioPointMarkers, audioAtPoint.first, R.drawable.passed);
    }

    private synchronized void startLocationTracking()
    {
        _tracker = new LocationTracker(this);
        _tracker.LocationChanged.subscribe(new IObserver<LatLng>() {
            @Override
            public void notify(LatLng location) {
                locationChanged(location);
            }
        });
        boolean stupid = PermissionChecker.CheckForLocationDetectionPermission(this);
        if (stupid)
            _tracker.startLocationTracking();
    }

    private boolean _fakeLocationStarted = false;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        _map = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            _map.setMyLocationEnabled(true);
        }

        if (_fakeLocation && !_fakeLocationStarted)
        {
            TestTracker.startFakeLocationTracking(this, _audioPlaybackController.geoPoints(), _map);
            _fakeLocationStarted = true;
        }

        PolylineOptions route = new PolylineOptions()
                .width(15)
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
            int resId = point.Done ? R.drawable.passed : R.drawable.play;
            _audioPointMarkers.add(MapHelper.putMarker(_map, point.Position, resId));
            _circles.add(MapHelper.addCircle(_map, point.Position, point.Radius));
        }

        _map.addPolyline(route);
        _map.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(
                route.getPoints().get(0), 16)));

        _map.setOnMapClickListener(new MapOnClickListener(_circles));

        // Isn't it stupid? Position doesn't update without it.
        _map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
            }
        });
        _map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Pair<Integer, ArrayList<Uri>> audioAtPoint = _audioPlaybackController
                        .getResourceToPlay(MapsActivity.this, marker.getPosition(), true);

                _audioPlaybackController.startPlaying(MapsActivity.this, audioAtPoint.second);
                return true;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode) {
            case PermissionChecker.LocationPermissions:
            {
                if (grantResults.length > 0)
                {
                    boolean allGranted = true;
                        for(int i = 0; i < grantResults.length; i++)
                        allGranted &= grantResults[i] == PackageManager.PERMISSION_GRANTED;

                    if (allGranted)
                    {
                        _tracker.startLocationTracking();
                        _map.setMyLocationEnabled(true);
                    }
                }
            }
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        savedInstanceState.putBooleanArray(getString(R.string.audio_point_state),
                _audioPlaybackController.GetDoneArray());
        savedInstanceState.putBoolean(getString(R.string.fake_location_started), _fakeLocationStarted);
        savedInstanceState.putInt(getString(R.string.route_name), _currentRouteId);

        // Only for route creation
        _audioPlaybackController.specialSaveRouteToDisc(_circles, _audioPointMarkers, this);

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