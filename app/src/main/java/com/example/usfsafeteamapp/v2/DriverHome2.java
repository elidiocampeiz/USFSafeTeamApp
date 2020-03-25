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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ebanx.swipebtn.OnStateChangeListener;
import com.ebanx.swipebtn.SwipeButton;
import com.example.usfsafeteamapp.MainActivity;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;

public class DriverHome2 extends AppCompatActivity implements OnMapReadyCallback {

    String TAG;
    private GoogleMap mMap;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    private FusedLocationProviderClient mFusedLocationClient;

    private SupportMapFragment mapFragment;
    private Button mLogout;
    private RelativeLayout mCustomerInfo;

    FirebaseFirestore mDb;
    String driverIdRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home2);

        //Creating the activity title and a back button
        getSupportActionBar().setTitle("Driver Home2");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mDb = FirebaseFirestore.getInstance(); // init firebase

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_frag);
        mapFragment.getMapAsync(this);

        driverIdRef = (String) FirebaseAuth.getInstance().getCurrentUser().getUid();

        mCustomerInfo = (RelativeLayout) findViewById(R.id.customerInfo);

        mCustomerInfo.setVisibility(View.VISIBLE);

        SwipeButton enableButton = (SwipeButton) findViewById(R.id.swipe_btn);
        enableButton.setOnStateChangeListener(new OnStateChangeListener() {
            @Override
            public void onStateChange(boolean active) {
                Toast.makeText(DriverHome2.this, "State: " + active, Toast.LENGTH_SHORT).show();
            }
        });

        final String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DocumentReference docref = mDb.collection("DriversOnline").document(driverId);
        docref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if ((documentSnapshot != null) && (documentSnapshot.getData() != null) && documentSnapshot.contains("nextRequest") && documentSnapshot.getData().size()>0 && documentSnapshot.exists()) {
                    getclientlocation(documentSnapshot);
                    getclientdest(documentSnapshot);
                    getclientinfo(documentSnapshot);
                }
                else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }
    private void getclientlocation(DocumentSnapshot documentSnapshot){
        Requests mRequest = (Requests) documentSnapshot.get("nextRequest", Requests.class);
        String clientId = (String) mRequest.getClient_id();

        DocumentReference cusloc = mDb.collection("Clients").document(clientId);
        cusloc.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if ((documentSnapshot != null) && documentSnapshot.exists() ){
                    GeoPoint geo = documentSnapshot.getGeoPoint("GeoPoint");

                    //GeoPoint pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("pickup location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));
                    //getRouteToMarker(pickupLatLng);

                }
            }
        });
    }
    private void getclientdest(DocumentSnapshot documentSnapshot){
        final Requests mRequest = (Requests) documentSnapshot.get("nextRequest", Requests.class);
        String clientId = (String) mRequest.getClient_id();

        DocumentReference cusloc = mDb.collection("Clients").document(clientId);
        cusloc.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if ((documentSnapshot != null) && documentSnapshot.exists() ){
                    LatLng LL = mRequest.getDest().getLatLng();

                    //GeoPoint pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("pickup location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));
                    //getRouteToMarker(pickupLatLng);

                }
            }
        });
    }
    private void getclientinfo(DocumentSnapshot documentSnapshot){
        final Requests mRequest = (Requests) documentSnapshot.get("nextRequest", Requests.class);
        String clientId = (String) mRequest.getClient_id();

        DocumentReference cusloc = mDb.collection("Clients").document(clientId);
        cusloc.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if ((documentSnapshot != null) && documentSnapshot.exists() ){
                    String clientname = mRequest.getClient_id();

                    //GeoPoint pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("pickup location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));
                    //getRouteToMarker(pickupLatLng);

                }
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
        connectDriver();




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
                    String driverId= (String) FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DocumentReference DO = mDb.collection("DriversOnline").document(driverId);
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

    private void connectDriver(){
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

}
