package com.home.croaton.audiotravel;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.home.croaton.audiotravel.instrumentation.MyObservable;

import org.osmdroid.util.GeoPoint;

public class LocationTracker implements GoogleApiClient.ConnectionCallbacks,
        LocationListener, GoogleApiClient.OnConnectionFailedListener
{

    public MyObservable<LatLng> LocationChanged = new MyObservable<>();

    private boolean _started = false;
    private GoogleApiClient _googleApiClient;
    private LocationRequest _locationRequest;
    private Activity _mapsActivity;

    public LocationTracker(Activity activity)
    {
        _mapsActivity = activity;
        _locationRequest = new LocationRequest();
        _locationRequest.setInterval(5000);
        _locationRequest.setFastestInterval(500);
        _locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void startLocationTracking()
    {
        _googleApiClient = new GoogleApiClient.Builder(_mapsActivity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        _started = true;
        _googleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (_started)
        {
            try
            {
                LocationServices.FusedLocationApi.requestLocationUpdates(_googleApiClient,
                        _locationRequest, this);
            }
            catch(SecurityException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        // ToDo: notify user that we are not tracking her right now
    }

    @Override
    public void onLocationChanged(Location location)
    {
        LocationChanged.notifyObservers(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void resume() {
        if (_started && _googleApiClient.isConnected())
        {
            try
            {
                LocationServices.FusedLocationApi.requestLocationUpdates(_googleApiClient,
                        _locationRequest, this);
            }
            catch(SecurityException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    public static float GetDistance(GeoPoint point1, GeoPoint point2)
    {
        float[] results = new float[10];
        Location.distanceBetween(
                point1.getLatitude(),
                point1.getLongitude(),
                point2.getLatitude(),
                point2.getLongitude(),
                results);

        // ToDo: check, is that always resulrs[0]?
        return results[0];
    }
}
