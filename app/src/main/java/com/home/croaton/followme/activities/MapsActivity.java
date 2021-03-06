package com.home.croaton.followme.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;

import com.home.croaton.followme.R;
import com.home.croaton.followme.audio.AudioPlaybackController;
import com.home.croaton.followme.audio.AudioPlayerUI;
import com.home.croaton.followme.domain.AudioPoint;
import com.home.croaton.followme.domain.ExcursionBrief;
import com.home.croaton.followme.domain.Point;
import com.home.croaton.followme.download.ExcursionDownloadManager;
import com.home.croaton.followme.instrumentation.IObserver;
import com.home.croaton.followme.location.LocationService;
import com.home.croaton.followme.location.TrackEmulator;
import com.home.croaton.followme.location.TrackerCommand;
import com.home.croaton.followme.maps.Circle;
import com.home.croaton.followme.maps.MapHelper;
import com.home.croaton.followme.maps.MapOnClickListener;
import com.home.croaton.followme.maps.OnMarkerClick;
import com.home.croaton.followme.security.PermissionChecker;

import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class MapsActivity extends FragmentActivity {

    public static final String WAKE_LOCK_NAME = "MyWakeLock";
    private MapView _map;
    private AudioPlaybackController _audioPlaybackController;
    private PowerManager.WakeLock _wakeLock;
    private boolean _fakeLocation;
    private AudioPlayerUI _audioPlayerUi;
    private ArrayList<Marker> _audioPointMarkers = new ArrayList<>();
    ArrayList<Circle> _circles = new ArrayList<>();
    private String _language;
    private IObserver<GeoPoint> _locationListener;
    private ExcursionBrief currentExcursion;
    private ExcursionDownloadManager downloadManager;

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

        PermissionChecker.checkForPermissions(this, new String[]
        {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, PermissionChecker.LocalStorageRequestCode);

        _audioPlayerUi = new AudioPlayerUI(this, currentExcursion, downloadManager);
    }

    private void loadState(Bundle savedInstanceState) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        _fakeLocation = sharedPref.getBoolean(getString(R.string.settings_fake_location_id), false);
        _language = sharedPref.getString(getString(R.string.settings_language_preference), "ru");

        currentExcursion = getIntent().getParcelableExtra(IntentNames.SELECTED_EXCURSION_BRIEF);
        downloadManager = new ExcursionDownloadManager(this, currentExcursion, _language);

        _audioPlaybackController = new AudioPlaybackController(this, downloadManager);

        if (savedInstanceState == null)
            return;

        boolean[] done = savedInstanceState.getBooleanArray(getString(R.string.audio_point_state));
        if (done != null) {
            int i = 0;
            for (AudioPoint p : _audioPlaybackController.audioPoints())
                _audioPlaybackController.markAudioPoint(p.Number, done[i++]);
        }
        _fakeLocationStarted = savedInstanceState.getBoolean(getString(R.string.fake_location_started));
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
        String[] requestedPermissions = new String[]
        {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        if (!PermissionChecker.checkForPermissions(this, requestedPermissions,
                PermissionChecker.LocalStorageRequestCode)) {
            return;
        }

        _locationListener = new IObserver<GeoPoint>() {
            @Override
            public void notify(GeoPoint location) {
                locationChanged(location);
            }
        };
        LocationService.LocationChanged.subscribe(_locationListener);

        sendCommandToLocationService(TrackerCommand.Start);
    }

    private void sendCommandToLocationService(TrackerCommand command) {
        Intent startingIntent = new Intent(this, LocationService.class);
        startingIntent.putExtra(LocationService.Command, command);

        startService(startingIntent);
    }

    private boolean _fakeLocationStarted = false;

    public void setUpMap() {
        _map = (MapView) findViewById(R.id.map);

        MapHelper.chooseBeautifulMapProvider(this, _map);

        _map.setMultiTouchControls(true);
        _map.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        _map.setFlingEnabled(false);
        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(this, _map);
        mRotationGestureOverlay.setEnabled(true);
        _map.getOverlayManager().add(mRotationGestureOverlay);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            final MyLocationNewOverlay locationOverlay = MapHelper.addLocationOverlay(this, _map);
            FloatingActionButton btCenterMap = (FloatingActionButton) findViewById(R.id.button_center_map);
            locationOverlay.setDrawAccuracyEnabled(true);
            btCenterMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GeoPoint myPosition = locationOverlay.getMyLocation();
                    if (myPosition != null)
                        _map.getController().animateTo(myPosition);
                }
            });
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
        MapHelper.drawAudioPoints(this, _map, _audioPlaybackController, _audioPointMarkers, _circles);

        for(Marker marker : _audioPointMarkers)
            marker.setOnMarkerClickListener(new OnMarkerClick(this, _audioPlaybackController, _language));

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
            case PermissionChecker.LocationRequestCode:
                for(int i = 0; i < grantResults.length; i++)
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                        return;

                sendCommandToLocationService(TrackerCommand.Start);
                MapHelper.addLocationOverlay(this, _map);

                break;
            case PermissionChecker.LocalStorageRequestCode:
                for(int i = 0; i < grantResults.length; i++)
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                    {
                        finish();
                        System.exit(0);
                    }
                break;
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
        savedInstanceState.putParcelable(IntentNames.SELECTED_EXCURSION_BRIEF, currentExcursion);

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

            try {
                _audioPlayerUi.close();
                _audioPlayerUi = null;
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            if (!_fakeLocation)
                LocationService.LocationChanged.unSubscribe(_locationListener);
            AudioPlaybackController.stopAnyPlayback(this);
            sendCommandToLocationService(TrackerCommand.Stop);
            stopService(new Intent(this, LocationService.class));
        }

        return super.onKeyDown(keyCode, event);
    }
}