package com.funstergames.funstoosh.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.content.Intent;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.TextView;

import com.funstergames.funstoosh.Constants;
import com.funstergames.funstoosh.Player;
import com.funstergames.funstoosh.R;
import com.funstergames.funstoosh.services.GameService;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PlayerActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 0;

    private ServiceConnection _serviceConnection;
    private BroadcastReceiver _scoreUpdatedReceiver;
    private BroadcastReceiver _wonLostReceiver;

    private GameService _gameService;

    private TextView _scoreText;
    private ImageButton _winButton;
    private ImageButton _loseButton;
    private TextView _wonText;
    private TextView _lostText;
    private TextView _playingText;
    private View _takePictureButton;
    private View _magicWandButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_players_view);

        _scoreText = (TextView)findViewById(R.id.score);
        _winButton = (ImageButton)findViewById(R.id.win);
        _loseButton = (ImageButton)findViewById(R.id.lose);
        _wonText = (TextView)findViewById(R.id.won);
        _lostText = (TextView)findViewById(R.id.lost);
        _playingText = (TextView)findViewById(R.id.playing);
        _takePictureButton = (View)findViewById(R.id.take_picture);
        _magicWandButton = (View)findViewById(R.id.magic_wand);

        initializeService();
    }

    @Override
    protected void onDestroy() {
        if (_serviceConnection != null) unbindService(_serviceConnection);
        unregisterReceiver(_scoreUpdatedReceiver);
        unregisterReceiver(_wonLostReceiver);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) uploadPicture((Bitmap)data.getParcelableExtra("data"));
                break;
        }
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

    private void wonLostUpdated() {
        _wonText.setText(String.valueOf(_gameService.won));
        _lostText.setText(String.valueOf(_gameService.lost));
        _playingText.setText(String.valueOf(_gameService.players.size() - 1 - _gameService.won - _gameService.lost));
        _winButton.setVisibility(_gameService.self.state == Player.State.PLAYING ? View.VISIBLE : View.INVISIBLE);
        _loseButton.setVisibility(_gameService.self.state == Player.State.PLAYING ? View.VISIBLE : View.INVISIBLE);
        _magicWandButton.setVisibility(_gameService.self.state == Player.State.PLAYING ? View.VISIBLE : View.INVISIBLE);
        _takePictureButton.setVisibility(_gameService.self.state == Player.State.PLAYING ? View.VISIBLE : View.INVISIBLE);
    }

    private void scoreUpdated() {
        _scoreText.setText(String.valueOf(_gameService.self.score));
        _magicWandButton.setClickable(_gameService.self.score >= Player.SCORE_REQUIRED_FOR_MAGIC_WAND);
    }

    public void addPicture(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void useMagicWand(View view) {
        _gameService.subscription.perform("use_magic_wand");
    }

    private void uploadPicture(Bitmap image) {
        FileOutputStream out;
        final File file;
        try { file = File.createTempFile("picture", ".jpg"); }
        catch (IOException e) {
            return;
        }
        try { out = new FileOutputStream(file); }
        catch (FileNotFoundException e) {
            file.delete();
            return;
        }
        try {
            image.compress(Bitmap.CompressFormat.JPEG, 50, out);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
            }
        }

        Ion.with(this)
                .load("POST", Constants.ROOT_URL + "/pictures")
                .setMultipartFile("file", file)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        file.delete();
                    }
                });
    }

    public void win(View view) {
        _gameService.subscription.perform("win");
    }

    public void lose(View view) {
        _gameService.subscription.perform("lose");
    }
}
