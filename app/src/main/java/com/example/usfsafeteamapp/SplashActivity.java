package com.example.usfsafeteamapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();

        new Handler().postDelayed(new Runnable(){
            public void run()
            {
                Intent SplashIntent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(SplashIntent);
                finish();
            }
        }, SPLASH_TIME_OUT);

    }
}
