package com.thinus.podcastftpbackup;

/**
 * Created by thinus on 2016/12/11.
 */
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MyService extends Service {
    private NotificationManager mNM;
    private doUploadAsync doSync;

    @Override
    public void onCreate()
    {
        Toast.makeText(this, "PodcastFTPBackup onCreate", Toast.LENGTH_SHORT).show();
        MainActivity.LogMessage("PodcastFTPBackup MyService onCreate");
        Log.v("MyService", "onCreate");
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        showNotification();
        //super.onCreate();
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "PodcastFTPBackup onDestroy", Toast.LENGTH_SHORT).show();
        MainActivity.LogMessage("PodcastFTPBackup MyService onDestroy");
        Log.v("MyService", "onDestroy");
        //super.onDestroy();
        if (doSync != null) {
            if (doSync.stop == 0) {
                doSync.stop = 1;
                Toast.makeText(this, "PodcastFTPBackup onDestroy waiting for stop", Toast.LENGTH_SHORT).show();
                MainActivity.LogMessage("PodcastFTPBackup MyService onDestroy waiting for stop");
                try {
                    while (doSync.stop == 1) {
                        Thread.sleep(1000);
                    }
                    Toast.makeText(this, "PodcastFTPBackup onDestroy stopped", Toast.LENGTH_SHORT).show();
                    MainActivity.LogMessage("PodcastFTPBackup MyService onDestroy stopped");
                } catch (Exception et) {

                }
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "PodcastFTPBackup onStartCommand", Toast.LENGTH_SHORT).show();
        MainActivity.LogMessage("PodcastFTPBackup MyService onStartCommand");
        Log.v("MyService", "onStartCommand" );
        //return super.onStartCommand(intent, flags, startId);
        //return Service.START_STICKY;

        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String wifiSSID = wifiInfo.getSSID();
        if (wifiSSID.contains("MudNinja")) {
            MainActivity.LogMessage("PodcastFTPBackup MyService onStartCommand doUploadAsync");
            doSync = new doUploadAsync();
            doSync.stop = 0;
            doSync.execute();
        } else {
            MainActivity.LogMessage("PodcastFTPBackup MyService onStartCommand not on MudNinja");
        }
        MainActivity.LogMessage("PodcastFTPBackup MyService onStartCommand done");
        return Service.START_NOT_STICKY;
    }
    @Override
    public IBinder onBind( Intent arg0 ) {
        Toast.makeText(this, "PodcastFTPBackup onBind", Toast.LENGTH_SHORT).show();
        MainActivity.LogMessage("PodcastFTPBackup MyService onBind");
        Log.v("MyService", "onBind");
        return null;
    }

    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = "Running";

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                //.setSmallIcon(R.drawable.stat_sample)  // the status icon
                .setTicker(text)  // the status text
                //.setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle("PodcastFTPService")  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        mNM.notify(0, notification);
    }
}