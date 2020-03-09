package com.example.usfsafeteamapp.Driver;

        import androidx.annotation.Nullable;
        import androidx.annotation.RequiresApi;
        import androidx.appcompat.app.AppCompatActivity;
        import androidx.core.app.ActivityCompat;

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
        import com.example.usfsafeteamapp.Client.ClientWait;
        import com.example.usfsafeteamapp.R;
        import com.google.android.gms.maps.CameraUpdateFactory;
        import com.google.android.gms.maps.GoogleMap;
        import com.google.android.gms.maps.OnMapReadyCallback;
        import com.google.android.gms.maps.SupportMapFragment;
        import com.google.android.gms.maps.model.LatLng;
        import com.google.android.gms.maps.model.MarkerOptions;
        import com.google.firebase.database.core.Tag;
        import com.google.firebase.firestore.CollectionReference;
        import com.google.firebase.firestore.DocumentReference;
        import com.google.firebase.firestore.DocumentSnapshot;
        import com.google.firebase.firestore.EventListener;
        import com.google.firebase.firestore.FirebaseFirestore;
        import com.google.firebase.firestore.FirebaseFirestoreException;
        import com.google.firebase.firestore.QuerySnapshot;

        import java.io.IOException;
        import java.util.List;

public class DriverHome extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap myMap;
    LocationManager locationManager;
    FirebaseFirestore mDb;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_driver_home);
        mDb = FirebaseFirestore.getInstance(); // init firebase

        final Button B = findViewById(R.id.buttonDriverConfirmation);

        B.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(DriverHome.this, DriverWait.class);
                startActivity(i);
                LayoutInflater inflater = LayoutInflater
                        .from(getApplicationContext());

            }
        });
        Button B1 = findViewById(R.id.button);

        B1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

        final DocumentReference docref = mDb.collection("Drivers").document("Driver0");
        docref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                    /*TextView textview=(TextView)findViewById(R.id.textnewrequest);
                    textview.setVisibility(View.VISIBLE);
                    B.setVisibility(View.VISIBLE);
                    TextView t2 = (TextView)findViewById(R.id.textViewRequestDisplay);
                    t2.setVisibility(View.INVISIBLE);*/

                /*if (e != null) {
                    return;
                }*/

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    String TAG = "";
                    Log.d(TAG, "CURRENT DATAAAAAA: " + documentSnapshot.getData());
                    TextView textview=(TextView)findViewById(R.id.textnewrequest);
                    textview.setVisibility(View.VISIBLE);
                    B.setVisibility(View.VISIBLE);
                    TextView t2 = (TextView)findViewById(R.id.textViewRequestDisplay);
                    t2.setVisibility(View.INVISIBLE);
                }

            }
        });




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

                    Geocoder geo = new Geocoder(getApplicationContext());

                    try{
                        List<Address> list = geo.getFromLocation(lat,lon,1);

                        String str = list.get(0).getLocality() + ", ";
                        str += list.get(0).getCountryName();

                        myMap.addMarker(new MarkerOptions().position(coords).title("This is my position"));
                        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coords, 12.2f));
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }
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

                    LatLng coords = new LatLng(lat,lon);

                    Geocoder geo = new Geocoder(getApplicationContext());

                    try{
                        List<Address> list = geo.getFromLocation(lat,lon,1);
                        String str = list.get(0).getLocality() + ", ";
                        str += list.get(0).getCountryName();

                        myMap.addMarker(new MarkerOptions().position(coords).title("This is my position"));
                        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coords, 12.2f));
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }
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

}