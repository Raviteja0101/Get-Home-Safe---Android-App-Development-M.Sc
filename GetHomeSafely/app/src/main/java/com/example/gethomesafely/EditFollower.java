package com.example.gethomesafely;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.persistence.local.UserIdStorageFactory;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditFollower extends AppCompatActivity {

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

        TextView title = findViewById(R.id.toolbar_title);
        title.setText("Edit Followers");

        // Get listview checkbox.
        final ListView listViewWithCheckbox = findViewById(R.id.list_view_with_checkbox);

        // Initiate listview data.
        final List<ListViewItemDTO> initItemList = new ArrayList<ListViewItemDTO>();

        // Fetch user contacts
        String userObjectId = UserIdStorageFactory.instance().getStorage().get();
        String whereClause = "user_id = '" + userObjectId +"'";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause( whereClause );

        Toast.makeText(getApplicationContext(), "Please wait while data is loading", Toast.LENGTH_LONG).show();

        Backendless.Data.of("User_Contacts").find(queryBuilder, new AsyncCallback<List<Map>>() {
            @Override
            public void handleResponse(List<Map> response) {
                int len = response.size();
                if (len == 0) {
                    Toast.makeText(getApplicationContext(), "No followers to show", Toast.LENGTH_LONG).show();
                } else {
                    Log.d("RESPONSE SIZE", String.valueOf(len));
                    for (int i = 0; i < len; i++) {
                        String name = (String) response.get(i).get("name");
                        String phone = (String) response.get(i).get("phone");
                        System.out.println("DATA:" + name + ", " + phone);
                        Log.d("Ver", "handleResponse: " + name);
                        ListViewItemDTO dto = new ListViewItemDTO();
                        dto.setChecked(false);
                        dto.setItemText(name + ", " + phone);
                        Log.d("DTOOBJECT", dto.toString());
                        initItemList.add(dto);
                    }
                    Log.d("DTO ITEMS", initItemList.toString());
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
                            ListViewItemDTO itemDto = (ListViewItemDTO)itemObject;

                            // Get the checkbox.
                            CheckBox itemCheckbox = view.findViewById(R.id.list_view_item_checkbox);

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
                            String phone = data.split(", ")[1];
                            phone = phone.replaceAll("\\s+","");
                            final String name = data.split(", ")[0];

                            String userObjectId = UserIdStorageFactory.instance().getStorage().get();

                            String whereClause = "phone='" + phone + "' and user_id='" + userObjectId + "'";
                            DataQueryBuilder queryBuilder = DataQueryBuilder.create();
                            queryBuilder.setWhereClause(whereClause);

                            final String finalPhone = phone;
                            Backendless.Data.of("User_Contacts").find(queryBuilder, new AsyncCallback<List<Map>>() {
                                @Override
                                public void handleResponse(List<Map> response) {
                                    for (int i = 0; i < response.size(); i++) {
                                        Backendless.Persistence.of("User_Contacts").remove(response.get(i), new AsyncCallback<Long>() {
                                            @Override
                                            public void handleResponse(Long response) {
                                                Toast.makeText(EditFollower.this, "Follower removed: " + name + ", " + finalPhone, Toast.LENGTH_LONG).show();
                                            }

                                            @Override
                                            public void handleFault(BackendlessFault fault) {
                                                Toast.makeText(EditFollower.this, "ERROR: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    Toast.makeText(EditFollower.this, "ERROR1: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });

                    // Click this button to select all listview items with checkbox checked.
                    Button selectAllButton = findViewById(R.id.list_select_all);
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
                    Button selectNoneButton = findViewById(R.id.list_select_none);
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
                    Button selectReverseButton = findViewById(R.id.list_select_reverse);
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
                    Button selectRemoveButton = findViewById(R.id.list_remove_selected_rows);
                    selectRemoveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            AlertDialog alertDialog = new AlertDialog.Builder(EditFollower.this).create();
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
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(EditFollower.this, "Error: " + fault.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    // Return an initialize list of ListViewItemDTO.
    private List<ListViewItemDTO> getInitViewItemDtoList()
    {
        final List<ListViewItemDTO> res = new ArrayList<ListViewItemDTO>();

        // Fetch user contacts
        String userObjectId = UserIdStorageFactory.instance().getStorage().get();
        String whereClause = "user_id = '" + userObjectId +"'";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause( whereClause );

        Backendless.Data.of("User_Contacts").find(queryBuilder, new AsyncCallback<List<Map>>() {
            @Override
            public void handleResponse(List<Map> response) {
                int len = response.size();
                Log.d("RESPONSE SIZE", String.valueOf(len));
                for (int i = 0; i < len; i++) {
                    String name = (String) response.get(i).get("name");
                    String phone = (String) response.get(i).get("phone");
                    System.out.println("DATA:" + name + ", " + phone);
                    Log.d("Ver", "handleResponse: " + name);
                    ListViewItemDTO dto = new ListViewItemDTO();
                    dto.setChecked(false);
                    dto.setItemText(name + ", " + phone);
                    Log.d("DTOOBJECT", dto.toString());
                    res.add(dto);
                }
                Log.d("DTO ITEMS", res.toString());
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(EditFollower.this, "Error: " + fault.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d("LISTVIEWITEMDTO", res.toString());
        return res;
    }
}
