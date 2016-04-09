package com.home.croaton.audiotravel.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Pair;
import android.view.KeyEvent;

import com.home.croaton.audiotravel.R;
import com.home.croaton.audiotravel.audio.AudioPlaybackController;
import com.home.croaton.audiotravel.audio.AudioPlayerUI;
import com.home.croaton.audiotravel.domain.AudioPoint;
import com.home.croaton.audiotravel.domain.Point;
import com.home.croaton.audiotravel.instrumentation.ConnectionHelper;
import com.home.croaton.audiotravel.instrumentation.IObserver;
import com.home.croaton.audiotravel.location.LocationService;
import com.home.croaton.audiotravel.location.TrackEmulator;
import com.home.croaton.audiotravel.location.TrackerCommand;
import com.home.croaton.audiotravel.maps.Circle;
import com.home.croaton.audiotravel.maps.MapHelper;
import com.home.croaton.audiotravel.maps.MapOnClickListener;
import com.home.croaton.audiotravel.maps.OnMarkerClick;
import com.home.croaton.audiotravel.security.PermissionChecker;

import org.osmdroid.bonuspack.cachemanager.CacheManager;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class MapsActivity extends FragmentActivity {

    public static final String WAKE_LOCK_NAME = "MyWakeLock";
    private MapView _map;
    private AudioPlaybackController _audioPlaybackController;
    private PowerManager.WakeLock _wakeLock;
    private boolean _fakeLocation;
    private String _currentRouteId = "";
    private AudioPlayerUI _audioPlayerUi;
    private ArrayList<Marker> _audioPointMarkers = new ArrayList<>();
    ArrayList<Circle> _circles = new ArrayList<>();
    private String _language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PowerManager mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
        _wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_NAME);
        _wakeLock.acquire();

        loadState(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        setUpMap();

        if (!_fakeLocation)
            startLocationTracking();

        _audioPlayerUi = new AudioPlayerUI(this, _currentRouteId);
    }

    private void loadState(Bundle savedInstanceState) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        _fakeLocation = sharedPref.getBoolean(getString(R.string.settings_fake_location_id), false);
        _language = sharedPref.getString(getString(R.string.settings_language_preference), "en");

        if (savedInstanceState != null) {
            _currentRouteId = savedInstanceState.getString(getString(R.string.route_name));
            _audioPlaybackController = new AudioPlaybackController(this, _currentRouteId);

            boolean[] done = savedInstanceState.getBooleanArray(getString(R.string.audio_point_state));
            if (done != null) {
                int i = 0;
                for (AudioPoint p : _audioPlaybackController.audioPoints())
                    _audioPlaybackController.markAudioPoint(p.Number, done[i++]);
            }
            _fakeLocationStarted = savedInstanceState.getBoolean(getString(R.string.fake_location_started));
        } else {
            Intent intent = getIntent();
            _currentRouteId = intent.getStringExtra(IntentNames.SELECTED_EXCURSION_NAME);
            _audioPlaybackController = new AudioPlaybackController(this, _currentRouteId);
        }
    }

    public String getLanguage()
    {
        return _language;
    }

    public void locationChanged(GeoPoint point) {
        Pair<Integer, ArrayList<String>> audioAtPoint = _audioPlaybackController.getResourceToPlay(point);

        if (audioAtPoint == null)
            return;

        _audioPlaybackController.startPlaying(this, audioAtPoint.second);
        _audioPlaybackController.markAudioPoint(audioAtPoint.first, true);

        MapHelper.changeIcon(this, _audioPointMarkers, audioAtPoint.first, R.drawable.passed);
    }

    private synchronized void startLocationTracking()
    {
        if (!PermissionChecker.CheckForLocationDetectionPermission(this))
            return;

        sendCommandToLocationService(TrackerCommand.Start);
    }

    private void sendCommandToLocationService(TrackerCommand command) {
        Intent startingIntent = new Intent(this, LocationService.class);
        startingIntent.putExtra(LocationService.Command, command);
        LocationService.LocationChanged.subscribe(new IObserver<GeoPoint>() {
            @Override
            public void notify(GeoPoint location) {
                locationChanged(location);
            }
        });

        startService(startingIntent);
    }

    private boolean _fakeLocationStarted = false;

    public void setUpMap() {
        _map = (MapView) findViewById(R.id.map);
        MapHelper.chooseBeautifulMapProvider(this, _map);

        if (ConnectionHelper.hasInternetConnection(this)) {
            CacheManager cacheManager = new CacheManager(_map);
            cacheManager.downloadAreaAsync(this, new BoundingBoxE6(59.32829, 18.07929, 59.32023, 18.05884), 5, 18);
        }
        _map.setMultiTouchControls(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            MapHelper.addLocationOverlay(this, _map);
        }

        if (_fakeLocation && !_fakeLocationStarted)
        {
            TrackEmulator.startFakeLocationTracking(this, _audioPlaybackController.geoPoints(), _map);
            _fakeLocationStarted = true;
        }

        List<Point> routePoints = _audioPlaybackController.geoPoints();
        MapHelper.drawRoute(this, _map, routePoints);
        MapHelper.focusCameraOnPoint(_map, _audioPlaybackController.getFirstNotDoneAudioPoint());
        MapHelper.setStartRouteIcon(this, _map, routePoints.get(0).Position);
        MapHelper.setEndRouteIcon(this, _map, routePoints.get(routePoints.size() - 1).Position);
        MapHelper.drawAudioPoints(this, _map, _audioPlaybackController, _audioPointMarkers,
                _circles);

        for(Marker marker : _audioPointMarkers)
            marker.setOnMarkerClickListener(new OnMarkerClick(this, _audioPlaybackController));

        _map.getOverlays().add(new MapEventsOverlay(this, new MapOnClickListener(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                _map.invalidate();
                return null;
            }
        }, _circles)));
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
                        sendCommandToLocationService(TrackerCommand.Start);
                        MapHelper.addLocationOverlay(this, _map);
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
        savedInstanceState.putBooleanArray(getString(R.string.audio_point_state), _audioPlaybackController.getDoneArray());
        savedInstanceState.putBoolean(getString(R.string.fake_location_started), _fakeLocationStarted);
        savedInstanceState.putString(getString(R.string.route_name), _currentRouteId);

        // Only for route creation
        _audioPlaybackController.specialSaveRouteToDisc(_circles, _audioPointMarkers, this);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onStop()
    {
        if (_wakeLock.isHeld())
            _wakeLock.release();

        super.onStop();
        TrackEmulator.stop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AudioPlaybackController.stopAnyPlayback(this);
            sendCommandToLocationService(TrackerCommand.Stop);

            try {
                _audioPlayerUi.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}