package com.wiley.wrox.chapter7.wearabledatafromscratch;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

public class MyActivityWear extends Activity {

    private GoogleApiClient mGoogleApiClient;
    private TextView mTextView;
    private int mColor;
    BroadcastReceiver resultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_wear);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.v("wrox-wear", "Connection established");
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.v("wrox-wear", "Connection suspended");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.v("wrox-wear", "Connection failed");
                    }
                })
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);

                stub.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        String s = "X=" + event.getX();
                        s += ", Y=" + event.getY();
                        Log.v("wrox-wear", s);

                        if(mGoogleApiClient==null)
                            return false;

                        final PutDataMapRequest putRequest = PutDataMapRequest.create("/WEAR2PHONE");
                        final DataMap map = putRequest.getDataMap();
                        map.putFloat("touchX", event.getX());
                        map.putFloat("touchY", event.getY());
                        Wearable.DataApi.putDataItem(mGoogleApiClient, putRequest.asPutDataRequest());

                        return false;
                    }
                });
            }
        });

        resultReceiver = createBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                resultReceiver,
                new IntentFilter("wearable.localIntent"));
    }

    private void setBackgroundColor(int color) {
        Log.v("wrox-wear", "Arrived color:" + color);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setBackgroundColor(color);
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
                setBackgroundColor(Integer.parseInt(intent.getStringExtra("result")));
            }
        };
    }
}
