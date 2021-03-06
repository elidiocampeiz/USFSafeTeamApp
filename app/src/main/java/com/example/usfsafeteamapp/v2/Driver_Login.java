package com.example.usfsafeteamapp.v2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.usfsafeteamapp.Objects.Clients;
import com.example.usfsafeteamapp.Objects.Drivers;
import com.example.usfsafeteamapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

public class Driver_Login extends AppCompatActivity {
    private EditText Temail, Tpassword;
    private Button Blogin, Bregistrate;
    private FirebaseAuth aut;
    private FirebaseAuth.AuthStateListener autlist;

    String TAG;
    private boolean isDriverAuth,isAuth;

    FirebaseFirestore mDb;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);

        //Creating the activity title and a back button
        getSupportActionBar().setTitle("Driver Login");

        isDriverAuth = false;
        isAuth=false;



//        FirebaseUser User = FirebaseAuth.getInstance().getCurrentUser();




        mDb = FirebaseFirestore.getInstance(); // init firebase

        Temail = (EditText) findViewById(R.id.emailtext_driver);
        Tpassword = (EditText) findViewById(R.id.passwordtext_driver);
        Blogin = (Button) findViewById(R.id.loginbutton_driver);
        Bregistrate = (Button) findViewById(R.id.registratebutton_driver);

        aut = FirebaseAuth.getInstance();
        autlist = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null){

                    isAuth = true;
                    CheckDriver();
//                    attachListener();
//                    driverAuth();
                }
                else{
                    Toast.makeText(Driver_Login.this, "Please login or register new account", Toast.LENGTH_SHORT).show();
                }
            }
        };

//



        Bregistrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = Temail.getText().toString();
                final String password = Tpassword.getText().toString();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (email.isEmpty() && password.isEmpty()){
                    Toast.makeText(Driver_Login.this, "Please type your email and password", Toast.LENGTH_SHORT).show();
                }
                else if(password.isEmpty()){
                    Toast.makeText(Driver_Login.this, "Please type your password", Toast.LENGTH_SHORT).show();
                }
                else if(email.isEmpty()){
                    Toast.makeText(Driver_Login.this, "Please type your email", Toast.LENGTH_SHORT).show();
                }
                else {

                    if (user != null) {

                        Toast.makeText(Driver_Login.this, "new Driver created", Toast.LENGTH_SHORT).show();
                        String user_ID = user.getUid();
                        DocumentReference docRef = mDb.collection("Drivers").document(user_ID);
                        DocumentReference clientRef = mDb.collection("Clients").document(user_ID);
                        Clients cl = new Clients(user_ID);
                        Drivers dr = new Drivers(user_ID);
//                    dr.setNextRequest(null);
                        docRef.set(dr, SetOptions.merge());
                        clientRef.set(cl, SetOptions.merge());
                        //set it to DriversOnline by default if not using Switch Button (Working?)


                    } else {
                        aut.createUserWithEmailAndPassword(email, password).addOnCompleteListener(Driver_Login.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(Driver_Login.this, "Sign up error", Toast.LENGTH_SHORT).show();


                                } else {
                                    String user_ID = aut.getCurrentUser().getUid();
                                    DocumentReference docRef = mDb.collection("Drivers").document(user_ID);
                                    Drivers dr = new Drivers(user_ID);
                                    DocumentReference clientRef = mDb.collection("Clients").document(user_ID);
                                    Clients cl = new Clients(user_ID);
                                    clientRef.set(cl, SetOptions.merge());
                                    docRef.set(dr, SetOptions.merge());
//                                mDb.collection("DriversOnline").document(user_ID).set(dr, SetOptions.merge());

                                }
                            }
                        });


                    }
                }
            }
        });
        Blogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = Temail.getText().toString();
                final String password = Tpassword.getText().toString();
                if (email.isEmpty() && password.isEmpty()){
                    Toast.makeText(Driver_Login.this, "Please type your email and password", Toast.LENGTH_SHORT).show();
                }
                else if(password.isEmpty()){
                    Toast.makeText(Driver_Login.this, "Please type your password", Toast.LENGTH_SHORT).show();
                }
                else if(email.isEmpty()){
                    Toast.makeText(Driver_Login.this, "Please type your email", Toast.LENGTH_SHORT).show();
                }
                else {
                    aut.signInWithEmailAndPassword(email, password).addOnCompleteListener(Driver_Login.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful())
                                Toast.makeText(Driver_Login.this, "Sign in error", Toast.LENGTH_SHORT).show();
                        }

                    });
                }

            }



        });


    }
    private void driverAuth(){
        if (isDriverAuth && isAuth){
            Intent intent = new Intent(Driver_Login.this, DriverHome2.class);
            startActivity(intent);
            finish();
        }

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

    public void CheckDriver ()
    {
        String ID = (String) aut.getCurrentUser().getUid();
        DocumentReference docRef = mDb.collection("Drivers").document(ID);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        Intent intent = new Intent(Driver_Login.this, DriverHome2.class);
                        startActivity(intent);
                        finish();
                    } else {
                        isDriverAuth = false;
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }
}
