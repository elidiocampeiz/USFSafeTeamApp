package com.example.usfsafeteamapp.Objects;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;

public class myPlace {
    private String name;
    private String place_id;
    private LatLng latLng;

    public myPlace(String name, String place_id,  LatLng latLng) {
        this.name = name;
        this.place_id = place_id;
        this.latLng = latLng;
    }
    public myPlace(Place place){
        this.name = place.getName();
        this.place_id = place.getId();
        this.latLng = place.getLatLng();
    }
    public myPlace(){

    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
}
