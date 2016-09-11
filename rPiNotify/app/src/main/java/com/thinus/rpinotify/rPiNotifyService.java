package com.thinus.rpinotify;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by thinus on 2016/08/23.
 */
public class rPiNotifyService extends Service {

    private static final String TAG = "rPiNotifyService";
    private boolean isRunning  = false;



    NotificationManager mNotifyManager = null;
    NotificationCompat.Builder mBuilderService = null;
    NotificationCompat.Builder mBuilderEvent = null;

    @Override
    public IBinder onBind(Intent arg0) {
        Log.i(TAG, "Service onBind");
        return null;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "Service onCreate");

        isRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilderService = new NotificationCompat.Builder(this);
        mBuilderService.setContentTitle("rPiNotify").setContentText("Test Text").setSmallIcon(R.drawable.abc_switch_thumb_material);

        mBuilderEvent = new NotificationCompat.Builder(this);
        mBuilderEvent.setContentTitle("Event").setContentText("").setSmallIcon(R.drawable.abc_btn_check_to_on_mtrl_015);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilderEvent.setSound(alarmSound);

        mBuilderService.setContentText("Started");
        mNotifyManager.notify(0, mBuilderService.build());

        //Creating new thread for my service
        //Always write your long running tasks in a separate thread, to avoid ANR
        new Thread(new Runnable() {
            @Override
            public void run() {

                if(isRunning){
                    Log.i(TAG, "Service Thread started");
                }

                Socket socket = null;
                boolean connected = false;
                boolean WiFiConnected = false;
                String response = "";
                String tosend = "[p]";

                while (isRunning) {
                    try {
                        Thread.sleep(1000);
                        Log.i(TAG, "Service Thread running");

                        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        String wifiSSID = wifiInfo.getSSID();

                        if (!connected) {
                            response = "";
                            if (wifiSSID.contains("MudNinja")) {
                                Log.i(TAG, "Service Thread connecting WIFI");
                                socket = new Socket();
                                socket.connect(new InetSocketAddress(MainActivity.SERVER_IP, MainActivity.SERVERPORT), 5000);
                                WiFiConnected = true;
                            } else {
                                Log.i(TAG, "Service Thread connecting Internet");
                                socket = new Socket();
                                socket.connect(new InetSocketAddress(MainActivity.SERVER_IP_INET, MainActivity.SERVERPORT), 5000);
                                WiFiConnected = false;
                            }
                        }
                        else {
                            if (!WiFiConnected) {
                                if (wifiSSID.contains("MudNinja")) {
                                    if (socket != null) {
                                        try {
                                            socket.close();
                                        } catch (IOException e) {
                                            // TODO Auto-generated catch block
                                            e.printStackTrace();
                                            Log.i(TAG, "Service Thread Close IOException" + e.toString());
                                        }
                                    }
                                }
                            }
                        }

                        if (socket.isConnected()) {
                            connected = true;
                            boolean ping = false;

                            if (!tosend.isEmpty()) {
                                ping = true;
                                Log.i(TAG, "Service Thread Connected Sending Ping");
                                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                                out.println(tosend);
                                tosend = "";
                            } else {
                                ping = false;
                                Log.i(TAG, "Service Thread Connected Waiting for event...");
                                tosend = "[p]";
                            }
                            response = "";

                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                            byte[] buffer = new byte[1024];
                            byte[] fullbuffer = new byte[1024000];
                            int totbytesRead = 0;
                            int bytesRead = 0;
                            Log.i(TAG, "Service Thread Connected Reading...");
                            InputStream inputStream = socket.getInputStream();
                            socket.setSoTimeout(5000);
                            try {
                                //while ((!response.endsWith("]")) && ((bytesRead = inputStream.read(buffer)) != -1)) {
                                byte lastbyte = 0;
                                while ((lastbyte != 93) && ((bytesRead = inputStream.read(buffer)) != -1)) {
                                    //byteArrayOutputStream.write(buffer, 0, bytesRead);
                                    //response += byteArrayOutputStream.toString("UTF-8");
                                    for (int i = 0; i < bytesRead; i++) {
                                        fullbuffer[totbytesRead++] = buffer[i];
                                    }
                                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                                    response += byteArrayOutputStream.toString("UTF-8");

                                    if (bytesRead > 0) lastbyte = buffer[bytesRead-1];
                                    else lastbyte = 0;

                                    Log.i(TAG, "Service Thread Connected response (" + bytesRead + "):" + response);
                                }
                            } catch (SocketTimeoutException se) {
                                Log.i(TAG, "Service Thread Connected read timeout");
                            }
                            Log.i(TAG, "Service Thread Connected read (" + bytesRead + "): " + response);

                            if (bytesRead == -1) {
                                if (socket != null) {
                                    try {
                                        socket.close();
                                    } catch (IOException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                        Log.i(TAG, "Service Thread Close IOException" + e.toString());
                                    }
                                }
                            } else {

                                if (bytesRead > 0) {

                                    if (response.equals("[p]")) {
                                        mBuilderService.setSmallIcon(R.drawable.abc_switch_thumb_material);
                                        if (WiFiConnected)
                                            mBuilderService.setContentText("Online WiFi");
                                        else
                                            mBuilderService.setContentText("Online Inet");
                                          mNotifyManager.notify(0, mBuilderService.build());

                                    } else {
                                        //received event...show it
                                        if (response.startsWith("[1;")) {
                                            //01234567890123456789012
                                            //[1;1;2016-9-9 17:21:52]
                                            //Command No:1; Value:1; Timestamp:2016-9-9 17:21:52

                                            mBuilderEvent.setContentTitle("Buzzer");
                                            mBuilderEvent.setContentIntent(null);
                                            String dateStr = response.substring(5,response.length()-6);
                                            if (response.substring(2,5).equals(";1;"))
                                            {
                                                String notificationMessage = "Buzz";
                                                Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
                                                notificationIntent.putExtra("NotificationMessage", notificationMessage);
                                                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                PendingIntent pendingNotificationIntent = PendingIntent.getActivity(getApplicationContext(),1,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);
                                                mBuilderEvent.setContentIntent(pendingNotificationIntent);
                                                mBuilderEvent.setContentText("Open - " + dateStr);
                                                Notification notif = mBuilderEvent.build();
                                                notif.flags |= Notification.FLAG_AUTO_CANCEL;
                                                mNotifyManager.notify(1, notif);
                                            }
                                            else {
                                                mNotifyManager.cancel(1);
                                            }

                                        }
                                        if (response.startsWith("[2;")) {
                                            String dateStr = response.substring(5,response.length()-6);
                                            mBuilderEvent.setContentTitle("Beam");
                                            String notificationMessage = "Beam";
                                            Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
                                            notificationIntent.putExtra("NotificationMessage", notificationMessage);
                                            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            PendingIntent pendingNotificationIntent = PendingIntent.getActivity(getApplicationContext(),1,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);
                                            mBuilderEvent.setContentIntent(pendingNotificationIntent);
                                            mBuilderEvent.setContentText("trigger - " + dateStr);
                                            //tosend = "[h1]";
                                            Notification notif = mBuilderEvent.build();
                                            notif.flags |= Notification.FLAG_AUTO_CANCEL;
                                            mNotifyManager.notify(2, notif);
                                        }
                                        if (response.startsWith("[h1;")) {
                                            mBuilderEvent.setContentTitle("Image");
                                            Notification notif = mBuilderEvent.build();
                                            notif.flags |= Notification.FLAG_AUTO_CANCEL;
                                            mNotifyManager.notify(81, notif);
                                        }

                                    }
                                }
                                else {
                                    if (ping) {
                                        mBuilderService.setContentText("Timeout").setSmallIcon(R.drawable.abc_btn_radio_material);;
                                        mNotifyManager.notify(0, mBuilderService.build());
                                    }
                                }
                            }
                        }
                        else {
                            connected = false;
                            //abc_btn_radio_material
                            mBuilderService.setContentText("Offline").setSmallIcon(R.drawable.abc_btn_radio_material);;
                            mNotifyManager.notify(0, mBuilderService.build());
                        }

                    } catch (UnknownHostException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Log.i(TAG, "Service Thread UnknownHostException" + e.toString());
                        mBuilderService.setContentText("UnknownHost").setSmallIcon(R.drawable.abc_btn_radio_material);;
                        mNotifyManager.notify(0, mBuilderService.build());
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Log.i(TAG, "Service Thread IOException" + e.toString());
                        mBuilderService.setContentText("Offline").setSmallIcon(R.drawable.abc_btn_radio_material);;
                        mNotifyManager.notify(0, mBuilderService.build());
                        try {
                            Thread.sleep(10000);
                        } catch (Exception et) {

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i(TAG, "Service Thread Exception" + e.toString());
                        mBuilderService.setContentText("Exception").setSmallIcon(R.drawable.abc_btn_radio_material);;
                        mNotifyManager.notify(0, mBuilderService.build());
                    }
                }

                if(!isRunning){
                    Log.i(TAG, "Service Thread stopped");
                }

                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                //Stop service once it finishes its task
                stopSelf();
            }
        }).start();

        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service onDestroy");
        isRunning = false;
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
        if ((mNotifyManager != null) && (mBuilderService != null)) {
            mBuilderService.setContentText("Stopped");
            mBuilderService.setSmallIcon(R.drawable.abc_menu_hardkey_panel_mtrl_mult);
            mNotifyManager.notify(0, mBuilderService.build());
        }
    }

}
