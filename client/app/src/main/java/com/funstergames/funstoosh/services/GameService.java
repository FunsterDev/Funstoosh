package com.funstergames.funstoosh.services;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.funstergames.funstoosh.Constants;
import com.funstergames.funstoosh.Player;
import com.funstergames.funstoosh.activities.CountdownActivity;
import com.funstergames.funstoosh.activities.LoginActivity;
import com.funstergames.funstoosh.activities.PlayerActivity;
import com.funstergames.funstoosh.activities.ReadyActivity;
import com.funstergames.funstoosh.activities.RequestPermissionActivity;
import com.funstergames.funstoosh.activities.SeekerActivity;
import com.funstergames.funstoosh.activities.WaitForPlayersActivity;
import com.funstergames.funstoosh.managers.NotificationsManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hosopy.actioncable.ActionCable;
import com.hosopy.actioncable.ActionCableException;
import com.hosopy.actioncable.Channel;
import com.hosopy.actioncable.Consumer;
import com.hosopy.actioncable.Subscription;
import com.koushikdutta.ion.Ion;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class GameService extends Service {
    public static final String ACTION_START_GAME = "start_game";
    public static final String ACTION_START_ACTIVITY = "start_activity";

    public static final String EXTRA_GAME_ID = "game_id";

    public static final String BROADCAST_PLAYERS_UPDATED = "players_updated";
    public static final String BROADCAST_READY = "ready";
    public static final String BROADCAST_START = "start";
    public static final String BROADCAST_PICTURES_UPDATED = "pictures_updated";
    public static final String BROADCAST_USED_MAGIC_WAND_UPDATED = "used_magic_wand_updated";
    public static final String BROADCAST_MESSAGES_UPDATED = "messages_updated";
    public static final String BROADCAST_SCORE_UPDATED = "score_updated";

    private Consumer _consumer;
    public Subscription subscription;

    private String _gameId = null;

    // Phone number -> Player
    public HashMap<String, Player> players = new HashMap<>();
    // Player, Picture ID
    public ArrayList<Map.Entry<Player, String>> pictures = new ArrayList<>();
    // Picture ID
    public HashSet<String> usedPictures = new HashSet<>();
    public HashMap<Player, Timer> usedMagicWand = new HashMap<>();
    // Player, Message body
    public ArrayList<Map.Entry<Player, String>> messages = new ArrayList<>();

    public Player self;

    public enum State {
        WAITING,
        READY,
        PLAYING,
    }

    public State state = State.WAITING;

    // Gameplay
    public Player seeker;
    public long countdownStartedAt = -1;

    public static final long COUNTDOWN_TIME = 30000;
    public static final long MAGIC_WAND_TIME = 30000;

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(NotificationsManager.GAME_NOTIFICATION_ID, NotificationsManager.game(this));
        Consumer.Options options = new Consumer.Options();
        options.cookieHandler = Ion.getDefault(this).getCookieMiddleware().getCookieManager();
        try {
            _consumer = ActionCable.createConsumer(new URI("ws://" + Constants.HOST + "/websocket"), options);
        } catch (URISyntaxException e) {
            stopSelf();
            return;
        }
        _consumer.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NotificationsManager.cancel(this, NotificationsManager.GAME_NOTIFICATION_ID);
        if (_consumer != null) _consumer.disconnect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getAction()) {
            case ACTION_START_GAME:
                String gameId = intent.getStringExtra(EXTRA_GAME_ID);
                if (gameId != null && !gameId.equals(_gameId)) {
                    _gameId = gameId;
                    // leaving previous game
                    if (subscription != null) _consumer.getSubscriptions().remove(subscription);

                    players = new HashMap<>();
                    pictures = new ArrayList<>();
                    usedPictures = new HashSet<>();
                    usedMagicWand = new HashMap<>();
                    messages = new ArrayList<>();

                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                        startActivity(
                                new Intent(this, RequestPermissionActivity.class)
                                        .putExtra(RequestPermissionActivity.EXTRA_SERVICE, getClass())
                                        .putExtra(RequestPermissionActivity.EXTRA_PERMISSION, Manifest.permission.READ_CONTACTS)
                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        );
                    } else {
                        connect();
                    }
                }
                break;

            case ACTION_START_ACTIVITY:
                startActivityByState();
                break;

            case RequestPermissionActivity.ACTION_PERMISSION_RESPONSE:
                connect();
                break;
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

    private void connect() {
        Channel channel = new Channel("GameChannel");
        channel.addParam("id", _gameId);
        subscription = _consumer.getSubscriptions().create(channel)
                .onDisconnected(new Subscription.DisconnectedCallback() {
                    @Override
                    public void call() {
                        stopSelf();
                    }
                })
                .onRejected(new Subscription.RejectedCallback() {
                    @Override
                    public void call() {
                        stopSelf();
                    }
                })
                .onFailed(new Subscription.FailedCallback() {
                    @Override
                    public void call(ActionCableException e) {
                        stopSelf();
                    }
                })
                .onReceived(new Subscription.ReceivedCallback() {
                        @Override
                        public void call(JsonElement jsonElement) {
                            JsonObject message = jsonElement.getAsJsonObject();
                            Player player = null;
                            Integer originalScore = null;
                            if (self != null) originalScore = self.score;
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

                                            if (seeker == null) seeker = player;
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
                                    state = State.READY;
                                    sendBroadcast(new Intent(BROADCAST_READY));
                                    break;

                                case "start":
                                    state = State.PLAYING;
                                    countdownStartedAt = System.currentTimeMillis();
                                    sendBroadcast(new Intent(BROADCAST_START));
                                    break;

                                case "added_picture":
                                    player.addedPicture();
                                    pictures.add(new AbstractMap.SimpleEntry<>(player, message.get("path").getAsString()));
                                    sendBroadcast(new Intent(BROADCAST_PICTURES_UPDATED));
                                    break;

                                case "used_picture":
                                    player.usedPicture();
                                    break;

                                case "used_magic_wand":
                                    player.usedMagicWand();
                                    startMagicWand(player);
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

                            if (originalScore != null && originalScore != self.score)
                                sendBroadcast(new Intent(BROADCAST_SCORE_UPDATED));
                        }
                });
    }

    public Class<? extends Activity> getActivityByState() {
        switch (state) {
            case WAITING:
                return WaitForPlayersActivity.class;
            case READY:
                if (seeker == self) {
                    return ReadyActivity.class;
                } else {
                    return CountdownActivity.class;
                }
            case PLAYING:
                if (System.currentTimeMillis() - countdownStartedAt < COUNTDOWN_TIME) {
                    return CountdownActivity.class;
                } else {
                    if (seeker == self) {
                        return SeekerActivity.class;
                    } else {
                        return PlayerActivity.class;
                    }
                }
            default:
                return null;
        }
    }

    private void startActivityByState() {
        startActivity(new Intent(this, getActivityByState()).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void startMagicWand(final Player player) {
        Timer oldTimer = usedMagicWand.get(player);
        if (oldTimer != null) oldTimer.cancel();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                usedMagicWand.remove(player);
                sendBroadcast(new Intent(BROADCAST_USED_MAGIC_WAND_UPDATED));
            }
        }, MAGIC_WAND_TIME);
        usedMagicWand.put(player, timer);
        sendBroadcast(new Intent(BROADCAST_USED_MAGIC_WAND_UPDATED));
    }
}
