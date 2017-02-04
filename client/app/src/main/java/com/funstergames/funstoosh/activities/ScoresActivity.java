package com.funstergames.funstoosh.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.funstergames.funstoosh.R;
import com.funstergames.funstoosh.adapters.ScoresAdapter;
import com.funstergames.funstoosh.services.GameService;

public class ScoresActivity extends AppCompatActivity {

    private GameService _gameService;
    private ServiceConnection _serviceConnection;
    private BroadcastReceiver _readyReceiver;

    private ScoresAdapter _scoresAdapter;

    private ListView _playersList;
    private Button _startGameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);

        initializeService();

        _playersList = (ListView)findViewById(R.id.players);
        _startGameButton = (Button)findViewById(R.id.ready);

        _scoresAdapter = new ScoresAdapter(this);
        _playersList.setAdapter(_scoresAdapter);
    }

    @Override
    protected void onDestroy() {
        if (_serviceConnection != null) unbindService(_serviceConnection);
        unregisterReceiver(_readyReceiver);
        _scoresAdapter.onDestroy(this);
        super.onDestroy();
    }

    private void initializeService() {
        _readyReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                startActivity(new Intent(ScoresActivity.this, _gameService.getActivityByState()));
                finish();
            }
        };
        registerReceiver(_readyReceiver, new IntentFilter(GameService.BROADCAST_READY));

        // finish when game ends
        _serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                _gameService = ((GameService.GameBinder)service).service;
                if (_gameService.self == _gameService.seeker) _startGameButton.setVisibility(View.VISIBLE);
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
