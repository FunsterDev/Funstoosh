package com.funstergames.funstoosh.activities;

import android.*;
import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.funstergames.funstoosh.Constants;
import com.funstergames.funstoosh.adapters.ContactsAdapter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.apache.commons.lang3.StringUtils;
import com.funstergames.funstoosh.R;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateGroupActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 0;

    private ListView _contactsList;
    private ContactsAdapter _contactsAdapter;

    // Name, Phone number
    private ArrayList<Map.Entry<String, String>> availableContacts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        _contactsList = (ListView)findViewById(R.id.contacts_list);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { android.Manifest.permission.READ_CONTACTS },
                    PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            initializeContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_CONTACTS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initializeContacts();
                }
                break;
        }
    }

    private void initializeContacts() {
        _contactsAdapter = new ContactsAdapter(this);
        _contactsList.setAdapter(_contactsAdapter);
    }

    public void createGroup(View view) {
        
    }
}