package com.thinus.podcastftpbackup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by thinus on 2016/12/11.
 */
public class MyStartServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "PodcastFTPBackup MyStartServiceReceiver", Toast.LENGTH_SHORT).show();
        MainActivity.LogMessage("PodcastFTPBackup MyStartServiceReceiver");
        Intent service = new Intent(context, MyService.class);
        context.startService(service);
    }
}
