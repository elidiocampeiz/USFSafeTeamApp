package com.example.usfsafeteamapp.v2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.usfsafeteamapp.Driver.DriverHome;
import com.example.usfsafeteamapp.Objects.Drivers;
import com.example.usfsafeteamapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

public class Driver_Login extends AppCompatActivity {
    private EditText Temail, Tpassword;
    private Button Blogin, Bregistrate;
    private FirebaseAuth aut;
    private FirebaseAuth.AuthStateListener autlist;
    FirebaseFirestore mDb;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);

        aut = FirebaseAuth.getInstance();
        autlist = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null){
                    Intent intent = new Intent(Driver_Login.this, DriverHome2.class);
                    startActivity(intent);
                    finish();
                }
            }
        };

        mDb = FirebaseFirestore.getInstance(); // init firebase

        Temail = (EditText) findViewById(R.id.emailtext);
        Tpassword = (EditText) findViewById(R.id.passwordtext);
        Blogin = (Button) findViewById(R.id.loginbut);
        Bregistrate = (Button) findViewById(R.id.registratebut);

        Bregistrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = Temail.getText().toString();
                final String password = Tpassword.getText().toString();
                aut.createUserWithEmailAndPassword(email,password).addOnCompleteListener(Driver_Login.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(Driver_Login.this, "Sign in error", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            String user_ID = aut.getCurrentUser().getUid();
                            DocumentReference docRef = mDb.collection("Drivers").document(user_ID);

                            Drivers dr = new Drivers(user_ID);
                            docRef.set(dr, SetOptions.merge());
                        }
                    }
                });
            }
        });

        Blogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = Temail.getText().toString();
                final String password = Tpassword.getText().toString();

                aut.createUserWithEmailAndPassword(email,password).addOnCompleteListener(Driver_Login.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful())
                            Toast.makeText(Driver_Login.this, "Sign in error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }
    @Override
    public void onStart(){
        super.onStart();
        aut.addAuthStateListener(autlist);
    }
    @Override
    public void onStop(){
        super.onStop();
        aut.removeAuthStateListener(autlist);
    }
}