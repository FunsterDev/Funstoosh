package com.funstergames.funstoosh.activities;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.content.Intent;
import android.provider.MediaStore;

import com.funstergames.funstoosh.Constants;
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

    private GameService _gameService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_players_view);

        initializeService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (_serviceConnection != null) unbindService(_serviceConnection);
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
            image.compress(Bitmap.CompressFormat.JPEG, 70, out);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
            }
        }

        Ion.with(this)
                .load("POST", Constants.ROOT_URL + "/picture")
                .setMultipartFile("file", file)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        file.delete();
                    }
                });
    }
}
