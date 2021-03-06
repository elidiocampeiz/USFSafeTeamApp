package com.example.usfsafeteamapp.Objects;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Clients {
    private String client_id;
    private GeoPoint geoPoint;
    private String current_request_id;
    private @ServerTimestamp Date time_stamp;
    private String username;

    public Clients(String client_id, GeoPoint geoPoint, String username ) {
        this.client_id = client_id;
        this.geoPoint = geoPoint;
        this.time_stamp =  null;
        this.username = username;
    }

    public Clients(String client_id, GeoPoint geoPoint) {
        this.client_id = client_id;
        this.geoPoint = geoPoint;
        this.time_stamp  = null;

    }
    public Clients(String client_id){
        this.client_id = client_id;
        this.time_stamp  = null;

    }
    public Clients(){

    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }





    public void setUsername(String username) {
        this.username = username;
    }





    public String getClient_id() {
        return client_id;
    }

    public String getUsername() {
        return username;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public Date getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(Date time_stamp) {
        this.time_stamp = time_stamp;
    }

    public String getCurrent_request_id() {
        return current_request_id;
    }

    public void setCurrent_request_id(String current_request_it) {
        this.current_request_id= current_request_it;
    }
}
