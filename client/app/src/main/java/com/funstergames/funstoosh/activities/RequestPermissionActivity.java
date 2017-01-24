package com.funstergames.funstoosh.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.funstergames.funstoosh.services.GameService;

public class RequestPermissionActivity extends AppCompatActivity {
    public static final String ACTION_PERMISSION_RESPONSE = "permission_response";

    public static final String EXTRA_SERVICE = "service";
    public static final String EXTRA_PERMISSION = "permission";
    public static final String EXTRA_PERMISSION_RESULT = "permission_result";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this,
                new String[] { getIntent().getStringExtra(EXTRA_PERMISSION) },
                0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Intent intent = new Intent(this, (Class)getIntent().getSerializableExtra(EXTRA_SERVICE));
        intent.putExtras(getIntent().getExtras());
        intent.setAction(ACTION_PERMISSION_RESPONSE);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            intent.putExtra(EXTRA_PERMISSION_RESULT, true);
        } else {
            intent.putExtra(EXTRA_PERMISSION_RESULT, false);
        }
        startService(intent);

        finish();
    }
}
