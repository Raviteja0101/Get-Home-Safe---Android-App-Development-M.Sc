package com.example.gethomesafely;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.persistence.local.UserIdStorageFactory;

import java.util.List;
import java.util.Map;

public class PopUp extends AppCompatActivity {

    EditText PopUpText;
    Button FinishButton;
    String TAG = "POPUP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_up);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width * 0.8), (int) (height * 0.6));

        PopUpText = findViewById(R.id.popup_text);

        FinishButton = findViewById(R.id.bn_popup);
        FinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PopUpText.getText().toString().trim().isEmpty()) {
                    Toast.makeText(PopUp.this, "Enter a message to notify your followers", Toast.LENGTH_LONG).show();
                } else {
                    String userObjectId = UserIdStorageFactory.instance().getStorage().get();
                    Backendless.UserService.findById(userObjectId, new AsyncCallback<BackendlessUser>() {
                        @Override
                        public void handleResponse(BackendlessUser response) {
                            System.out.println("USER" + response.toString());
                            String name = (String) response.getProperty("name");

                            Intent transferIntentData = getIntent();
                            final String button = transferIntentData.getStringExtra("BUTTON");
                            MapsActivity mapsActivity = new MapsActivity();
                            mapsActivity.journeyStatusChanged(button, name.trim() + " says, " + PopUpText.getText().toString().trim(), false);

                            updateJourneyStatus(button);

                            PopUp.this.finish();
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Toast.makeText(getApplicationContext(), "ERROR: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    public void updateJourneyStatus(final String button) {
        String userObjectId = UserIdStorageFactory.instance().getStorage().get();
        String whereClause = "user_id = '" + userObjectId + "' and status in ('ongoing', 'paused')";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);

        Backendless.Data.of("User_Journey").find(queryBuilder, new AsyncCallback<List<Map>>() {
            @Override
            public void handleResponse(List<Map> response) {
                Log.d(TAG, "handleResponse: " + response.toString());
                for (int i = 0; i < response.size(); i++) {
                    String status = "";
                    if (button.equals("pause")) {
                        status = "paused";
                    }

                    if (button.equals("abort")) {
                        status = "aborted";
                    }

                    if (button.equals("destination")) {
                        status = "ended";
                    }

                    if (button.equals("resume")) {
                        status = "ongoing";
                    }

                    response.get(i).put("status", status);
                    Log.d(TAG, "handleResponse1: " + response.get(i));
                    final String finalStatus = status;
                    Backendless.Persistence.of("User_Journey").save(response.get(i), new AsyncCallback<Map>() {
                        @Override
                        public void handleResponse(Map response) {
                            Log.d(TAG, "handleResponse2: " + response);
                            if (finalStatus.equals("paused")) {
                                Toast.makeText(getApplicationContext(), "Your journey status has been updated to paused", Toast.LENGTH_LONG).show();
                            } else if (finalStatus.equals("aborted")) {
                                Toast.makeText(getApplicationContext(), "Your journey has been cancelled", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Toast.makeText(getApplicationContext(), "Error: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(getApplicationContext(), "Error: " + fault.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
