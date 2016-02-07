package com.home.croaton.audiotravel;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.home.croaton.audiotravel.activities.MapsActivity;
import com.home.croaton.audiotravel.domain.Point;

import java.util.ArrayList;

public class TestTracker
{
    private static Thread _trackerThread;

    public static void startFakeLocationTracking(MapsActivity activity, ArrayList<Point> points,
        GoogleMap map)
    {
        final MapsActivity activityCopy = activity;
        final ArrayList<Point> pointsCopy = points;
        final GoogleMap innerMap = map;

        _trackerThread = new Thread()
        {
            public void run()
            {
                Point prev = null;
                for(Point point : pointsCopy)
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

                        activityCopy.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                innerMap.addMarker(new MarkerOptions()
                                        .position(position)
                                        .icon(BitmapDescriptorFactory.defaultMarker()));
                                activityCopy.locationChanged(position);
                            }
                        });



                        try
                        {
                            Thread.sleep(3000);
                          }
                        catch (InterruptedException e)
                        {
                            return;
                        }
                    }

                    prev = point;
                }
            }
        };

        _trackerThread.start();
    }

    public static void stop()
    {
        if (_trackerThread != null) {
            _trackerThread.interrupt();
        }
    }
}
