package com.example.usfsafeteamapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.usfsafeteamapp.v2.Client_Login;
import com.example.usfsafeteamapp.v2.Driver_Login;

//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.GoogleApiAvailability;
//
//import static com.example.usfsafeteamapp.Constants.ERROR_DIALOG_REQUEST;
//import static com.example.usfsafeteamapp.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
//import static com.example.usfsafeteamapp.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

public class MainActivity extends AppCompatActivity
{
    //Button B;
    //Button B1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button B = findViewById(R.id.buttonClient);

        B.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Client_Login.class);
                startActivity(i);
            }
        });

        Button B1 = findViewById(R.id.buttonDriver);

        B1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(MainActivity.this, Driver_Login.class);
                startActivity(in);
            }
        });


    }
//
}
