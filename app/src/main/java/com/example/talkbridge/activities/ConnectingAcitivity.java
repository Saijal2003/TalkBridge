package com.example.talkbridge.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.example.talkbridge.R;
import com.example.talkbridge.databinding.ActivityConnectingAcitivityBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class ConnectingAcitivity extends AppCompatActivity {
    ActivityConnectingAcitivityBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    boolean isOkay=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityConnectingAcitivityBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        auth= FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();

        String profile=getIntent().getStringExtra("profile");
        Glide.with(this)
                .load(profile)
                .into(binding.profile);

        String username=auth.getUid();

        database.getReference().child("users")
                .orderByChild("status")
                .equalTo(0).limitToFirst(1)//if status is 0 then person is on call and if status is 1 then call already chal rahi hai person has to wait
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.getChildrenCount()>0){
                            isOkay=true;
                            //Room avaliable
                           for(DataSnapshot childSnap:snapshot.getChildren()){
                               database.getReference()
                                       .child("users")
                                       .child(childSnap.getKey())
                                       .child("incoming")
                                       .setValue(username);
                               database.getReference()
                                       .child("users")
                                       .child(childSnap.getKey())
                                       .child("status")
                                       .setValue(1);

                               Intent intent=new Intent(ConnectingAcitivity.this,CallActivity.class);
                               String incoming=childSnap.child("incoming").getValue(String.class);
                               String createdBy=childSnap.child("incoming").getValue(String.class);
                               boolean isAvailable=childSnap.child("incoming").getValue(Boolean.class);

                               intent.putExtra("username",username);
                               intent.putExtra("incoming",incoming);
                               intent.putExtra("createdBy",createdBy);
                               intent.putExtra("isAvailable",isAvailable);

                               startActivity(intent);
                               finish();
                           }
                        }
                        else{
                            //Not available
                            //so create room
                            HashMap<String, Object> room=new HashMap<>();
                            room.put("incoming",username);
                            room.put("createdBy",username);
                            room.put("isAvailable",true);
                            room.put("status",1);

                            database.getReference()
                                    .child("users")
                                    .child(username)
                                    .setValue(room).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            database.getReference()
                                                    .child("users")
                                                    .child(username).addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if(snapshot.child("status").exists()){
                                                                if(snapshot.child("status").getValue(Integer.class)==1){

                                                                    if(isOkay)
                                                                        return;

                                                                    isOkay=true;
                                                                    Intent intent=new Intent(ConnectingAcitivity.this,CallActivity.class);
                                                                    String incoming=snapshot.child("incoming").getValue(String.class);
                                                                    String createdBy=snapshot.child("incoming").getValue(String.class);
                                                                    boolean isAvailable=snapshot.child("incoming").getValue(Boolean.class);

                                                                    intent.putExtra("username",username);
                                                                    intent.putExtra("incoming",incoming);
                                                                    intent.putExtra("createdBy",createdBy);
                                                                    intent.putExtra("isAvailable",isAvailable);

                                                                    startActivity(intent);
                                                                    finish();
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }
}