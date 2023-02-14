package com.example.gethomesafely;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JourneyHistory extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "JOURNEYHISTORY";
    TextView JourneyHistoryData, JourneyCurrentData;
    private LinearLayout PauseButton, AbortButton, PanicButton;
    Button NewJourneyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_history);

        Toast.makeText(getApplicationContext(), "Please wait while data is loading", Toast.LENGTH_LONG).show();

        JourneyHistoryData = findViewById(R.id.journey_history);
        JourneyCurrentData = findViewById(R.id.journey_current);

        NewJourneyButton = findViewById(R.id.bn_new_journey);
        NewJourneyButton.setOnClickListener(this);

        PauseButton = findViewById(R.id.bn_pause);
        PauseButton.setOnClickListener(this);

        AbortButton = findViewById(R.id.bn_abort);
        AbortButton.setOnClickListener(this);

        PanicButton = findViewById(R.id.bn_panic);
        PanicButton.setOnClickListener(this);

        String userObjectId = UserIdStorageFactory.instance().getStorage().get();
        String whereClause = "user_id = '" + userObjectId + "'";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setSortBy("created DESC");
        queryBuilder.setWhereClause(whereClause);

        Backendless.Data.of("User_Journey").find(queryBuilder, new AsyncCallback<List<Map>>() {
            @Override
            public void handleResponse(List<Map> response) {
                Log.d(TAG, "handleResponse: " + response.size());
                String defaultText = "No Journeys in the past";
                String defaultOngoingText = "No ongoing Journey";
                String journeyHistoryText = "";
                String journeyOngoingText = "";
                int count = 1;
                for (int i = 0; i < response.size(); i++) {
                    double startLat = (double) response.get(i).get("start_lat");
                    double startLong = (double) response.get(i).get("start_long");
                    double endLat = (double) response.get(i).get("end_lat");
                    double endLong = (double) response.get(i).get("end_long");
                    String startAddress = getAddress(startLat, startLong);
                    String endAddress = getAddress(endLat, endLong);
                    if (response.get(i).get("status").toString().trim().equals("ongoing") || response.get(i).get("status").toString().trim().equals("paused")) {
                        journeyOngoingText += "Source: " + startAddress;
//                        journeyOngoingText += response.get(i).get("start_lat");
//                        journeyOngoingText += ", ";
//                        journeyOngoingText += response.get(i).get("start_long");
                        journeyOngoingText += "\n";
                        journeyOngoingText += "Destination: " + endAddress;
//                        journeyOngoingText += response.get(i).get("end_lat");
//                        journeyOngoingText += ", ";
//                        journeyOngoingText += response.get(i).get("end_long");
                        journeyOngoingText += "\n";
                        journeyOngoingText += "Status: ";
                        journeyOngoingText += response.get(i).get("status");
                    } else {
                        if (count == 6) {
                            break;
                        } else {
                            journeyHistoryText += count + ". ";
                            journeyHistoryText += "Source: " + startAddress;
//                            journeyHistoryText += response.get(i).get("start_lat");
//                            journeyHistoryText += ", ";
//                            journeyHistoryText += response.get(i).get("start_long");
                            journeyHistoryText += "\n";
                            journeyHistoryText += "Destination: " + endAddress;
//                            journeyHistoryText += response.get(i).get("end_lat");
//                            journeyHistoryText += ", ";
//                            journeyHistoryText += response.get(i).get("end_long");
                            journeyHistoryText += "\n";
                            journeyHistoryText += "Status: ";
                            journeyHistoryText += response.get(i).get("status");
                            journeyHistoryText += "\n";
                            count += 1;
                        }
                    }
                }
                if (journeyHistoryText.equals("")) {
                    JourneyHistoryData.setText(defaultText);
                } else {
                    JourneyHistoryData.setText(journeyHistoryText);
                }

                if (journeyOngoingText.equals("")) {
                    JourneyCurrentData.setText(defaultOngoingText);
                } else {
                    JourneyCurrentData.setText(journeyOngoingText);
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(getApplicationContext(), "Error: " + fault.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public String getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(JourneyHistory.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0);

            return add;
            // Toast.makeText(this, "Address=>" + add,
            // Toast.LENGTH_SHORT).show();

            // TennisAppActivity.showDialog(add);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bn_new_journey:
                String userObjectId = UserIdStorageFactory.instance().getStorage().get();
                String whereClause = "user_id = '" + userObjectId + "' and status in ('ongoing', 'paused')";
                DataQueryBuilder queryBuilder = DataQueryBuilder.create();
                queryBuilder.setWhereClause(whereClause);

                Backendless.Data.of("User_Journey").find(queryBuilder, new AsyncCallback<List<Map>>() {
                    @Override
                    public void handleResponse(List<Map> response) {
                        if (response.size() > 0) {
                            Toast.makeText(getApplicationContext(), "You need to end your current journey to start a new journey", Toast.LENGTH_LONG).show();
                        } else {
                            Intent intent = new Intent(JourneyHistory.this, MapsActivity.class);
                            startActivity(intent);
                            JourneyHistory.this.finish();
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(getApplicationContext(), "Error: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
                break;
            case R.id.bn_pause:
                userObjectId = UserIdStorageFactory.instance().getStorage().get();
                whereClause = "user_id = '" + userObjectId + "' and status = 'ongoing'";
                queryBuilder = DataQueryBuilder.create();
                queryBuilder.setWhereClause(whereClause);

                Backendless.Data.of("User_Journey").find(queryBuilder, new AsyncCallback<List<Map>>() {
                    @Override
                    public void handleResponse(List<Map> response) {
                        if (response.size() == 0) {
                            Toast.makeText(getApplicationContext(), "You don't have an ongoing journey to pause", Toast.LENGTH_LONG).show();
                        } else {
                            Intent transferIntent = new Intent(JourneyHistory.this, PopUp.class);
                            transferIntent.putExtra("BUTTON", "pause");
                            startActivity(transferIntent);
                            JourneyHistory.this.finish();
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(getApplicationContext(), "Error: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
                break;
            case R.id.bn_abort:
                userObjectId = UserIdStorageFactory.instance().getStorage().get();
                whereClause = "user_id = '" + userObjectId + "' and status in ('ongoing', 'paused')";
                queryBuilder = DataQueryBuilder.create();
                queryBuilder.setWhereClause(whereClause);

                Backendless.Data.of("User_Journey").find(queryBuilder, new AsyncCallback<List<Map>>() {
                    @Override
                    public void handleResponse(List<Map> response) {
                        if (response.size() == 0) {
                            Toast.makeText(getApplicationContext(), "You don't have an ongoing journey to abort", Toast.LENGTH_LONG).show();
                        } else {
                            Intent transferIntent = new Intent(JourneyHistory.this, PopUp.class);
                            transferIntent.putExtra("BUTTON", "abort");
                            startActivity(transferIntent);
                            JourneyHistory.this.finish();
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(getApplicationContext(), "Error: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
                break;
            case R.id.bn_panic:
                userObjectId = UserIdStorageFactory.instance().getStorage().get();
                whereClause = "user_id = '" + userObjectId + "' and status in ('ongoing', 'paused')";
                queryBuilder = DataQueryBuilder.create();
                queryBuilder.setWhereClause(whereClause);

                Backendless.Data.of("User_Journey").find(queryBuilder, new AsyncCallback<List<Map>>() {
                    @Override
                    public void handleResponse(List<Map> response) {
                        if (response.size() == 0) {
                            Toast.makeText(getApplicationContext(), "You don't have an ongoing journey to panic", Toast.LENGTH_LONG).show();
                        } else {
                            String userObjectId = UserIdStorageFactory.instance().getStorage().get();
                            Backendless.UserService.findById(userObjectId, new AsyncCallback<BackendlessUser>() {
                                @Override
                                public void handleResponse(BackendlessUser response) {
                                    System.out.println("USER" + response.toString());
                                    String name = (String) response.getProperty("name");
                                    MapsActivity mapsActivity = new MapsActivity();
                                    mapsActivity.journeyStatusChanged("panic", name.trim() + " has pressed the panic button", false);
                                    Toast.makeText(getApplicationContext(), "You have pressed the panic button", Toast.LENGTH_LONG).show();
                                    JourneyHistory.this.finish();
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    Toast.makeText(getApplicationContext(), "ERROR: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(getApplicationContext(), "Error: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
                break;
            case R.id.bn_dest:
                userObjectId = UserIdStorageFactory.instance().getStorage().get();
                whereClause = "user_id = '" + userObjectId + "' and status in ('ongoing', 'paused')";
                queryBuilder = DataQueryBuilder.create();
                queryBuilder.setWhereClause(whereClause);

                Backendless.Data.of("User_Journey").find(queryBuilder, new AsyncCallback<List<Map>>() {
                    @Override
                    public void handleResponse(List<Map> response) {
                        if (response.size() == 0) {
                            Toast.makeText(getApplicationContext(), "You don't have an ongoing journey to end", Toast.LENGTH_LONG).show();
                        } else {
                            String userObjectId = UserIdStorageFactory.instance().getStorage().get();
                            Backendless.UserService.findById(userObjectId, new AsyncCallback<BackendlessUser>() {
                                @Override
                                public void handleResponse(BackendlessUser response) {
                                    System.out.println("USER" + response.toString());
                                    String name = (String) response.getProperty("name");
                                    MapsActivity mapsActivity = new MapsActivity();
                                    mapsActivity.journeyStatusChanged("destination", name.trim() + " has reached destination", false);
                                    PopUp popUp = new PopUp();
                                    popUp.updateJourneyStatus("destination");
//                            mapsActivity.handleAlarmManager();
                                    JourneyHistory.this.finish();
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    Toast.makeText(getApplicationContext(), "ERROR: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(getApplicationContext(), "Error: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
                break;
            case R.id.bn_resume:
                userObjectId = UserIdStorageFactory.instance().getStorage().get();
                whereClause = "user_id = '" + userObjectId + "' and status = 'paused'";
                queryBuilder = DataQueryBuilder.create();
                queryBuilder.setWhereClause(whereClause);

                Backendless.Data.of("User_Journey").find(queryBuilder, new AsyncCallback<List<Map>>() {
                    @Override
                    public void handleResponse(List<Map> response) {
                        if (response.size() == 0) {
                            Toast.makeText(getApplicationContext(), "You don't have an ongoing journey to resume", Toast.LENGTH_LONG).show();
                        } else {
                            String userObjectId = UserIdStorageFactory.instance().getStorage().get();
                            Backendless.UserService.findById(userObjectId, new AsyncCallback<BackendlessUser>() {
                                @Override
                                public void handleResponse(BackendlessUser response) {
                                    System.out.println("USER" + response.toString());
                                    String name = (String) response.getProperty("name");
                                    MapsActivity mapsActivity = new MapsActivity();
                                    mapsActivity.journeyStatusChanged("resume", name.trim() + " has resumed the journey", false);
                                    PopUp popUp = new PopUp();
                                    popUp.updateJourneyStatus("resume");
                                    Toast.makeText(getApplicationContext(), "Your journey has resumed", Toast.LENGTH_LONG).show();
                                    JourneyHistory.this.finish();
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    Toast.makeText(getApplicationContext(), "ERROR: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(getApplicationContext(), "Error: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
                break;
        }
    }
}
