package com.funstergames.funstoosh.activities;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;

public class PictureActivity extends AppCompatActivity {

    public static final String EXTRA_BITMAP = "bitmap";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setImageBitmap((Bitmap)getIntent().getParcelableExtra(EXTRA_BITMAP));

        setContentView(imageView);
    }
}
