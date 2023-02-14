package com.example.gethomesafely;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.local.UserIdStorageFactory;

public class About extends AppCompatActivity {

    String name, email, phone;
    Button LogoutButton;
    TextView NameView, EmailView, PhoneView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout(width, height);

        NameView = findViewById(R.id.name);
        EmailView = findViewById(R.id.email);
        PhoneView = findViewById(R.id.phone);

        String userObjectId = UserIdStorageFactory.instance().getStorage().get();
        Backendless.UserService.findById(userObjectId, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser response) {
                System.out.println("USER" + response.toString());
                name = (String) response.getProperty("name");
                phone = (String) response.getProperty("phone");
                email = (String) response.getProperty("email");

                NameView.setText(name);
                PhoneView.setText(phone);
                EmailView.setText(email);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(About.this, "ERROR: " + fault.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        LogoutButton = findViewById(R.id.bn_logout);
        LogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Backendless.UserService.logout(new AsyncCallback<Void>() {
                    @Override
                    public void handleResponse(Void response) {
                        Toast.makeText(About.this, "Logged out Successfully", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(About.this, Login.class));
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(About.this, "Error: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
