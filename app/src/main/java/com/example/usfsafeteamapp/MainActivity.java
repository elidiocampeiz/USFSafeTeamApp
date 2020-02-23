package com.example.usfsafeteamapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.firestore.auth.User;

public class MainActivity extends AppCompatActivity {

    Button Users;
    Button Drivers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /*Users = findViewById(R.id.User);
        Drivers = findViewById(R.id.driver);

        Users.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, User.class);
                startActivity(i);
            }
        });
        Drivers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, DriverHome.class);
                startActivity(i);
            }
        });*/
    }
}
