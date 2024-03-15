package com.example.runningapp;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class RunningAPPWearableListener extends WearableListenerService {
    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent){
        String data = new String(messageEvent.getData());
        System.out.println("Message received: " + data);
        JSONObject o;
        try {
            o = new JSONObject(data);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        try {
            System.out.println("Message key1: " + o.getString("key1"));
            System.out.println("Message key2: " + o.getString("key2"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        if (messageEvent.getPath().equals( "/runningAppMessage" )) {
            Intent startIntent = new Intent(this, AndroidCompanion.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startIntent.putExtra("MessageData", messageEvent.getData());

            startActivity(startIntent);
        }

    }
}
