package com.funstergames.funstoosh.activities;

import android.graphics.Bitmap;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.funstergames.funstoosh.Constants;
import com.funstergames.funstoosh.PicassoHelper;
import com.squareup.picasso.Picasso;

public class PictureActivity extends AppCompatActivity {

    public static final String EXTRA_PICTURE_ID = "picture_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(450,450));
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        PicassoHelper.getPicasso(this)
                .load(Constants.ROOT_URL + "/pictures/" + getIntent().getStringExtra(EXTRA_PICTURE_ID))
                .into(imageView);

        setContentView(imageView);
    }
}
