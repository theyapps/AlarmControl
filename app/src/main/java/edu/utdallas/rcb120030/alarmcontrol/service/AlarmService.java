package edu.utdallas.rcb120030.alarmcontrol.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import edu.utdallas.rcb120030.alarmcontrol.MainActivity;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class AlarmService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FETCH_STATUS = "edu.utdallas.rcb120030.alarmcontrol.action.FETCH_STATUS";
    private static final String ACTION_ARM = "edu.utdallas.rcb120030.alarmcontrol.action.ARM";
    private static final String ACTION_DISARM = "edu.utdallas.rcb120030.alarmcontrol.action.DISARM";

    public static final int FETCH_STATUS = 0;
    public static final int FETCH_ARM = 1;
    public static final int FETCH_DISARM = 2;

    // TODO: Rename parameters
    private static final String EXTRA_HOSTNAME = "edu.utdallas.rcb120030.alarmcontrol.extra.HOSTNAME";
    private static final String EXTRA_ARMED_OUT = "edu.utdallas.rcb120030.alarmcontrol.extra.ARMED_OUT";
    private static final String EXTRA_ALARM_OUT = "edu.utdallas.rcb120030.alarmcontrol.extra.ALARM_OUT";


    public static void startActionFetchStatus(Context context, String hostname) {
        Intent intent = new Intent(context, AlarmService.class);
        intent.setAction(ACTION_FETCH_STATUS);
        intent.putExtra(EXTRA_HOSTNAME, hostname);
        context.startService(intent);
    }

    public static void startActionArm(Context context, String hostname) {
        Intent intent = new Intent(context, AlarmService.class);
        intent.setAction(ACTION_ARM);
        intent.putExtra(EXTRA_HOSTNAME, hostname);
        context.startService(intent);
    }

    public static void startActionDisarm(Context context, String hostname) {
        Intent intent = new Intent(context, AlarmService.class);
        intent.setAction(ACTION_DISARM);
        intent.putExtra(EXTRA_HOSTNAME, hostname);
        context.startService(intent);
    }

    public AlarmService() {
        super("AlarmService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FETCH_STATUS.equals(action)) {
                final String hostname = intent.getStringExtra(EXTRA_HOSTNAME);
                handleActionFetchStatus(hostname);
            }
            else if (ACTION_ARM.equals(action)) {
                final String hostname = intent.getStringExtra(EXTRA_HOSTNAME);
                handleActionArm(hostname);
            }
            else if (ACTION_DISARM.equals(action)) {
                final String hostname = intent.getStringExtra(EXTRA_HOSTNAME);
                handleActionDisarm(hostname);
            }
        }
    }

    private void handleActionFetchStatus(String hostname) {
        // TODO: Handle action
        Log.d("AlarmService", "FetchStatus");

        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(hostname);
        HttpResponse response = null;
        HttpEntity entity = null;
        InputStream is = null;
        String json = null;
        JSONObject jObj = null;
        try {
            response = client.execute(get);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(response != null){
            entity = response.getEntity();
            try {
                is = entity.getContent();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(is != null) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            is, "iso-8859-1"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    json = sb.toString();
                    Log.d("AlarmService", json);
                } catch (Exception e) {
                    Log.e("Buffer Error", "Error converting result " + e.toString());
                }

                try {
                    jObj = new JSONObject(json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        /* Broadcast Result */
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("edu.utdallas.rcb120030.intent.action.FETCH_COMPLETE");
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        try {
            if (jObj != null) {
                broadcastIntent.putExtra(EXTRA_ARMED_OUT, jObj.getBoolean("armed"));
                broadcastIntent.putExtra(EXTRA_ALARM_OUT, jObj.getBoolean("alarm"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendBroadcast(broadcastIntent);
    }

    private void handleActionArm(String hostname) {
        // TODO: Handle action
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(hostname + "ARM");
        HttpResponse response;
        try {
            response = client.execute(post);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("AlarmService", "Arm");
    }

    private void handleActionDisarm(String hostname) {
        // TODO: Handle action
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(hostname + "DISARM");
        HttpResponse response;
        try {
            response = client.execute(post);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("AlarmService", "Disarm");
    }
}
