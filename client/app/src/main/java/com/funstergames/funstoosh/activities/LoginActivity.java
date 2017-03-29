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
import android.widget.TextView;

import com.funstergames.funstoosh.R;
import com.funstergames.funstoosh.Constants;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.body.JSONObjectBody;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONObject;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    public static final String PREFERENCE_LOGGED_IN_PHONE_NUMBER = "logged_in_phone_number";

    private static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;

    private EditText _countryCodeEdit;
    private EditText _phoneNumberEdit;
    private TextView _errorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (PreferenceManager.getDefaultSharedPreferences(this).contains(PREFERENCE_LOGGED_IN_PHONE_NUMBER)) {
            startActivity(new Intent(this, MenuActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        _countryCodeEdit = (EditText)findViewById(R.id.country_code);
        _phoneNumberEdit = (EditText)findViewById(R.id.phone_number);
        _errorText = (TextView)findViewById(R.id.error);

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

    public void login(View view) {
        _errorText.setVisibility(View.INVISIBLE);
        Ion.with(this)
                .load("POST", Constants.ROOT_URL + "/users")
                .setBodyParameter("country_code", _countryCodeEdit.getText().toString())
                .setBodyParameter("phone_number", _phoneNumberEdit.getText().toString())
                .setBodyParameter("fcm_registration_id", FirebaseInstanceId.getInstance().getToken())
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> response) {
                        if (e != null || response.getHeaders().code() != 201) {
                            _errorText.setVisibility(View.VISIBLE);
                            return;
                        }
                        PreferenceManager.getDefaultSharedPreferences(LoginActivity.this)
                                .edit()
                                .putString(PREFERENCE_LOGGED_IN_PHONE_NUMBER, response.getResult().get("phone_number").getAsString())
                                .apply();
                        startActivity(new Intent(LoginActivity.this, MenuActivity.class));
                        finish();
                    }
                });
    }
}

