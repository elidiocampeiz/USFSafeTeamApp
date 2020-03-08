package com.example.usfsafeteamapp.Objects;

import com.google.firebase.firestore.ServerTimestamp;
import com.google.type.Date;
import com.google.type.LatLng;

public class Clients {
    private String client_id;
    private LatLng latlng;
    private @ServerTimestamp Date time_stamp;
    private String username;

    public Clients(String client_id, LatLng latlng, String username ) {
        this.client_id = client_id;
        this.latlng = latlng;
        this.time_stamp =  null;
        this.username = username;
    }

    public Clients(String client_id, LatLng latlng, Date time_stamp ) {
        this.client_id = client_id;
        this.latlng = latlng;
        this.time_stamp  = null;

    }
    public Clients(){

    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
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

    public String getClient_id() {
        return client_id;
    }

    public String getUsername() {
        return username;
    }
}
