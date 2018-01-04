package com.wlu.android.khan_fark_project;

/**
 * Created by Tyler on 2017-11-29.
 */

public class University {

    private String name;
    private double latitude;
    private double longitude;

    public University(String name, double latitude, double longitude){
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName(){
        return name;
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }
}
