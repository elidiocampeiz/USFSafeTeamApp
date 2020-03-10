package com.example.usfsafeteamapp.Client;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.usfsafeteamapp.DataParser.FetchURL;
import com.example.usfsafeteamapp.DataParser.TaskLoadedCallback;
import com.example.usfsafeteamapp.Objects.Drivers;
import com.example.usfsafeteamapp.Objects.Requests;
import com.example.usfsafeteamapp.Objects.myPlace;
import com.example.usfsafeteamapp.R;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class ClientHome extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {

    public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9002;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9003;
    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    LocationManager locm;
    LatLng curr_coords, dest_coords;
    //28.063959, -82.413417
    FirebaseFirestore mDb;

    PlacesClient placesClient;

    myPlace myCurrPlace , myDestPlace;

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

        //Creating the activity title and a back button
        getSupportActionBar().setTitle("Client Home");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDb = FirebaseFirestore.getInstance(); // init firebase

        Button B = findViewById(R.id.buttonConfirm);

        B.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if(destination == null){
                    String txt = "No Place Selected";
                    SpannableStringBuilder biggerText = new SpannableStringBuilder(txt);
                    biggerText.setSpan(new RelativeSizeSpan(1.35f), 0, txt.length(), 0);
                    Toast t = Toast.makeText(ClientHome.this, biggerText, Toast.LENGTH_LONG);

                    t.setGravity(0, 45, 0);
                    t.show();
//                   Toast t = Toast.makeText(ClientHome.this, , Toast.LENGTH_LONG);
//                   t.setGravity(0, 40, 50);
//
//                   t.setText();
////                   t.setMargin(5f, 5f);
//                   t.show();
                }
                else{
                    CollectionReference placesCollectionRef = mDb.collection("Places");
                    CollectionReference requestCollectionRef = mDb.collection("Requests");
                    DocumentReference DriverRef = mDb.collection("Drivers").document("Driver");

                    //add destination place to DB
                    myDestPlace = new myPlace(destination);
                    placesCollectionRef.document(myDestPlace.getPlace_id()).set(myDestPlace, SetOptions.merge())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error writing document", e);
                                }
                            });







                    placesCollectionRef.document(myCurrPlace.getPlace_id()).set(myCurrPlace, SetOptions.merge())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully written!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error writing document", e);
                                }
                            });
//                    myPlace currPlace = myCurrPlace;
//                    myCurrPlace.setLatLng(curr_coords);
//                    myCurrPlace.setName("Current Location");
//                    myCurrPlace.setPlace_id("Curr Location Test");
                    Requests nRequest = new Requests();
                    String req_id = requestCollectionRef.document().getId();
                    nRequest.setRequest_id(req_id);
                    nRequest.setDest(myDestPlace);

