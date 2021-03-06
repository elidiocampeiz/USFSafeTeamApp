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
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.usfsafeteamapp.AboutSafeTeam;
import com.example.usfsafeteamapp.MainActivity;
import com.example.usfsafeteamapp.Objects.Clients;
import com.example.usfsafeteamapp.Objects.Drivers;
import com.example.usfsafeteamapp.Objects.Requests;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.google.android.gms.maps.model.JointType.ROUND;

public class ClientHome2 extends AppCompatActivity implements OnMapReadyCallback, RoutingListener {

    //Msc Location
    final LatLng msc_LatLng = new LatLng(28.0639,-82.4134);

    //Variable to update the time
    TextView estimatedTime;


    String TAG;
    private GoogleMap mMap;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;

    private FusedLocationProviderClient mFusedLocationClient;
    private PlacesClient placesClient;
    private SupportMapFragment mapFragment;
    private Button ConfButton;
    private Button cancelButton;
    CardView mCardView;

    private MarkerOptions curr_mkr, dest_mkr, driver_mkr;

    private FirebaseFirestore mDb;
    private String clientIdRef;
    //    List<Drivers> availableDrivers;
    private Drivers assignDriver;
    private String assignDriverId;

//    ListenerRegistration mClosestDriversListener;

    private myPlace myCurrPlace ;
    private Requests mRequest;
    private Query mClosestDriverQuery;
    private boolean isTrackingEnable;
    private AutocompleteSupportFragment autocompleteFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_home2);


        //Creating the activity title and a back button
        getSupportActionBar().setTitle("Client Map");
//        getSupportActionBar().hide();
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        isTrackingEnable = true;
        //Declaring the polyline array
        polylines = new ArrayList<>();

        estimatedTime = findViewById(R.id.textViewEstimatedTimeHome2);

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
        mCardView = findViewById(R.id.cardView_autocomplete);


        cancelButton = (Button) findViewById(R.id.buttonClientCancelHome2);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
               cancelRequestMessage();


            }
        });



        ConfButton = (Button) findViewById(R.id.buttonClientConfirmHome2);
        ConfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mRequest == null){

                    Toast t = Toast.makeText(ClientHome2.this, "No place selected, PLease try again", Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                }
                else {
                    //TODO: Put the following code in the clickListener of the Confirm button

                    getClosestAvailableDriver();

                }



            }
        });
