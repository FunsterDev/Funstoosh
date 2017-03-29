package com.funstergames.funstoosh.adapters;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.funstergames.funstoosh.R;
import com.funstergames.funstoosh.activities.PictureActivity;
import com.funstergames.funstoosh.adapters.PicturesAdapter;
import com.funstergames.funstoosh.services.GameService;

import com.funstergames.funstoosh.Constants;
import com.funstergames.funstoosh.PicassoHelper;
import com.funstergames.funstoosh.Player;
import com.funstergames.funstoosh.activities.HintPictureGalleryActivity;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.internal.DiskLruCache;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class PicturesAdapter extends BaseAdapter {
   private Context _context;

    private GameService _gameService;

    private ServiceConnection _serviceConnection;
    private BroadcastReceiver _picturesUpdatedReceiver;
    private BroadcastReceiver _usedWandReceiver;

    public PicturesAdapter(Context context) {
        initializeService(context);
        _context = context;
    }

    private void initializeService(final Context context) {
        _picturesUpdatedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                notifyDataSetChanged();
            }
        };
        context.registerReceiver(_picturesUpdatedReceiver, new IntentFilter(GameService.BROADCAST_PICTURES_UPDATED));

        _usedWandReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                notifyDataSetChanged();
            }
        };
        context.registerReceiver(_usedWandReceiver, new IntentFilter(GameService.BROADCAST_USED_MAGIC_WAND_UPDATED));

        _serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                _gameService = ((GameService.GameBinder)service).service;
                notifyDataSetChanged();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                _serviceConnection = null;
                onDestroy(context);
                _gameService = null;
                notifyDataSetChanged();
            }
        };
        context.bindService(new Intent(context, GameService.class), _serviceConnection, 0);
    }

    public void onDestroy(Context context) {
        if (_serviceConnection != null) {
            context.unbindService(_serviceConnection);
            _serviceConnection = null;
        }
        if (_picturesUpdatedReceiver != null) {
            context.unregisterReceiver(_picturesUpdatedReceiver);
            _picturesUpdatedReceiver = null;
        }
        if (_usedWandReceiver != null) {
            context.unregisterReceiver(_usedWandReceiver);
            _usedWandReceiver = null;
        }
    }

    @Override
    public int getCount() {
        if (_gameService == null) return 0;
        return _gameService.pictures.size();
    }

    @Override
    public Map.Entry<Player, String> getItem(int position) {
        if (_gameService == null) return null;
        return _gameService.pictures.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean isEnabled(int position) {
        Map.Entry<Player, String> picture = getItem(position);
        if (picture == null) return false;
        return !_gameService.usedMagicWand.containsKey(picture.getKey());
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new GridView.LayoutParams(450, 450));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setClickable(true);
            imageView.setAdjustViewBounds(true);

          View v = imageView;
          v.setOnClickListener(new  View.OnClickListener() {
              @Override
              public void onClick(View view) {

                  Map.Entry<Player, String> picture = _gameService.pictures.get(position);
                  if (!_gameService.usedPictures.contains(picture.getValue())) {
                                _gameService.usedPictures.add(picture.getValue());
                                _gameService.subscription.perform("used_picture");
                            }
                            _context.startActivity(
                                    new Intent(_context.getApplicationContext(), PictureActivity.class).putExtra(PictureActivity.EXTRA_PICTURE_ID, picture.getValue())
                             );
              }
          });
        } else {
            imageView = (ImageView)convertView;
        }

        Map.Entry<Player, String> picture = getItem(position);
        if (picture == null) {
            imageView.setImageBitmap(null);
            return imageView;
        }
        String pictureId = picture.getValue();

        PicassoHelper.getPicasso(parent.getContext())
                .load(Constants.ROOT_URL + "/pictures/" + pictureId)
                .into(imageView);

        return imageView;
    }
}
