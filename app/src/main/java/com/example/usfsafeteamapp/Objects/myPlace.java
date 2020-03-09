package com.example.usfsafeteamapp.Objects;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.firestore.GeoPoint;

public class myPlace {
    private String name;
    private String place_id;
    private GeoPoint geoPoint;

    public myPlace(String name, String place_id,  GeoPoint geoPoint) {
        this.name = name;
        this.place_id = place_id;
        this.geoPoint = geoPoint;
    }
    public myPlace(Place place){
        this.name = place.getName();
        this.place_id = place.getId();
        if (place.getLatLng()!=null)
            this.geoPoint = new GeoPoint(place.getLatLng().latitude, place.getLatLng().longitude);
    }
    public myPlace(){

    }
    public myPlace(Object...objects){
        this.name = (String)objects[1];
        this.place_id = (String) objects[1];
        this.geoPoint = null;
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

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }
    public LatLng getLatLng(){
        return new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
    }
}
