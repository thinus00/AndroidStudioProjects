package com.thinus.podcastcopyftp;

import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button uploadButton = (Button) findViewById(R.id.buttonUpload);
        uploadButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        buttonuploadClick(v);
                    }
                }
        );
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

        return super.onOptionsItemSelected(item);
    }

    private void buttonuploadClick(View view) {
        ((TextView) findViewById(R.id.textViewMemo)).setText("");

        new doUploadAsync().execute(((EditText) findViewById(R.id.editTextFTPServer)).getText(), ((EditText) findViewById(R.id.editTextPodCasts)).getText());
    }




    class doUploadAsync extends AsyncTask {


        class FileRecord{
            public String FileName;
            public Date Filedate;
            public String FilePath;
            public String FolderPath;
            //public String FolderDate;
        }
        private Exception exception;
        EditText editTextServer;
        TextView memo;
        ScrollView scroll_view;
        doUploadAsync() {
            editTextServer = (EditText) findViewById(R.id.editTextFTPServer);
            memo = (TextView) findViewById(R.id.textViewMemo);
            scroll_view = (ScrollView) findViewById(R.id.scrollView);
        }

        @Override
        protected Object doInBackground(Object[] params) {
            try {
                List<FileRecord> fileList = new ArrayList<FileRecord>();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                //local files
                File extStorageDirectory = Environment.getExternalStorageDirectory();
                String path = extStorageDirectory.toString()+"/Android/data/de.danoeh.antennapod/files/media/";
                publishProgress("Checking " + path);

                addLocalFiles(path, "", fileList);

                publishProgress("Creating FTP Object....");
                FTPClient ftpClient = new FTPClient();
                publishProgress("Server IP: " + params[0]);
                publishProgress("Working folder: " + params[1]);

                String[] FTPServerIPs = params[0].toString().split("\\.");
                String WorkingDir = params[1].toString();
                publishProgress(FTPServerIPs.length);
                publishProgress("Parsing address "+FTPServerIPs[0]+"."+FTPServerIPs[1]+"."+FTPServerIPs[2]+"."+FTPServerIPs[3]+"."+"....");
                InetAddress ia = InetAddress.getByAddress(new byte[]{(byte)Integer.parseInt(FTPServerIPs[0]), (byte)Integer.parseInt(FTPServerIPs[1]),(byte)Integer.parseInt(FTPServerIPs[2]),(byte)Integer.parseInt(FTPServerIPs[3])});
                publishProgress("Connecting....");
                ftpClient.connect(ia);
                publishProgress("Status: "+ ftpClient.getStatus());
                publishProgress("Logging in....");
                if (ftpClient.login("pi", "M@t0rb1k3pi")) {
                    publishProgress("...done.");
                    publishProgress("Status: " + ftpClient.getStatus());
                    publishProgress("Changing Working Dir to " + WorkingDir + "....");
                    if (ftpClient.changeWorkingDirectory(WorkingDir))
                        publishProgress("...done.");
                    publishProgress("Getting filenames....");

                    for(FileRecord fr:fileList) {
                        String frDate = dateFormat.format(fr.Filedate);
                        boolean foundDateDirectory = false;
                        publishProgress("Changing Working Dir to " + frDate + "....");
                        if (ftpClient.changeWorkingDirectory(frDate)) {
                            publishProgress("...done.");
                            foundDateDirectory = true;
                        } else {
                            publishProgress("  " + frDate + " not found.");
                            publishProgress("  creating Show Directory " + frDate);
                            ftpClient.makeDirectory(frDate);
                            if (ftpClient.changeWorkingDirectory(frDate)) {
                                publishProgress("...done.");
                                foundDateDirectory = true;
                            }
                        }
                        if (foundDateDirectory){
                            boolean foundShowDirectory = false;
                            publishProgress("Changing Working Dir to " + fr.FolderPath + "....");
                            if (ftpClient.changeWorkingDirectory(fr.FolderPath)) {
                                publishProgress("...done.");
                                foundShowDirectory = true;
                            } else {
                                publishProgress("  " + fr.FolderPath + " not found.");
                                publishProgress("  creating Show Directory " + fr.FolderPath);
                                ftpClient.makeDirectory(fr.FolderPath);
                                if (ftpClient.changeWorkingDirectory(fr.FolderPath)) {
                                    publishProgress("...done.");
                                    foundShowDirectory = true;
                                }
                            }
                            if (foundShowDirectory) {
                                publishProgress("Checking for " + fr.FileName);
                                FTPFile[] ftpFiles = ftpClient.listFiles();
                                boolean foundFile = false;
                                for (FTPFile ftpF : ftpFiles) {
                                    if (ftpF.getName().equals(fr.FileName)) {
                                        publishProgress("Found File!!!");
                                        foundFile = true;
                                        break;
                                    }
                                }
                                if (!foundFile) {
                                    publishProgress("File not found!!!");
                                    publishProgress("COPYING File " + fr.FileName);
                                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                                    BufferedInputStream buffIn = null;
                                    File localFile = new File(fr.FilePath);
                                    buffIn = new BufferedInputStream(new FileInputStream(localFile));
                                    ftpClient.enterLocalPassiveMode();
                                    ftpClient.storeFile(fr.FileName, buffIn);
                                    buffIn.close();
                                    publishProgress("DONE COPYING File " + fr.FileName);

                                }
                                publishProgress("Changing Working Dir back to ..");
                                if (ftpClient.changeWorkingDirectory("..")) {
                                    publishProgress("...done.");
                                }
                            } else {
                                publishProgress("could not get directory " + fr.FolderPath);
                            }
                            publishProgress("Changing Working Dir back to ..");
                            if (ftpClient.changeWorkingDirectory("..")) {
                                publishProgress("...done.");
                            }
                        } else {
                            publishProgress("could not get directory " + frDate);
                        }
                    }

                    publishProgress("Logout....");
                    ftpClient.logout();
                }
                else
                {
                    publishProgress("...Could not login");
                }
                publishProgress("Disconnect...");
                ftpClient.disconnect();
                publishProgress("Disconnected...DONE", "scroll");
            } catch (Exception e) {
                StackTraceElement[] ste = e.getStackTrace();
                publishProgress("Exception: " + e.toString() + "\n\n" + ste[0].toString(), "scroll");
                e.printStackTrace();

            }
            return null;
        }

        private void addLocalFiles(String path, String dirPath, List<FileRecord> fileList){
            File f = new File(path);
            publishProgress("Got " + f.getName() + " - " + f.getPath());
            publishProgress("Getting list of Files");
            File file[] = f.listFiles();
            publishProgress("Files: "+ file.length);
            for (int i=0; i < file.length; i++)
            {
                String tmpdirPath = dirPath;
                File tmpFile = file[i];
                if (tmpFile.isDirectory()) {
                    publishProgress(i + "Directory: " + tmpFile.getName());
                    if (!tmpdirPath.isEmpty()){
                        tmpdirPath = tmpdirPath + "/";
                    }
                    tmpdirPath = tmpdirPath + tmpFile.getName();
                    addLocalFiles(tmpFile.getPath(), tmpdirPath, fileList);
                }
                if (tmpFile.isFile()) {
                    publishProgress(i + "File: " + tmpFile.getName());
                    String filePath = tmpFile.getPath().toString();
                    String fileExt = filePath.substring(filePath.lastIndexOf(".") + 1);
                    if (fileExt.equals("mp3")){
                        FileRecord fr = new FileRecord();
                        fr.Filedate = new Date(tmpFile.lastModified());
                        fr.FileName = tmpFile.getName();
                        fr.FilePath = filePath;
                        fr.FolderPath = dirPath;
                        fileList.add(fr);
                    }
                }
            }
        }

        protected void onProgressUpdate(Object[] params) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(params);
            memo.setText(memo.getText() + "\n(" + memo.getText().length() + ") " + params[0]);
//            if (params[1] != null) {
//                //if (params[1] == "scroll") {
                    scroll_view.post(new Runnable() {
                        @Override
                        public void run() {
                            // This method works but animates the scrolling
                            // which looks weird on first load
                            // scroll_view.fullScroll(View.FOCUS_DOWN);

                            // This method works even better because there are no animations.
                            //scroll_view.scrollTo(0, scroll_view.getBottom());
                            scroll_view.fullScroll(scroll_view.FOCUS_DOWN);
                        }
                    });
//                //}
//            }
        }

        protected void onPostExecute(Object a) {
            // TODO: check this.exception
            // TODO: do something with the feed
        }
    }
}