//        getClosestAvailableDriver();
    }




    private void StartRequestTracking() {

        if (mRequest!= null){
            DocumentReference mRequestRef = mDb.collection("Requests").document(mRequest.getRequest_id());
            mRequestRef.addSnapshotListener(ClientHome2.this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);

                    }
                    if (snapshot != null && snapshot.exists() && mRequest != null) {
                        Log.d(TAG, "Request snapshot success!");
                        mRequest = snapshot.toObject(Requests.class);
                        getRouteFromRequest();
                    }

                }
            });

        }


    }

    // "unassigned" | "assigned" | "ride" | "fulfilled" | "canceled"
    private void getRouteFromRequest() {
        //clear map


        if (mRequest != null){
            Log.d(TAG, "Current State: " + mRequest.getState());

            if ( mRequest.getState().equals("unassigned") ) {
                mMap.clear();
                // display "looking for Driver" -> "Waiting for Confirmation" -> "Driver found!"
                Toast t = Toast.makeText(getApplicationContext(), "Request assigned. Confirming closest available driver", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();

                //TODO: Start Animation of looking for driver
                cancelButton.setVisibility(View.VISIBLE);
                mCardView.setVisibility(View.INVISIBLE);
                ConfButton.setVisibility(View.INVISIBLE);

                getRouteToMarker(mRequest.getStart().getLatLng(), mRequest.getDest().getLatLng());

                //mkrs from start to dest
                curr_mkr = new MarkerOptions().position(mRequest.getStart().getLatLng()).title(mRequest.getStart().getName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                dest_mkr = new MarkerOptions().position(mRequest.getDest().getLatLng()).title(mRequest.getDest().getName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

                mMap.addMarker(curr_mkr);
                mMap.addMarker(dest_mkr);
            }
            else if( mRequest.getState().equals("assigned") ) {

                Toast t = Toast.makeText(getApplicationContext(), "Your Safe Team is on its way!", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();

                mCardView.setVisibility(View.INVISIBLE);
                ConfButton.setVisibility(View.INVISIBLE);

                if(cancelButton.getVisibility() == View.INVISIBLE)
                {
                    cancelButton.setVisibility(View.VISIBLE);
                }

                // display "Driver Assigned" -> "Time of arrival: --:--"
                getRouteFromDriver();



            }
            else if( mRequest.getState().equals("ride") ) {


                Toast t = Toast.makeText(getApplicationContext(), "Your Safe Team Arrived", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();


                if(mCardView.getVisibility() == View.VISIBLE)
                {
                    mCardView.setVisibility(View.INVISIBLE);
                }

                if(ConfButton.getVisibility() == View.VISIBLE)
                {
                    ConfButton.setVisibility(View.INVISIBLE);
                }

                if(cancelButton.getVisibility() == View.VISIBLE)
                {
                    cancelButton.setVisibility(View.INVISIBLE);
                }




                // display "Driver Assigned" -> "Driver found!" -> "Waiting for Confirmation"
                //TODO: Set Cart image as marker icon

                getRouteToDestination();


            }
            else if( mRequest.getState().equals("fulfilled") ) {


                //... ->Store in clients history
                //...

                Toast t = Toast.makeText(getApplicationContext(), "Request Fulfilled", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();

                resetClient();
                mMap.clear();
                autocompleteFragment.setText("");
                autocompleteFragment.setHint("Where to?");

                estimatedTime.setVisibility(View.INVISIBLE);

                mCardView.setVisibility(View.VISIBLE);
                ConfButton.setVisibility(View.INVISIBLE);

                if(cancelButton.getVisibility() == View.VISIBLE)
                {
                    cancelButton.setVisibility(View.INVISIBLE);
                }


                erasePolylines();
                //mRequest = null;
            }

            else if( mRequest.getState().equals("canceled"))
            {

                Toast t = Toast.makeText(getApplicationContext(), "Request Canceled", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();


                mMap.clear();
                erasePolylines();
                autocompleteFragment.setText("");
                autocompleteFragment.setHint("Where to?");

                estimatedTime.setVisibility(View.INVISIBLE);

                mCardView.setVisibility(View.VISIBLE);
                ConfButton.setVisibility(View.INVISIBLE);

                if(cancelButton.getVisibility() == View.VISIBLE)
                {
                    cancelButton.setVisibility(View.INVISIBLE);
                }
                deleteRequest();
                resetClient();
            }

        }else {
            Log.d(TAG, "getRouteToDestination Fail");
        }



    }

    private void deleteRequest() {
        if (mRequest != null){
            DocumentReference RequestRef = mDb.collection("Requests").document(mRequest.getRequest_id());
            RequestRef.delete().addOnCompleteListener(new OnCompleteListener<Void>()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if (task.isSuccessful()){
                        Log.d(TAG, "Document delete success");
                    }
                    else {
                        Log.d(TAG, "Document delete Error:");
                    }
                }
            });
        }

    }

    private void cancelRequestMessage() {

        new AlertDialog.Builder(this)
                .setTitle("Cancel Request")
                .setMessage("Are you sure you want to cancel the current Request?")

                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        cancelRequest();
                        dialogInterface.cancel();
                    }
                })
                .create()
                .show();
    }

    private void cancelRequest()
    {
        DocumentReference RequestRef = mDb.collection("Requests").document(mRequest.getRequest_id());
        RequestRef.update("state","canceled").addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful()){
                    Log.d(TAG, "Document update success");
                }
                else {
                    Log.d(TAG, "Document update Error:");
                }
            }
        });

    }


    //function called as a result of a change in the Request Object from FireStore
    private void resetClient()
    {

        DocumentReference doc = mDb.collection("Clients").document(clientIdRef);
        doc.update("current_request_id", null).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "DocumentSnapshot successfully !");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting updated", e);
                    }
                });

        //TODO: make the mRequest update happen from an event in the database
        mRequest = null;
        assignDriver = null;

    }

    private void getRouteToDestination() {
        if ( mRequest != null && mLastLocation != null ){
            Log.d(TAG, "getRouteToDestination success!");
            LatLng myCurrLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mMap.clear();

            getRouteToMarker( myCurrLocation, mRequest.getDest().getLatLng());

            mMap.addMarker(dest_mkr);

        } else {
            Log.d(TAG, "getRouteToDestination Fail");
        }

    }

    private void getRouteFromDriver() {
        if (mRequest != null && mRequest.getDriver_id() != null){
            Log.d(TAG, "getRouteFromDriver success");
            Query mDriverQueury = mDb.collection("DriversOnline").whereEqualTo("driver_id", mRequest.getDriver_id());
            mDriverQueury.addSnapshotListener(ClientHome2.this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    for (QueryDocumentSnapshot doc : value) {
                        GeoPoint driverGp = doc.get("geoPoint", GeoPoint.class);
//TODO: ******
                        if (driverGp !=  null &&  mRequest != null && mRequest.getState().equals("assigned") ) {

                            LatLng driver_pos = new LatLng(driverGp.getLatitude(), driverGp.getLongitude());
                            //TODO: Set Cart image as marker icon

                            mMap.clear();
                            driver_mkr = new MarkerOptions().position(driver_pos).title("Your Driver is here").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                            curr_mkr = new MarkerOptions().position(mRequest.getStart().getLatLng()).title(mRequest.getStart().getName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

                            mMap.addMarker(curr_mkr);
                            mMap.addMarker(driver_mkr);

                            getRouteToMarker(driver_pos, mRequest.getStart().getLatLng() );
                            Log.d(TAG, "New GeoPoint: " + driverGp);
                        }
                    }

                }
            });
        }
        else {
            Log.d(TAG, "getRouteFromDriver Fail");
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(msc_LatLng, 12.2f));

        mLocationRequest = new LocationRequest();
