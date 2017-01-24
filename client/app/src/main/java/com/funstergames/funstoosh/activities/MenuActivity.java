package com.funstergames.funstoosh.activities;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.funstergames.funstoosh.R;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void startGame(View view) {
        startActivity(new Intent(this, CreateGroupActivity.class));
    }

    public void login(View view) {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .remove(LoginActivity.PREFERENCE_LOGGED_IN_PHONE_NUMBER)
                .apply();
        startActivity(
                new Intent(this, LoginActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        );
    }
}
