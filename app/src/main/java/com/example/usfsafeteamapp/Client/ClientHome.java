package com.example.usfsafeteamapp.Client;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;
import android.widget.TextView;

import com.example.usfsafeteamapp.FetchURL;
import com.example.usfsafeteamapp.R;
import com.example.usfsafeteamapp.TaskLoadedCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteFragment;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;


import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ClientHome extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {

    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    LocationManager locm;
    LatLng curr_coords, dest_coords;
    //28.063959, -82.413417


    final LatLng msc_LatLng = new LatLng(28.0639,-82.4134);
    MarkerOptions curr_mkr, msc_mkr;
    Polyline currentPolyline;

    int AUTOCOMPLETE_REQUEST_CODE = 1;
    String TAG;
    Place destination;



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_client_home);

        Button B = findViewById(R.id.buttonConfirm);

        B.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ClientHome.this, ClientWait.class);
                startActivity(i);
            }
        });
        //Initialize Places API
        setUpPlacesAPI();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment2);
        mapFragment.getMapAsync(this);

        msc_mkr = new MarkerOptions().position(new LatLng(28.0639,-82.4134)).title("MSC");
//        msc_mkr = new MarkerOptions().position(new LatLng(28.0639,-82.4134)).title("MSC").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));


        // Initialize the AutocompleteSupportFragment.
        setUpAutocompleteSupportFragment();


        locm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);

        }

        if(locm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {
            locm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener()
            {

                @Override
                public void onLocationChanged(Location location) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();

//                    mapFragment.getMapAsync(ClientHome.this);
                    curr_coords = new LatLng(lat,lon );
                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curr_coords, 14.2f));

                    curr_mkr = new MarkerOptions().position(curr_coords).title("This is my position");

//                    String url = getUrl(curr_mkr.getPosition(), msc_mkr.getPosition(), "bicycling");
//
//                    new FetchURL(ClientHome.this).execute(url, "bicycling");
                    mMap.addMarker(curr_mkr);

//                    Geocoder geo = new Geocoder(getApplicationContext());
//
//                    try{
//                        List<Address> list = geo.getFromLocation(lat,lon,1);
//
//                        String str = list.get(0).getLocality() + ", ";
//                        str += list.get(0).getCountryName();
//                        mkr = new MarkerOptions().position(curr_coords).title("This is my position");
//
//                    }
//                    catch(IOException e){
//                        e.printStackTrace();
//                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });

        }
        else if(locm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            locm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();

                    curr_coords = new LatLng(lat,lon );
                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curr_coords, 14.2f));

                    curr_mkr = new MarkerOptions().position(curr_coords).title("This is my position");

//                    String url = getUrl(curr_mkr.getPosition(), msc_mkr.getPosition(), "bicycling");
//
//                    new FetchURL(ClientHome.this).execute(url, "bicycling");
                    mMap.addMarker(curr_mkr);


//
//                    Geocoder geo = new Geocoder(getApplicationContext());
//
//                    try{
//                        List<Address> list = geo.getFromLocation(lat,lon,1);
//                        String str = list.get(0).getLocality() + ", ";
//                        str += list.get(0).getCountryName();
//
//                        mMap.addMarker(new MarkerOptions().position(curr_coords).title("This is my position"));
//                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curr_coords, 12.2f));
//                    }
//                    catch(IOException e){
//                        e.printStackTrace();
//                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });

        }

//        final Context cntx = this;
//        TextView txt2 = findViewById(R.id.textView2);
//        txt2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Set the fields to specify which types of place data to
//                // return after the user has made a selection.
//                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);
//
//                // Start the autocomplete intent.
//                Intent Placeintent = new Autocomplete.IntentBuilder(
//                        AutocompleteActivityMode.OVERLAY, fields)
//                        .build(cntx);
//                startActivityForResult(Placeintent, AUTOCOMPLETE_REQUEST_CODE);
//            }
//        });

    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(msc_LatLng, 12.2f));

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));

        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//        mMap.addMarker(new MarkerOptions().position(dest_coords).title("Destination!").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode)
    {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_api_key);
        return url;
    }


//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (mMap != null){
//            mMap.clear();
//        }
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
//            if (resultCode == RESULT_OK) {
//                Place place = Autocomplete.getPlaceFromIntent(data);
//
//                //TO DO: ADD Route
//                //mMap.addMarker(new MarkerOptions().)
//
//
//                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
//            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
//                // TODO: Handle the error.
//                Status status = Autocomplete.getStatusFromIntent(data);
//                Log.i(TAG, status.getStatusMessage());
//            } else if (resultCode == RESULT_CANCELED) {
//                // The user canceled the operation.
//            }
//        }
//    }
    public void setUpPlacesAPI(){
        //Init Places
        String apiKey = getString(R.string.google_maps_api_key);


        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);
    }
    public void setUpAutocompleteSupportFragment()
    {
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);


        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected( Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId()+", LatLng: "+ place.getLatLng() );
//                assert place.getLatLng()!= null;
                destination = place;
                LatLng LL = place.getLatLng();
                mMap.clear();
//                mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title("Destination: "+ place.getName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                //dest_coords = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
                MarkerOptions place_mkr = new MarkerOptions().position(LL).title(place.getName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                mMap.addMarker(place_mkr);
                mMap.addMarker(curr_mkr);
                String url = getUrl(curr_mkr.getPosition(), place_mkr.getPosition(), "bicycling");

                new FetchURL(ClientHome.this).execute(url, "walking");
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LL, 12.2f));
                LatLngBounds LLB = new LatLngBounds(LL, curr_coords);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LLB.getCenter(), 15f));
//


            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }
}