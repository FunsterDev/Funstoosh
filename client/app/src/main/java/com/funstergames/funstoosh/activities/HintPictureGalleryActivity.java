package com.funstergames.funstoosh.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ExpandedMenuView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.funstergames.funstoosh.Player;
import com.funstergames.funstoosh.R;
import com.funstergames.funstoosh.adapters.PicturesAdapter;
import com.funstergames.funstoosh.services.GameService;

import java.util.Map;

public class HintPictureGalleryActivity extends AppCompatActivity {

    private GridView _picturesGrid;
    private PicturesAdapter _picturesAdapter;

    private ServiceConnection _serviceConnection;
    private BroadcastReceiver _scoreUpdatedReceiver;

    private GameService _gameService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hint_picture_gallery);

        _picturesGrid = (GridView)findViewById(R.id.pictures_grid);

        _picturesAdapter = new PicturesAdapter(this);
        _picturesGrid.setAdapter(_picturesAdapter);

        _picturesGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map.Entry<Player, String> picture = _gameService.pictures.get(position);
                if (!_gameService.usedPictures.contains(picture.getValue())) {
                    _gameService.usedPictures.add(picture.getValue());
                    _gameService.subscription.perform("used_picture");
                }
                startActivity(
                        new Intent(HintPictureGalleryActivity.this, PictureActivity.class)
                                .putExtra(PictureActivity.EXTRA_BITMAP, _gameService.picturesCache.get(picture.getValue()))
                );
            }
        });

        initializeService();
    }

    @Override
    protected void onDestroy() {
        _picturesAdapter.onDestroy(this);
        if (_serviceConnection != null) unbindService(_serviceConnection);
        unregisterReceiver(_scoreUpdatedReceiver);
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

        _serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                _gameService = ((GameService.GameBinder)service).service;
                scoreUpdated();
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
        _picturesGrid.setEnabled(_gameService.self.score >= Player.SCORE_REQUIRED_FOR_PICTURE);
    }
}
