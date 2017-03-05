package com.thinus.podcastftpbackup;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Intent intent = new Intent( this, MyService.class );
        //this.startService(intent);

        LogMessage("MainActivity onCreate");
    }

    public void onbtnTestClick(View v) {
        LogMessage("MainActivity onbtnTestClick");
        Intent service = new Intent(this, MyService.class);

        this.startService(service);
    }

    public void onbtnStartServiceClick(View v) {
        final long REPEAT_TIME = 1000 * 60 * 60 * 24;
        LogMessage("MainActivity onbtnStartServiceClick");
        AlarmManager service = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        //Intent i = new Intent(this, MyStartServiceReceiver.class);
        Intent i = new Intent(this, MyService.class);
      //  PendingIntent pending = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pending = PendingIntent.getService(this, 0, i, 0);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        // start 30 seconds after boot completed
        //cal.add(Calendar.SECOND, 10);
        cal.set(Calendar.HOUR_OF_DAY, 4);
        cal.set(Calendar.MINUTE, 00);
        cal.set(Calendar.SECOND, 00);
        // fetch every 30 seconds
        // InexactRepeating allows Android to optimize the energy consumption
        //service.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), REPEAT_TIME, pending);
        LogMessage("MainActivity onbtnStartServiceClick setRepeating " + cal.getTime().toString());
        //service.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, cal.getTimeInMillis(), REPEAT_TIME, pending);
        service.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), REPEAT_TIME, pending);
    }

    public void onbtnStopServiceClick(View v) {
        LogMessage("MainActivity onbtnStopServiceClick");
        Intent intent = new Intent( this, MyService.class );
        this.stopService(intent);
    }

    public static void LogMessage(String message){
        try {
            String msgDate = DateFormat.getDateTimeInstance().format(new Date());
            String fileName;
            fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/PodcastPDFBackup.txt";

            FileWriter f;
            f = new FileWriter(fileName, true);
            f.write(msgDate + " - " + message + "\n\r");
            f.flush();
            f.close();

        } catch (IOException ioe) {
            final String message2 = ioe.getMessage();
        }
    }

}
