package com.wiley.wrox.chapter7.wearabledatafromscratch;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

public class MyActivityPhone extends Activity {

    private GoogleApiClient mGoogleApiClient;
    private int colorCount = 0;
    BroadcastReceiver resultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_phone);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.v("wrox-mobile", "Connection established");
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.v("wrox-mobile", "Connection suspended");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.v("wrox-mobile", "Connection failed");
                    }
                })
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();

        resultReceiver = createBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                resultReceiver,
                new IntentFilter("phone.localIntent"));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_activity_phone, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void syncDataItem(View view) {
        if(mGoogleApiClient==null)
            return;

        int r = (int) (255 * Math.random());
        int g = (int) (255 * Math.random());
        int b = (int) (255 * Math.random());

        final PutDataMapRequest putRequest = PutDataMapRequest.create("/PHONE2WEAR");
        final DataMap map = putRequest.getDataMap();
        map.putInt("color", Color.rgb(r,g,b));
        map.putString("colorChanges", "Amount of changes: " + colorCount++);
        Wearable.DataApi.putDataItem(mGoogleApiClient,  putRequest.asPutDataRequest());

        Log.v("wrox-mobile", "Handheld sent new random color to watch");
        Log.v("wrox-mobile", "color:" + r + ", " + g + ", " + b);
        Log.v("wrox-mobile", "iteration:" + colorCount);
    }

    private void updateTextField(String text) {
        Log.v("wrox-mobile", "Arrived text:" + text);
        ((TextView)findViewById(R.id.reply_text)).setText(text);
    }

    @Override
    protected void onDestroy() {
        if (resultReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(resultReceiver);
        }
        super.onDestroy();
    }

    private BroadcastReceiver createBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateTextField(intent.getStringExtra("result"));
            }
        };
    }

}
