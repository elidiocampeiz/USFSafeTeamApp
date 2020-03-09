package com.example.usfsafeteamapp.Driver;

        import androidx.annotation.NonNull;
        import androidx.annotation.Nullable;
        import androidx.annotation.RequiresApi;
        import androidx.appcompat.app.AppCompatActivity;
        import androidx.core.app.ActivityCompat;
        import androidx.core.app.BundleCompat;

        import android.Manifest;
        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.location.Address;
        import android.location.Geocoder;
        import android.location.Location;
        import android.location.LocationListener;
        import android.location.LocationManager;
        import android.os.Build;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.Button;
        import android.widget.TextView;

        import com.example.usfsafeteamapp.Client.ClientHome;
        import com.example.usfsafeteamapp.Client.ClientHome.*;
        import com.example.usfsafeteamapp.Client.ClientWait;
        import com.example.usfsafeteamapp.DataParser.FetchURL;
        import com.example.usfsafeteamapp.DataParser.TaskLoadedCallback;
        import com.example.usfsafeteamapp.Objects.Requests;
        import com.example.usfsafeteamapp.Objects.myPlace;
        import com.example.usfsafeteamapp.R;
        import com.google.android.gms.location.FusedLocationProviderClient;
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
        import com.google.android.gms.tasks.OnCompleteListener;
        import com.google.android.gms.tasks.Task;
        import com.google.firebase.database.core.Tag;
        import com.google.firebase.firestore.CollectionReference;
        import com.google.firebase.firestore.DocumentReference;
        import com.google.firebase.firestore.DocumentSnapshot;
        import com.google.firebase.firestore.EventListener;
        import com.google.firebase.firestore.FirebaseFirestore;
        import com.google.firebase.firestore.FirebaseFirestoreException;
        import com.google.firebase.firestore.GeoPoint;
        import com.google.firebase.firestore.ListenerRegistration;
        import com.google.firebase.firestore.Query;
        import com.google.firebase.firestore.QueryDocumentSnapshot;
        import com.google.firebase.firestore.QuerySnapshot;

        import org.json.JSONObject;

        import java.io.IOException;
        import java.util.ArrayList;
        import java.util.Collection;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;
        import java.util.Objects;

public class DriverHome extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {


    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleMap myMap;
    private ListenerRegistration mPlacesEventListener;
    LocationManager locationManager;
    FirebaseFirestore mDb;
    Requests nRequest;
    String TAG;
    Polyline currentPolyline;
    LatLng curr_coords, start_coords, dest_coords;
    MarkerOptions curr_mkr, start_mkr, dest_mkr;

    myPlace Defaut_place;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_driver_home);
        mDb = FirebaseFirestore.getInstance(); // init firebase
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        final Button B = findViewById(R.id.buttonDriverConfirmation);

        B.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(DriverHome.this, DriverWait.class);

//                i.putExtra("request", nRequest.getRequest_id());
                startActivity(i);
//                LayoutInflater inflater = LayoutInflater
//                        .from(getApplicationContext());

            }
        });
        Button B1 = findViewById(R.id.button);

        B1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

        getLastKnownLocation();
