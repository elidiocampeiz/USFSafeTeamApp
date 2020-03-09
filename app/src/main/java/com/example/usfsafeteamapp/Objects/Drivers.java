package com.example.usfsafeteamapp.Objects;

import com.google.firebase.firestore.ServerTimestamp;
import com.google.type.Date;
import com.google.type.LatLng;

public class Drivers {
    private String driver_id;
    private LatLng latlng;
    private @ServerTimestamp Date time_stamp;
    private String username;
    private Requests nextRequest;

    public Drivers(String driver_id, LatLng latlng, String username ) {
        this.driver_id = driver_id;
        this.latlng = latlng;
        this.time_stamp =   null;
        this.username = username;
        this.nextRequest = new Requests();
    }

    public Drivers(String driver_id, LatLng latlng, Date time_stamp ) {
        this.driver_id = driver_id;
        this.latlng = latlng;
        this.time_stamp  = null;
        this.nextRequest = new Requests();

    }

    public Drivers(String username){
        this.username = username;
        this.nextRequest = new Requests();
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }

    public void setLatlng(LatLng latlng) {
        this.latlng = latlng;
    }

    public void setTime_stamp(Date time_stamp) {
        this.time_stamp = time_stamp;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getTime_stamp() {
        return time_stamp;
    }

    public LatLng getLatlng() {
        return latlng;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public String getUsername() {
        return username;
    }

    public Requests getNextRequest() {
        return nextRequest;
    }

    public void setNextRequest(Requests nextRequest) {
        this.nextRequest = nextRequest;
    }
    public void updateLatlng(LatLng latlng){
        this.latlng = latlng;
        this.time_stamp =null;

    }
}
