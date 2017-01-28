package com.funstergames.funstoosh.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

import com.funstergames.funstoosh.Constants;
import com.funstergames.funstoosh.Contact;
import com.funstergames.funstoosh.R;
import com.funstergames.funstoosh.adapters.InviteContactsAdapter;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Map;

public class CreateGroupActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 0;

    private ListView _contactsList;
    private InviteContactsAdapter _inviteContactsAdapter;

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
        _inviteContactsAdapter = new InviteContactsAdapter(this);
        _contactsList.setAdapter(_inviteContactsAdapter);
    }

    public void createGroup(View view) {
        if (_inviteContactsAdapter == null || _inviteContactsAdapter.getSelected().isEmpty()) return;

        Ion.with(this)
                .load("POST", Constants.ROOT_URL + "/games")
                .addQuery("phone_numbers", StringUtils.join(_inviteContactsAdapter.getSelected().keySet(), ','))
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> result) {
                        if (e != null || result.getHeaders().code() != 201) return;

                        String gameId = result.getResult().get("id").getAsString();
                        startActivity(
                                new Intent(CreateGroupActivity.this, WaitForPlayersActivity.class)
                                        .putExtra(WaitForPlayersActivity.EXTRA_GAME_ID, gameId)
                                        .putExtra(
                                                WaitForPlayersActivity.EXTRA_INVITED_PLAYERS,
                                                _inviteContactsAdapter.getSelected().values().toArray(new Contact[_inviteContactsAdapter.getSelected().size()])
                                        )
                        );
                    }
                });
    }
}
