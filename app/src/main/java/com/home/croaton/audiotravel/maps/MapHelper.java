package com.home.croaton.audiotravel.maps;

import android.content.Context;
import android.os.Build;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;

public class MapHelper
{
    public static Marker putMarker(Context context, MapView map, GeoPoint position, int resourceId)
    {
        Marker marker = new Marker(map);
        marker.setPosition(position);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);

        setMarkerIconFromResource(context, resourceId, marker);

        marker.setDraggable(true);
        map.getOverlays().add(marker);
        map.invalidate();

        return marker;
    }

    public static Circle addCircle(Context context, MapView map, GeoPoint position, Integer radius)
    {
        Circle circle = new Circle(context, position, radius);
        circle.setStrokeColor(0xFFFF0050);
        circle.setStrokeWidth(4);

        map.getOverlays().add(circle);
        map.invalidate();

        return circle;
    }

    public static void changeIcon(Context context, ArrayList<Marker> audioPointMarkers, Integer index, int resId)
    {
        if (audioPointMarkers.size() > index)
            setMarkerIconFromResource(context, resId, audioPointMarkers.get(index));
    }

    private static void setMarkerIconFromResource(Context context, int resourceId, Marker marker) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            marker.setIcon(context.getResources().getDrawable(resourceId, null));
        }
        else
        {
            marker.setIcon(context.getResources().getDrawable(resourceId));
        }
    }
}
