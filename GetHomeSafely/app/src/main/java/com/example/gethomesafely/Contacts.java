package com.example.gethomesafely;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.persistence.local.UserIdStorageFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Contacts extends AppCompatActivity {

    public  static final int RequestPermissionCode = 1;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view_with_checkbox);

        Button selectButtons = findViewById(R.id.list_select_all);
        selectButtons.setVisibility(View.GONE);

        selectButtons = findViewById(R.id.list_remove_selected_rows);
        selectButtons.setVisibility(View.GONE);

        selectButtons = findViewById(R.id.list_select_none);
        selectButtons.setVisibility(View.GONE);

        selectButtons = findViewById(R.id.list_select_reverse);
        selectButtons.setVisibility(View.GONE);

        setTitle("Select Followers");

        // Get listview checkbox.
        final ListView listViewWithCheckbox = (ListView)findViewById(R.id.list_view_with_checkbox);

        // Initiate listview data.
        final List<ListViewItemDTO> initItemList = this.getInitViewItemDtoList();

        // Create a custom list view adapter with checkbox control.
        final ListViewItemCheckboxBaseAdapter listViewDataAdapter = new ListViewItemCheckboxBaseAdapter(getApplicationContext(), initItemList);

        listViewDataAdapter.notifyDataSetChanged();

        // Set data adapter to list view.
        listViewWithCheckbox.setAdapter(listViewDataAdapter);

        // When list view item is clicked.
        listViewWithCheckbox.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int itemIndex, long l) {
                // Get user selected item.
                Object itemObject = adapterView.getAdapter().getItem(itemIndex);

                // Translate the selected item to DTO object.
                final ListViewItemDTO itemDto = (ListViewItemDTO)itemObject;

                // Get the checkbox.
                CheckBox itemCheckbox = (CheckBox) view.findViewById(R.id.list_view_item_checkbox);

                // Reverse the checkbox and clicked item check state.
                if(itemDto.isChecked())
                {
                    itemCheckbox.setChecked(false);
                    itemDto.setChecked(false);
                }else
                {
                    itemCheckbox.setChecked(true);
                    itemDto.setChecked(true);
                }
                String data = itemDto.getItemText();
                final String name = data.split(", ")[0];
                String phone = data.split(", ")[1];
                phone = phone.replaceAll("\\s+","");

                if (phone.substring(0, 1).equals("0")) {
                    phone = phone.substring(1);
                }
                if (phone.substring(0, 3).equals("+91") || phone.substring(0, 3).equals("+49")) {
                    phone = phone.substring(3);
                }

                final String userObjectId = UserIdStorageFactory.instance().getStorage().get();

                String whereClause = "phone='" + phone + "' and user_id='" + userObjectId + "'";
                DataQueryBuilder queryBuilder = DataQueryBuilder.create();
                queryBuilder.setWhereClause(whereClause);

                final String finalPhone = phone;
                Backendless.Data.of("User_Contacts").find(queryBuilder, new AsyncCallback<List<Map>>() {
                    @Override
                    public void handleResponse(List<Map> response) {
                        Log.d("DEBUGTAG", "handleResponse: " + response.toString());
                        if (response.size() > 0) {
                            Toast.makeText(getApplicationContext(), "Contact already added to follower list", Toast.LENGTH_SHORT).show();
                        } else {
                            HashMap contact = new HashMap();
                            contact.put( "phone", finalPhone);
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
                                    Toast.makeText(getApplicationContext(), "ERROR: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                            Toast.makeText(getApplicationContext(), "Contact added to follower list: " + itemDto.getItemText(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(getApplicationContext(), "ERROR: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Click this button to select all listview items with checkbox checked.
        Button selectAllButton = (Button)findViewById(R.id.list_select_all);
        selectAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int size = initItemList.size();
                for(int i=0;i<size;i++)
                {
                    ListViewItemDTO dto = initItemList.get(i);
                    dto.setChecked(true);
                }

                listViewDataAdapter.notifyDataSetChanged();
            }
        });

        // Click this button to disselect all listview items with checkbox unchecked.
        Button selectNoneButton = (Button)findViewById(R.id.list_select_none);
        selectNoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int size = initItemList.size();
                for(int i=0;i<size;i++)
                {
                    ListViewItemDTO dto = initItemList.get(i);
                    dto.setChecked(false);
                }

                listViewDataAdapter.notifyDataSetChanged();
            }
        });

        // Click this button to reverse select listview items.
        Button selectReverseButton = (Button)findViewById(R.id.list_select_reverse);
        selectReverseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int size = initItemList.size();
                for(int i=0;i<size;i++)
                {
                    ListViewItemDTO dto = initItemList.get(i);

                    if(dto.isChecked())
                    {
                        dto.setChecked(false);
                    }else {
                        dto.setChecked(true);
                    }
                }

                listViewDataAdapter.notifyDataSetChanged();
            }
        });

        // Click this button to remove selected items from listview.
        Button selectRemoveButton = (Button)findViewById(R.id.list_remove_selected_rows);
        selectRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog alertDialog = new AlertDialog.Builder(Contacts.this).create();
                alertDialog.setMessage("Are you sure to remove selected listview items?");

                alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        int size = initItemList.size();
                        for(int i=0;i<size;i++)
                        {
                            ListViewItemDTO dto = initItemList.get(i);

                            if(dto.isChecked())
                            {
                                initItemList.remove(i);
                                i--;
                                size = initItemList.size();
                            }
                        }

                        listViewDataAdapter.notifyDataSetChanged();
                    }
                });

                alertDialog.show();
            }
        });

    }


    // Return an initialize list of ListViewItemDTO.
    private List<ListViewItemDTO> getInitViewItemDtoList()
    {
        List<ListViewItemDTO> ret = new ArrayList<ListViewItemDTO>();

        EnableRuntimePermission();
        // Fetch device contacts
        cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC");

        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phonenumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            phonenumber = phonenumber.replaceAll("\\s+","");

            if (phonenumber.substring(0, 1).equals("0")) {
                phonenumber = phonenumber.substring(1);
            }
            if (phonenumber.substring(0, 3).equals("+91") || phonenumber.substring(0, 3).equals("+49")) {
                phonenumber = phonenumber.substring(3);
            }

            if (phonenumber.contains("*") || phonenumber.contains("#") || phonenumber.length() < 10) {
                continue;
            } else {
                ListViewItemDTO dto = new ListViewItemDTO();
                dto.setChecked(false);
                dto.setItemText(name + ", " + phonenumber);
                ret.add(dto);
            }
        }
        cursor.close();
        return ret;
    }

    private void EnableRuntimePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                Contacts.this,
                Manifest.permission.READ_CONTACTS))
        {

            Toast.makeText(Contacts.this,"CONTACTS permission allows us to Access CONTACTS app", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(Contacts.this,new String[]{
                    Manifest.permission.READ_CONTACTS}, RequestPermissionCode);

        }
    }
}