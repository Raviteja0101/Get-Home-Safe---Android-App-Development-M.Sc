package com.example.gethomesafely;

import android.app.Application;

import com.backendless.Backendless;

public class ApplicationClass extends Application {

    public static final String APPLICATION_ID = "177E5ACB-87D1-3C56-FF11-7AFE824D3800";
    public static final String API_KEY = "CD7988DD-2940-A7B5-FF6E-C4BC00F09300";
    public static final String SERVER_URL = "https://api.backendless.com";

    @Override
    public void onCreate() {
        super.onCreate();

        Backendless.setUrl( SERVER_URL );
        Backendless.initApp( getApplicationContext(),
                APPLICATION_ID,
                API_KEY );

    }
}
