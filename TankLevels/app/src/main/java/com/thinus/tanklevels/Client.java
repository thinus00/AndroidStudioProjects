package com.thinus.tanklevels;

/**
 * Created by thinus on 2015/11/24.
 */
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.os.AsyncTask;
import android.widget.TextView;

public class Client extends AsyncTask<Void, String, Void> {

    String dstAddress;
    int dstPort;
    String response = "";
    TextView textResponse;
    ArrayList<Tank> tankList;

    Client(String addr, int port, TextView textResponse, ArrayList<Tank> tankList) {
        dstAddress = addr;
        dstPort = port;
        this.textResponse = textResponse;
        this.tankList = tankList;
    }

    @Override
    protected Void doInBackground(Void... arg0) {

        Socket socket = null;

        try {
            publishProgress("Creating Socket...");
            socket = new Socket(dstAddress, dstPort);

            publishProgress("Sending q...");
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
            out.println("q");


            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
            byte[] buffer = new byte[1024];

            int bytesRead;
            InputStream inputStream = socket.getInputStream();

         /*
          * notice: inputStream.read() will block if no data return
          */
            publishProgress("Reading response...");
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
                response += byteArrayOutputStream.toString("UTF-8");
            }
            publishProgress("Done with socket.");

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            response = "UnknownHostException: " + e.toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            response = "IOException: " + e.toString();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            response = "Exception: " + e.toString();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... params) {
        super.onProgressUpdate(params);
        textResponse.setText(params[0]);
    }

    @Override
    protected void onPostExecute(Void result) {
        textResponse.setText("Processing data.");
        String[] tanks = response.split(";");
        for (int i=0; i < tanks.length; i++) {
            String[] tankValues = tanks[i].split(",");

            int tankId = Integer.parseInt(tankValues[0]);
            String timeStamp = tankValues[1];
            int value = Integer.parseInt(tankValues[2]);

            boolean foundTank = false;
            for (int j=0; j<MainActivity.tankItems.size(); j++ ){
                Tank tmpTank = MainActivity.tankItems.get(j);
                if (tmpTank.getTankID() == tankId) {
                    //udpate
                    foundTank = true;
                    tmpTank.setTimeStamp(timeStamp);
                    tmpTank.setValue(value);
                    break;
                }
            }
            if (!foundTank) {
                Tank tankItem = new Tank(tankId, timeStamp, value);
                MainActivity.tankItems.add(tankItem);
            }
        }
        textResponse.setText("Data processed.");
        super.onPostExecute(result);
    }

}
