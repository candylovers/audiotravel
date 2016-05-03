package com.home.croaton.followme.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
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
import com.home.croaton.followme.maps.MapHelper;
import com.home.croaton.followme.security.PermissionAndConnectionChecker;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity {

    public static final String WAKE_LOCK_NAME = "MyWakeLock";
    private MapView _map;
    private AudioPlaybackController _audioPlaybackController;
    private PowerManager.WakeLock _wakeLock;
    private AudioPlayerUI _audioPlayerUi;
    private ArrayList<Marker> _audioPointMarkers = new ArrayList<>();
    private String _language;
    private IObserver<GeoPoint> _locationListener;
    private ExcursionBrief currentExcursion;
    private ExcursionDownloadManager downloadManager;
    private int lastActiveMarker = -1;
    private MyLocationNewOverlay _locationOverlay;
    private boolean _isActivityPresentOnScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PowerManager mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
        _wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_NAME);
        _wakeLock.acquire();

        loadState(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        setUpMap();

        startLocationTracking();

        PermissionAndConnectionChecker.checkForPermissions(this, new String[]
                {Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionAndConnectionChecker.LocalStorageRequestCode);

        _audioPlayerUi = new AudioPlayerUI(this, currentExcursion, downloadManager);
    }

    private void loadState(Bundle savedInstanceState) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
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
        lastActiveMarker = savedInstanceState.getInt(getString(R.string.last_active_marker));
    }

    public String getLanguage()
    {
        return _language;
    }

    public void locationChanged(GeoPoint point) {
        if (_locationOverlay == null && _isActivityPresentOnScreen)
            enableMyLocation();

        Pair<Integer, ArrayList<String>> audioAtPoint = _audioPlaybackController.getResourceToPlay(point);

        if (audioAtPoint == null)
            return;

        _audioPlaybackController.startPlaying(this, audioAtPoint.second);
        _audioPlaybackController.markAudioPoint(audioAtPoint.first, true);

        if (_map != null) {
            if (lastActiveMarker != -1)
                MapHelper.setMarkerIconFromResource(this, R.drawable.audio_point_big, _audioPointMarkers.get(lastActiveMarker));
            MapHelper.setMarkerIconFromResource(this, R.drawable.audio_point_big_active, _audioPointMarkers.get(audioAtPoint.first));
            _map.invalidate();
            lastActiveMarker = audioAtPoint.first;
        }
    }

    private synchronized void startLocationTracking()
    {
        String[] requestedPermissions = new String[]
        {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        if (!PermissionAndConnectionChecker.checkForPermissions(this, requestedPermissions,
                PermissionAndConnectionChecker.LocationRequestCode)) {
            return;
        }

        askToEnableGps();
        enableMyLocation();

        _locationListener = new IObserver<GeoPoint>() {
            @Override
            public void notify(GeoPoint location) {
                locationChanged(location);
            }
        };
        LocationService.LocationChanged.subscribe(_locationListener);

        sendCommandToLocationService(TrackerCommand.Start);
    }

    private void askToEnableGps() {
        if ( PermissionAndConnectionChecker.gpsIsEnabled(this))
            return;

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.enable_gps_message))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void sendCommandToLocationService(TrackerCommand command) {
        Intent startingIntent = new Intent(this, LocationService.class);
        startingIntent.putExtra(LocationService.Command, command);

        startService(startingIntent);
    }

    public void setUpMap() {
        _map = (MapView) findViewById(R.id.map);

        MapHelper.chooseBeautifulMapProvider(this, _map);

        _map.setMultiTouchControls(true);
        _map.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        _map.setFlingEnabled(false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
            enableMyLocation();

        List<Point> routePoints = _audioPlaybackController.geoPoints();
        MapHelper.drawRoute(this, _map, routePoints);
        MapHelper.focusCameraOnPoint(_map, _audioPlaybackController.getFirstNotDoneAudioPoint());
        MapHelper.setStartRouteIcon(this, _map, routePoints.get(0).Position);
        MapHelper.setEndRouteIcon(this, _map, routePoints.get(routePoints.size() - 1).Position);
        MapHelper.drawAudioPoints(this, _map, _audioPlaybackController, _audioPointMarkers);

        for(Marker marker : _audioPointMarkers)
            marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker, MapView mapView) {
                    Pair<Integer, ArrayList<String>> audioAtPoint = _audioPlaybackController
                            .getResourceToPlay(marker.getPosition(), true);

                    if (lastActiveMarker != -1)
                        MapHelper.setMarkerIconFromResource(MapsActivity.this, R.drawable.audio_point_big, _audioPointMarkers.get(lastActiveMarker));
                    MapHelper.setMarkerIconFromResource(MapsActivity.this, R.drawable.audio_point_big_active, marker);
                    mapView.invalidate();

                    _audioPlaybackController.startPlaying(MapsActivity.this, audioAtPoint.second);
                    lastActiveMarker = audioAtPoint.first;

                    return true;
                }
            });
    }

    private void enableMyLocation() {
        _locationOverlay = MapHelper.addLocationOverlay(this, _map);
        if (_locationOverlay == null)
            return;

        FloatingActionButton btCenterMap = (FloatingActionButton) findViewById(R.id.button_center_map);
        _locationOverlay.setDrawAccuracyEnabled(true);
        btCenterMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeoPoint myPosition = _locationOverlay.getMyLocation();
                if (myPosition != null)
                    _map.getController().animateTo(myPosition);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode) {
            case PermissionAndConnectionChecker.LocationRequestCode:
                for(int i = 0; i < grantResults.length; i++)
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                        return;

                startLocationTracking();

                break;
            case PermissionAndConnectionChecker.LocalStorageRequestCode:
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

        _isActivityPresentOnScreen = false;
        if (_locationOverlay != null)
            _locationOverlay.disableMyLocation();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        savedInstanceState.putBooleanArray(getString(R.string.audio_point_state), _audioPlaybackController.getDoneArray());
        savedInstanceState.putParcelable(IntentNames.SELECTED_EXCURSION_BRIEF, currentExcursion);
        savedInstanceState.putInt(getString(R.string.last_active_marker), lastActiveMarker);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        _isActivityPresentOnScreen = true;
        if (_locationOverlay != null)
            _locationOverlay.enableMyLocation();
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

            LocationService.LocationChanged.unSubscribe(_locationListener);

            AudioPlaybackController.stopAnyPlayback(this);
            sendCommandToLocationService(TrackerCommand.Stop);
            stopService(new Intent(this, LocationService.class));
        }

        return super.onKeyDown(keyCode, event);
    }
}