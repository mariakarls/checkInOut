package com.example.ubicomp.checkinoutsystem;

/**
 * Class to represent a specific place through its latitude and longitude
 */
public class LatLng {

    public double latitude;
    public double longitude;

    public LatLng(double _latitude, double _longitude){
        this.latitude = _latitude;
        this.longitude = _longitude;
    }

}