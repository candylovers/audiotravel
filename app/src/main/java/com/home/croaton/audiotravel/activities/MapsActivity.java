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
import android.view.KeyEvent;

import com.home.croaton.audiotravel.R;
import com.home.croaton.audiotravel.audio.AudioPlaybackController;
import com.home.croaton.audiotravel.audio.AudioPlayerUI;
import com.home.croaton.audiotravel.domain.AudioPoint;
import com.home.croaton.audiotravel.domain.Point;
import com.home.croaton.audiotravel.instrumentation.IObserver;
import com.home.croaton.audiotravel.location.LocationService;
import com.home.croaton.audiotravel.location.TrackEmulator;
import com.home.croaton.audiotravel.location.TrackerCommand;
import com.home.croaton.audiotravel.maps.Circle;
import com.home.croaton.audiotravel.maps.MapHelper;
import com.home.croaton.audiotravel.maps.MapOnClickListener;
import com.home.croaton.audiotravel.maps.OnMarkerClick;
import com.home.croaton.audiotravel.security.PermissionChecker;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.tileprovider.tilesource.MapBoxTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class MapsActivity extends FragmentActivity {

    public static final String WAKE_LOCK_NAME = "MyWakeLock";
    private MapView _map;
    //private LocationHelper _tracker;
    private AudioPlaybackController _audioPlaybackController;
    private PowerManager.WakeLock _wakeLock;
    private boolean _fakeLocation;
    private int _currentRouteId = -1;
    private AudioPlayerUI _audioPlayerUi;
    private ArrayList<Marker> _audioPointMarkers = new ArrayList<>();
    private MyLocationNewOverlay _locationOverlay;
    ArrayList<Circle> _circles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PowerManager mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
        _wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_NAME);
        _wakeLock.acquire();

        loadState(savedInstanceState);
        setContentView(R.layout.activity_maps);

        setUpMap();

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

    public void locationChanged(GeoPoint point) {
        Pair<Integer, ArrayList<Uri>> audioAtPoint = _audioPlaybackController
                .getResourceToPlay(this, point, false);

        if (audioAtPoint == null)
            return;

        _audioPlaybackController.startPlaying(this, audioAtPoint.second);
        _audioPlaybackController.doneAudioPoint(audioAtPoint.first);

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
        _map.getTileProvider().clearTileCache();
        final MapBoxTileSource tileSource = new MapBoxTileSource(this);
        tileSource.setMapboxMapid("mapbox.emerald");

        _map.setTileSource(tileSource);
        _map.getTileProvider().clearTileCache();
        _map.setMultiTouchControls(true);

        Polyline line = new Polyline(this);
        line.setSubDescription(Polyline.class.getCanonicalName());
        line.setWidth(15f);
        line.setColor(0x7F0000FF);
        List<GeoPoint> geoPoints = new ArrayList<>();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            _locationOverlay = new MyLocationNewOverlay(this,
                    new GpsMyLocationProvider(this),_map);
            _locationOverlay.enableMyLocation();
            _map.getOverlays().add(_locationOverlay);
        }

        if (_fakeLocation && !_fakeLocationStarted)
        {
            TrackEmulator.startFakeLocationTracking(this, _audioPlaybackController.geoPoints(), _map);
            _fakeLocationStarted = true;
        }

        for(Point point : _audioPlaybackController.geoPoints())
            geoPoints.add(point.Position);

        line.setPoints(geoPoints);
        line.setGeodesic(true);
        _map.getOverlayManager().add(line);

        IMapController mapController = _map.getController();
        mapController.setZoom(16);
        mapController.setCenter(geoPoints.get(0));

        MapHelper.putMarker(this, _map, geoPoints.get(0), R.drawable.start);
        MapHelper.putMarker(this, _map, geoPoints.get(geoPoints.size() - 1), R.drawable.finish);

        for(AudioPoint point : _audioPlaybackController.audioPoints())
        {
            int resId = point.Done ? R.drawable.passed : R.drawable.play;
            Marker marker = MapHelper.putMarker(this, _map, point.Position, resId);
            _audioPointMarkers.add(marker);
            marker.setOnMarkerClickListener(new OnMarkerClick(this, _audioPlaybackController));
            _circles.add(MapHelper.addCircle(this, _map, point.Position, point.Radius));
        }

        _map.getOverlays().add(new MapEventsOverlay(this, new MapOnClickListener(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                _map.invalidate();
                return null;
            }
        }, _circles)));

        _map.getController().setCenter(geoPoints.get(0));
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
                        _locationOverlay = new MyLocationNewOverlay(this,
                                new GpsMyLocationProvider(this),_map);
                        _locationOverlay.enableMyLocation();
                        _map.getOverlays().add(_locationOverlay);
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
        savedInstanceState.putBooleanArray(getString(R.string.audio_point_state), _audioPlaybackController.GetDoneArray());
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
        }
        return super.onKeyDown(keyCode, event);
    }
}