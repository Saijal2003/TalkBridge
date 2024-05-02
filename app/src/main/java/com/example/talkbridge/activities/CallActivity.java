package com.example.talkbridge.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.talkbridge.R;
import com.example.talkbridge.activities.models.InterfaceJava;
import com.example.talkbridge.activities.models.user;
import com.example.talkbridge.databinding.ActivityCallBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

public class CallActivity extends AppCompatActivity {
    ActivityCallBinding binding;
    String uniqueId="";
    FirebaseAuth auth;
    String username="";
    String friendusername="";
    boolean isPeerConnected=false;
    DatabaseReference firebaseRef;
    boolean isAudio=true;
    boolean isvideo=true;
    String createdBy;
    boolean pageExit =false;
    WebView webView = findViewById(R.id.webView);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth=FirebaseAuth.getInstance();

        firebaseRef= FirebaseDatabase.getInstance().getReference().child("users");
        username=getIntent().getStringExtra("username");
        String incoming=getIntent().getStringExtra("incoming");
        createdBy=getIntent().getStringExtra("createdBy");
        //   friendusername="";
      //  if(incoming.equalsIgnoreCase(friendusername))
      //      friendusername=incoming;

        friendusername=incoming;
        setupWebView();
        //uniqueId=getUniqueId();

        binding.mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAudio=!isAudio;
                callJavascriptFunction("javascript:toogleAudio(\""+isAudio+"\")");
                if (isAudio) {
                    binding.mic.setImageResource(R.drawable.btn_unmute_normal);
                }else{
                    binding.mic.setImageResource(R.drawable.btn_mute_normal);

                }
            }
        });

        binding.video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isvideo=!isvideo;
                callJavascriptFunction("javascript:toogleAudio(\""+isvideo+"\")");
                if (isvideo) {
                    binding.video.setImageResource(R.drawable.btn_video_normal);
                }else{
                    binding.video.setImageResource(R.drawable.btn_video_muted);

                }
            }
        });
        binding.endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    void setupWebView(){
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                // Handle permission request here
                // For example, you can grant or deny the permission request
                request.grant(request.getResources());

            }

        });
        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        binding.webView.addJavascriptInterface(new InterfaceJava(this), "Android");

        loadVideoCall();
    }
    public void loadVideoCall(){
        String filePath="file:android_asset/call.html";
        binding.webView.loadUrl(filePath);

        binding.webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                initializePeer();
            }
        });

    }

    void initializePeer(){
        uniqueId=getUniqueId();
        callJavascriptFunction("javascript:init(\""+ uniqueId+"\"");

        if(createdBy.equalsIgnoreCase(username)){
            if(pageExit)
                return;
            firebaseRef.child(username).child("connId").setValue(uniqueId);
            firebaseRef.child(username).child("isAvailable").setValue(true);

            binding.connImage.setVisibility(View.GONE);
            binding.controls.setVisibility(View.VISIBLE);

            FirebaseDatabase.getInstance().getReference()
                    .child("profiles")
                    .child(friendusername)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            user User=snapshot.getValue(user.class);
                            Glide.with(CallActivity.this).load(User.getProfile())
                                    .into(binding.profile);
                            binding.name.setText(User.getName());
                            binding.city.setText(User.getCity());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

        }else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    friendusername=createdBy;
                    FirebaseDatabase.getInstance().getReference()
                            .child("profiles")
                            .child(friendusername)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    user User=snapshot.getValue(user.class);
                                    Glide.with(CallActivity.this).load(User.getProfile())
                                            .into(binding.profile);
                                    binding.name.setText(User.getName());
                                    binding.city.setText(User.getCity());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                    FirebaseDatabase.getInstance().getReference()
                            .child("users")
                            .child(friendusername)
                            .child("connId")
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.getValue()!=null){
                                        sendCallRequest();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            },2000);
        }
    }

    public void onPeerConnected(){
        isPeerConnected=true;
    }

    void sendCallRequest(){
        if(!isPeerConnected){
            Toast.makeText(this, "You are not connected! Please check your network connection", Toast.LENGTH_SHORT).show();
            return;
        }

        listenConnId();
    }

    void listenConnId(){
        firebaseRef.child(friendusername).child("connId").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue()==null)
                    return;

                binding.connImage.setVisibility(View.GONE);
                binding.controls.setVisibility(View.VISIBLE);
                String connId=snapshot.getValue(String.class);
                callJavascriptFunction("javascript:startCall(\""+connId+"\")");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void callJavascriptFunction(String function){
        binding.webView.post(new Runnable() {
            @Override
            public void run() {
                binding.webView.evaluateJavascript(function,null);
            }
        });
    }
    String getUniqueId(){
        return UUID.randomUUID().toString();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        pageExit=true;
        firebaseRef.child(createdBy).setValue(null);
        finish();
    }
}