package com.funstergames.funstoosh.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.funstergames.funstoosh.R;

public class ReadyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ready);
    }


    public void start_countdown(View view)
    {
        Intent nextPage = new Intent(ReadyActivity.this, CountDownActivity.class);
        startActivity(nextPage);
    }

}
