package com.thinus.rpinotify;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.content.Intent;

/**
 * Created by thinus on 2016/09/20.
 */
public class AlarmActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = this.getIntent();
        boolean start = i.getExtras().getBoolean("Start");
        if (start) {
        /*GateOpen*/
            GateAlert galert = new GateAlert();
            galert.show(getFragmentManager(), "GateAlert");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        boolean start = intent.getExtras().getBoolean("Start");
        if (!start) {
            this.finish();
        }
    }
}
