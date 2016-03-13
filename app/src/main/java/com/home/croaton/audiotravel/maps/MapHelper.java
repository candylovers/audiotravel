package com.home.croaton.audiotravel.maps;

import android.content.Context;
import android.os.Build;

import com.google.android.gms.maps.model.LatLng;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class MapHelper
{
    public static Marker putMarker(Context context, MapView map, GeoPoint position, Integer resourceId)
    {
        Marker marker = new Marker(map);
        marker.setPosition(position);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            marker.setIcon(context.getResources().getDrawable(resourceId, null));
        }
        else
        {
            marker.setIcon(context.getResources().getDrawable(resourceId));
        }

        marker.setDraggable(true);
        map.getOverlays().add(marker);
        map.invalidate();

        return marker;
    }

    public static Marker putMarker(Context context, MapView map, LatLng position, Integer resourceId)
    {
        return putMarker(context, map, new GeoPoint(position.latitude, position.longitude), resourceId);
    }

    public static Polygon addCircle(Context context, MapView map, GeoPoint position, Integer radius)
    {
        Polygon circle = new Polygon(context);
        circle.setPoints(Polygon.pointsAsCircle(position, radius));
        circle.setStrokeColor(0xFFFF0050);
        circle.setStrokeWidth(2);

        map.getOverlays().add(circle);
        map.invalidate();

        return circle;
    }

//    public static void changeIcon(ArrayList<Marker> audioPointMarkers, Integer index, int resId)
//    {
//        if (audioPointMarkers.size() > index)
//            audioPointMarkers.get(index).setIcon(BitmapDescriptorFactory.fromResource(resId));
//    }
}
