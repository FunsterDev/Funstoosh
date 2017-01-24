package com.funstergames.funstoosh.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.RequiresPermission;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.funstergames.funstoosh.Contact;
import com.funstergames.funstoosh.R;
import com.funstergames.funstoosh.adapters.WaitForPlayersAdapter;
import com.funstergames.funstoosh.services.GameService;

import java.util.Arrays;

public class WaitForPlayersActivity extends AppCompatActivity {
    public static final String EXTRA_GAME_ID = "game_id";
    public static final String EXTRA_INVITED_PLAYERS = "invited_players";

    private GameService _gameService;
    private ServiceConnection _serviceConnection;
    private BroadcastReceiver _readyReceiver;

    private WaitForPlayersAdapter _waitForPlayersAdapter;

    private ListView _playersList;
    private Button _startGameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_for_players);

        initializeService();

        _playersList = (ListView)findViewById(R.id.players);
        _startGameButton = (Button)findViewById(R.id.ready);

        if (!getIntent().hasExtra(EXTRA_INVITED_PLAYERS)) _startGameButton.setVisibility(View.GONE);

        Parcelable[] invitedParcelable = getIntent().getParcelableArrayExtra(EXTRA_INVITED_PLAYERS);
        Contact[] invited = null;
        if (invitedParcelable != null) invited = Arrays.copyOf(invitedParcelable, invitedParcelable.length, Contact[].class);
        _waitForPlayersAdapter = new WaitForPlayersAdapter(this, invited);
        _playersList.setAdapter(_waitForPlayersAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (_serviceConnection != null) unbindService(_serviceConnection);
        unregisterReceiver(_readyReceiver);
        _waitForPlayersAdapter.onDestroy(this);
    }

    private void initializeService() {
        _readyReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                startActivity(new Intent(WaitForPlayersActivity.this, ReadyActivity.class));
                finish();
            }
        };
        registerReceiver(_readyReceiver, new IntentFilter(GameService.BROADCAST_READY));

        startService(
                new Intent(this, GameService.class)
                        .setAction(GameService.ACTION_START_GAME)
                        .putExtra(GameService.EXTRA_GAME_ID, getIntent().getIntExtra(EXTRA_GAME_ID, -1))
        );
        // finish when game ends
        _serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                _gameService = ((GameService.GameBinder)service).service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                _serviceConnection = null;
                finish();
            }
        };
        bindService(new Intent(this, GameService.class), _serviceConnection, 0);
    }

    public void ready(View view) {
        _gameService.subscription.perform("ready");
    }
}
