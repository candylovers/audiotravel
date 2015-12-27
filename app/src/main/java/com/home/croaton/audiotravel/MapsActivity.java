package com.home.croaton.audiotravel;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private Route _route;
    private GoogleMap _map;
    private GoogleApiClient _googleApiClient;
    private LocationTracker _tracker;
    private RouteController _routeController;
    private Thread _trackerThread;
    private PowerManager.WakeLock _wakeLock;

    @Override
    // ToDo: save _route and load after activity destroyed
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        PowerManager mgr = (PowerManager)getSystemService(Context.POWER_SERVICE);
        _wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        _wakeLock.acquire();

        _routeController = new RouteController();
        _route = _routeController.getDemoRoute();

        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //startLocationTracking();
    }

    private void startFakeLocationTracking()
    {
        final MapsActivity me = this;
        _trackerThread = new Thread() {
            public void run()
            {
                Point prev = null;
                for(Point point : _route.geoPoints())
                {
                    if (prev == null)
                    {
                        prev = point;
                        continue;
                    }

                    for (double i = 0.1; i <= 1; i += 0.1)
                    {
                        final LatLng position = new LatLng(
                                point.Position.latitude * i + prev.Position.latitude * (1d - i),
                                point.Position.longitude * i + prev.Position.longitude * (1d - i));

                        me.runOnUiThread(new Runnable() {
                             @Override
                             public void run()
                             {
                                 _map.addMarker(new MarkerOptions()
                                         .position(position)
                                         .icon(BitmapDescriptorFactory.defaultMarker()));
                             }
                         });

                        locationChanged(position);

                        try
                        {
                            Thread.sleep(3000);
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    prev = point;
                }
            }
        };

        _trackerThread.start();
    }

    public void locationChanged(LatLng point)
    {
        if (AudioService.isPlaying())
            stopService(new Intent(this, AudioService.class));

        Uri uri = _routeController.getResourceToPlay(_route, point);

        if (uri == Uri.EMPTY)
            return;

        AudioService.setTrack(this, uri);
        startService(new Intent(this, AudioService.class));

        _routeController.doneCurrentAudioPoint();
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
            startFakeLocationTracking();
            _once = true;
        }

        PolylineOptions route = new PolylineOptions()
                .width(25)
                .color(0x7F0000FF)
                .geodesic(true);

        ArrayList<Point> points = _route.geoPoints();
        for(Point point : points)
        {
            route.add(point.Position);
        }

        MapHelper.putMarker(_map, points.get(0).Position, R.drawable.start);
        MapHelper.putMarker(_map, points.get(points.size() - 1).Position, R.drawable.finish);

        for(Point point : this._route.audioPoints())
        {
            MapHelper.putMarker(_map, point.Position, R.drawable.play);
        }

        Polyline polyline = _map.addPolyline(route);

        _map.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(
                this._route.getGeoPoint(0).Position, 16)));
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

    }

    @Override
    protected void onStop()
    {
        _wakeLock.release();
        super.onStop();

        stopService(new Intent(this, AudioService.class));
    }
}