package com.example.ubicomp.checkinoutsystem;

import java.util.HashMap;

public class Constants {

    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = 24*60*60*1000; // -1 means NEVER_ESPIRE
    public static final float GEOFENCE_RADIUS_IN_METERS = 150;

    public static final HashMap<String, LatLng> LANDMARKS = new HashMap<String, LatLng>();
    static {
        // HCO Kollegiet 55.373641, 10.403528
        LANDMARKS.put("HCO Kollegiet", new LatLng(55.373641,10.403528));

        // SDU 55.370846, 10.428020
        LANDMARKS.put("SDU", new LatLng(55.370846,10.428020));


    }
}
