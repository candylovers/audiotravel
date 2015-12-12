package com.home.croaton.audiotravel;

import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

public class LocationTracker implements GoogleApiClient.ConnectionCallbacks,
        LocationListener, GoogleApiClient.OnConnectionFailedListener
{
    private boolean requestingLocationUpdates = true;
    private GoogleApiClient _googleApiClient;
    private LocationRequest _locationRequest;
    private Location _lastLocation;
    private MapsActivity _mapsActivity;

    public LocationTracker(MapsActivity activity)
    {
        _mapsActivity = activity;
        _locationRequest = new LocationRequest();
        _locationRequest.setInterval(10000);
        _locationRequest.setFastestInterval(5000);
        _locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void setGogleApiClient(GoogleApiClient googleApiClient)
    {
        _googleApiClient = googleApiClient;
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        if (requestingLocationUpdates)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(_googleApiClient,
                    _locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i)
    { }

    @Override
    public void onLocationChanged(Location location)
    {
        _lastLocation = location;

        // ToDo: write event firing here
        _mapsActivity.locationChanged(new LatLng(_lastLocation.getLatitude(), _lastLocation.getLongitude()));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void resume() {
        if (_googleApiClient.isConnected() && !requestingLocationUpdates) {
            LocationServices.FusedLocationApi.requestLocationUpdates(_googleApiClient, _locationRequest,
                    this);
        }
    }
}
