package com.example.gethomesafely;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.GeofencingClient;

public class Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("SCHEDULER", "It is working");
        MapsActivity mapsActivity = new MapsActivity();
        mapsActivity.journeyStatusChanged(intent.getStringExtra("BUTTON"), intent.getStringExtra("MESSAGE"), false);
    }
}
