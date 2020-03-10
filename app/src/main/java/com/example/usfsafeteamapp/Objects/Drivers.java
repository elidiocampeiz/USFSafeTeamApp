package com.example.usfsafeteamapp.Objects;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
public class Drivers {
    private String driver_id;
    private GeoPoint geoPoint;
    private @ServerTimestamp Date time_stamp;
//    private String username;
    private Requests nextRequest;

    public Drivers(String driver_id, GeoPoint geoPoint ) {
        this.driver_id = driver_id;
        this.geoPoint = geoPoint;
        this.time_stamp =   null;
//        this.username = username;
        this.nextRequest = new Requests();
    }

    public Drivers(String driver_id, GeoPoint geoPoint, Date time_stamp ) {
        this.driver_id = driver_id;
        this.geoPoint = geoPoint;
        this.time_stamp  = null;
        this.nextRequest = new Requests();

    }

    public Drivers(String driver_id){
        this.driver_id = driver_id;
        this.nextRequest = new Requests();
    }
    public Drivers(){
        this.driver_id = "Driver";
        this.time_stamp  = null;
        this.nextRequest = new Requests();
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
    public LatLng getLatLng(){
        return new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
    }

}
