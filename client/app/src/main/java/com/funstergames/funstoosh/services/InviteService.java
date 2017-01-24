package com.funstergames.funstoosh.services;

import android.Manifest;
import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import com.funstergames.funstoosh.Contact;
import com.funstergames.funstoosh.activities.RequestPermissionActivity;
import com.funstergames.funstoosh.managers.NotificationsManager;

public class InviteService extends IntentService {

    public static final String ACTION_INVITED = "invited";

    public static final String EXTRA_WHO = "who";
    public static final String EXTRA_GAME_ID = "game_id";

    public InviteService() {
        super("InviteService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        switch (intent.getAction()) {
            case ACTION_INVITED:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    startActivity(
                            new Intent(this, RequestPermissionActivity.class)
                                    .putExtras(intent.getExtras())
                                    .putExtra(RequestPermissionActivity.EXTRA_SERVICE, getClass())
                                    .putExtra(RequestPermissionActivity.EXTRA_PERMISSION, Manifest.permission.READ_CONTACTS)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    );
                } else {
                    createNotification(intent);
                }
                break;

            case RequestPermissionActivity.ACTION_PERMISSION_RESPONSE:
                createNotification(intent);
                break;
        }
    }

    private void createNotification(Intent intent) {
        String phoneNumber = intent.getStringExtra(EXTRA_WHO);
        String name = Contact.getNameByPhoneNumber(this, phoneNumber);
        if (name == null) name = phoneNumber;
        NotificationsManager.invite(this, name, intent.getIntExtra(EXTRA_GAME_ID, -1));
    }
}
