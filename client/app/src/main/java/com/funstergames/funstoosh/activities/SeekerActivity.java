package com.funstergames.funstoosh.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.funstergames.funstoosh.R;
import com.funstergames.funstoosh.services.GameService;

public class SeekerActivity extends AppCompatActivity {

    private ServiceConnection _serviceConnection;

    private GameService _gameService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seeker_view);

        initializeService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (_serviceConnection != null) unbindService(_serviceConnection);
    }

    private void initializeService() {
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

    public void getPictureHint(View view) {
        startActivity(new Intent(this, HintPictureGalleryActivity.class));
    }
}
