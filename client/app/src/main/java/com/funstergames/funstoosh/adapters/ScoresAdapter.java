package com.funstergames.funstoosh.adapters;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.funstergames.funstoosh.Player;
import com.funstergames.funstoosh.services.GameService;

public class ScoresAdapter extends BaseAdapter {

    private GameService _gameService;
    private ServiceConnection _serviceConnection;

    public ScoresAdapter(Context context) {
        initializeService(context);
    }

    private void initializeService(final Context context) {
        _serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                _gameService = ((GameService.GameBinder)service).service;
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
    }

    @Override
    public int getCount() {
        if (_gameService == null) return 0;
        return _gameService.players.size();
    }

    @Override
    public Player getItem(int position) {
        if (_gameService == null) return null;
        return _gameService.players.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final TextView textView;
        if (convertView == null) {
            textView = new TextView(parent.getContext());
        } else {
            textView = (TextView)convertView;
        }

        Player player = getItem(position);
        textView.setText(player.toString() + " - " + player.score);

        return textView;
    }
}
