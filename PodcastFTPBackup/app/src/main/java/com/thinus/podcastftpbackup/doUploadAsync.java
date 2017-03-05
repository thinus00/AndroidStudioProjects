package com.thinus.podcastftpbackup;

import android.app.Notification;
import android.app.NotificationManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

/**
 * Created by thinus on 2016/12/11.
 */
public class doUploadAsync extends AsyncTask {


    class FileRecord {
        public String FileName;
        public Date Filedate;
        public String FilePath;
        public String FolderPath;
        //public String FolderDate;
    }

    public String ServerIP;
    public int stop = -1;
    private Exception exception;
    private NotificationManager mNM;
    private NotificationCompat.Builder mBuilder;

    doUploadAsync(NotificationManager nm, NotificationCompat.Builder mb) {
        mNM = nm;
        mBuilder = mb;
        ServerIP = "192.168.1.106";
    }

    @Override
    protected Object doInBackground(Object[] params) {
        String fileNamesCopied = "";
        try {
            publishProgress("*doInBackground");
            List<FileRecord> fileList = new ArrayList<FileRecord>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            mNM.notify(0, mBuilder.setContentText("Start doUpload").setContentTitle("PodcastFTPBackup").setSmallIcon(R.drawable.abc_btn_check_to_on_mtrl_000).build());

            //local files
            File extStorageDirectory = Environment.getExternalStorageDirectory();
            //String path = extStorageDirectory.toString() + "/Android/data/de.danoeh.antennapod/files/media/";
            String path = extStorageDirectory.toString() + "/Android/data/com.bambuna.podcastaddict/files/podcast/";
            publishProgress("Checking " + path);

            addLocalFiles(path, "", fileList);
            String WorkingDir = "Podcasts";

            publishProgress("Creating FTP Object....");
            FTPClient ftpClient = new FTPClient();
            publishProgress("Server IP: " + ServerIP);
            publishProgress("Working folder: " + WorkingDir);

            String[] FTPServerIPs = ServerIP.toString().split("\\.");
            publishProgress(FTPServerIPs.length);
            publishProgress("Parsing address " + FTPServerIPs[0] + "." + FTPServerIPs[1] + "." + FTPServerIPs[2] + "." + FTPServerIPs[3] + "." + "....");
            InetAddress ia = InetAddress.getByAddress(new byte[]{(byte) Integer.parseInt(FTPServerIPs[0]), (byte) Integer.parseInt(FTPServerIPs[1]), (byte) Integer.parseInt(FTPServerIPs[2]), (byte) Integer.parseInt(FTPServerIPs[3])});
            publishProgress("Connecting....");
            ftpClient.connect(ia);
            publishProgress("Status: " + ftpClient.getStatus());
            if (ftpClient.isConnected()) {
                publishProgress("Logging in....");
                if (ftpClient.login("pi", "M@t0rb1k3pi")) {
                    publishProgress("...done.");
                    publishProgress("Status: " + ftpClient.getStatus());
                    publishProgress("Changing Working Dir to " + WorkingDir + "....");
                    if (ftpClient.changeWorkingDirectory(WorkingDir))
                        publishProgress("...done.");
                    publishProgress("Getting filenames....");

                    stop = 0;
                    for (FileRecord fr : fileList) {
                        if (stop == 1) {
                            stop = 2;
                            break;
                        }

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
                        if (foundDateDirectory) {
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
                                    publishProgress("*COPYING File " + fr.FileName);
                                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                                    BufferedInputStream buffIn = null;
                                    File localFile = new File(fr.FilePath);
                                    buffIn = new BufferedInputStream(new FileInputStream(localFile));
                                    ftpClient.enterLocalPassiveMode();
                                    ftpClient.storeFile(fr.FileName, buffIn);
                                    buffIn.close();
                                    publishProgress("*DONE COPYING File " + fr.FileName);
                                    fileNamesCopied = fileNamesCopied.concat(fr.FileName + "; ");
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
                    if (fileNamesCopied == "")
                        fileNamesCopied = "None";
                    mNM.notify(0, mBuilder.setContentText(fileNamesCopied).setContentTitle("PodcastFTPBackup").setSmallIcon(R.drawable.abc_btn_check_to_on_mtrl_000).build());
                } else {
                    publishProgress("...Could not login");
                    mNM.notify(0, mBuilder.setContentText("Could not login to FTP").setContentTitle("PodcastFTPBackup").setSmallIcon(R.drawable.abc_btn_check_to_on_mtrl_000).build());
                }
                publishProgress("Disconnect...");
                ftpClient.disconnect();

                publishProgress("Disconnected...DONE", "scroll");
            } else {
                publishProgress("...Could not connect");
                mNM.notify(0, mBuilder.setContentText("Could not connect to FTP").setContentTitle("PodcastFTPBackup").setSmallIcon(R.drawable.abc_btn_check_to_on_mtrl_000).build());

            }
        } catch (Exception e) {
            StackTraceElement[] ste = e.getStackTrace();
            publishProgress("Exception: " + e.toString() + "\n\n" + ste[0].toString(), "scroll");
            e.printStackTrace();

        }
        stop = 2;
        publishProgress("*doInBackground done");
        return null;
    }

    private void addLocalFiles(String path, String dirPath, List<FileRecord> fileList) {
        File f = new File(path);
        publishProgress("Got " + f.getName() + " - " + f.getPath());
        publishProgress("Getting list of Files");
        File file[] = f.listFiles();
        publishProgress("Files: " + file.length);
        for (int i = 0; i < file.length; i++) {
            String tmpdirPath = dirPath;
            File tmpFile = file[i];
            if (tmpFile.isDirectory()) {
                publishProgress(i + "Directory: " + tmpFile.getName());
                if (!tmpdirPath.isEmpty()) {
                    tmpdirPath = tmpdirPath + "/";
                }
                tmpdirPath = tmpdirPath + tmpFile.getName();
                addLocalFiles(tmpFile.getPath(), tmpdirPath, fileList);
            }
            if (tmpFile.isFile()) {
                publishProgress(i + "File: " + tmpFile.getName());
                String filePath = tmpFile.getPath().toString();
                String fileExt = filePath.substring(filePath.lastIndexOf(".") + 1);
                if (fileExt.equals("mp3")) {
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
        if (params[0].toString().startsWith("*"))
            MainActivity.LogMessage(params[0].toString());
    }

    protected void onPostExecute(Object a) {
        // TODO: check this.exception
        // TODO: do something with the feed
    }
}
