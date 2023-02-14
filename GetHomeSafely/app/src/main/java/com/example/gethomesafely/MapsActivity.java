package com.example.gethomesafely;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.ColorLong;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.persistence.local.UserIdStorageFactory;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener, View.OnClickListener, ResultCallback<Status> {

    private GoogleMap mMap;
    GoogleApiClient client, mGoogleApiClient;
    LocationRequest locationRequest;
    Location lastLocation;
    Marker currentMarkerLocation;
    public static final int REQUEST_LOCATION_CODE = 99;
    Button MapButton, DirectionButton, HospitalButton, SchoolButton, RestaurantButton;
    AutoCompleteTextView Search;
    double startLat, endLat, startLong, endLong;
    int PROXIMITY_RADIUS = 10000;
    private PlaceAutocompleteAdapter mplaceAutocompleteAdapter;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40, -168), new LatLng(71, 136));
    TextView txtView;
    String TAG = "placeautocomplete";
    String placeId, placeName = "";
    protected LocationManager locationManager;
    final ArrayList<String> initialJourneyData = new ArrayList<String>();
    Marker geoFenceMarker;
    GeofencingRequest geofencingRequest;
    Circle geoFenceLimits;
    PendingIntent geoFencePendingIntent;
    PendingIntent pendingIntent;
    AlarmManager alarmManager;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        DirectionButton = findViewById(R.id.btnDirections);
        DirectionButton.setOnClickListener(this);

