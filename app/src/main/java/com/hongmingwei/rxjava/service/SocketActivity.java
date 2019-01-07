package com.hongmingwei.rxjava.service;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.TextView;

import com.hongmingwei.rxjava.R;

import java.lang.ref.WeakReference;
/**
 * Created by Hongmingwei on 2018/5/21.
 * Email: 648600445@qq.com
 */

public class SocketActivity extends Activity {
    /**
     * TAG
     */
    private static final String TAG = SocketActivity.class.getSimpleName();
    /**
     * View
     */
    private TextView mResultText;
    /**
     * Params
     */
    private LocalBroadcastManager mLocalBroadcastManager;
    private IntentFilter mIntentFilter;
    private MessageBackReciver mReciver;
    private Intent mServiceIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mResultText = (TextView) findViewById(R.id.text);
        mReciver = new MessageBackReciver(mResultText);
        mServiceIntent = new Intent(this, SocketService.class);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(SocketService.HEART_BEAT_ACTION);
        mIntentFilter.addAction(SocketService.MESSAGE_ACTION);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(mServiceIntent);
            }
        });
    }



    @Override
    protected void onStart() {
        super.onStart();
        mLocalBroadcastManager.registerReceiver(mReciver, mIntentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocalBroadcastManager.unregisterReceiver(mReciver);
    }



    private class MessageBackReciver extends BroadcastReceiver{

        private WeakReference<TextView> mTextView;

        public MessageBackReciver(TextView textView){
            mTextView  = new WeakReference<TextView>(textView);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            TextView textView = mTextView.get();
            if (action.equals(SocketService.HEART_BEAT_ACTION)){
                if (textView != null){
                    textView.setText("得到一个心跳");
                }
            } else {
                String message = intent.getStringExtra("message");
                textView.setText(message);
            }
        }
    }
}
