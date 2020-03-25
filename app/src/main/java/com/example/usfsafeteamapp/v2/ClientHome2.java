package com.example.usfsafeteamapp.v2;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.usfsafeteamapp.Driver.DriverWait;
import com.example.usfsafeteamapp.MainActivity;
import com.example.usfsafeteamapp.Objects.myPlace;
import com.example.usfsafeteamapp.R;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientHome2 extends AppCompatActivity implements OnMapReadyCallback, RoutingListener {

    String TAG;
    private GoogleMap mMap;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;

    private FusedLocationProviderClient mFusedLocationClient;
    private PlacesClient placesClient;
    private SupportMapFragment mapFragment;
    private Button ConfButton;
    private TextView txtTime;
    private RelativeLayout mCustomerInfo;

    private MarkerOptions curr_mkr;

    private FirebaseFirestore mDb;
    private String clientIdRef;

    private myPlace myCurrPlace , myDestPlace;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_home2);

        //Creating the activity title and a back button
        getSupportActionBar().setTitle("Client Home2");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Declaring the polyline array
        polylines = new ArrayList<>();

        mDb = FirebaseFirestore.getInstance(); // init firebase

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_frag2);

        mapFragment.getMapAsync(this);

        clientIdRef = (String) FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Google Places autocomplete fragment
        setUpPlacesAPI();
        setCurrPlace();
        // Initialize the AutocompleteSupportFragment.
        setUpAutocompleteSupportFragment();

        //Confirm Request layout
        txtTime = (TextView ) findViewById(R.id.textViewEstimatedTimeHome2);

        ConfButton = (Button) findViewById(R.id.buttonClientConfirmHome2);
        ConfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ClientHome2.this, DriverWait.class);

//                i.putExtra("request", nRequest.getRequest_id());
                startActivity(i);
//                LayoutInflater inflater = LayoutInflater
//                        .from(getApplicationContext());

            }
        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mLocationRequest = new LocationRequest();
//        mLocationRequest.setSmallestDisplacement(10);
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            }else{
                checkLocationPermission();
            }
        }

//        //Get & Display Driver's current location in map
//        //TODO: Put it into a function that makes the driver "online"
////        checkLocationPermission();
//        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
//        mMap.setMyLocationEnabled(true);
        connectLocation();




    }


    LocationCallback mLocationCallback = new LocationCallback()
    {
        @Override
        public void onLocationResult(LocationResult locationResult) {

            for(Location location : locationResult.getLocations()){
//                Log.d(TAG, "onLocationResult: B1");
                if(getApplicationContext()!=null) {

                    mLastLocation = location;
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

//                    Log.d(TAG, "onLocationResult: B2");

                    //update it in the db
                    //NOTE: At this point a driver with the Auth usr id as the document id is already
                    DocumentReference DO = mDb.collection("Clients").document(clientIdRef);
//                    Drivers dr = new Drivers(driverIdRef,new GeoPoint( location.getLatitude(), location.getLongitude() ) );
//                    DO.set(dr, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            Log.d(TAG, "DocumentSnapshot successfully written!:");
//                        }
//                    })
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Log.w(TAG, "Error writing document", e);
//                                }
//                            });
                    GeoPoint gp = new GeoPoint( location.getLatitude(), location.getLongitude() );
//                    Drivers dr = new Drivers(driverIdRef,gp );
//                    DO.set(dr, SetOptions.merge());
                    DO.update("geoPoint",gp )
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully updated!:");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error updating document", e);
                                }
                            });


                }

            }
        }

    };

    public void setUpPlacesAPI(){
        //Init Places
        String apiKey = getString(R.string.google_maps_api_key);


        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        // Create a new Places client instance.
        placesClient = Places.createClient(this);
    }
    public void setUpAutocompleteSupportFragment()
    {
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragmentHome2);


        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected( Place place)
            {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId()+", LatLng: "+ place.getLatLng() );


                txtTime.setVisibility(View.VISIBLE);
                ConfButton.setVisibility(View.VISIBLE);



                //Transform it into LatLng
                LatLng LL = place.getLatLng();

                //Clear the map after the user changes the location selected
                mMap.clear();

                //Create a marker for the place selected by the user
                MarkerOptions place_mkr = new MarkerOptions().position(LL).title(place.getName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                //Place the marker for your location and the chosen destination into the map
                mMap.addMarker(place_mkr);
                mMap.addMarker(curr_mkr);

                //Calling function that will create the route to the destination
                getRouteToMarker(LL);



            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    private void getRouteToMarker(LatLng LL) {
        Routing routing = new Routing.Builder()
                .key(getString(R.string.google_maps_api_key))
                .travelMode(AbstractRouting.TravelMode.BIKING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(myCurrPlace.getLatLng(), LL)
                .build();
        routing.execute();
    }

    public void setCurrPlace(){
//        myPlace ret = new myPlace();
//        String placeN , placeId="";
//        final LatLng placeLL;
//        float max = 0;
        // Use fields to define the data types to return.
        // Use fields to define the data types to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

// Use the builder to create a FindCurrentPlaceRequest.
        FindCurrentPlaceRequest request =
                FindCurrentPlaceRequest.newInstance(placeFields);

        //check location permission
        checkLocationPermission();
// Call findCurrentPlace and handle the response (first check that the user has granted permission).

        Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
        placeResponse.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
            @Override
            public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                if (task.isSuccessful()) {
                    FindCurrentPlaceResponse response = task.getResult();
                    if(response!=null)
                    {
                        myCurrPlace = new myPlace(response.getPlaceLikelihoods().get(0).getPlace());
                        curr_mkr = new MarkerOptions().position(myCurrPlace.getLatLng()).title(myCurrPlace.getName());
                        mMap.addMarker(curr_mkr);
                    }
                    else{
                        Log.e(TAG, "Place not found: " );

                        GeoPoint geo = new GeoPoint(mLastLocation.getLatitude(),mLastLocation.getLongitude());
                        myCurrPlace = new myPlace("Pick Up Spot", "Stating Location", geo);
                        curr_mkr = new MarkerOptions().position(myCurrPlace.getLatLng()).title(myCurrPlace.getName());
                        mMap.addMarker(curr_mkr);
                    }

                } else {
                    Exception exception = task.getException();
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                    }
                }
            }
        });


    }

    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(ClientHome2.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(ClientHome2.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    private void connectLocation(){
        checkLocationPermission();
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);


    }
    //function that removes the driver from the DriverOnline Collection
    //Triggered when a driver goes offline or accepts a new request
    private void disconnectDriver(){
        if(mFusedLocationClient != null){
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }

        CollectionReference DriversOnlineRef = mDb.collection("Clients");
        DriversOnlineRef.document(clientIdRef)
                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "DocumentSnapshot successfully deleted!");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });


    }



    //Menu Options

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.example_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.item1:
                Toast.makeText(this, "You are now logged out", Toast.LENGTH_SHORT).show();

                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ClientHome2.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;

            case R.id.item2:
                Toast.makeText(this, "To be implemented", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }




    //Methods to create the polyline

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};

    @Override
    public void onRoutingFailure(RouteException e)
    {
        // The Routing request failed
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex)
    {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingCancelled() {

    }
}