package com.example.network;

import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.network.net.NetworkReceiver;

public class MainActivity extends AppCompatActivity {
    private TextView mTextView;
    private NetworkReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.text);
        mReceiver = new NetworkReceiver(new NetworkReceiver.OnNetworkConnectedListener() {
            @Override
            public void onNetworkConnected(String netName, String netId) {
                mTextView.setText("net work name: " + netName + ", wifi net id: " + netId);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }
}
