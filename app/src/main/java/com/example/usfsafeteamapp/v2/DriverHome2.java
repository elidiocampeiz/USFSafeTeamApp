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
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.ebanx.swipebtn.OnStateChangeListener;
import com.ebanx.swipebtn.SwipeButton;
import com.example.usfsafeteamapp.AboutSafeTeam;
import com.example.usfsafeteamapp.MainActivity;
import com.example.usfsafeteamapp.Objects.Drivers;
import com.example.usfsafeteamapp.Objects.Requests;
import com.example.usfsafeteamapp.R;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class DriverHome2 extends AppCompatActivity implements OnMapReadyCallback , RoutingListener {

    private TextView txtClientName;
    private TextView txtClientLocation;
    private TextView txtClientDestination;
    private TextView mRequestStateTextBox;
    String TAG;
    private GoogleMap mMap;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    //Msc Location
    final LatLng msc_LatLng = new LatLng(28.0639,-82.4134);

    private FusedLocationProviderClient mFusedLocationClient;

    private SupportMapFragment mapFragment;
    private SwipeButton mSwipe;
    private Button mCancelButton;
    Switch enableButton;
    private RelativeLayout mCustomerInfo;
    //    ListenerRegistration mClientListener;
    FirebaseFirestore mDb;
    String driverIdRef;
    Drivers mDriver;
    private String mClientID, mRequestStateMessage, mRequestButtonMessage;
    private GeoPoint mClientGeoPoint;
    private Requests mRequest;
    private boolean isTrackingEnable;
    private boolean mCheckedBool;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home2);

        //Creating the activity title and a back button
        getSupportActionBar().setTitle("Driver Map");
//        getSupportActionBar().hide();
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mDb = FirebaseFirestore.getInstance(); // init firebase

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_frag);
        mapFragment.getMapAsync(this);


        // Request Info layout
        txtClientName = (TextView) findViewById(R.id.ClientName);
        txtClientLocation = (TextView) findViewById(R.id.ClientLocation);
        txtClientDestination = (TextView) findViewById(R.id.ClientDestination);
        mRequestStateTextBox = (TextView) findViewById(R.id.RequestState);

        mRequestStateMessage = "Please confirm ride request";
        mRequestButtonMessage = "CONFIRM REQUEST";

        mCustomerInfo = (RelativeLayout) findViewById(R.id.customerInfo);
        mCustomerInfo.setVisibility(View.GONE);

        mCancelButton = (Button) findViewById(R.id.cancelbutton);

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelRequestMessage();
            }
        });
        mSwipe = (SwipeButton) findViewById(R.id.swipe_btn);

        mSwipe.setOnStateChangeListener(new OnStateChangeListener() {
            @Override
            public void onStateChange(boolean active) {
                if (active)
                {
                    updateRequest();


//                    mSwipe.setActivated(false);
//                    mSwipe.setPressed(false);
                }

            }
        });


        driverIdRef = (String) FirebaseAuth.getInstance().getCurrentUser().getUid();

