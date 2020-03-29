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
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import com.google.android.gms.maps.model.LatLng;
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

import java.util.Date;
import java.util.HashMap;

public class DriverHome2 extends AppCompatActivity implements OnMapReadyCallback {

    String TAG;
    private GoogleMap mMap;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    //Msc Location
    final LatLng msc_LatLng = new LatLng(28.0639,-82.4134);

    private FusedLocationProviderClient mFusedLocationClient;

    private SupportMapFragment mapFragment;
    private Button mLogout;
    private RelativeLayout mCustomerInfo;

    FirebaseFirestore mDb;
    String driverIdRef;
    Drivers dr;
    private String mClientID;
    private GeoPoint mClientGeoPoint;

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



        mCustomerInfo = (RelativeLayout) findViewById(R.id.customerInfo);

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
                    Requests mRequest = (Requests) documentSnapshot.get("nextRequest", Requests.class);
                    if (mRequest!=null && mRequest.getRequest_id()!=null){
                        mClientID = mRequest.getClient_id();

                        // TODO: we need attach a listener to usr curr location that is not inside the function
//                        getclientinfo(mRequest);
                        mCustomerInfo.setVisibility(View.VISIBLE);
                    }

                }
                else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
        Query mClientLocationQueury = mDb.collection("Clients").whereEqualTo("client_id", mClientID);

        mClientLocationQueury.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                for (QueryDocumentSnapshot doc : value) {
                    if (doc.get("geoPoint") != null) {
                        mClientGeoPoint = doc.getGeoPoint("GeoPoint");
                        Log.d(TAG, "New Client GeoPoint: " + mClientGeoPoint );
                    }
                }
                Log.d(TAG, "Current  " );
            }
        });
    }


    private void getclientinfo(final Requests mRequest){

        String clientId = (String) mRequest.getClient_id();

        DocumentReference cusloc = mDb.collection("Clients").document(clientId);
        cusloc.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if ((documentSnapshot != null) && documentSnapshot.exists() ){

                    Log.d(TAG, "getclientinfo successfully written!:");
                    mClientID = mRequest.getClient_id();
                    GeoPoint geo = documentSnapshot.getGeoPoint("GeoPoint");
                    Log.d(TAG, "getclientlocation successfully written!:");
                    //GeoPoint pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("pickup location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));
                    //getRouteToMarker(pickupLatLng);

                }
            }
        });
    }
    


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(msc_LatLng, 12.2f));

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
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

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
