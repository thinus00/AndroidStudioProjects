package com.thinus.tanklevels;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    private static final int SERVERPORT = 3490;
    private static final String SERVER_IP = "192.168.1.106";
    private static final String SERVER_IP_INET = "thinus00-lte.dyndns.org";

    private TextView response;
    private ListView listView;
    private CustomBaseAdapterTankList baseTankListAdapter;

    public static ArrayList<Tank> tankItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tankItems = new ArrayList<Tank>();
        response = (TextView) findViewById(R.id.debug);
        listView = (ListView) findViewById(R.id.listViewTankLevels);
        baseTankListAdapter = new CustomBaseAdapterTankList(this, tankItems);
        listView.setAdapter(baseTankListAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        baseTankListAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.refresh) {
            refreshRTValues();
            return true;
        }
//        if (id == R.id.refreshLocal) {
//            refreshRTValuesLocal();
//            return true;
//        }
//        if (id == R.id.refreshInet                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  ) {
//            refreshRTValuesInet();
//            return true;
//        }


        return super.onOptionsItemSelected(item);
    }

    private void refreshRTValues() {
        response.setText("Checking Wifi...\n");
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String wifiSSID = wifiInfo.getSSID();
        response.setText(response.getText() + "...found " + wifiSSID + "\n");

        if (wifiSSID.contains("MudNinja")) {
            refreshRTValuesLocal();
        }
        else {
            refreshRTValuesInet();
        }
        baseTankListAdapter.notifyDataSetChanged();
    }

    private void refreshRTValuesLocal() {
        response.setText(response.getText() + "Refreshing Local...");
        Client myClient = new Client(SERVER_IP, SERVERPORT, response, tankItems);
        myClient.execute();
    }

    private void refreshRTValuesInet() {
        response.setText(response.getText() + "Refreshing Internet...");
        Client myClient = new Client(SERVER_IP_INET, SERVERPORT, response, tankItems);
        myClient.execute();
    }
}
