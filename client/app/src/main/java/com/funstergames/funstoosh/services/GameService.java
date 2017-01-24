package com.funstergames.funstoosh.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.funstergames.funstoosh.Constants;
import com.funstergames.funstoosh.Player;
import com.funstergames.funstoosh.activities.LoginActivity;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hosopy.actioncable.ActionCable;
import com.hosopy.actioncable.Channel;
import com.hosopy.actioncable.Consumer;
import com.hosopy.actioncable.Subscription;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class GameService extends Service {
    public static final String EXTRA_GAME_ID = "game_id";

    public static final String BROADCAST_PLAYERS_UPDATED = "players_updated";
    public static final String BROADCAST_READY = "ready";
    public static final String BROADCAST_START = "start";
    public static final String BROADCAST_MESSAGES_UPDATED = "messages_updated";

    private Consumer _consumer;
    private Subscription _subscription;

    private int _gameId = -1;

    // Phone number -> Player
    public Player self;
    public HashMap<String, Player> players;
    public ArrayList<Map.Entry<Player, String>> messages;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            _consumer = ActionCable.createConsumer(new URI("ws://" + Constants.HOST + "/cable"));
        } catch (URISyntaxException e) {
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (_consumer != null) _consumer.disconnect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int gameId = intent.getIntExtra(EXTRA_GAME_ID, -1);
        if (gameId != -1 && gameId != _gameId) {
            // leaving previous game
            if (_subscription != null) _consumer.getSubscriptions().remove(_subscription);

            players = new HashMap<>();
            messages = new ArrayList<>();

            _subscription = _consumer.getSubscriptions().create(new Channel("GameChannel"));
            _subscription.onDisconnected(new Subscription.DisconnectedCallback() {
                @Override
                public void call() {
                    stopSelf();
                }
            });
            handleMessages();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public class GameBinder extends Binder {
        public GameService service = GameService.this;
    };
    private GameBinder _binder = new GameBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return _binder;
    }

    private void handleMessages() {
        _subscription.onReceived(new Subscription.ReceivedCallback() {
            @Override
            public void call(JsonElement jsonElement) {
                JsonObject message = jsonElement.getAsJsonObject();
                Player player = new Player();
                if (message.has("who")) player = players.get(message.get("who").getAsString());

                switch (message.get("type").getAsString()) {
                    case "players":
                        HashSet<String> currentPlayers = new HashSet<String>();
                        // Adding missing players
                        for (JsonElement phoneNumberJson : message.get("phone_numbers").getAsJsonArray()) {
                            String phoneNumber = phoneNumberJson.getAsString();
                            currentPlayers.add(phoneNumber);
                            if (!players.containsKey(phoneNumber)) {
                                player = new Player(GameService.this, phoneNumber);
                                if (phoneNumber.equals(PreferenceManager.getDefaultSharedPreferences(GameService.this).getString(LoginActivity.PREFERENCE_LOGGED_IN_PHONE_NUMBER, null))) {
                                    self = player;
                                    player.name = "You";
                                }
                                players.put(phoneNumber, player);
                            }
                        }
                        // Removing old players
                        for (String phoneNumber : players.keySet()) {
                            if (!currentPlayers.contains(phoneNumber)) {
                                players.remove(phoneNumber);
                            }
                        }

                        sendBroadcast(new Intent(BROADCAST_PLAYERS_UPDATED));
                        break;

                    case "ready":
                        sendBroadcast(new Intent(BROADCAST_READY));
                        break;

                    case "start":
                        sendBroadcast(new Intent(BROADCAST_START));
                        break;

                    case "added_picture":
                        player.addedPicture();
                        break;

                    case "used_picture":
                        player.usedPicture();
                        break;

                    case "used_magic_wand":
                        player.usedMagicWand();
                        break;

                    case "win":
                        player.win();
                        break;

                    case "lose":
                        player.lose();
                        break;

                    case "message":
                        messages.add(new AbstractMap.SimpleEntry<>(
                                player,
                                message.get("body").getAsString()
                        ));
                        sendBroadcast(new Intent(BROADCAST_MESSAGES_UPDATED));
                        break;

                    case "end":
                        stopSelf();
                        break;
                }
            }
        });
    }
}
