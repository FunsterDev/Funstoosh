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
import android.widget.CheckBox;

import com.funstergames.funstoosh.Player;
import com.funstergames.funstoosh.Contact;
import com.funstergames.funstoosh.services.GameService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WaitForPlayersAdapter extends BaseAdapter {
    private ArrayList<Contact> _invited;

    private GameService _gameService;

    private ServiceConnection _serviceConnection;
    private BroadcastReceiver _playersUpdatedReceiver;

    public WaitForPlayersAdapter(Context context, Contact[] invited) {
        if (invited != null) {
            _invited = new ArrayList<>(Arrays.asList(invited));
        } else {
            _invited = new ArrayList<>();
        }

        initializeService(context);
    }

    private void initializeService(final Context context) {
        _playersUpdatedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (_gameService == null) return;
                setPlayers();
            }
        };
        context.registerReceiver(_playersUpdatedReceiver, new IntentFilter(GameService.BROADCAST_PLAYERS_UPDATED));

        _serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                _gameService = ((GameService.GameBinder)service).service;
                setPlayers();
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

    private void setPlayers() {
        if (_gameService == null) return;
        for (Player player : _gameService.players) {
            _invited.remove(player);
        }
        notifyDataSetChanged();
    }

    public void onDestroy(Context context) {
        if (_serviceConnection != null) {
            context.unbindService(_serviceConnection);
            _serviceConnection = null;
        }
        if (_playersUpdatedReceiver != null) {
            context.unregisterReceiver(_playersUpdatedReceiver);
            _playersUpdatedReceiver = null;
        }
    }

    @Override
    public int getCount() {
        if (_gameService == null) return 0;
        return _gameService.players.size() + _invited.size();
    }

    @Override
    public Contact getItem(int position) {
        if (position < _gameService.players.size()) {
            return _gameService.players.get(position);
        } else {
            return _invited.get(position - _gameService.players.size());
        }
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final CheckBox checkbox;
        if (convertView == null) {
            checkbox = new CheckBox(parent.getContext());
            checkbox.setEnabled(false);
        } else {
            checkbox = (CheckBox)convertView;
        }

        Contact contact = getItem(position);
        checkbox.setText(contact.toString());
        checkbox.setChecked(position < _gameService.players.size());

        return checkbox;
    }
}
