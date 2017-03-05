package com.thinus.podcastftpbackup;

/**
 * Created by thinus on 2016/12/11.
 */
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MyScheduleReceiver extends BroadcastReceiver {

    // restart service every 30 seconds
    private static final long REPEAT_TIME = 1000 * 60 * 60 * 6;

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "PodcastFTPBackup MyScheduleReceiver", Toast.LENGTH_SHORT).show();
        MainActivity.LogMessage("PodcastFTPBackup MyScheduleReceiver");

        final long REPEAT_TIME2 = 1000 * 60 * 60 * 24;
        AlarmManager service = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, MyService.class);
        PendingIntent pending = PendingIntent.getService(context, 0, i, 0);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, 4);
        cal.set(Calendar.MINUTE, 00);
        cal.set(Calendar.SECOND, 00);
        service.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), REPEAT_TIME2, pending);

//        AlarmManager service = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        Intent i = new Intent(context, MyStartServiceReceiver.class);
//        PendingIntent pending = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
//        Calendar cal = Calendar.getInstance();
//        // start 30 seconds after boot completed
//        cal.add(Calendar.SECOND, 30);
//        // fetch every 30 seconds
//        // InexactRepeating allows Android to optimize the energy consumption
//        //service.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), REPEAT_TIME, pending);
//        service.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, cal.getTimeInMillis(), REPEAT_TIME, pending);
    }
}