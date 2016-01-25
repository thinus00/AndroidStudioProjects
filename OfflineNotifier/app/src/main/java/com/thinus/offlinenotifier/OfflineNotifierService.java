package com.thinus.offlinenotifier;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import java.io.IOException;

/**
 * Created by thinus on 2015/01/02.
 */
public class OfflineNotifierService extends Service {
    boolean _terminated = false;
    static boolean _threadRunning = false;
    NotificationManager manager;

    @Override
    public void onCreate() {
        manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!_threadRunning) {
            _terminated = false;
            Runnable r = new Runnable() {
                public void run() {

                    int icount = 0;
                    boolean firstRun = true;
                    int offlinecount = 0;
                    int waittime = 60000;

                    while (!_terminated) {
                        if (firstRun) {
                            manager.notify(0, new NotificationCompat.Builder(OfflineNotifierService.this)
                                    .setSmallIcon(R.drawable.ic_launcher)
                                    .setContentText("ServiceStarted")
                                    .setContentTitle("OfflineNotifier")
                                    .build());
                            firstRun  = false;
                        }
                        synchronized (this) {
                            try {
                                icount++;
                                String icountstr;

                                if (!isOnline()) {
                                    offlinecount++;
                                    if (offlinecount > 6) {
                                        manager.notify(0, new NotificationCompat.Builder(OfflineNotifierService.this)
                                                .setSmallIcon(R.drawable.ic_launcher)
                                                .setContentText("OFFLINE")
                                                .setContentTitle("OfflineNotifier")
                                                .build());
                                        //ALARM
                                        try {
                                            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                                            r.play();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        manager.notify(0, new NotificationCompat.Builder(OfflineNotifierService.this)
                                                .setSmallIcon(R.drawable.ic_launcher)
                                                .setContentText("Offline - " + String.valueOf(offlinecount))
                                                .setContentTitle("OfflineNotifier")
                                                .build());
                                        waittime = 10000;
                                    }
                                } else {
                                    if (offlinecount > 0) {
                                        manager.notify(0, new NotificationCompat.Builder(OfflineNotifierService.this)
                                                .setSmallIcon(R.drawable.ic_launcher)
                                                .setContentText("Online")
                                                .setContentTitle("OfflineNotifier")
                                                .build());
                                        offlinecount = 0;
                                        waittime = 60000;
                                    } else {
                                        manager.cancel(0);
                                    }
                                }
                                Thread.sleep(waittime);

                            } catch (Exception e) {
                            }
                        }
                    }
                    _threadRunning = false;
                }
            };

            Thread t = new Thread(r);
            t.start();

            _threadRunning = true;
        }

        return START_STICKY;
        //return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (_threadRunning){
            _terminated = true;
        }
        manager.notify(0, new NotificationCompat.Builder(OfflineNotifierService.this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentText("ServiceStopped")
                .setContentTitle("OfflineNotifier")
                .build());

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {

            Process ipProcess = runtime.exec("/system/bin/ping -W10 -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }

}


