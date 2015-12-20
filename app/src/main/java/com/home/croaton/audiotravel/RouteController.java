package com.home.croaton.audiotravel;

import com.google.android.gms.maps.model.LatLng;

public class RouteController
{
    public static Route GetDemoRoute()
    {
        Route route = new Route();

        route.addPoint(new LatLng(59.32311, 18.06753));
        route.addPoint(new LatLng(59.32284, 18.06667));
        route.addPoint(new LatLng(59.32449, 18.06564));
        route.addPoint(new LatLng(59.32505, 18.0651));
        route.addPoint(new LatLng(59.32491, 18.06382));
        route.addPoint(new LatLng(59.32465, 18.06215));
        route.addPoint(new LatLng(59.32536, 18.06185));
        route.addPoint(new LatLng(59.32587, 18.06138));

        return route;
    }
}
