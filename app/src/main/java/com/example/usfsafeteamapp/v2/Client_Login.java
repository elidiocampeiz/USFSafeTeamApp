package com.example.usfsafeteamapp.v2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.usfsafeteamapp.Objects.Clients;
import com.example.usfsafeteamapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

public class Client_Login extends AppCompatActivity {
    private boolean bool = false;
    private EditText Temail, Tpassword;
    private Button Blogin, Bregistrate;
    private FirebaseAuth aut;
    private FirebaseAuth.AuthStateListener autlist;
    FirebaseFirestore mDb;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_login);

        //Creating the activity title and a back button
        getSupportActionBar().setTitle("Client Login");

        aut = FirebaseAuth.getInstance();
        autlist = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null){
                    Intent intent = new Intent(Client_Login.this, ClientHome2.class);
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
                String email = Temail.getText().toString();
                String password = Tpassword.getText().toString();
                if (email == ""){
                    Toast.makeText(Client_Login.this, "Please type your email", Toast.LENGTH_SHORT).show();
                }
                else if(password == ""){
                    Toast.makeText(Client_Login.this, "Please type your password", Toast.LENGTH_SHORT).show();
                }
                else {
                    aut.createUserWithEmailAndPassword(email,password).addOnCompleteListener(Client_Login.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(Client_Login.this, "Sign up error", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                String user_ID = aut.getCurrentUser().getUid();
                                DocumentReference docRef = mDb.collection("Clients").document(user_ID);

                                Clients cl = new Clients(user_ID);
                                docRef.set(cl, SetOptions.merge());

                            }
                        }
                    });
                }

            }
        });

        Blogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = Temail.getText().toString();
                String password = Tpassword.getText().toString();
                if (email == ""){
                    Toast.makeText(Client_Login.this, "Please type your email", Toast.LENGTH_SHORT).show();
                }
                else if(password == "" ){
                    Toast.makeText(Client_Login.this, "Please type your password", Toast.LENGTH_SHORT).show();
                }
                else {
                    aut.signInWithEmailAndPassword(email, password).addOnCompleteListener(Client_Login.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful())
                                Toast.makeText(Client_Login.this, "Sign in error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
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