//                    GeoPoint geo = new GeoPoint(curr_coords.latitude,curr_coords.longitude);
//                    myCurrPlace = new myPlace(null, requestCollectionRef.document().getId(),geo );

                    nRequest.setStart(myCurrPlace);
                    nRequest.setTime_stamp(null);
                    nRequest.setDriver_id("Driver");

                    // add new request to DB
                    requestCollectionRef.document(req_id).set(nRequest).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error writing document", e);
                                }
                            });
                    //set request inside driver
                    Drivers dr = new Drivers("Driver",myCurrPlace.getGeoPoint());
                    dr.setNextRequest(nRequest);
                    DriverRef.set(dr, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error writing document", e);
                                }
                            });;

                    Intent i = new Intent(ClientHome.this, ClientWait.class);
                    Bundle bd;
                    i.putExtra("Request_Id", req_id);
                    startActivity(i);
                }
            }
        });

        //Initialize Places API
        setUpPlacesAPI();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment2);
        mapFragment.getMapAsync(this);

        msc_mkr = new MarkerOptions().position(new LatLng(28.0639,-82.4134)).title("MSC");

        setCurrPlace();
        // Initialize the AutocompleteSupportFragment.
        setUpAutocompleteSupportFragment();



        locm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(checkSelfPermission(ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
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

                    //Gets the user current location and places a marker there
                    curr_coords = new LatLng(lat,lon );
                    curr_mkr = new MarkerOptions().position(curr_coords).title("This is your position");
                    mMap.addMarker(curr_mkr);
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

                    //Gets the user current location and places a marker there
                    curr_coords = new LatLng(lat,lon);
                    curr_mkr = new MarkerOptions().position(curr_coords).title("This is your position");
                    mMap.addMarker(curr_mkr);

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
////        TextView txt2 = findViewById(R.id.textView2);
////        txt2.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                // Set the fields to specify which types of place data to
////                // return after the user has made a selection.
////                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);
////
////                // Start the autocomplete intent.
////                Intent Placeintent = new Autocomplete.IntentBuilder(
////                        AutocompleteActivityMode.OVERLAY, fields)
////                        .build(cntx);
////                startActivityForResult(Placeintent, AUTOCOMPLETE_REQUEST_CODE);
////            }
////        });

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
    }

    public String getUrl(LatLng origin, LatLng dest, String directionMode)
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
private void getLastKnownLocation() {
    Log.d(TAG, "getLastKnownLocation: called.");
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        return;
    }

    mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
        @Override
        public void onComplete(@NonNull Task<Location> task) {
            if (task.isSuccessful() && task.getResult()!=null) {
                Location location = task.getResult();

                GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

                curr_coords = new LatLng(location.getLatitude(), location.getLongitude());
                curr_mkr = new MarkerOptions().position(curr_coords).title("This is your position");

            }
        }
    });

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

// Call findCurrentPlace and handle the response (first check that the user has granted permission).
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
            placeResponse.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
                @Override
                public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                    if (task.isSuccessful()) {
                        FindCurrentPlaceResponse response = task.getResult();
                        if(response!=null)
                            myCurrPlace = new myPlace(response.getPlaceLikelihoods().get(0).getPlace());
                        else{
                            GeoPoint geo = new GeoPoint(curr_coords.latitude,curr_coords.longitude);
                            myCurrPlace = new myPlace("Pick Up Spot", "Stating Location", geo);
                            curr_mkr = new MarkerOptions().position(curr_coords).title("This is your position");
                        }
//                        for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
//                            Log.i(TAG, String.format("Place '%s' has likelihood: %f",
//                                    placeLikelihood.getPlace().getName(),
//                                    placeLikelihood.getLikelihood()));
//                        }
                    } else {
                        Exception exception = task.getException();
                        if (exception instanceof ApiException) {
                            ApiException apiException = (ApiException) exception;
                            Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                        }
                    }
                }
            });
        } else {
            // A local method to request required permissions;
            // See https://developer.android.com/training/permissions/requesting
            getLocationPermission();
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
            }
        }
    }
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
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);


        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected( Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId()+", LatLng: "+ place.getLatLng() );

                //Get the destination entered by the user
                destination = place;

                //Transform it into LatLng
                LatLng LL = place.getLatLng();

                //Clear the map after the user changes the location selected
                mMap.clear();

                //Create a marker for the place selected by the user
                MarkerOptions place_mkr = new MarkerOptions().position(LL).title(place.getName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                //Place the marker for your location and the chosen destination into the map
                mMap.addMarker(place_mkr);

                mMap.addMarker(curr_mkr);

                //Push and fetch it into the String
                String url = getUrl(curr_mkr.getPosition(), place_mkr.getPosition(), "walking");
                new FetchURL(ClientHome.this).execute(url, "walking");

                //Zoom into the path
                //Northern Lat (secound parameter) has to be bellow Southern Lat(first paramenter)
                if (LL.latitude < curr_coords.latitude){
                    LatLngBounds LLB =  new LatLngBounds(LL, curr_coords) ;
                    //TODO: Handle the case in which a new path causes a bug
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LLB.getCenter(), 15f));

                }else{
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(msc_LatLng, 14f));
                }


                //set display estimated time
                //ToDO: Fetch it from the server or Get it from the directions api
                TextView txtTime = findViewById(R.id.textViewEstimatedTime);
                String str = "Estimated time: 5-10 min";
                txtTime.setText(str);
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