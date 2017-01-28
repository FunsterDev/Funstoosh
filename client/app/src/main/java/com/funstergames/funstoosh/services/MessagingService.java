package com.funstergames.funstoosh.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.funstergames.funstoosh.Contact;
import com.funstergames.funstoosh.Player;
import com.funstergames.funstoosh.R;
import com.funstergames.funstoosh.activities.WaitForPlayersActivity;
import com.funstergames.funstoosh.managers.NotificationsManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        switch (remoteMessage.getData().get("type")) {
            case "invite":
                startService(
                        new Intent(this, InviteService.class)
                                .setAction(InviteService.ACTION_INVITED)
                                .putExtra(InviteService.EXTRA_WHO, remoteMessage.getData().get("who"))
                                .putExtra(InviteService.EXTRA_GAME_ID, remoteMessage.getData().get("game_id"))
                );
                break;
        }
    }
}
