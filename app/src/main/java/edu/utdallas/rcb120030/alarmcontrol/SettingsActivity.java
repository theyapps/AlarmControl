package edu.utdallas.rcb120030.alarmcontrol;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private EditText et_ipAddress;
    private Switch sw_autoRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        preferences = getSharedPreferences("edu.utdallas.rcb120030.alarmcontrol", MODE_PRIVATE);

        et_ipAddress = (EditText) findViewById(R.id.ipAddress);
        sw_autoRefresh = (Switch) findViewById(R.id.autoRefresh);

        et_ipAddress.setText(preferences.getString("ip_address", ""));
        sw_autoRefresh.setChecked(preferences.getBoolean("auto_refresh", false));
    }

    @Override
    protected void onPause() {
        SharedPreferences.Editor edit = preferences.edit();

        String ip_address = et_ipAddress.getText().toString();
        ip_address.replaceFirst("^(http://|https://)","");
        ip_address.replaceAll("/$", "");

        edit.putString("ip_address", ip_address);
        edit.putBoolean("auto_refresh", sw_autoRefresh.isChecked());

        edit.commit();
        super.onPause();
    }
}
