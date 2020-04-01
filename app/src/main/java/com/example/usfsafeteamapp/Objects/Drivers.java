package com.example.usfsafeteamapp.Objects;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
public class Drivers {
    private String driver_id;
    private GeoPoint geoPoint;
    private @ServerTimestamp Date time_stamp;
//    private String username;
    private Requests nextRequest;
    private String current_request_id;


    public Drivers(String driver_id, GeoPoint geoPoint, Requests nextRequest) {
        this.driver_id = driver_id;
        this.geoPoint = geoPoint;
        this.time_stamp  = null;
        this.nextRequest = nextRequest;
    }
    public Drivers(String driver_id, GeoPoint geoPoint ) {
        this.driver_id = driver_id;
        this.geoPoint = geoPoint;
        this.time_stamp =   null;
//        this.username = username;

    }

    public Drivers(String driver_id){
        this.driver_id = driver_id;
        this.geoPoint =  null;
        this.time_stamp =   null;

    }
    public Drivers(){

    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }


    public Date getTime_stamp() {
        return time_stamp;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public String getDriver_id() {
        return driver_id;
    }



    public Requests getNextRequest() {
        return nextRequest;
    }

    public void setNextRequest(Requests nextRequest) {
        this.nextRequest = nextRequest;
    }


    public String getCurrent_request_id() {
        return current_request_id;
    }

    public void setCurrent_request_id(String current_request_id) {
        this.current_request_id = current_request_id;
    }
}
