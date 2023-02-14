package com.example.gethomesafely;

import android.Manifest;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.local.UserIdStorageFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactList extends AppCompatActivity {

    ListView listView;
    ArrayList<String> StoreContacts;
    ArrayAdapter<String> arrayAdapter;
    Cursor cursor;
    String name, phonenumber;
    public  static final int RequestPermissionCode = 1;
    Button contactButton, SaveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        listView = (ListView)findViewById(R.id.contactlist);
        StoreContacts = new ArrayList<String>();

//        EnableRuntimePermission();

        GetContactsIntoArrayList();

        arrayAdapter = new ArrayAdapter<String>(
                ContactList.this,
                R.layout.activity_contact_list,
                R.id.textView, StoreContacts
        );

        listView.setAdapter(arrayAdapter);
        SaveData = findViewById(R.id.bn_saveData);
        SaveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userObjectId = UserIdStorageFactory.instance().getStorage().get();
                System.out.println(userObjectId);

//                List<Map<String, Object>> userFavContacts = new ArrayList<>();

                for(int i=0; i<5; i++) {
                    String name = StoreContacts.get(i).split(":")[0].trim();
                    String phone = StoreContacts.get(i).split(":")[1].trim();

                    HashMap contact = new HashMap();
                    contact.put( "phone", phone );
                    contact.put( "name", name );
                    contact.put( "user_id", userObjectId);

                    Backendless.Data.of( "User_Contacts" ).save( contact, new AsyncCallback<Map>() {
                        public void handleResponse( Map response )
                        {
                            // new Contact instance has been saved
                            Log.i( "MYAPP", "Objects have been saved" );
                        }

                        public void handleFault( BackendlessFault fault )
                        {
                            // an error has occurred, the error code can be retrieved with fault.getCode()
                            Log.i( "MYAPP", "Server reported an error " + fault );
                        }
                    });
                }

//                Backendless.Data.of( "User_Contacts" ).create( userFavContacts, new AsyncCallback<List<String>>()
//                {
//                    @Override
//                    public void handleResponse( List<String> response )
//                    {
//                        Log.i( "MYAPP", "Objects have been saved" );
//                    }
//
//                    @Override
//                    public void handleFault( BackendlessFault fault )
//                    {
//                        Log.i( "MYAPP", "Server reported an error " + fault );
//                    }
//                } );
            }
        });
    }

    public void GetContactsIntoArrayList(){
        EnableRuntimePermission();

        cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null, null, null);

        while (cursor.moveToNext()) {

            name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            phonenumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            if (phonenumber.contains("*") || phonenumber.contains("#") || phonenumber.length() < 10) {
                continue;
            } else {
                StoreContacts.add(name + " "  + ":" + " " + phonenumber);
            }
        }

        cursor.close();

    }

    private void EnableRuntimePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                ContactList.this,
                Manifest.permission.READ_CONTACTS))
        {

            Toast.makeText(ContactList.this,"CONTACTS permission allows us to Access CONTACTS app", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(ContactList.this,new String[]{
                    Manifest.permission.READ_CONTACTS}, RequestPermissionCode);

        }
    }
}