//        DocumentReference docRef = mDb.collection("Drivers").document(driverIdRef);
//        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
//                        //mDriver = document.toObject(Drivers.class);
//                    } else {
//
//                        Log.d(TAG, "No such document");
//
//                    }
//                }else{
//                        Log.d(TAG, "get failed with ", task.getException());
//                    }
//
//            }
//        });
        isTrackingEnable = true;
        mCheckedBool = false;
        enableButton = (Switch) findViewById(R.id.workingSwitch);
        enableButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCheckedBool = isChecked;
                if (isChecked ){

                    connectDriver();


                }else {

                    disconnectDriver();

                }

            }
        });


        DocumentReference mDriverOref = mDb.collection("DriversOnline").document(driverIdRef);

        mDriverOref.addSnapshotListener(DriverHome2.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if ((documentSnapshot != null) && documentSnapshot.getData() !=null) {

                    Log.d(TAG, "Event1");

                    mDriver = documentSnapshot.toObject(Drivers.class);
                    getRequestinfo();
//                    mRequest = (Requests) documentSnapshot.get("nextRequest", Requests.class);
//                    if (mRequest!=null && mRequest.getClient_id() != null){
//
//                        mClientID = mRequest.getClient_id();
//
//                        // TODO: we need attach a listener to Request
//                        getClientInfo();
//                        RouteRequest();
////                        mSwipe.setVisibility(View.VISIBLE);
////                        mSwipe.setAlpha(1f);
//                    }

                }
                else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });


    }
    private void cancelRequestMessage() {
        if (mRequest!=null){
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

    }
    private void cancelRequest() {
        //function that changes the state of current request to "canceled"
        //NOTE: This function does not unsubscribe the driver to the request because that event is handle as a result of the request's
            //  state being "canceled", since it could have happened as a result of either driver or client changing the state of the request



        if (mRequest != null && mRequest.getRequest_id() != null && !mRequest.getRequest_id().isEmpty() )
        {
            DocumentReference RequestRef = mDb.collection("Requests").document(mRequest.getRequest_id());

            RequestRef.update("state","canceled").addOnCompleteListener(new OnCompleteListener<Void>() {
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
        }
        mRequestStateMessage = "Request Canceled";
//                            mRequestButtonMessage = "CONFIRM PICK UP";


        Toast t = Toast.makeText(getApplicationContext(), mRequestStateMessage, Toast.LENGTH_SHORT);
        t.setGravity(Gravity.CENTER, 0, 0);
        t.show();


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

    private void updateRequest()
    {

        if (mRequest != null && mRequest.getRequest_id() != null && !mRequest.getRequest_id().isEmpty() )
        {
            DocumentReference RequestRef = mDb.collection("Requests").document(mRequest.getRequest_id());
            if (mRequest.getState().equals("unassigned"))
            {
                RequestRef.update("state","assigned").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "Document update success");
                            //Toast.makeText(getApplicationContext(), "Pick up client!", Toast.LENGTH_LONG).show();


                        }
                        else {

                            Log.d(TAG, "Document update Error:");
                        }
                    }
                });
            }
            else if (mRequest.getState().equals("assigned"))
            {
                RequestRef.update("state","ride").addOnCompleteListener(new OnCompleteListener<Void>() {
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
            }

            else if (mRequest.getState().equals("ride"))
            {

                RequestRef.update("state","fulfilled").addOnCompleteListener(new OnCompleteListener<Void>() {
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
                mRequestStateMessage = "Request Fulfilled";
//                            mRequestButtonMessage = "CONFIRM PICK UP";


                Toast t = Toast.makeText(getApplicationContext(), mRequestStateMessage, Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
            }

            else if (mRequest.getState().equals("fulfilled") || mRequest.getState().equals("canceled")  )
            {

            // Should neve reach this state TODO: Handle/Display error
            }

        }


    }

    private void getRequestinfo() {

        if ( mDriver != null && mDriver.getCurrent_request_id() != null && !mDriver.getCurrent_request_id().isEmpty() ) {
            final DocumentReference mRequestRef = mDb.collection("Requests").document(mDriver.getCurrent_request_id());

            mRequestRef.addSnapshotListener(DriverHome2.this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);

                    }
                    if (snapshot != null && snapshot.exists()) {
                        Log.d(TAG, "Request snapshot success!");
                        mRequest = snapshot.toObject(Requests.class);
                        if(mRequest != null){


                            getClientInfo();
                            RouteRequest();


                        }

                    }
                    else{
                        Log.d(TAG, "Request snapshot fail!");
                    }

                }
            });
        }
        else{
            mRequest = null;
            Log.d(TAG, "Request snapshot fail222");
        }

    }

    private void displayRequestInfo() {
        //Display Cancel Button
        mCancelButton.setVisibility(View.VISIBLE);

        mCustomerInfo.setVisibility(View.VISIBLE);

        mRequestStateTextBox.setText(mRequestStateMessage);



        txtClientName.setText("Client Name: Bob");
        txtClientLocation.setText("Client Location: "+ mRequest.getStart().getName());
        txtClientDestination.setText("Client Destination: "+ mRequest.getDest().getName());
    }


    private void getClientInfo(){

        if (mRequest != null && mRequest.getClient_id() != null && !mRequest.getClient_id().isEmpty())
        {
            Query mClientLocationQueury = mDb.collection("Clients").whereEqualTo("client_id", mRequest.getClient_id());

            mClientLocationQueury.addSnapshotListener(DriverHome2.this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    for (QueryDocumentSnapshot doc : value) {
                        GeoPoint gp = doc.get("geoPoint", GeoPoint.class);

                        if (gp !=  null) {
                            //TODO: add client's current location and display it in the map
                            mClientGeoPoint = gp;
                            Log.d(TAG, "New GeoPoint: " + mClientGeoPoint);
                        }
                    }
                    Log.d(TAG, "Current  " );
                }
            });
        }

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(msc_LatLng, 15.2f));

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


    LocationCallback mLocationCallback = new LocationCallback()
    {
        @Override
        public void onLocationResult(LocationResult locationResult) {

            for(Location location : locationResult.getLocations()){
//                Log.d(TAG, "onLocationResult: B1");
                if(getApplicationContext() != null && mCheckedBool ) {

                    mLastLocation = location;
                    if (mRequest != null)
                        RouteRequest();

                    if(isTrackingEnable){

                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

                    }

                    //update it in the db
                    //NOTE: At this point a driver with the Auth usr id as the document id is already

                    WriteBatch batch = mDb.batch();
                    String driverId = (String) FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DocumentReference DO = mDb.collection("DriversOnline").document(driverId);
                    DocumentReference D = mDb.collection("Drivers").document(driverId);
                    GeoPoint gp = new GeoPoint( location.getLatitude(), location.getLongitude() );


                    HashMap<String, Object> data = new HashMap<String, Object>();
                    data.put("geoPoint", gp  );
                    data.put("time_stamp", new Date());
                    batch.update(DO,"geoPoint", gp);
                    batch.update(D,"geoPoint", gp);
                    batch.update(DO,"time_stamp", new Date());
                    batch.update(D,"time_stamp", new Date());
//                    batch.set(DO, data, SetOptions.merge());
//                    batch.set(D,  data, SetOptions.merge());
                    batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Log.d(TAG, "DocumentSnapshot success");
                            }
                            else {
                                Log.d(TAG, "Error:");
                            }
                        }
                    });
                }

            }
        }

    };
    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(DriverHome2.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(DriverHome2.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
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
    //
    private void connectDriver(){

        connectLocation();
        String user_ID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Drivers newD = new Drivers(user_ID);
        if (mRequest!=null){
            newD.setCurrent_request_id(mRequest.getRequest_id());
        }
        mDb.collection("DriversOnline").document(driverIdRef).set(newD, SetOptions.merge());

        isTrackingEnable = true;

    }
    //function that removes the driver from the DriverOnline Collection
    //Triggered when a driver goes offline or accepts a new request
    private void disconnectDriver(){

        mCustomerInfo.setVisibility(View.GONE);
        mMap.clear();
        if(mFusedLocationClient != null){
            mMap.setMyLocationEnabled(false);
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);

        }

        cancelRequestMessage();
        resetDriver();
        DocumentReference doc = mDb.collection("DriversOnline").document(driverIdRef);
//
//        if (mDriver.getCurrent_request_id() != null){
//            mDriver.setCurrent_request_id(null);
//        }
//        //TODO ***** ***** ***** *****
//        //TODO: set variable mRequest to null as well
//        //TODO ***** ***** ***** *****
//        mRequest = null;


//        CollectionReference DriversOnlineRef = mDb.collection("DriversOnline");


//        doc.update("current_request_id", null).addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//                Log.d(TAG, "DocumentSnapshot successfully !");
//            }
//        })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.w(TAG, "Error deleting updated", e);
//                    }
//                });
        //Delete driver instance from the "DriversOnline" Collection
        doc.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
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

        erasePolylines();
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
                disconnectDriver();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(DriverHome2.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;

            case R.id.item2:
                Toast.makeText(this, "To be implemented", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.about:
                Intent it = new Intent(DriverHome2.this, AboutSafeTeam.class);
                startActivity(it);
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void RouteRequest() {
        if (mRequest != null && mLastLocation != null){
            LatLng StartLL = mRequest.getStart().getLatLng();
            LatLng DestinationLL = mRequest.getDest().getLatLng();
            LatLng myLocationLL = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

            MarkerOptions str_mkr = new MarkerOptions().position(mRequest.getStart().getLatLng()).title(mRequest.getStart().getName());
            MarkerOptions dest_mkr = new MarkerOptions().position(mRequest.getDest().getLatLng()).title(mRequest.getDest().getName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            MarkerOptions driver_mkr = new MarkerOptions().position(myLocationLL ).title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

            if ( mRequest.getState().equals("unassigned") )
            {
                mMap.clear();
                getRouteToMarker(myLocationLL, StartLL, DestinationLL);
                mMap.addMarker(driver_mkr);
                mMap.addMarker(str_mkr);
                mMap.addMarker(dest_mkr);

                mRequestStateMessage = "Confirm Request";

//                Toast t = Toast.makeText(getApplicationContext(), mRequestStateMessage, Toast.LENGTH_SHORT);
//                t.setGravity(Gravity.CENTER, 0, 0);
//                t.show();

            }
            else if ( mRequest.getState().equals("assigned") )
            {
                mMap.clear();
                getRouteToMarker(myLocationLL, StartLL);
                mMap.addMarker(driver_mkr);
                mMap.addMarker(str_mkr);
                mRequestStateMessage = "Proceed for client pick up";

//                Toast t = Toast.makeText(getApplicationContext(), mRequestStateMessage, Toast.LENGTH_SHORT);
//                t.setGravity(Gravity.CENTER, 0, 0);
//                t.show();
            }
            else if ( mRequest.getState().equals("ride") )
            {
                mMap.clear();
                getRouteToMarker(myLocationLL, DestinationLL);
                mMap.addMarker(driver_mkr);
                mMap.addMarker(dest_mkr);

                mRequestStateMessage = "Proceed for client drop off";
//                            mRequestButtonMessage = "CONFIRM PICK UP";
//                Toast t = Toast.makeText(getApplicationContext(), mRequestStateMessage, Toast.LENGTH_SHORT);
//                t.setGravity(Gravity.CENTER, 0, 0);
//                t.show();
            }
            else if ( mRequest.getState().equals("fulfilled") )
            {
                //TODO: recordRequest() - > Function that adds the current request to the request history sub-collection of the driver

                mMap.clear();
                erasePolylines();






                resetDriver();// reset driver sets its mRequest to null such that the toast will appear only once
            }
            else if ( mRequest.getState().equals("canceled") )
            {

                mMap.clear();
                erasePolylines();
//                mRequestStateMessage = "Request Canceled";
////                            mRequestButtonMessage = "CONFIRM PICK UP";
//
//                if (mRequest != null){
//                    Toast t = Toast.makeText(getApplicationContext(), mRequestStateMessage, Toast.LENGTH_SHORT);
//                    t.setGravity(Gravity.CENTER, 0, 0);
//                    t.show();
//                }
                deleteRequest();
                resetDriver(); // reset driver sets its mRequest to null such that the toast will appear only once



            }


        }
        else{
            Log.i(TAG, "Routing error occurred: ") ;
        }

    }

    private void resetDriver() {


//        //TODO ***** ***** ***** *****
//        //TODO: make sure mRequest is set to null as a result of the change in the Database
//        //TODO ***** ***** ***** *****

//        mRequest = null;
        mCustomerInfo.setVisibility(View.GONE);
        mCancelButton.setVisibility(View.GONE);

        DocumentReference DriverRef = mDb.collection("Drivers").document(driverIdRef);
        DocumentReference DriverOnlineRef = mDb.collection("DriversOnline").document(driverIdRef);
        WriteBatch batch = mDb.batch();
        batch.update(DriverRef,"current_request_id",null);
        batch.update(DriverOnlineRef,"current_request_id",null);
        // Update current_request_id to null, which "unsubscribe" the driver to the request
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Log.d(TAG, "Batch success");
                }
                else {
                    Log.d(TAG, "Batch Error:");
                }
            }
        });
        mRequest = null;


//        DocumentReference doc = mDb.collection("DriversOnline").document(driverIdRef);
//        doc.update("current_request_id", null).addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//                Log.d(TAG, "DocumentSnapshot successfully !");
//            }
//        })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.w(TAG, "Error deleting updated", e);
//                    }
//                });
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

        if(polylines!=null && polylines.size()>0) {
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
            polyOptions.width(15 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);


        }
        //Making the green background display the client location, destination and name
        if (mRequest != null)
        {
//            mCustomerInfo.setVisibility(View.VISIBLE);
//            txtClientName.setText("Client Name: Bob");
//            txtClientLocation.setText("Client Location: "+ mRequest.getStart().getName());
//            txtClientDestination.setText("Client Destination: "+ mRequest.getDest().getName());
            displayRequestInfo();
        }



    }



    @Override
    public void onRoutingCancelled() {
        erasePolylines();
    }
    private void erasePolylines(){

        if (polylines != null){
            for(Polyline line : polylines){
                line.remove();
            }
            polylines.clear();
        }

    }
}