package com.home.croaton.audiotravel.maps;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import com.home.croaton.audiotravel.R;
import com.home.croaton.audiotravel.activities.MapsActivity;
import com.home.croaton.audiotravel.audio.AudioPlaybackController;
import com.home.croaton.audiotravel.domain.AudioPoint;
import com.home.croaton.audiotravel.domain.Point;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.tileprovider.tilesource.MapBoxTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

public class MapHelper
{
    public static Marker putMarker(Context context, MapView map, GeoPoint position, int resourceId,
        float horizontalAnchor, float verticalAnchor)
    {
        Marker marker = new Marker(map);
        marker.setPosition(position);
        marker.setAnchor(horizontalAnchor, verticalAnchor);

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

        //map.getOverlays().add(circle);
        map.invalidate();

        return circle;
    }

    public static void changeIcon(Context context, ArrayList<Marker> audioPointMarkers, Integer index, int resId)
    {
        if (audioPointMarkers.size() > index)
            setMarkerIconFromResource(context, resId, audioPointMarkers.get(index));
    }

    public static void chooseBeautifulMapProvider(Context context, MapView map)
    {
        MapBoxTileSource tileSource = new MapBoxTileSource(context);
        map.setTilesScaledToDpi(true);
        map.setMaxZoomLevel(17);
        tileSource.setMapboxMapid("mapbox.emerald");
        map.setTileSource(tileSource);
    }

    public static void addLocationOverlay(Context context, MapView map)
    {
        OverlayManager overlayManager = map.getOverlayManager();
        List<Overlay> overlays = overlayManager.overlays();
        for(int i = overlays.size() - 1; i >= 0 ; i--)
        {
            if (overlays.get(i) instanceof MyLocationNewOverlay)
            {
                overlayManager.remove(i);
            }
        }

        MyLocationNewOverlay locationOverlay = new MyLocationNewOverlay(context,
                new GpsMyLocationProvider(context),map);
        locationOverlay.enableMyLocation();
        map.getOverlays().add(locationOverlay);
    }

    public static void drawRoute(Context context, MapView map, List<Point> points) {
        Polyline line = new Polyline(context);

        line.setSubDescription(Polyline.class.getCanonicalName());
        line.setWidth(15f);
        line.setColor(ContextCompat.getColor(context, R.color.orange_partially_transparent));

        List<GeoPoint> geoPoints = new ArrayList<>();

        for(Point point : points)
            geoPoints.add(point.Position);

        line.setPoints(geoPoints);
        line.setGeodesic(true);
        map.getOverlayManager().add(line);
    }

    public static void drawAudioPoints(Context context, MapView map, AudioPlaybackController controller,
                                  List<Marker> markers, List<Circle> circles) {
        for(AudioPoint point : controller.audioPoints())
        {
            boolean isPointPassed = controller.isAudioPointPassed(point.Number);
            int resId = isPointPassed
                    ? R.drawable.passed
                    : R.drawable.audio_point_big;

            float anchor = isPointPassed
                    ? Marker.ANCHOR_CENTER
                    : Marker.ANCHOR_BOTTOM;

            Marker marker = MapHelper.putMarker(context, map, point.Position, resId,
                    Marker.ANCHOR_CENTER, anchor);
            markers.add(marker);
            circles.add(MapHelper.addCircle(context, map, point.Position, point.Radius));
        }
    }

    public static void focusCameraOnPoint(MapView map, Point point) {
        IMapController mapController = map.getController();
        mapController.setZoom(16);
        mapController.setCenter(point.Position);

    }

    public static void setStartRouteIcon(MapsActivity context, MapView map, GeoPoint position) {
        putMarker(context, map, position, R.drawable.start, 0.15f, 0.9f);
    }

    public static void setEndRouteIcon(MapsActivity context, MapView map, GeoPoint position) {
        putMarker(context, map, position, R.drawable.finish, 0.15f, 0.9f);
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
