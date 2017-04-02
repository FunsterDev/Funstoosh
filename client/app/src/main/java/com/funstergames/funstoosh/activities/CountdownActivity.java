package com.funstergames.funstoosh.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.funstergames.funstoosh.R;
import com.funstergames.funstoosh.services.GameService;
import com.funstergames.funstoosh.views.TimerView;

public class CountdownActivity extends AppCompatActivity {

    private TimerView _timerView;

    private ServiceConnection _serviceConnection;
    private BroadcastReceiver _startReceiver;

    private GameService _gameService;

    TextView text1;

    private static final String FORMAT = "%02d:%02d:%02d";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);

        _timerView = (TimerView) findViewById(R.id.timer);

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
                initializeTimer();
            }
        };
        registerReceiver(_startReceiver, new IntentFilter(GameService.BROADCAST_START));

        _serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                _gameService = ((GameService.GameBinder)service).service;
                if (_gameService.state == GameService.State.PLAYING) {
                    initializeTimer();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                _serviceConnection = null;
                finish();
            }
        };
        bindService(new Intent(this, GameService.class), _serviceConnection, 0);
    }

    private void initializeTimer() {
        _timerView.start(GameService.COUNTDOWN_TIME - (System.currentTimeMillis() - _gameService.countdownStartedAt));
        new Timer().schedule(new TimerTask() {
            @Override

            public void run() {

                startActivity(new Intent(CountdownActivity.this, _gameService.getActivityByState()));
                finish();
            }
        }, GameService.COUNTDOWN_TIME - (System.currentTimeMillis() - _gameService.countdownStartedAt));
    }
}