//        setDefaut_place("ENB - Engineering Building II");// creates a defaut place as the place to be the starting point of the trip



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_container1);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);

        }
        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener()
            {

                @Override
                public void onLocationChanged(Location location) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    LatLng coords = new LatLng(lat,lon);

                    //Gets the user current location and places a marker there
                    curr_coords = new LatLng(lat,lon);
                    curr_mkr = new MarkerOptions().position(curr_coords).title("This is your position");
                    myMap.addMarker(curr_mkr);
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
        else if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();

                    //Gets the user current location and places a marker there
                    curr_coords = new LatLng(lat,lon);
                    curr_mkr = new MarkerOptions().position(curr_coords).title("This is your position");
                    myMap.addMarker(curr_mkr);
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
        final DocumentReference docref = mDb.collection("Drivers").document("Driver");
        docref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                    /*TextView textview=(TextView)findViewById(R.id.textnewrequest);
                    textview.setVisibility(View.VISIBLE);
                    B.setVisibility(View.VISIBLE);
                    TextView t2 = (TextView)findViewById(R.id.textViewRequestDisplay);
                    t2.setVisibility(View.INVISIBLE);*/

                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if ( documentSnapshot!=null && documentSnapshot.getData()!=null&& documentSnapshot.exists()) {



//                    Map<String, Object> data = new HashMap<>();
//                    data = documentSnapshot.getData();
//                    String S = "start" , D ="dest", LL = "latLng";
////                    documentSnapshot.get(S); = new JSONObject ();
//                    Object object ;
//                    object = data.get(S);
//
////                    Collection<Object> coll = data.values();
//                    Log.i("Object : %s", documentSnapshot.get(S).toString());
////                    int i = 0;
////                    for(Object obj : coll){
////
////                        Log.i("Object  "+i +": %s",obj.toString());
////                        i++;
////                    }
//                    Requests nRequest = new Requests(object);

                    Requests nRequest = documentSnapshot.toObject( Requests.class);

                    //Query DB
//                    myPlace Defaut_place = new myPlace();
                    if (Defaut_place!=null){
                        nRequest.setStart(Defaut_place);
                    }



                    Log.d(TAG, "Request Id: " + nRequest.getRequest_id() + "\nStart location:  "+ "Start LatLng: "+nRequest.getStart().getGeoPoint() + nRequest.getStart() +"\n Destionation:  " +
                            ""+nRequest.getDest().getName()+ "Dest LatLng: "+nRequest.getDest().getGeoPoint());

                    myMap.clear();

                    LatLng start_coords = nRequest.getStart().getLatLng();
                    LatLng dest_coords = nRequest.getDest().getLatLng();

                    //Create a marker for the place selected by the user
                    start_mkr = new MarkerOptions().position(start_coords).title(nRequest.getStart().getName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    dest_mkr = new MarkerOptions().position(dest_coords).title(nRequest.getDest().getName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    //Place the marker for your location and the chosen destination into the map


//                    assert curr_coords!=null;
                    if (curr_mkr==null) {
                        getLastKnownLocation();
                    }
                    myMap.addMarker(start_mkr);
                    myMap.addMarker(dest_mkr);
                    myMap.addMarker(curr_mkr);
                    Log.d(TAG,"Curr: " + curr_coords + " S: " + start_coords +" D: "+dest_coords);
                    //Push and fetch it into the String

                    String url = getUrl(curr_coords, start_coords, dest_coords, "walking");
                    new FetchURL(DriverHome.this).execute(url, "walking");

                    //Zoom into the path

                    if (dest_coords.latitude < curr_coords.latitude){
                        LatLngBounds LLB =  new LatLngBounds(dest_coords, curr_coords) ;
                        LLB.including(start_coords);
                        //TODO: Handle the case in which a new path causes a bug
                        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LLB.getCenter(), 15f));

                    }else{
                        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curr_coords, 14f));
                    }



//                    Log.d(TAG, "CURRENT DATAAAAAA: " + documentSnapshot.getData());
                    TextView textview=(TextView)findViewById(R.id.textnewrequest);
                    textview.setVisibility(View.VISIBLE);
                    B.setVisibility(View.VISIBLE);
                    TextView t2 = (TextView)findViewById(R.id.textViewRequestDisplay);
                    t2.setVisibility(View.INVISIBLE);
                }
                else {
                    Log.d(TAG, "Current data: null");
                }

            }
        });

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
        myMap = googleMap;

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //myMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //myMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    public String getUrl(LatLng origin, LatLng start, LatLng dest, String directionMode)
    {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        //start of ride as waypoint of route
        String str_start = "waypoints=via:" + start.latitude + "," + start.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + str_start + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_api_key);
        return url;
    }
    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = myMap.addPolyline((PolylineOptions) values[0]);
    }

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
    private void setDefaut_place(String place_name){
        if (place_name==null){
            place_name = "place_name";
        }
        mPlacesEventListener = mDb.collection("Places").whereEqualTo("name", place_name).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }


                if(queryDocumentSnapshots != null) {
                    Defaut_place = queryDocumentSnapshots.getDocuments().get(0).toObject(myPlace.class);

                }

            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mPlacesEventListener != null){
            mPlacesEventListener .remove();
        }
    }

}