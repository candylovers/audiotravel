package com.home.croaton.audiotravel;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private ArrayList<LatLng> mainRoute = new ArrayList<>();
    private GoogleMap mMap;
    private AudioService audioService;
    private GoogleApiClient _googleApiClient;
    private LocationTracker _tracker;
    private boolean _onlyOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FillMainRoute();

        startLocationTracking();
    }

    public void locationChanged(LatLng point)
    {
        float[] results = new float[10];
        Location.distanceBetween(
                59.32284,
                18.06667,
                point.latitude,
                point.longitude,
                results);
        if (!_onlyOnce && results[0] < 50)
        {
            _onlyOnce = true;
            startService(new Intent(this, AudioService.class));
        }

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

    private void FillMainRoute() {
        mainRoute.add(new LatLng(59.32311, 18.06753));
        mainRoute.add(new LatLng(59.32284, 18.06667));
        mainRoute.add(new LatLng(59.32449, 18.06564));
        mainRoute.add(new LatLng(59.32505, 18.0651));
        mainRoute.add(new LatLng(59.32491, 18.06382));
        mainRoute.add(new LatLng(59.32465, 18.06215));
        mainRoute.add(new LatLng(59.32536, 18.06185));
        mainRoute.add(new LatLng(59.32587, 18.06138));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        PolylineOptions route = new PolylineOptions()
                .width(25)
                .color(0x7F0000FF)
                .geodesic(true);

        for(LatLng point : mainRoute)
        {
            route.add(point);
            CircleOptions circleOptions = new CircleOptions()
                    .center(point)
                    .radius(5)
                    .fillColor(0x7F0000FF)
                    .strokeColor(0x000000FF)
                    .zIndex(1);

            Circle circle = mMap.addCircle(circleOptions);

            /*mMap.addMarker(new MarkerOptions()
                .position(point)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .alpha(0.8f));*/
        }

        Polyline polyline = mMap.addPolyline(route);

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(
                mainRoute.get(0), 16)));
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (_googleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(_googleApiClient, _tracker);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        _tracker.resume();
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        stopService(new Intent(this, AudioService.class));
    }
}