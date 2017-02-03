package com.funstergames.funstoosh.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.funstergames.funstoosh.R;
import com.funstergames.funstoosh.services.GameService;

public class SeekerActivity extends AppCompatActivity {

    private ServiceConnection _serviceConnection;
    private BroadcastReceiver _scoreUpdatedReceiver;
    private BroadcastReceiver _wonLostReceiver;

    private GameService _gameService;

    private TextView _scoreText;
    private TextView _wonText;
    private TextView _lostText;
    private TextView _playingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seeker_view);

        _scoreText = (TextView)findViewById(R.id.score);
        _wonText = (TextView)findViewById(R.id.won);
        _lostText = (TextView)findViewById(R.id.lost);
        _playingText = (TextView)findViewById(R.id.playing);

        initializeService();
    }

    @Override
    protected void onDestroy() {
        if (_serviceConnection != null) unbindService(_serviceConnection);
        unregisterReceiver(_scoreUpdatedReceiver);
        unregisterReceiver(_wonLostReceiver);
        super.onDestroy();
    }

    private void initializeService() {
        _scoreUpdatedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                scoreUpdated();
            }
        };
        registerReceiver(_scoreUpdatedReceiver, new IntentFilter(GameService.BROADCAST_SCORE_UPDATED));

        _wonLostReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                wonLostUpdated();
            }
        };
        registerReceiver(_wonLostReceiver, new IntentFilter(GameService.BROADCAST_PLAYERS_UPDATED));
        registerReceiver(_wonLostReceiver, new IntentFilter(GameService.BROADCAST_WON_LOST_UPDATED));

        _serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                _gameService = ((GameService.GameBinder)service).service;
                scoreUpdated();
                wonLostUpdated();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                _serviceConnection = null;
                finish();
            }
        };
        bindService(new Intent(this, GameService.class), _serviceConnection, 0);
    }

    private void scoreUpdated() {
        _scoreText.setText(String.valueOf(_gameService.self.score));
    }

    private void wonLostUpdated() {
        _wonText.setText(String.valueOf(_gameService.won));
        _lostText.setText(String.valueOf(_gameService.lost));
        _playingText.setText(String.valueOf(_gameService.players.size() - 1 - _gameService.won - _gameService.lost));
    }

    public void getPictureHint(View view) {
        startActivity(new Intent(this, HintPictureGalleryActivity.class));
    }
}
