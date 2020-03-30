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
import com.google.firebase.firestore.CollectionReference;
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

    String TAG;
    private GoogleMap mMap;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    //Msc Location
    final LatLng msc_LatLng = new LatLng(28.0639,-82.4134);

    private FusedLocationProviderClient mFusedLocationClient;

    private SupportMapFragment mapFragment;
    private SwipeButton mSwipe;
    private RelativeLayout mCustomerInfo;
//    ListenerRegistration mClientListener;
    FirebaseFirestore mDb;
    String driverIdRef;
    Drivers dr;
    private String mClientID;
    private GeoPoint mClientGeoPoint;
    private Requests mRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home2);

        //Creating the activity title and a back button
        getSupportActionBar().setTitle("Driver Home2");
//        getSupportActionBar().hide();
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mDb = FirebaseFirestore.getInstance(); // init firebase

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_frag);
        mapFragment.getMapAsync(this);


        //Confirm Request layout
        txtClientName = (TextView) findViewById(R.id.ClientName);
        txtClientLocation = (TextView) findViewById(R.id.ClientLocation);
        txtClientDestination = (TextView) findViewById(R.id.ClientDestination);



        mCustomerInfo = (RelativeLayout) findViewById(R.id.customerInfo);
        mCustomerInfo.setVisibility(View.GONE);

        mSwipe = (SwipeButton) findViewById(R.id.swipe_btn);

        mSwipe.setOnStateChangeListener(new OnStateChangeListener() {
            @Override
            public void onStateChange(boolean active) {
                if (active){
                    //Confirm Request
                    //Either send a "signal" back to the client  (write driver id in)
                    // Or send the driver to the next activity

                }

            }
        });


        driverIdRef = (String) FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference docRef = mDb.collection("Drivers").document(driverIdRef);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        dr = document.toObject(Drivers.class);
                    } else {

                        Log.d(TAG, "No such document");

                    }
                }else{
                        Log.d(TAG, "get failed with ", task.getException());
                    }

            }
        });

        Switch enableButton = (Switch) findViewById(R.id.workingSwitch);

        enableButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    connectDriver();
                    if (mRequest != null)
                    {
                        mCustomerInfo.setVisibility(View.VISIBLE);
                    }

                }else{
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

                if ((documentSnapshot != null) ) {

                    Log.d(TAG, "Event1");
                    mRequest = (Requests) documentSnapshot.get("nextRequest", Requests.class);
                    if (mRequest!=null && mRequest.getClient_id() != null){

                        mClientID = mRequest.getClient_id();

                        // TODO: we need attach a listener to usr curr location that is not inside the function
                        getClientInfo();
                        RouteRequest();

//                        mSwipe.setVisibility(View.VISIBLE);
//                        mSwipe.setAlpha(1f);

                    }

                }
                else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });


    }



    private void getClientInfo(){

        Query mClientLocationQueury = mDb.collection("Clients").whereEqualTo("client_id", mClientID);

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
                        mClientGeoPoint = gp;
                        Log.d(TAG, "New GeoPoint: " + mClientGeoPoint);
                    }
                }
                Log.d(TAG, "Current  " );
            }
        });
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(msc_LatLng, 15.2f));

        mLocationRequest = new LocationRequest();
//        mLocationRequest.setSmallestDisplacement(10);
        mLocationRequest.setInterval(100);
        mLocationRequest.setFastestInterval(100);
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
//        connectLocation();




    }


    LocationCallback mLocationCallback = new LocationCallback()
    {
        @Override
        public void onLocationResult(LocationResult locationResult) {

            for(Location location : locationResult.getLocations()){
//                Log.d(TAG, "onLocationResult: B1");
                if(getApplicationContext()!=null) {

                    mLastLocation = location;
                    if (mRequest!=null)
                        RouteRequest();


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

                    batch.set(DO, data,SetOptions.merge());
                    batch.set(D,  data,SetOptions.merge());
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
//
//                    DO.set(data, SetOptions.merge() )
//                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            Log.d(TAG, "DocumentSnapshot successfully updated!:");
//                        }
//                    })
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Log.w(TAG, "Error updating document", e);
//                                }
//                            });
//


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
    private void connectDriver(){

        connectLocation();
        String user_ID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mDb.collection("DriversOnline").document(driverIdRef).set(dr, SetOptions.merge());


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

        CollectionReference DriversOnlineRef = mDb.collection("DriversOnline");
        DriversOnlineRef.document(driverIdRef)
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
        }
        return super.onOptionsItemSelected(item);
    }

    private void RouteRequest() {
        if (mRequest != null && mLastLocation != null){

            if (mCustomerInfo.getVisibility() != View.VISIBLE)
                mCustomerInfo.setVisibility(View.VISIBLE);

            LatLng StartLL = mRequest.getStart().getLatLng();
            LatLng DestinationtLL = mRequest.getDest().getLatLng();
            LatLng myLocationtLL = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());



            Routing routing = new Routing.Builder()
                    .key(getString(R.string.google_maps_api_key))
                    .travelMode(AbstractRouting.TravelMode.BIKING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(myLocationtLL, StartLL, DestinationtLL)
                    .build();

            routing.execute();
        }
        else{
            Log.i(TAG, "Routing error occurred: ") ;
        }

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

            txtClientName.setText("Client Name: Bob");
            txtClientLocation.setText("Client Location: "+ mRequest.getStart().getName());
            txtClientDestination.setText("Client Destination: "+ mRequest.getDest().getName());

            //Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();



        }

        MarkerOptions str_mkr = new MarkerOptions().position(mRequest.getStart().getLatLng()).title(mRequest.getStart().getName());
        MarkerOptions dest_mkr = new MarkerOptions().position(mRequest.getDest().getLatLng()).title(mRequest.getDest().getName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

//        if (mRequest.getDest().getLatLng().latitude < mRequest.getDest().getLatLng().latitude){
////            LatLngBounds BB = new LatLngBounds(mRequest.getStart().getLatLng(),mRequest.getDest().getLatLng());
////
////            BB.including(new LatLng( mLastLocation.getLatitude(), mLastLocation.getLongitude() ) ) ;
////
////            //TODO: Handle the case in which a new path causes a bug
////            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(BB.getCenter(), 15f));
////
////        }else{
////            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(msc_LatLng, 14f));
////        }

        mMap.addMarker(str_mkr);
        mMap.addMarker(dest_mkr);
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
