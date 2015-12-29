package com.home.croaton.audiotravel;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
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

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap _map;
    private GoogleApiClient _googleApiClient;
    private LocationTracker _tracker;
    private RouteController _routeController;
    private PowerManager.WakeLock _wakeLock;
    private static final String PREFS_NAME = "PreferencesFile";

    @Override
    // ToDo: save _route and load after activity destroyed
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        PowerManager mgr = (PowerManager)getSystemService(Context.POWER_SERVICE);
        _wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        _wakeLock.acquire();

        /*SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        for(AudioPoint p : _routeController.audioPoints())
        {
            if (settings.getBoolean("AudioPoint" + p.Number, false))
                _routeController.
        }*/

        _routeController = new RouteController();

        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //startLocationTracking();
    }

    public void locationChanged(LatLng point)
    {
        if (AudioService.isPlaying())
            stopService(new Intent(this, AudioService.class));

        Pair<Integer, ArrayList<Uri>> audioAtPoint = _routeController.getResourceToPlay(point);

        if (audioAtPoint == null)
            return;

        AudioService.setTrackQueue(this, audioAtPoint.second);
        startService(new Intent(this, AudioService.class));

        _routeController.doneAudioPoint(audioAtPoint.first);
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

    private boolean _once = false;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        _map = googleMap;

        if (!_once)
        {
            TestTracker.startFakeLocationTracking(this, _routeController.geoPoints(), _map);
            _once = true;
        }

        PolylineOptions route = new PolylineOptions()
                .width(25)
                .color(0x7F0000FF)
                .geodesic(true);

        ArrayList<Point> points = _routeController.geoPoints();
        for(Point point : points)
        {
            route.add(point.Position);
        }

        MapHelper.putMarker(_map, points.get(0).Position, R.drawable.start);
        MapHelper.putMarker(_map, points.get(points.size() - 1).Position, R.drawable.finish);

        for(Point point : _routeController.audioPoints())
        {
            MapHelper.putMarker(_map, point.Position, R.drawable.play);
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

        if (_googleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(_googleApiClient, _tracker);

        stopService(new Intent(this, AudioService.class));
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
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        for(AudioPoint p : _routeController.audioPoints())
            editor.putBoolean("AudioPoint" + p.Number, p.Done);
    }

    @Override
    protected void onStop()
    {
        if (_wakeLock.isHeld())
            _wakeLock.release();

        super.onStop();
        stopService(new Intent(this, AudioService.class));
    }
}