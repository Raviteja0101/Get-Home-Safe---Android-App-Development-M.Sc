package com.example.gethomesafely;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.DeviceRegistration;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.messaging.MessageStatus;
import com.backendless.push.DeviceRegistrationResult;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class HomePage extends AppCompatActivity {

    String destination, source;
    private LinearLayout Prof_Section, ContactListButton, MapsButton, EditFollowerButton, AboutButton;
    private TextView Name, Email, Phone;
    Button journey_button, SignOut;
    private ImageView Prof_Pic;

    EditText destinationInput, source_input;
    private LinearLayout Journey_Section;
    private TextView Distance, Duration, Travel_Mode, Instructions;

    private View mProgressView;
    private TextView tvLoad;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

//        mProgressView = findViewById(R.id.login_progress);
//        tvLoad = findViewById(R.id.tvLoad);

//        Prof_Section = (LinearLayout)findViewById(R.id.prof_section);
//        Name = (TextView)findViewById(R.id.name);
//        Email = (TextView)findViewById(R.id.email);
//        Phone = (TextView)findViewById(R.id.phoneDisplay);
//        Prof_Pic = (ImageView)findViewById(R.id.prof_pic);
//        SignOut = (Button)findViewById(R.id.bn_logout);
//        SignOut.setVisibility(View.GONE);

        Intent transferIntentData = getIntent();
        String name = transferIntentData.getStringExtra("NAME");
        String email = transferIntentData.getStringExtra("EMAIL");
        String img_url = transferIntentData.getStringExtra("IMAGE");
        String phone = transferIntentData.getStringExtra("PHONE");

//        Name.setText(name);
//        Email.setText(email);
//        Phone.setText(phone);
//        if (img_url != null && !img_url.isEmpty() && !img_url.equals("null")) {
//            Glide.with(this).load(img_url).into(Prof_Pic);
//        }
//
//        updateUI();

//        Journey_Section = (LinearLayout)findViewById(R.id.journey_section);
//        Journey_Section.setVisibility(View.GONE);
//        Distance = (TextView)findViewById(R.id.distance);
//        Duration = (TextView)findViewById(R.id.duration);
//        Travel_Mode = (TextView)findViewById(R.id.travel_mode);
//        Instructions = (TextView)findViewById(R.id.instructions);
//
//        destinationInput = findViewById(R.id.destination_input);
//        source_input =  findViewById(R.id.source_input);

        journey_button = findViewById(R.id.bn_journey);
        journey_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotification("", "fd_DBmEgVQ4:APA91bF_RhIpoEhbk1e-X5QZbIbnFw6Jhhi8rB01fwy3Wrqpif9qEd6zTNYaxTBNGZQW2N4gixMeFzrWEbonbUkaIewzBPRlRHBXSflXGV0fswSriquK9PhiyWkHHDhnOH2pQ4xizK2y", "test", "");

//                destination = destinationInput.getText().toString().trim();
//                source = source_input.getText().toString().trim();
//                System.out.println(destination);
//                System.out.println(source);
//
//                int len_destination = destination.length();
//                int len_source = source.length();
//
//                if(len_destination == 0 || len_source == 0) {
//                    Toast.makeText(HomePage.this, "Source or Destination not filled", Toast.LENGTH_SHORT).show();
//                } else {
//                    request();
//                }
            }
        });

        ContactListButton = findViewById(R.id.getContacts);
        ContactListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showProgress(true);
                startActivity(new Intent(HomePage.this, Contacts.class));
            }
        });

        MapsButton = findViewById(R.id.getMaps);
        MapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomePage.this, JourneyHistory.class));
            }
        });

        EditFollowerButton = findViewById(R.id.remove_followers);
        EditFollowerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomePage.this, EditFollower.class));
            }
        });

        AboutButton = findViewById(R.id.bn_info);
        AboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomePage.this, About.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        showProgress(false);
    }