//        mLocationRequest.setSmallestDisplacement(10);
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            }else{
                checkLocationPermission();
            }
        }

        connectLocation();

        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    isTrackingEnable = false;

                }
            }
        });
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Log.d(TAG, "onMyLocationButtonClick:");
                isTrackingEnable = true;
                return false;
            }
        });
        mMap.setOnMyLocationClickListener(new GoogleMap.OnMyLocationClickListener() {
            @Override
            public void onMyLocationClick(@NonNull Location location) {
                Log.d(TAG, "onMyLocationClick:");
                isTrackingEnable = true;
                location.describeContents();
            }
        });




    }
    private void confirmRequest(){
//        getClosestAvailableDriver();
        if (assignDriver != null && assignDriver.getDriver_id() != null && clientIdRef != null){
            WriteBatch batch = mDb.batch();
            mRequest.setDriver_id(assignDriver.getDriver_id());
            DocumentReference clientRef = mDb.collection("Clients").document(clientIdRef);
            DocumentReference RequestRef = mDb.collection("Requests").document(mRequest.getRequest_id());
            DocumentReference DriverRef = mDb.collection("Drivers").document(assignDriver.getDriver_id());
            DocumentReference DriverOnlineRef = mDb.collection("DriversOnline").document(assignDriver.getDriver_id());

//            clientRef.update("current_request_id", mRequest.getRequest_id());
//            DriverOnlineRef.update("current_request_id", mRequest.getRequest_id());

            HashMap<String, Object> data = new HashMap<String, Object>();
            data.put("nextRequest", mRequest);

            //Update request id in the respective documents
            batch.update(clientRef,"current_request_id" ,mRequest.getRequest_id());
            batch.update(DriverRef,"current_request_id" ,mRequest.getRequest_id());
            batch.update(DriverOnlineRef,"current_request_id" ,mRequest.getRequest_id());

            //Setting the request to the right documents
            batch.set(RequestRef, mRequest, SetOptions.merge());
//            batch.set(DriverRef, data, SetOptions.merge());
//            batch.set(DriverOnlineRef, data, SetOptions.merge());

            batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Log.d(TAG, "Batch success");
                        StartRequestTracking();
                    }
                    else {
                        Log.d(TAG, "Batch Error:");
                    }
                }
            });

        }

    }
    private void getClosestAvailableDriver(){
        mClosestDriverQuery = mDb.collection("DriversOnline").whereEqualTo("current_request_id", null);
        mClosestDriverQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    QuerySnapshot result = task.getResult();


                    if ( result != null && !result.isEmpty() ) {
                        float shortestDistance = Float.MAX_VALUE;
                        for (QueryDocumentSnapshot document : result) {
                            Location dest = new Location("");
                            GeoPoint geo = document.getGeoPoint("geoPoint");
                            if (geo != null) {
                                String temp_id = (String) document.get("driver_id");

                                dest.setLatitude(geo.getLatitude());
                                dest.setLongitude(geo.getLongitude());

                                if (mLastLocation != null) {
                                    float dist = dest.distanceTo(mLastLocation);
                                    if (shortestDistance >= dist) {
                                        assignDriverId = temp_id;
                                        assignDriver = document.toObject(Drivers.class);

                                        shortestDistance = dist;

                                        Log.i(TAG, "Driver: " + assignDriver.getDriver_id() + " Distance: " + dist);

                                    }
                                }
                            }
                        }
                    }
                    if (assignDriver != null && assignDriver.getDriver_id() != null)
                    {
                        confirmRequest();
                    }
                    else{
                        Toast t = Toast.makeText(getApplicationContext(), "All Drivers are currently busy. Please try again in 5~10 minutes", Toast.LENGTH_SHORT);
                        t.setGravity(Gravity.CENTER, 0, 0);
                        t.show();
                    }



                } else {
                    Log.d(TAG, "Error getting Drivers Online documents: ", task.getException());
                }
            }
        });
        //return whether assignment was successful
        //return assignDriver!=null && assignDriver.getNextRequest() == null; //risky
