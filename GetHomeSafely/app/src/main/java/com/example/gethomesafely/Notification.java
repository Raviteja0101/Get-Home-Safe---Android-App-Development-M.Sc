package com.example.gethomesafely;

import android.util.Log;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.DeviceRegistration;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.messaging.DeliveryOptions;
import com.backendless.messaging.MessageStatus;
import com.backendless.messaging.PublishOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class Notification {
    public void sendNotification(final String template, String deviceToken) {
        // do not forget to call Backendless.initApp in the app initialization code

        Backendless.Data.of("DeviceRegistration").find(new AsyncCallback<List<Map>>() {
            @Override
            public void handleResponse(List<Map> response) {
                System.out.println(response.toString());
            }

            @Override
            public void handleFault(BackendlessFault fault) {

            }
        });

        if (deviceToken.isEmpty()) {
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
            AsyncHttpClient client = new AsyncHttpClient();
            client.addHeader("Content-Type", "application/json");
            client.addHeader("Authorization", "key=AAAA3xILHvA:APA91bES40ae--DfsgAWmfT73yg5TanTv6zSv3YlHdyElIQPm3uoe_xSPEHmtvzgpB4M_qsdJJCTZfbLS6ARoTFQL9BPyASrfEKbP2M3zwaMhp-ju1LAmAdXMJqil7GAQ3djB7GYefvN");

            RequestParams params = new RequestParams();
            JSONObject requestObject = new JSONObject();
            JSONObject notificationObject = new JSONObject();// Included object
            try {
                notificationObject.put("title", "Test Title");
                notificationObject.put("body", "Test Body");
//                requestObject.put("notification", notificationObject);
//                requestObject.put("to", deviceToken);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            params.put("notification", notificationObject);
            params.put("to", deviceToken);
//            params.put("body", requestObject);
            Log.i("REQPARAMS", params.toString());
            client.post("https://fcm.googleapis.com/fcm/send/", params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    System.out.println("Success" + response.toString());
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    System.out.println("Throwable" + throwable.toString());
                    System.out.println("Error" + errorResponse.toString());
                }
            });
            // Code not working
//            DeliveryOptions deliveryOptions = new DeliveryOptions();
//            deliveryOptions.setPushSinglecast( Arrays.asList( deviceToken ) );
//
//            Log.i("DEVICETOKEN", deviceToken);
//
//            PublishOptions publishOptions = new PublishOptions();
//            publishOptions.putHeader( "android-ticker-text", "You just got a private push notification!" );
//            publishOptions.putHeader( "android-content-title", "This is a notification title" );
//            publishOptions.putHeader( "android-content-text", "Push Notifications are cool" );
//
//            Backendless.Messaging.publish("default", "this is a private message", publishOptions, deliveryOptions, new AsyncCallback<MessageStatus>() {
//                @Override
//                public void handleResponse(MessageStatus response) {
//                    System.out.println("Success:" + response.toString());
//                }
//
//                @Override
//                public void handleFault(BackendlessFault fault) {
//                    System.out.println("Error:" + fault.toString());
//                }
//            });

//            Backendless.Messaging.publish(template,
//                    deliveryOptions, new AsyncCallback<MessageStatus>() {
//                        @Override
//                        public void handleResponse(MessageStatus response) {
//                            System.out.println(response.toString());
//                        }
//
//                        @Override
//                        public void handleFault(BackendlessFault fault) {
//                            System.out.println(fault.toString());
//                        }
//                    });
        }
    }

    public void getDeviceRegistration( AsyncCallback<DeviceRegistration> responder ) {

    }
}