//    private void request() {
//        AsyncHttpClient client = new AsyncHttpClient();
//        RequestParams params = new RequestParams();
//        params.put("source", source);
//        params.put("destination", destination);
//        params.put("mode", "walking");
//        client.get("http://192.168.43.155:3000/start_journey", params, new JsonHttpResponseHandler() {
//
//            @Override
//            public void onStart() {
//                // called before request is started
//            }
//
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                // called when response HTTP status is "200 OK"
//                System.out.println(response.toString());
////                Toast.makeText(HomePage.this, response.toString(), Toast.LENGTH_SHORT).show();
//                try {
//                    Distance.setText("Distance: " + response.getString("distance"));
//                    Duration.setText("Duration: " + response.getString("duration"));
//                    Travel_Mode.setText("Travel_Mode: " + response.getString("travel_mode"));
//                    JSONArray steps = response.getJSONArray("steps");
//                    String stepsData = "";
//                    for (int i=0; i < steps.length(); i++) {
//                        JSONObject step = steps.getJSONObject(i);
//                        stepsData += "Instructions: " + Html.fromHtml(step.getString("html_instructions")).toString() + "\n";
//                        stepsData += "Distance: " + step.getJSONObject("distance").getString("text") + "\n";
//                        stepsData += "Duration: " + step.getJSONObject("duration").getString("text") + "\n\n";
//                    }
//                    Instructions.setText("Instructions: " + stepsData);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                Journey_Section.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
//                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
//            }
//
//            @Override
//            public void onRetry(int retryNo) {
//                // called when request is retried
//            }
//        });
//    }

    private void updateUI() {
        Prof_Section.setVisibility(View.VISIBLE);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });

            tvLoad.setVisibility(show ? View.VISIBLE : View.GONE);
            tvLoad.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    tvLoad.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            tvLoad.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    public void sendNotification(final String template, String deviceToken, String button, String message) {
        if (deviceToken.equals("")) {
            Backendless.Messaging.getDeviceRegistration(new AsyncCallback<DeviceRegistration>() {
                @Override
                public void handleResponse(DeviceRegistration response) {
                    Backendless.Messaging.pushWithTemplate(template, new AsyncCallback<MessageStatus>() {
                        @Override
                        public void handleResponse(MessageStatus response) {
                            System.out.println(response.toString());
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {

                        }
                    });
                }

                @Override
                public void handleFault(BackendlessFault fault) {

                }
            });
        } else {
            String title = null;
            switch (button) {
                case "pause":
                    title = "Journey Paused";
                    break;
                case "abort":
                    title = "Journey Aborted";
                    break;
                case "panic":
                    title = "EMERGENCY";
                    break;
                case "resume":
                    title = "Journey Resumed";
                    break;
                case "progress":
                    title = "Journey Progress";
                    break;
                case "start":
                    title = "Journey Started";
                    break;
                case "notified":
                    title = "Followers Notified";
                    break;
                case "destination":
                    title = "Destination reached";
                    break;
                default:
                    title = "This is a test title";
                    message = "This is a test message";
                    break;
            }

            AsyncHttpClient client = new AsyncHttpClient();
            client.addHeader("Content-Type", "application/json");
            client.addHeader("Authorization", "key=AAAA3xILHvA:APA91bES40ae--DfsgAWmfT73yg5TanTv6zSv3YlHdyElIQPm3uoe_xSPEHmtvzgpB4M_qsdJJCTZfbLS6ARoTFQL9BPyASrfEKbP2M3zwaMhp-ju1LAmAdXMJqil7GAQ3djB7GYefvN");

            JSONObject requestObject = new JSONObject();
            JSONObject notificationObject = new JSONObject();// Included object
            try {
                notificationObject.put("title", title);
                notificationObject.put("body", message);
                requestObject.put("notification", notificationObject);
                requestObject.put("to", deviceToken);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            StringEntity entity = null;
            try {
                entity = new StringEntity(requestObject.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            Log.i("REQPARAMS", entity.toString());
            client.post(HomePage.this, "https://fcm.googleapis.com/fcm/send", entity, "application/json", new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    System.out.println("Success" + response.toString());
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    System.out.println("Throwable1" + throwable.toString());
                    System.out.println("Error1" + responseString.toString());
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    System.out.println("Throwable" + throwable.toString());
                    System.out.println("Error" + errorResponse.toString());
                }
            });
        }
    }

    public void logout() {
        Backendless.UserService.logout(new AsyncCallback<Void>() {
            @Override
            public void handleResponse(Void response) {
                Toast.makeText(HomePage.this, "Logged out Successfully", Toast.LENGTH_LONG).show();
                startActivity(new Intent(HomePage.this, Login.class));
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(HomePage.this, "Error: " + fault.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}