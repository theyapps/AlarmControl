package edu.utdallas.rcb120030.alarmcontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import edu.utdallas.rcb120030.alarmcontrol.service.AlarmService;

public class MainActivity extends AppCompatActivity {
    private static String hostname = "http://192.168.0.7/";
    private static Boolean auto_refresh = false;

    private SharedPreferences preferences;

    private AlarmResponseReceiver alarmResponseReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button btnArm = (Button) findViewById(R.id.btn_arm);
        Button btnDisarm = (Button) findViewById(R.id.btn_disarm);
        Button btnRefresh = (Button) findViewById(R.id.btn_refesh);

        preferences = getSharedPreferences("edu.utdallas.rcb120030.alarmcontrol", MODE_PRIVATE);

        SharedPreferences.Editor edit = preferences.edit();

        if(!preferences.contains("ip_address")){
            edit.putString("ip_address", hostname);
        }
        if(!preferences.contains("auto_refresh")){
            edit.putBoolean("auto_refresh", auto_refresh);
        }

        edit.apply();

        hostname = preferences.getString("ip_address", "");
        auto_refresh = preferences.getBoolean("auto_refresh", false);

        btnArm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Arm();
            }
        });

        btnDisarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disArm();
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayStatus();
            }
        });

        IntentFilter filter = new IntentFilter("edu.utdallas.rcb120030.intent.action.FETCH_COMPLETE");
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        alarmResponseReceiver = new AlarmResponseReceiver();
        registerReceiver(alarmResponseReceiver, filter);

        Runnable refreshTask = new Runnable() {
            @Override
            public void run() {
                if(auto_refresh){
                    displayStatus();
                }
            }
        };

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        ScheduledFuture refreshHandle = scheduler.scheduleAtFixedRate(refreshTask, 5, 5, TimeUnit.SECONDS);
        displayStatus();
    }

    @Override
    protected void onRestart() {
        hostname = preferences.getString("ip_address", "");
        auto_refresh = preferences.getBoolean("auto_refresh", false);
        super.onRestart();
    }

    private void Arm() {
        AlarmService.startActionArm(getApplicationContext(), hostname);
    }

    private void disArm() {
        AlarmService.startActionDisarm(getApplicationContext(), hostname);
    }

    private void displayStatus(){
        AlarmService.startActionFetchStatus(getApplicationContext(), hostname);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(alarmResponseReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        IntentFilter filter = new IntentFilter("edu.utdallas.rcb120030.intent.action.FETCH_COMPLETE");
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        alarmResponseReceiver = new AlarmResponseReceiver();
        registerReceiver(alarmResponseReceiver, filter);

        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class AlarmResponseReceiver extends BroadcastReceiver {
        public static final String ALARM_RESP = "edu.utdallas.rcb120030.intent.action.FETCH_COMPLETE";

        @Override
        public void onReceive(Context context, Intent intent) {
            TextView tvArmed = (TextView) findViewById(R.id.txt_armed);
            TextView tvAlarm = (TextView) findViewById(R.id.txt_alarm);

            tvArmed.setText(intent.getBooleanExtra("edu.utdallas.rcb120030.alarmcontrol.extra.ARMED_OUT", false) ? "true" : "false");
            tvAlarm.setText(intent.getBooleanExtra("edu.utdallas.rcb120030.alarmcontrol.extra.ALARM_OUT", false) ? "true" : "false");
        }
    }
}
