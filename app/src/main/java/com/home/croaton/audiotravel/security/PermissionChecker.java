package com.home.croaton.audiotravel.security;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;

public class PermissionChecker
{
    public static final int LocationRequestCode = 1;
    public static final int LocalStorageRequestCode = 2;

    public static boolean checkForPermissions(Activity activity, String[] permissionNames,
                                              int requestCode)
    {
        Integer[] permissions = new Integer[permissionNames.length];
        ArrayList<String> notPermited = new ArrayList<>();
        for(int i = 0; i < permissions.length; i++)
        {
            permissions[i] = ActivityCompat.checkSelfPermission(activity, permissionNames[i]);
            if (permissions[i] != PackageManager.PERMISSION_GRANTED)
                notPermited.add(permissionNames[i]);
        }

        if (notPermited.size() == 0)
            return true;

        ActivityCompat.requestPermissions(activity, notPermited.toArray(new String[0]), requestCode);

        return false;
    }
}
