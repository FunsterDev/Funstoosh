package com.funstergames.funstoosh.managers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.funstergames.funstoosh.R;
import com.funstergames.funstoosh.activities.WaitForPlayersActivity;
import com.funstergames.funstoosh.services.GameService;

public class NotificationsManager {
    public static final int INVITE_NOTIFICATION_ID = 0;
    public static final int GAME_NOTIFICATION_ID = 1;

    public static void invite(Context context, String name, String gameId) {
        getNotificationManager(context).notify(
                INVITE_NOTIFICATION_ID,
                new NotificationCompat.Builder(context)
                        .setContentTitle("Invitation to play with " + name)
                        .setSmallIcon(R.drawable.play_icon)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentIntent(
                                PendingIntent.getActivity(
                                        context, 0,
                                        new Intent(context, WaitForPlayersActivity.class)
                                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                .putExtra(WaitForPlayersActivity.EXTRA_GAME_ID, gameId)
                                        , PendingIntent.FLAG_UPDATE_CURRENT
                                )
                        ).build()
        );
    }

    public static Notification game(Context context) {
        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle("Game is running")
                .setSmallIcon(R.drawable.play_icon)
                .setOngoing(true)
                .setContentIntent(
                        PendingIntent.getService(
                                context, 0,
                                new Intent(context, GameService.class)
                                        .setAction(GameService.ACTION_START_ACTIVITY)
                                , 0
                        )
                ).build();
        getNotificationManager(context).notify(
                GAME_NOTIFICATION_ID,
                notification
        );
        return notification;
    }

    private static NotificationManager getNotificationManager(Context context) {
        return (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void cancel(Context context, int notification_id) {
        getNotificationManager(context).cancel(notification_id);
    }
}
