package com.example.gethomesafely;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import android.support.annotation.Nullable;

import com.backendless.geo.geofence.GeoFence;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

public class GeoFenceTransitionService extends IntentService {

    private static final String TAG = "GEOSERVICE";

    public GeoFenceTransitionService(String name) {
        super(name);
        Log.d(TAG, "GeoFenceTransitionService: ");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent: ");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            String error = String.valueOf(geofencingEvent.getErrorCode());
            Toast.makeText(getApplicationContext(), "ERROR: " + error, Toast.LENGTH_LONG).show();
            return;
        }

        int geoFenceTransition = geofencingEvent.getGeofenceTransition();

        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT || geoFenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            List<Geofence> tiggeringGeoFences = geofencingEvent.getTriggeringGeofences();

            String geoTransDetails = getGeoFenceTransitionDetails(geoFenceTransition, tiggeringGeoFences);
            Log.d(TAG, "onHandleIntent: " + geoTransDetails);
            Toast.makeText(getApplicationContext(), "GEOFENCE: " + geoTransDetails, Toast.LENGTH_LONG).show();
        }
    }


    private String getGeoFenceTransitionDetails(int geoFenceTransition, List<Geofence> tiggeringGeoFences) {
        Log.d(TAG, "getGeoFenceTransitionDetails: ");
        ArrayList<String> triggerFenceList = new ArrayList<>();

        for (Geofence geofence : tiggeringGeoFences) {
            triggerFenceList.add(geofence.getRequestId());
        }

        String status = null;

        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            status = "ENTERING";
        } else if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            status = "EXITING";
        } else if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            status = "DWELLING";
        }

        return status + TextUtils.join(", ", triggerFenceList);
    }
}
