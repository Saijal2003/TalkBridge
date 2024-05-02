package com.example.talkbridge.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import com.example.talkbridge.R;
import com.example.talkbridge.activities.models.user;
import com.example.talkbridge.databinding.ActivityMainBinding;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    long coins=0;

    String[] permissions=new String[]{
            android.Manifest.permission.CAMERA,android.Manifest.permission.RECORD_AUDIO
    };
    private int requestCode=1;
    user User;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

       /* KProgressHUD hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.ANNULAR_DETERMINATE)
                .setLabel("Please wait")
                .setMaxProgress(100)
                .show();
        hud.setProgress(90);*/

        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();

        FirebaseUser currentUser=auth.getCurrentUser();

        database.getReference().child("profiles")
                        .child(currentUser.getUid())
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        User = snapshot.getValue(user.class);
                                        coins=User.getCoins();
                                        binding.coins.setText("You Have: "+coins);

                                        Glide.with(MainActivity.this)
                                                .load(User.getProfile())
                                                .into(binding.profilePic);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

        binding.Findbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPermissionGranted()) {
                    if (coins > 10) {
                        coins=coins-5;
                        database.getReference().child("profiles")
                                .child(currentUser.getUid())
                                .child("coins")
                                .setValue(coins);
                        Intent intent=new Intent(MainActivity.this,ConnectingAcitivity.class);
                        intent.putExtra("profile",User.getProfile());
                        startActivity(intent);
                        //startActivity(new Intent(MainActivity.this, ConnectingAcitivity.class));
                    } else {
                        Toast.makeText(MainActivity.this, "Insufficient coins", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    askPermission();
                }


            }
        });

        binding.rewardbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,RewardedActivity.class));
            }
        });

    }

    void askPermission(){
        ActivityCompat.requestPermissions(this, permissions,requestCode);
    }

    private boolean isPermissionGranted(){
        for(String permission:permissions){
            if(ActivityCompat.checkSelfPermission(this,permission)!= PackageManager.PERMISSION_GRANTED){
                return false;
            }

        }
        return true;
    }
}
