package com.funstergames.funstoosh.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;

import com.funstergames.funstoosh.R;
import com.funstergames.funstoosh.Constants;
import com.google.firebase.iid.FirebaseInstanceId;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    public static final String PREFERENCE_LOGGED_IN = "logged_in";

    private static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;

    private EditText _countryCodeEdit;
    private EditText _phoneNumberEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (PreferenceManager.getDefaultSharedPreferences(this).contains(PREFERENCE_LOGGED_IN)) {
            startActivity(
                    new Intent(this, MenuActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            );
            return;
        }

        setContentView(R.layout.activity_login);

        _countryCodeEdit = (EditText)findViewById(R.id.country_code);
        _phoneNumberEdit = (EditText)findViewById(R.id.phone_number);

        if (ContextCompat.checkSelfPermission(this,  Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.READ_PHONE_STATE },
                    PERMISSIONS_REQUEST_READ_PHONE_STATE);
        } else {
            initializePhoneNumber();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_PHONE_STATE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initializePhoneNumber();
                }
                break;
        }
    }

    // From: http://stackoverflow.com/questions/5402253/getting-telephone-country-code-with-android
    private void initializePhoneNumber() {
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);

        String countryID = telephonyManager.getSimCountryIso().toUpperCase();
        if (countryID != null) {
            String[] rl = this.getResources().getStringArray(R.array.CountryCodes);
            for (int i = 0; i < rl.length; i++) {
                String[] g = rl[i].split(",");
                if (g[1].trim().equals(countryID.trim())) {
                    _countryCodeEdit.setText("+" + g[0]);
                    break;
                }
            }
        }

        String phoneNumber = telephonyManager.getLine1Number();
        if (phoneNumber != null) _phoneNumberEdit.setText(phoneNumber);
    }

    private int getAppVersion() {
        try {
            PackageInfo packageInfo = getPackageManager()
                    .getPackageInfo(getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public void login(View view) {
        Ion.with(this)
                .load("POST", Constants.ROOT_URL + "/users")
                .setBodyParameter("country_code", _countryCodeEdit.getText().toString())
                .setBodyParameter("phone_number", _phoneNumberEdit.getText().toString())
                .setBodyParameter("fcm_registration_id", FirebaseInstanceId.getInstance().getToken())
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> response) {
                        if (e != null || response.getHeaders().code() != 201) return;
                        PreferenceManager.getDefaultSharedPreferences(LoginActivity.this)
                                .edit()
                                .putBoolean(PREFERENCE_LOGGED_IN, true)
                                .apply();
                        startActivity(
                                new Intent(LoginActivity.this, MenuActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        );
                    }
                });
    }
}