//        return assignDriver != null && assignDriver.getDriver_id() != null;

    }

    LocationCallback mLocationCallback = new LocationCallback()
    {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onLocationResult(LocationResult locationResult) {

            for(Location location : locationResult.getLocations()){
//                Log.d(TAG, "onLocationResult: B1");
                if(getApplicationContext() != null ) {

                    mLastLocation = location;

                    if (mRequest != null && mRequest.getState().equals("ride") )
                        getRouteToDestination();

                    if(isTrackingEnable){

                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

                    }


//                    Log.d(TAG, "onLocationResult: B2");

                    //update it in the db
                    //NOTE: At this point a driver with the Auth usr id as the document id is already

//                    String clientId = (String) FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DocumentReference DO = mDb.collection("Clients").document(clientIdRef);
//
                    GeoPoint gp = new GeoPoint( location.getLatitude(), location.getLongitude() );

                    Clients CL = new Clients(clientIdRef, gp );
                    DO.update("geoPoint",gp).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Log.d(TAG, "Document update success");
                            }
                            else {

                                Log.d(TAG, "Document update Error:");
                            }
                        }
                    });
//
//                    DO.set(CL, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if (task.isSuccessful()){
//                                Log.d(TAG, "Document update success");
//                            }
//                            else {
//
//                                Log.d(TAG, "Document update Error:");
//                            }
//                        }
//                    });

