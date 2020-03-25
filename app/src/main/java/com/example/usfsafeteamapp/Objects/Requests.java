package com.example.usfsafeteamapp.Objects;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;


public class Requests  {
    private myPlace start;
    private myPlace dest;
    private String request_id;
    private @ServerTimestamp Date time_stamp; //when null object of this type is passed to firestore the current date is automatically assigned
    private String driver_id;
    private String client_id;
    private boolean isStarted;

    public Requests(myPlace start, myPlace dest, String request_id) {
        this.start = start;
        this.dest = dest;
        this.request_id = request_id;
        this.time_stamp  = null;
        this.isStarted = false;
    }
    public Requests(myPlace start, myPlace dest, String request_id, String client_id) {
        this.start = start;
        this.dest = dest;
        this.request_id = request_id;
        this.client_id = client_id;

        this.time_stamp  = null;
        this.isStarted = false;
    }
    public Requests(myPlace start, myPlace dest, String request_id, String client_id, String driver_id) {
        this.start = start;
        this.dest = dest;
        this.request_id = request_id;
        this.client_id = client_id;
        this.driver_id = driver_id;
        this.time_stamp  = null;
        this.isStarted = false;
    }

    public Requests() {

    }

//    public Requests(Object...objects) {
//        this.start = new myPlace(objects[0]);
//        this.dest = new myPlace(objects[1]);
//        this.request_id = objects[3].toString();
//        this.time_stamp  = null;
//    }


    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }

    public myPlace getStart() {
        return start;
    }

    public void setStart(myPlace start) {
        this.start = start;
    }

    public myPlace getDest() {
        return dest;
    }

    public void setDest(myPlace dest) {
        this.dest = dest;
    }

    public Date getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(Date time_stamp) {
        this.time_stamp = time_stamp;
    }


    public String getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }
}
