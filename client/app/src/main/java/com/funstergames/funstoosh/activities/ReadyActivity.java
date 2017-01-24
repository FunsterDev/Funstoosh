package com.funstergames.funstoosh;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void start_countdown(View view)
    {
        Intent nextPage = new Intent(MainActivity.this, CountDownView.class);
        startActivity(nextPage);
    }

}