//                    getClosestDirver();
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
        autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragmentHome2);

        autocompleteFragment.setHint("Where to?");

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected( Place place)
            {

                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId()+", LatLng: "+ place.getLatLng() );

                //Transform it into LatLng
                LatLng LL = place.getLatLng();

                //Clear the map after the user changes the location selected
                mMap.clear();


                //Create a marker for the place selected by the user
                MarkerOptions place_mkr = new MarkerOptions().position(LL).title(place.getName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                curr_mkr = new MarkerOptions().position(myCurrPlace.getLatLng()).title(myCurrPlace.getName());
                //Place the marker for your location and the chosen destination into the map


                // create a Requests Object

                String requestId = mDb.collection("Requests").document().getId(); // get new request id from fireStore

                String clientId = FirebaseAuth.getInstance().getCurrentUser().getUid();//get clientId from Firebase auth

                myPlace myDestinationPlace = new myPlace(place); // get destination place

                mRequest = new Requests(myCurrPlace, myDestinationPlace, requestId, clientId);


                //Calling function that will create the route to the destination

                mMap.addMarker(place_mkr);
                mMap.addMarker(curr_mkr);

                getRouteToMarker(mRequest.getStart().getLatLng(), mRequest.getDest().getLatLng());

                //stop zoom on current location
                isTrackingEnable = false;
                //Zoom into the path
                //Northern Lat (secound parameter) has to be bellow Southern Lat(first paramenter)
                if (LL.latitude < myCurrPlace.getLatLng().latitude){
                    LatLngBounds LLB =  new LatLngBounds(LL, myCurrPlace.getLatLng()) ;

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LLB.getCenter(), 15f));

                }else{
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(msc_LatLng, 13f));
                }


                displayConfirmRequestButton();



            }

            @Override
            public void onError(Status status) {

                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }
    private void displayConfirmRequestButton(){
        estimatedTime.setVisibility(View.VISIBLE);
        ConfButton.setVisibility(View.VISIBLE);

    }

    private void getRouteToMarker(LatLng... params) {

        Routing routing = new Routing.Builder()
                .key(getString(R.string.google_maps_api_key))
                .travelMode(AbstractRouting.TravelMode.BIKING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(params)
                .build();

        routing.execute();
    }

    public void setCurrPlace()
    {
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


    public void getClosestDriver(){

        Query colRef = mDb.collection("DriversOnline").whereEqualTo("geoPoint", true);

        colRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            float shortestDistance = Float.MAX_VALUE;

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Location dest = new Location("");
                                GeoPoint geo = document.get("geoPoint", GeoPoint.class);

                                dest.setLatitude(geo.getLatitude());
                                dest.setLongitude(geo.getLongitude());

                                if (mLastLocation != null && dest!=null)
                                {
                                    float dist = dest.distanceTo(mLastLocation);
                                    if (shortestDistance >= dist) {

                                        assignDriver = document.toObject(Drivers.class);

                                        shortestDistance = dist;

                                        Log.i(TAG, "Driver: " + assignDriver.getDriver_id() + " Distance: " + dist);
                                    }
                                }


                            }
                            if (assignDriver == null) {
                                Log.i(TAG, "Driver not found ");
                                assignDriver = new Drivers("Drivers");
                            }
                        }
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

            case R.id.about:
                Intent it = new Intent(ClientHome2.this, AboutSafeTeam.class);
                startActivity(it);
                finish();

        }
        return super.onOptionsItemSelected(item);
    }




    //Methods to create the polyline

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorPrimary};

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
            polyOptions.color(getResources().getColor(COLORS[colorIndex]))
                    .width(15 + i * 3)
                    .addAll(route.get(i).getPoints())
                    .startCap(new SquareCap())
                    .endCap(new SquareCap())
                    .jointType(ROUND)
                    .clickable(true);

            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);
//            route.get(i).getLatLgnBounds();
//            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(route.get(i).getLatLgnBounds(), 2));



            String str = "Estimated time: "+ route.get(i).getDurationValue()/60+" Minutes";

            estimatedTime.setVisibility(View.VISIBLE);
            estimatedTime.setText(str);
            estimatedTime.setBackgroundResource(R.color.colorPrimary);
            //Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();


        }
    }

    @Override
    public void onRoutingCancelled() {
        erasePolylines();
    }
    private void erasePolylines(){
        for(Polyline line : polylines){
            line.remove();
        }
        polylines.clear();
    }
}