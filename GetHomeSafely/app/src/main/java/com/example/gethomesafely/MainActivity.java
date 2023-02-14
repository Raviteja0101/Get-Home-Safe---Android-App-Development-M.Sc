package com.example.gethomesafely;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONObject;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.loopj.android.http.*;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private LinearLayout Prof_Section;
    private Button SignOut, Http;
    private SignInButton SignIn;
    private TextView Name, Email;
    private ImageView Prof_Pic;
    private GoogleApiClient googleApiClient;
    private static final int REQ_CODE = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);
        Prof_Section = (LinearLayout)findViewById(R.id.prof_section);
        SignOut = (Button)findViewById(R.id.bn_logout);
        Http = (Button)findViewById(R.id.bn_http);
        Http.setVisibility(View.GONE);
        SignIn = (SignInButton)findViewById(R.id.bn_signin);
        Name = (TextView)findViewById(R.id.name);
        Email = (TextView)findViewById(R.id.email);
        Prof_Pic = (ImageView)findViewById(R.id.prof_pic);
        SignIn.setOnClickListener(this);
        SignOut.setOnClickListener(this);
        Http.setOnClickListener(this);

        Prof_Section.setVisibility(View.GONE);

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions).build();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bn_signin:
                signIn();
                break;

            case R.id.bn_logout:
                signOut();
                break;

            case R.id.bn_http:
//                request();
                break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void signIn() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent, REQ_CODE);
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                updateUI(false);
            }
        });
    }

//    private void request() {
//        final String[] token = new String[1];
//        FirebaseApp.initializeApp(this);
//        FirebaseInstanceId.getInstance().getInstanceId()
//            .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
//                @Override
//                public void onComplete(@NonNull Task<InstanceIdResult> task) {
//                    if (!task.isSuccessful()) {
//                        Log.w("TAG", "getInstanceId failed", task.getException());
//                        return;
//                    }
//
//                    // Get new Instance ID token
//                    token[0] = task.getResult().getToken();
//
//                    // Log and toast
//                    Log.d("TAG", token[0]);
//                    AsyncHttpClient client = new AsyncHttpClient();
//                    RequestParams params = new RequestParams();
//                    params.put("token", token[0]);
//                    params.put("source", "Hasselbachplatz, Magdeburg, Germany");
//                    params.put("destination", "Opernhaus, Magdeburg, Germany");
//                    params.put("mode", "walking");
//                    params.put("mode", "walking");
//                    client.get("http://192.168.43.155:3000", params, new JsonHttpResponseHandler() {
//
//                        @Override
//                        public void onStart() {
//                            // called before request is started
//                        }
//
//                        @Override
//                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                            // called when response HTTP status is "200 OK"
//                            System.out.println(response.toString());
//                        }
//
//                        @Override
//                        public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
//                            // called when response HTTP status is "4XX" (eg. 401, 403, 404)
//                        }
//
//                        @Override
//                        public void onRetry(int retryNo) {
//                            // called when request is retried
//                        }
//                    });
//                    Toast.makeText(MainActivity.this, token[0], Toast.LENGTH_SHORT).show();
//                }
//            });
//    }

    private void handleResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            String name = account.getDisplayName();
            String email = account.getEmail();
            String img_url = account.getPhotoUrl().toString();

            User user = new User();
            user.setName(name);
            user.setEmail(email);

            Backendless.Data.of(User.class).save(user, new AsyncCallback<User>() {
                @Override
                public void handleResponse(User response) {

                }

                @Override
                public void handleFault(BackendlessFault fault) {

                }
            });

            Intent transferIntent = new Intent(this, HomePage.class);
            transferIntent.putExtra("NAME", name);
            transferIntent.putExtra("EMAIL", email);

            if (img_url != null && !img_url.isEmpty() && !img_url.equals("null")) {
                Glide.with(this).load(img_url).into(Prof_Pic);
                transferIntent.putExtra("IMAGE", img_url);
            }

            startActivity(transferIntent);

//            Name.setText(name);
//            Email.setText(email);
//
//            updateUI(true);
        }
        else {
            updateUI(false);
        }
    }

    private void updateUI(boolean isLogIn) {
        if (isLogIn) {
            Prof_Section.setVisibility(View.VISIBLE);
            SignIn.setVisibility(View.GONE);
        }
        else {
            Prof_Section.setVisibility(View.GONE);
            SignIn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            startActivity(new Intent(MainActivity.this, HomePage.class));
            handleResult(result);
        }
    }
}

