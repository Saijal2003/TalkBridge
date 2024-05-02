package com.example.talkbridge.activities.models;

import android.telecom.Call;
import android.webkit.JavascriptInterface;

import com.example.talkbridge.activities.CallActivity;

public class InterfaceJava {
    CallActivity callActivity;

    public InterfaceJava(CallActivity callActivity){
        this.callActivity=callActivity;
    }
    @JavascriptInterface
    public void onPeerConnected(){
        callActivity.onPeerConnected();

    }
}
