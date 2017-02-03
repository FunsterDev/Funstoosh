package com.funstergames.funstoosh.activities;

import android.app.Service;
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

import com.funstergames.funstoosh.R;
import com.funstergames.funstoosh.services.GameService;

public class ReadyActivity extends AppCompatActivity {

    private ServiceConnection _serviceConnection;
    private BroadcastReceiver _startReceiver;

    private GameService _gameService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ready);

        initializeService();
    }

    @Override
    protected void onDestroy() {
        if (_serviceConnection != null) unbindService(_serviceConnection);
        unregisterReceiver(_startReceiver);
        super.onDestroy();
    }

    private void initializeService() {
        _startReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                startActivity(new Intent(ReadyActivity.this, CountdownActivity.class));
                finish();
            }
        };
        registerReceiver(_startReceiver, new IntentFilter(GameService.BROADCAST_START));

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

    public void start(View view) {
        _gameService.subscription.perform("start");
    }

}
