package com.home.croaton.audiotravel;

import com.google.android.gms.maps.model.LatLng;
import com.home.croaton.audiotravel.domain.AudioPoint;
import com.home.croaton.audiotravel.domain.Route;
import com.home.croaton.audiotravel.domain.RouteSerializer;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class RouteGenerator
{
    @Test
    // Stas Khalash would not forgive me using unit tests for generating xml files. Sorry, Stas...
    public void route_serializer_not_a_test()
    {
        Route route = FillAbrahamsbergRoute();
        //RouteSerializer.serialize(route);
    }

    private Route FillDemoRoute()
    {
        Route route = new Route();
        route.addGeoPoint(new LatLng(59.32312, 18.06767));
        route.addGeoPoint(new LatLng(59.3228, 18.06668));
        route.addGeoPoint(new LatLng(59.32449, 18.06564));
        route.addGeoPoint(new LatLng(59.32508, 18.06496));
        route.addGeoPoint(new LatLng(59.32491, 18.06382));
        route.addGeoPoint(new LatLng(59.32465, 18.06215));
        route.addGeoPoint(new LatLng(59.32536, 18.06185));
        route.addGeoPoint(new LatLng(59.32587, 18.06138));

        AudioPoint point = new AudioPoint(new LatLng(59.32303, 18.06742), 5);
        route.addAudioPoint(point);
        route.addAudioTrack(point, R.raw.a0_1);
        route.addAudioTrack(point, R.raw.a0_2);

        point = new AudioPoint(new LatLng(59.3228, 18.06668), 5);
        route.addAudioPoint(point);
        route.addAudioTrack(point, R.raw.a0_3);

        point = new AudioPoint(new LatLng(59.32302, 18.06654), 5);
        route.addAudioPoint(point);
        route.addAudioTrack(point, R.raw.a0_3_2);

        point = new AudioPoint(new LatLng(59.32382, 18.06607), 5);
        route.addAudioPoint(point);
        route.addAudioTrack(point, R.raw.a1_0);
        route.addAudioTrack(point, R.raw.a1_1);

        point = new AudioPoint(new LatLng(59.3249, 18.06383), 10);
        route.addAudioPoint(point);
        route.addAudioTrack(point, R.raw.a2_0);
        route.addAudioTrack(point, R.raw.a2_1);

        point = new AudioPoint(new LatLng(59.32463, 18.06216), 5);
        route.addAudioPoint(point);
        route.addAudioTrack(point, R.raw.a3_0);

        return route;
    }

    private Route FillAbrahamsbergRoute()
    {
        Route route = new Route();
        route.addGeoPoint(new LatLng(59.33136, 17.95303));
        route.addGeoPoint(new LatLng(59.33115, 17.95282));
        route.addGeoPoint(new LatLng(59.33104, 17.9523));
        route.addGeoPoint(new LatLng(59.33107, 17.95162));
        route.addGeoPoint(new LatLng(59.33123, 17.95118));
        route.addGeoPoint(new LatLng(59.33123, 17.9508));
        route.addGeoPoint(new LatLng(59.33254, 17.94924));
        route.addGeoPoint(new LatLng(59.33226, 17.9486));
        route.addGeoPoint(new LatLng(59.33508, 17.95127));
        route.addGeoPoint(new LatLng(59.33624, 17.95212));
        route.addGeoPoint(new LatLng(59.33667, 17.94959));
        route.addGeoPoint(new LatLng(59.33611, 17.9491));
        route.addGeoPoint(new LatLng(59.33623, 17.94827));
        route.addGeoPoint(new LatLng(59.3366, 17.94734));
        route.addGeoPoint(new LatLng(59.33687, 17.94657));
        route.addGeoPoint(new LatLng(59.33701, 17.94594));
        route.addGeoPoint(new LatLng(59.3367, 17.94433));
        route.addGeoPoint(new LatLng(59.33762, 17.94024));
        route.addGeoPoint(new LatLng(59.33749, 17.93964));
        route.addGeoPoint(new LatLng(59.33698, 17.93902));
        route.addGeoPoint(new LatLng(59.33703, 17.93881));
        route.addGeoPoint(new LatLng(59.33629, 17.93882));
        route.addGeoPoint(new LatLng(59.33619, 17.93851));
        route.addGeoPoint(new LatLng(59.33595, 17.9388));
        route.addGeoPoint(new LatLng(59.3354, 17.93865));
        route.addGeoPoint(new LatLng(59.335, 17.93804));
        route.addGeoPoint(new LatLng(59.3347, 17.93832));
        route.addGeoPoint(new LatLng(59.33452, 17.9382));
        route.addGeoPoint(new LatLng(59.33437, 17.93862));
        route.addGeoPoint(new LatLng(59.33437, 17.93906));
        route.addGeoPoint(new LatLng(59.33435, 17.94024));
        route.addGeoPoint(new LatLng(59.33336, 17.9406));
        route.addGeoPoint(new LatLng(59.33293, 17.94045));
        route.addGeoPoint(new LatLng(59.33249, 17.94082));
        route.addGeoPoint(new LatLng(59.3322, 17.94119));
        route.addGeoPoint(new LatLng(59.33189, 17.9416));
        route.addGeoPoint(new LatLng(59.33182, 17.94273));
        route.addGeoPoint(new LatLng(59.33086, 17.94349));
        route.addGeoPoint(new LatLng(59.32992, 17.94465));
        route.addGeoPoint(new LatLng(59.32955, 17.94616));
        route.addGeoPoint(new LatLng(59.32961, 17.94675));
        route.addGeoPoint(new LatLng(59.32933, 17.94807));
        route.addGeoPoint(new LatLng(59.32966, 17.94862));
        route.addGeoPoint(new LatLng(59.32989, 17.94932));
        route.addGeoPoint(new LatLng(59.32963, 17.95022));
        route.addGeoPoint(new LatLng(59.32974, 17.95095));
        route.addGeoPoint(new LatLng(59.33074, 17.94964));
        route.addGeoPoint(new LatLng(59.33095, 17.95052));
        route.addGeoPoint(new LatLng(59.33123, 17.95118));
        route.addGeoPoint(new LatLng(59.33107, 17.95162));
        route.addGeoPoint(new LatLng(59.33104, 17.9523));
        route.addGeoPoint(new LatLng(59.33115, 17.95282));
        route.addGeoPoint(new LatLng(59.33138, 17.95288));

        AudioPoint point = new AudioPoint(0, new LatLng(59.33115, 17.95282), 5);
        route.addAudioPoint(point);
        route.addAudioTrack(point, R.raw.a0_1);
        route.addAudioTrack(point, R.raw.test_1);

        point = new AudioPoint(1, new LatLng(59.33107, 17.95162), 10);
        route.addAudioPoint(point);
        route.addAudioTrack(point, R.raw.test_2);

        point = new AudioPoint(2, new LatLng(59.33123, 17.9508), 15);
        route.addAudioPoint(point);
        route.addAudioTrack(point, R.raw.test_3);

        point = new AudioPoint(3, new LatLng(59.33254, 17.94924), 20);
        route.addAudioPoint(point);
        route.addAudioTrack(point, R.raw.test_4);

        point = new AudioPoint(4, new LatLng(59.33508, 17.95127), 5);
        route.addAudioPoint(point);
        route.addAudioTrack(point, R.raw.test_5);

        point = new AudioPoint(5, new LatLng(59.33667, 17.94959), 5);
        route.addAudioPoint(point);
        route.addAudioTrack(point, R.raw.test_6);

        point = new AudioPoint(6, new LatLng(59.3366, 17.94734), 5);
        route.addAudioPoint(point);
        route.addAudioTrack(point, R.raw.test_7);

        point = new AudioPoint(7, new LatLng(59.3367, 17.94433), 5);
        route.addAudioPoint(point);
        route.addAudioTrack(point, R.raw.test_8);

        point = new AudioPoint(8, new LatLng(59.33749, 17.93964), 5);
        route.addAudioPoint(point);
        route.addAudioTrack(point, R.raw.test_9);

        point = new AudioPoint(9, new LatLng(59.3347, 17.93832), 5);
        route.addAudioPoint(point);
        route.addAudioTrack(point, R.raw.test_10);

        point = new AudioPoint(10, new LatLng(59.33086, 17.94349), 5);
        route.addAudioPoint(point);
        route.addAudioTrack(point, R.raw.test_11);

        return route;
    }
}