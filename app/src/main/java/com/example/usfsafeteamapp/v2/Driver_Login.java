package com.example.usfsafeteamapp.v2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.usfsafeteamapp.Objects.Drivers;
import com.example.usfsafeteamapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
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

        isDriverAuth = false;
        isAuth=false;


        aut = FirebaseAuth.getInstance();
//        FirebaseUser User = FirebaseAuth.getInstance().getCurrentUser();




        mDb = FirebaseFirestore.getInstance(); // init firebase

        Temail = (EditText) findViewById(R.id.emailtext);
        Tpassword = (EditText) findViewById(R.id.passwordtext);
        Blogin = (Button) findViewById(R.id.loginbut);
        Bregistrate = (Button) findViewById(R.id.registratebut);

        autlist = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null){

                    isAuth =true;
                    driverAuth();
                }
                else{
                    Toast.makeText(Driver_Login.this, "Please login or register new account", Toast.LENGTH_SHORT).show();
                }
            }
        };


        String ID = (String) aut.getCurrentUser().getUid();
        final DocumentReference docRef = mDb.collection("Drivers").document(ID);

        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                //add getMetadata().hasPendingWrites() to trigger the intent only once
                if (snapshot != null && snapshot.exists() && snapshot.getMetadata().hasPendingWrites()) {
                    Log.d(TAG, "Current data found");
                    isDriverAuth = true;
                    driverAuth();
                } else {
                    isDriverAuth = false;
                    Log.d(TAG, "Current data: null");
                }
            }
        });

        Bregistrate.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               final String email = Temail.getText().toString();
                                               final String password = Tpassword.getText().toString();
                                               FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                               if (user != null) {

                                                   Toast.makeText(Driver_Login.this, "new Driver created", Toast.LENGTH_SHORT).show();
                                                   String user_ID = user.getUid();
                                                   DocumentReference docRef = mDb.collection("Drivers").document(user_ID);

                                                   Drivers dr = new Drivers(user_ID);
                                                   dr.setNextRequest(null);
                                                   docRef.set(dr, SetOptions.merge());
                                                   //set it to DriversOnline by default if not using Switch Button (Working?)

                                                   mDb.collection("DriversOnline").document(user_ID).set(dr, SetOptions.merge());


                                               }
                                               else {
                                                   aut.createUserWithEmailAndPassword(email, password).addOnCompleteListener(Driver_Login.this, new OnCompleteListener<AuthResult>() {
                                                       @Override
                                                       public void onComplete(@NonNull Task<AuthResult> task) {
                                                           if (!task.isSuccessful()) {
                                                               Toast.makeText(Driver_Login.this, "Sign up error", Toast.LENGTH_SHORT).show();


                                                           } else {
                                                               String user_ID = aut.getCurrentUser().getUid();
                                                               DocumentReference docRef = mDb.collection("Drivers").document(user_ID);
                                                               Drivers dr = new Drivers(user_ID);
                                                               dr.setNextRequest(null);

                                                               docRef.set(dr, SetOptions.merge());
                                                               mDb.collection("DriversOnline").document(user_ID).set(dr, SetOptions.merge());

                                                           }
                                                       }
                                                   });


                                               }
                                           }
                                       });




        Blogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = Temail.getText().toString();
                final String password = Tpassword.getText().toString();
                aut.signInWithEmailAndPassword(email, password).addOnCompleteListener(Driver_Login.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful())
                            Toast.makeText(Driver_Login.this, "Sign in error", Toast.LENGTH_SHORT).show();
                    }
                });
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
}
