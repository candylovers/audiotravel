package com.home.croaton.audiotravel.location;

import com.home.croaton.audiotravel.R;
import com.home.croaton.audiotravel.activities.MapsActivity;
import com.home.croaton.audiotravel.domain.Point;
import com.home.croaton.audiotravel.maps.MapHelper;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;

public class TrackEmulator
{
    private static Thread _trackerThread;

    public static void startFakeLocationTracking(MapsActivity activity, ArrayList<Point> points,
        MapView map)
    {
        final MapsActivity activityCopy = activity;
        final ArrayList<Point> pointsCopy = points;
        final MapView innerMap = map;

        _trackerThread = new Thread()
        {
            public void run()
            {
                Point prev = null;
                final ArrayList<Marker> markers = new ArrayList<>();
                for(Point point : pointsCopy)
                {
                    if (prev == null)
                    {
                        prev = point;
                        continue;
                    }

                    for (double i = 0.1; i <= 1; i += 0.1)
                    {
                        final GeoPoint position = new GeoPoint(
                                point.Position.getLatitude() * i + prev.Position.getLatitude() * (1d - i),
                                point.Position.getLongitude() * i + prev.Position.getLongitude() * (1d - i));

                        activityCopy.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                markers.add(MapHelper.putMarker(activityCopy, innerMap, position, R.drawable.step));

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
