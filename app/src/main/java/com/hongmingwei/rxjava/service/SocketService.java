package com.hongmingwei.rxjava.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


import com.hongmingwei.rxjava.util.Base64;
import com.hongmingwei.rxjava.util.TcpBase;
import com.hongmingwei.rxjava.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Created by Hongmingwei on 2018/5/21.
 * Email: 648600445@qq.com
 */

public class SocketService extends Service {
    /**
     * TAG
     */
    private static final String TAG = "SocketService";

    private static final String IP = "123.56.219.76";
    private static final int PORT = 9527;
    private WeakReference<Socket> mSocket;
    private LocalBroadcastManager mLocalBroadcastManager;
    private ReadThread mReadThread;
    private Handler mHandler = new Handler();
    private static final long HEART_BEAT_RATE = 3 * 1000;
    private long sendTime = 0L;
    public static final String HEART_BEAT_ACTION="org.feng.heart_beat_ACTION"; //心跳广播
    public static final String MESSAGE_ACTION="org.feng.message_ACTION"; //消息广播
    private final String HEADER = "www.kdcaopan.com";

    @Override
    public void onCreate() {
        super.onCreate();
        new InitSocketThread().start();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendMsg();
        return super.onStartCommand(intent, flags, startId);
    }

    private boolean sendMsg(){
        if (null == mSocket || mSocket.get() == null){
            return false;
        }
        Socket socket = mSocket.get();
        try {
            if (!socket.isClosed() && !socket.isOutputShutdown()) {
                Log.e(TAG, "sendMsg: ===========");
                OutputStream os = socket.getOutputStream();
                byte[] headerByte = HEADER.getBytes();
                byte[] contentByte = getSendData();
                int len = headerByte.length + 4 + contentByte.length;
                byte[] lenByte = TcpBase.byteReversed(TcpBase.intToBytes2(contentByte.length));
                byte[] byteRequest = new byte[len];
                System.arraycopy(headerByte, 0, byteRequest, 0, headerByte.length);
                for (int i = 0; i < 4; i++) {
                    System.arraycopy(lenByte, 0, byteRequest, headerByte.length, lenByte.length);
                }
                System.arraycopy(contentByte, 0, byteRequest, headerByte.length + lenByte.length, contentByte.length);
                os.write(byteRequest, 0, byteRequest.length);
                os.flush();
                sendTime = System.currentTimeMillis();
            } else {
                return false;
            }
        } catch (IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void initSocket(){
        try {
            Socket socket = new Socket(IP, PORT);
            mSocket = new WeakReference<Socket>(socket);
            mReadThread = new ReadThread(socket);
            mReadThread.start();
            mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);
        } catch (UnknownHostException e){
            e.printStackTrace();
            Log.e(TAG, "initSocket: ======111===" + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "initSocket: ========222======" + e.getMessage() );
        }
    }

    private void releaseLastSocket(WeakReference<Socket> sockets){
        try {
            if (sockets != null){
                Socket socket = sockets.get();
                if (!socket.isClosed()){
                    socket.close();
                }
                socket.close();
                sockets.clear();
                socket = null;
                sockets = null;
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }


    private Runnable heartBeatRunnable = new Runnable() {
        @Override
        public void run() {
            if (System.currentTimeMillis() - sendTime >= HEART_BEAT_RATE){
                boolean isSuccess = sendMsg();
                if (!isSuccess){
                    Log.e(TAG, "run: ===========");
                    mHandler.removeCallbacks(heartBeatRunnable);
                    mReadThread.release();
                    releaseLastSocket(mSocket);
                    new InitSocketThread().start();
                }
            }
            mHandler.postDelayed(this, HEART_BEAT_RATE);
        }
    };

    class InitSocketThread extends Thread{

        @Override
        public void run() {
            super.run();
            initSocket();
        }
    }

    private class ReadThread extends Thread{

        private WeakReference<Socket> mWeakSocket;
        private boolean isStart = true;

        public ReadThread(Socket socket){
            mWeakSocket = new WeakReference<Socket>(socket);
        }

        private void release(){
            isStart = false;
            releaseLastSocket(mWeakSocket);
        }

        @Override
        public void run() {
            super.run();
            Socket socket = mWeakSocket.get();
            if (socket != null){
                try {
                    InputStream is = socket.getInputStream();
                    byte[] buffer = new byte[1024 * 8];
                    int length = 0;
                    while(!socket.isClosed() && !socket.isInputShutdown() && isStart && ((length = is.read(buffer)) != -1)){
                        if (length > 0){
                            String message = new String(Arrays.copyOf(buffer, length)).trim();
                            Log.e(TAG, "run: =========== input");
                            if (message.equals("ok")){//处理心跳回复
                                Intent intent = new Intent(HEART_BEAT_ACTION);
                                mLocalBroadcastManager.sendBroadcast(intent);
                            } else {
                                Intent intent = new Intent(MESSAGE_ACTION);
                                intent.putExtra("message", message);
                                mLocalBroadcastManager.sendBroadcast(intent);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private byte[] getSendData() {
        String data = "";
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "select_goods");
            jsonObject.put("data", new JSONArray());
            data = Base64.encode(jsonObject.toString() + Utils.md5(jsonObject.toString() + "chengrongshangyangxiaojunlihuangde"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data.getBytes();
    }

}