//        Search = findViewById(R.id.map_search);

        txtView = findViewById(R.id.txtView);

        // Initialize Places.
        Places.initialize(getApplicationContext(), "AIzaSyAkuW_0VdZanRyOEInyJBcppFUyaob65N4");
        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                txtView.setText(place.getName() + "," + place.getId());
                placeId = place.getId();
                placeName = place.getName();
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
                Toast.makeText(MapsActivity.this, "Error: " + status.toString(), Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (client == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
                return;
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMarkerDragListener(this);
        mMap.setOnMarkerClickListener(this);
    }

    protected synchronized void buildGoogleApiClient() {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        client.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_normal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;

            case R.id.action_hybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;

            case R.id.action_satellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;

            case R.id.action_terrain:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;

            case R.id.action_none:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
        }

        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;

        if (currentMarkerLocation != null) {
            currentMarkerLocation.remove();
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        startLat = location.getLatitude();
        startLong = location.getLongitude();
        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        currentMarkerLocation = mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(10));

        if (client != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();

        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setSmallestDisplacement((float) 0.0);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.setDraggable(true);
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        endLat = marker.getPosition().latitude;
        endLong = marker.getPosition().longitude;
    }

    @Override
    public void onClick(View v) {
        Object[] dataTransfer = new Object[2];
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
        switch (v.getId()) {
            case R.id.btnDirections:
                if (placeName.equals("")) {
                    Toast.makeText(getApplicationContext(), "Please enter a destination first", Toast.LENGTH_LONG).show();
                } else if (startLat == 0.0 || startLong == 0.0) {
                    Toast.makeText(getApplicationContext(), "Unable to fetch current location, try turning off and on the location services on your device", Toast.LENGTH_LONG).show();
                } else {
                    HomePage homePage = new HomePage();
                    final String[] userJourneyId = {""};
                    if (!placeName.isEmpty()) {
                        mMap.clear();
                        List<Address> addressList = null;
                        MarkerOptions mo = new MarkerOptions();

                        if (!placeName.equals("")) {
                            Geocoder geocoder = new Geocoder(MapsActivity.this);
                            try {
                                addressList = geocoder.getFromLocationName(placeName, 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            for (int i = 0; i < addressList.size(); i++) {
                                Address myAddress = addressList.get(i);
                                LatLng latLng = new LatLng(myAddress.getLatitude(), myAddress.getLongitude());
                                System.out.println(latLng.toString());
                                mo.position(latLng);
                                mo.title("Your Search Result");
                                mMap.addMarker(mo);
                                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                                endLat = myAddress.getLatitude();
                                endLong = myAddress.getLongitude();
                            }

                            dataTransfer = new Object[3];
                            final String url = getDirectionsUrl();
                            GetDirectionsData getDirectionsData = new GetDirectionsData();
                            dataTransfer[0] = mMap;
                            dataTransfer[1] = url;
                            dataTransfer[2] = new LatLng(endLat, endLong);

                            final String userObjectId = UserIdStorageFactory.instance().getStorage().get();
                            Log.i("USEROBJECTID", userObjectId);
                            HashMap journeyData = new HashMap();
                            journeyData.put("start_lat", startLat);
                            journeyData.put("start_long", startLong);
                            journeyData.put("end_lat", endLat);
                            journeyData.put("end_long", endLong);
                            journeyData.put("user_id", userObjectId);
                            journeyData.put("status", "ongoing");

                            Backendless.Data.of("User_Journey").save(journeyData, new AsyncCallback<Map>() {
                                @Override
                                public void handleResponse(Map response) {
                                    userJourneyId[0] = (String) response.get("objectId");
//                                    System.out.println(userJourneyId[0]);
//                                    Toast.makeText(MapsActivity.this, "Message: " + response.toString(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    Toast.makeText(MapsActivity.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                            String whereClause = "user_id = '" + userObjectId + "'";
                            DataQueryBuilder queryBuilder = DataQueryBuilder.create();
                            queryBuilder.setWhereClause(whereClause);

                            Backendless.Data.of("User_Contacts").find(queryBuilder,
                                    new AsyncCallback<List<Map>>() {
                                        @Override
                                        public void handleResponse(List<Map> foundContacts) {
                                            // every loaded object from the "Contact" table is now an individual java.util.Map
                                            for (int i = 0; i < foundContacts.size(); i++) {
                                                HashMap journeyData = new HashMap();
                                                Log.i("USERJOURNEYID", userJourneyId[0]);
                                                journeyData.put("journey_id", userJourneyId[0]);
                                                journeyData.put("name", foundContacts.get(i).get("name"));
                                                journeyData.put("phone", foundContacts.get(i).get("phone"));
                                                journeyData.put("user_id", userObjectId);

                                                Backendless.Data.of("User_Journey_Contacts").save(journeyData, new AsyncCallback<Map>() {
                                                    @Override
                                                    public void handleResponse(Map response) {
                                                        Log.i("USERJOURNEYCONTACTS", response.toString());
//                                                        Toast.makeText(MapsActivity.this, "Message: " + response.toString(), Toast.LENGTH_SHORT).show();
                                                    }

                                                    @Override
                                                    public void handleFault(BackendlessFault fault) {
                                                        Toast.makeText(MapsActivity.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void handleFault(BackendlessFault fault) {
                                            // an error has occurred, the error code can be retrieved with fault.getCode()
                                        }
                                    });


                            getDirectionsData.execute(dataTransfer);
//                            homePage.sendNotification("StartJourney", "", "start", "User has started a journey");
                            Backendless.UserService.findById(userObjectId, new AsyncCallback<BackendlessUser>() {
                                @Override
                                public void handleResponse(BackendlessUser response) {
                                    System.out.println("USER" + response.toString());
                                    String name = (String) response.getProperty("name");
                                    journeyStatusChanged("start", name.trim() + " has started a journey", true);
                                    geo();
                                    getJourneyData(url);
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    Toast.makeText(getApplicationContext(), "ERROR: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            Toast.makeText(MapsActivity.this, "Please enter a destination first", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(MapsActivity.this, "Please enter a destination first", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    private String getUrl(double latitude, double longitude, String nearbyPlace) {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location=" + latitude + "," + longitude);
        googlePlaceUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlaceUrl.append("&type=" + nearbyPlace);
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key=" + "AIzaSyA5K2fbI4KEeQ3fDX4z5RsF5xCP6kWoQx8");

        return googlePlaceUrl.toString();
    }

    private String getDirectionsUrl() {
        Log.d("STARTLATLONG", "getDirectionsUrl: " + startLat + startLong);
        StringBuilder googleDirectionUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionUrl.append("origin=" + startLat + "," + startLong);
        googleDirectionUrl.append("&destination=" + endLat + ',' + endLong);
        googleDirectionUrl.append("&mode=walking");
        googleDirectionUrl.append("&key=AIzaSyAkuW_0VdZanRyOEInyJBcppFUyaob65N4");

        Log.d("GETDIRECTIONSURL:", "getDirectionsUrl: " + googleDirectionUrl.toString());

        return googleDirectionUrl.toString();
    }

    public void journeyStatusChanged(final String button, final String message, final boolean initial) {
        final String userObjectId = UserIdStorageFactory.instance().getStorage().get();
        final String whereClause = "user_id = '" + userObjectId + "'";
        final DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);

        Backendless.Data.of("User_Contacts").find(queryBuilder, new AsyncCallback<List<Map>>() {
            @Override
            public void handleResponse(List<Map> response) {
                for (int i = 0; i < response.size(); i++) {
                    String phone = (String) response.get(i).get("phone");

                    String whereClauseUsers = "phone = '" + phone + "'";
                    DataQueryBuilder queryBuilderUsers = DataQueryBuilder.create();
                    queryBuilderUsers.setWhereClause(whereClauseUsers);

                    Backendless.Data.of(BackendlessUser.class).find(queryBuilderUsers, new AsyncCallback<List<BackendlessUser>>() {
                        @Override
                        public void handleResponse(List<BackendlessUser> response) {
                            for (int j = 0; j < response.size(); j++) {
                                String userId = (String) response.get(j).getProperty("objectId");

                                String whereClauseDevice = "user.objectId = '" + userId + "'";
                                DataQueryBuilder queryBuilderDevice = DataQueryBuilder.create();
                                queryBuilderDevice.setWhereClause(whereClauseDevice);

                                Backendless.Data.of("DeviceRegistration").find(queryBuilderDevice, new AsyncCallback<List<Map>>() {
                                    @Override
                                    public void handleResponse(List<Map> response) {
                                        final HomePage homePage = new HomePage();
                                        for (int k = 0; k < response.size(); k++) {
                                            String deviceToken = (String) response.get(k).get("deviceToken");
                                            homePage.sendNotification("", deviceToken, button, message);
                                        }

                                        if (!initial) {
                                            String whereClauseDeviceOwn = "user.objectId = '" + userObjectId + "'";
                                            DataQueryBuilder queryBuilderDeviceOwn = DataQueryBuilder.create();
                                            queryBuilderDeviceOwn.setWhereClause(whereClauseDeviceOwn);

                                            Backendless.Data.of("DeviceRegistration").find(queryBuilderDeviceOwn, new AsyncCallback<List<Map>>() {
                                                @Override
                                                public void handleResponse(List<Map> response) {
                                                    for (int l = 0; l < response.size(); l++) {
                                                        String deviceTokenOwn = (String) response.get(l).get("deviceToken");
                                                        homePage.sendNotification("", deviceTokenOwn, button, "Your followers on the journey are notified");
                                                    }
                                                }

                                                @Override
                                                public void handleFault(BackendlessFault fault) {

                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void handleFault(BackendlessFault fault) {
                                        Toast.makeText(MapsActivity.this, "ERROR: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Toast.makeText(MapsActivity.this, "ERROR: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(MapsActivity.this, "ERROR: " + fault.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void scheduledJob(String button, String timeInSecs, String distance) {
        int interval = Integer.parseInt(timeInSecs);
        String progress = null;

        if (interval > 3600) {
            interval = (int) (interval * 0.1);
            progress = "10%";
        } else if (interval > 1800) {
            interval = (int) (interval * 0.15);
            progress = "15%";
        } else if (interval > 900) {
            interval = (int) (interval * 0.20);
            progress = "20%";
        } else {
            interval = (int) (interval * 0.25);
            progress = "25%";
        }

        Log.d(TAG, "scheduledJob: Duration, Interval = " + timeInSecs + interval);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, interval);
        intent = new Intent(getApplicationContext(), Receiver.class);
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        intent.putExtra("BUTTON", "progress");
        intent.putExtra("MESSAGE", "User has progressed " + progress + " in the journey");

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), interval * 1000, pendingIntent);
    }

    public void getJourneyData(String url) {
        AsyncHttpClient client = new AsyncHttpClient();

        client.post(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                System.out.println("Success getJourneyData" + response.toString());
                try {
                    String distance, duration;
                    distance = response.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").get("value").toString();
                    duration = response.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").get("value").toString();

                    Log.d("DISTANCE + DURATION ", distance + ", " + duration);
//                    scheduledJob("start", duration, distance);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                System.out.println("Throwable1 getJourneyData" + throwable.toString());
                System.out.println("Error1 getJourneyData" + responseString.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                System.out.println("Throwable getJourneyData" + throwable.toString());
                System.out.println("Error getJourneyData" + errorResponse.toString());
            }
        });
    }

    private void geo() {
        if (geoFenceMarker != null) {
            geoFenceMarker.remove();
        }

        LatLng latLng = new LatLng(endLat, endLong);
        Log.d(TAG, "geo: " + latLng);

        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("Destination");

        geoFenceMarker = mMap.addMarker(markerOptions);
        Log.d(TAG, "geo: ");

        startGeoFence();
    }

    private void startGeoFence() {
        if (geoFenceMarker != null) {
            Geofence geofence = createGeoFence(geoFenceMarker.getPosition(), 40f);
            geofencingRequest = createGeoRequest(geofence);

            Log.d(TAG, "startGeoFence: ");
            
            addGeoFence(geofence);
        }
    }

    private void addGeoFence(Geofence geofence) {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG, "addGeoFence: ");
            LocationServices.GeofencingApi.addGeofences(client, geofencingRequest, createGeoFencingPendingIntent())
                    .setResultCallback(this);
        }
    }

    private PendingIntent createGeoFencingPendingIntent() {
        if (geoFencePendingIntent != null) {
            Log.d(TAG, "createGeoFencingPendingIntent 1: ");
            return geoFencePendingIntent;
        }
        Intent intent = new Intent(this, GeoFenceTransitionService.class);

        Log.d(TAG, "createGeoFencingPendingIntent: ");
        
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private GeofencingRequest createGeoRequest(Geofence geofence) {
        Log.d(TAG, "createGeoRequest: ");
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }

    private Geofence createGeoFence(LatLng position, float v) {
        Log.d(TAG, "createGeoFence: ");
        return new Geofence.Builder()
                .setRequestId("My Geofence")
                .setCircularRegion(position.latitude, position.longitude, v)
                .setExpirationDuration(60 * 60 * 1000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.d(TAG, "onResult: ");
        drawGeoFence();
    }

    public void drawGeoFence() {
        Log.d(TAG, "drawGeoFence: ");
        if (geoFenceLimits != null) {
            geoFenceLimits.remove();
        }
        CircleOptions circleOptions= new CircleOptions()
                .center(geoFenceMarker.getPosition())
                .strokeColor(Color.argb(50, 70, 70, 70))
                .fillColor(Color.argb(100, 150, 150, 150))
                .radius(400f);

        geoFenceLimits = mMap.addCircle(circleOptions);
    }

    public void handleAlarmManager() {
        alarmManager.cancel(pendingIntent);
    }
}
