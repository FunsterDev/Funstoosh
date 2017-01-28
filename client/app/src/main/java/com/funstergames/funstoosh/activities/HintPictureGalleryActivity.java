package com.funstergames.funstoosh.activities;

import android.content.ComponentName;
import android.content.Intent;
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
                                .putExtra(PictureActivity.EXTRA_BITMAP, _picturesAdapter.cache.get(picture.getValue()))
                );
            }
        });

        initializeService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _picturesAdapter.onDestroy(this);
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
}
