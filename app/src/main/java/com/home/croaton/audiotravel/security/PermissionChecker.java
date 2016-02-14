package com.home.croaton.audiotravel.security;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

public class PermissionChecker
{
    public static final int LocationPermissions = 1;

    public static boolean CheckForLocationDetectionPermission(Activity activity)
    {
        int fineLocationPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocationPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION);

        boolean alreadyHavePermission = true;
        if (fineLocationPermission != PackageManager.PERMISSION_GRANTED
                || coarseLocationPermission != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(activity,
                new String[]
                    {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    }, LocationPermissions);

            alreadyHavePermission = false;
        }

        return alreadyHavePermission;
    }
}
