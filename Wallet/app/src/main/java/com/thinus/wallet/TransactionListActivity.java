package com.thinus.wallet;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.content.Intent;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
//import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.database.sqlite.SQLiteDatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


public class TransactionListActivity extends ActionBarActivity {

    public static ArrayList<Transaction> transactionItems;
    public static ArrayList<Transaction> transactionItems_month;

    public static NotificationManager notificationManager;
    public static SQLiteDatabase db;
    private CustomBaseAdapterTransactionList baseTransactionListAdapter;
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat dateFormatCSV = new SimpleDateFormat("yyyyMMdd");

    public static int monthstartonday = 23;
    public static int day = 0;
    public static int month = 0;
    public static int year = 0;
    public static String monthName;
    public static Date startDate;
    public static Date endDate;
    public static int catFilterID = 0;
    private String mode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_list);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null) {
            if (extras.containsKey("mode")) {
                mode = extras.getString("mode");
            }
            if (extras.containsKey("id")) {
                catFilterID = extras.getInt("id");
            }
        } else {
            mode = "";
        }

        if (mode.equals("category")) {

            final ListView lv1 = (ListView) findViewById(R.id.listViewTransactions);
            baseTransactionListAdapter = new CustomBaseAdapterTransactionList(this, transactionItems);
            lv1.setAdapter(baseTransactionListAdapter);
            baseTransactionListAdapter.getFilter().filter("monthCategory", new Filter.FilterListener() {
                public void onFilterComplete(int count) {
                    setTitle(monthName + " " + year + " (" + count + ") - " + CategoryListActivity.getCategoryName(catFilterID));
                }
            });
            lv1.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                    Object o = lv1.getItemAtPosition(position);
                    Transaction fullObject = (Transaction) o;
                    Intent intent = new Intent(TransactionListActivity.this, TransactionAddActivity.class);
                    intent.putExtra("mode", "edit");
                    intent.putExtra("id", fullObject.getId());
                    startActivity(intent);
                }
            });

        } else {
            // Get ListView object from xml
            db = openOrCreateDatabase("WalletDB", Context.MODE_PRIVATE, null);
            db.execSQL("CREATE TABLE IF NOT EXISTS [category] (id INT PRIMARY KEY NOT NULL, name VARCHAR, categorytype INT, budget DOUBLE, smsDescription VARCHAR);");
            db.execSQL("CREATE TABLE IF NOT EXISTS [transaction] (id INT PRIMARY KEY NOT NULL, amount DOUBLE, categoryid INT, account VARCHAR,description VARCHAR,date VARCHAR, reference VARCHAR, income_expense int);");
            db.execSQL("CREATE TABLE IF NOT EXISTS [settings] (id INT PRIMARY KEY NOT NULL, name VARCHAR,value VARCHAR);");


            transactionItems = new ArrayList<Transaction>();
            CategoryListActivity.categoryItems = new ArrayList<Category>();
            //Toast.makeText(getApplicationContext(), "Loading data from DB (2)", Toast.LENGTH_LONG).show();
            try {
                CategoryListActivity.LoadCategoriesFromDB();
                LoadTransactionsFromDB();
            } catch (Exception ex) {
                Toast.makeText(TransactionListActivity.this.getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            }

            Date date = new Date();
            DateFormat dateFormat0 = new SimpleDateFormat("dd");
            day = Integer.parseInt(dateFormat0.format(date));
            dateFormat0 = new SimpleDateFormat("MM");
            month = Integer.parseInt(dateFormat0.format(date));
            dateFormat0 = new SimpleDateFormat("yyyy");
            year = Integer.parseInt(dateFormat0.format(date));
            refreshDates();

            LoadSettingsFromDB();

            final ListView lv1 = (ListView) findViewById(R.id.listViewTransactions);
            baseTransactionListAdapter = new CustomBaseAdapterTransactionList(this, transactionItems);
            lv1.setAdapter(baseTransactionListAdapter);

            lv1.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                    Object o = lv1.getItemAtPosition(position);
                    Transaction fullObject = (Transaction) o;
                    Intent intent = new Intent(TransactionListActivity.this, TransactionAddActivity.class);
                    intent.putExtra("mode", "edit");
                    intent.putExtra("id", fullObject.getId());
                    startActivity(intent);
                }
            });
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (!mode.equals("category")) {
            getMenuInflater().inflate(R.menu.menu_transaction_list, menu);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    final String path0 = uri.getPath();
                    final ProgressDialog barSMSScanDialog = new ProgressDialog(this);
                    barSMSScanDialog.setTitle("Scan CSV");
                    barSMSScanDialog.setMessage("Scanning...");
                    barSMSScanDialog.setProgressStyle(barSMSScanDialog.STYLE_HORIZONTAL);
                    barSMSScanDialog.setProgress(0);
                    barSMSScanDialog.setMax(100); //no of SMS's to scan

                    barSMSScanDialog.show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int transactionItemscount = 0;


                            //StringBuilder stringList = new StringBuilder();
                            ArrayList<String> stringList = new ArrayList<String>();
                            try {
                                File myFile2 = new File(path0);
                                myFile2.createNewFile();
                                FileInputStream fis = new FileInputStream(myFile2);
                                BufferedReader r = new BufferedReader(new InputStreamReader(fis));
                                String line;
                                while ((line = r.readLine()) != null) {
                                    stringList.add(line);
                                }
                                fis.close();
                            } catch (IOException ioe) {
                                //Toast.makeText(TransactionListActivity.this.getApplicationContext(), ioe.getMessage(), Toast.LENGTH_LONG).show();
                            }

                            barSMSScanDialog.setMax(stringList.size());


                            //Credit-Prov "PROV",20160120,"",-0000000000623.10,"C*ANB VET      NB6019    HONEY","",0,000.00
                            //cheque-prov "PROV",20160205,"",-0000000000280.35,"OUTSTANDING CARD AUTHORISATION","Cltx Five Sta2016-02-05 15H14",6099,000.00
                            //cheque-hist "HIST",20160107,"",-0000000001500.00,"IB TRANSFER TO","CCard          11H36",378,000.00
                            String _account = "";
                            for (String s : stringList) {
                                if (!s.equals("")) {
                                    String[] array = s.split(",");
                                    if (_account.equals("") && (array[2].equals("\"ACC-NO\""))) {
                                        _account = array[1].substring(array[1].length() - 4);
                                    }
                                    if (array[0].equals("\"HIST\"") || array[0].equals("\"PROV\"")) {
                                        double amount = Double.parseDouble(array[3]);
                                        int categoryId = 0;
                                        String account = _account;
                                        String description = "";
                                        if (array[2].equals("\"##\""))
                                            description = array[4] + " - " + array[5];
                                        else {
                                            if (array[5].equals("\"\"")) {
                                                description = array[4];
                                            } else {
                                                description = array[5];
                                            }
                                        }
                                        String date = array[1];
                                        int incom_expense = 0;

                                        if (array[3].startsWith("+"))
                                            incom_expense = 1;
                                        else
                                            incom_expense = 2;

                                        if (amount < 0)
                                            amount = amount * -1;

                                        if (amount > 0) {

                                            try {
                                                Transaction sr0 = new Transaction(0,//get next ID
                                                        amount,
                                                        0,
                                                        account,
                                                        description,
                                                        dateFormatCSV.parse(date),
                                                        s,
                                                        incom_expense);

                                                if (sr0 != null) {
                                                    boolean dup = checkForDuplicateTrans(sr0);
                                                    if (!dup) {
                                                        sr0.Save();
                                                        transactionItems.add(sr0);
                                                        transactionItemscount++;
                                                    }
                                                }
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                                barSMSScanDialog.incrementProgressBy(1);
                            }
                            barSMSScanDialog.dismiss();
                            final String message = "Found " + transactionItemscount + " transactions";
                            TransactionListActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(TransactionListActivity.this.getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                }
                            });
                            transactionItems.clear();
                            LoadTransactionsFromDB();
                            TransactionListActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    TransactionListActivity.this.refreshList();
                                }
                            });
                        }
                    }).start();

                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_prev) {
            if (month == 1) {
                month = 12;
                year--;
            } else {
                month--;
            }
            refreshDates();
        }

        if (id == R.id.action_next) {
            if (month == 12) {
                month = 1;
                year++;
            } else {
                month++;
            }
            refreshDates();
        }


        if (id == R.id.action_refresh) {
            Toast.makeText(getApplicationContext(), "Refreshing list", Toast.LENGTH_LONG).show();
            transactionItems.clear();
            LoadTransactionsFromDB();

            refreshList();
            return true;
        }

        if (id == R.id.add_transaction) {
            Intent intent = new Intent(TransactionListActivity.this, TransactionAddActivity.class);
            intent.putExtra("mode", "add");
            intent.putExtra("id", 0);
            this.startActivity(intent);
            return true;
        }

        if (id == R.id.categories) {
            Intent intent = new Intent(TransactionListActivity.this, CategoryListActivity.class);
            this.startActivity(intent);
            return true;
        }

        if (id == R.id.summary) {
            CategoryListActivity.calcBugetTotals();
            Intent intent = new Intent(TransactionListActivity.this, Summary.class);
            this.startActivity(intent);
            return true;
        }

        if (id == R.id.action_settings) {

            return true;
        }

        if (id == R.id.show_unlinked) {
            baseTransactionListAdapter.getFilter().filter("unlinked", new Filter.FilterListener() {
                public void onFilterComplete(int count) {
                    setTitle("Wallet (" + count + ")");
                }
            });
            return true;
        }

        if (id == R.id.show_all) {
            baseTransactionListAdapter.getFilter().filter("all", new Filter.FilterListener() {
                public void onFilterComplete(int count) {
                    setTitle("Wallet (" + count + ")");
                }
            });
            return true;
        }

        if (id == R.id.scan_csv) {
            showFileChooser();
            return true;

        }
        if (id == R.id.scan_sms) {
            final ProgressDialog barSMSScanDialog = new ProgressDialog(this);
            barSMSScanDialog.setTitle("Scan SMS's");
            barSMSScanDialog.setMessage("Scanning...");
            barSMSScanDialog.setProgressStyle(barSMSScanDialog.STYLE_HORIZONTAL);
            barSMSScanDialog.setProgress(0);
            barSMSScanDialog.setMax(100); //no of SMS's to scan

            barSMSScanDialog.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int transactionItemscount = transactionItems.size();
                    Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
                    barSMSScanDialog.setMax(cursor.getCount());
                    if (cursor.moveToFirst()) {
                        try {
                            String fileName;
//                            fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/sms.txt";
                            fileName = Environment.getExternalStorageDirectory().getPath() + "/sms.txt";
                            File myFile = new File(fileName);
                            myFile.createNewFile();
                            FileOutputStream fs = new FileOutputStream(myFile);
                            OutputStreamWriter out = new OutputStreamWriter(fs);
                            do {
                                String msgBody = cursor.getString(12);
                                String msgDate = cursor.getString(4);


                                out.write(msgDate);
                                out.write("\n\r");
                                out.write(msgBody);
                                out.write("\n\r");

                                barSMSScanDialog.incrementProgressBy(1);

                                Transaction sr2 = getTransactionByReference(msgBody);
                                if (sr2 == null) {
                                    Transaction sr1 = ProcessSMSBody(msgDate,msgBody, true);
                                }
                            } while (cursor.moveToNext());
                            out.close();
                            fs.close();
                        } catch (IOException ioe) {
                           final String message = ioe.getMessage();
                            TransactionListActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(TransactionListActivity.this.getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        //TransactionListActivity.this.refreshList();
                    }
                    barSMSScanDialog.dismiss();
                    final String message = "Found " + (transactionItems.size()-transactionItemscount) + " transactions";
                    TransactionListActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(TransactionListActivity.this.getApplicationContext(), message, Toast.LENGTH_LONG).show();
                        }
                    });
                    transactionItems.clear();
                    LoadTransactionsFromDB();
                    TransactionListActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            TransactionListActivity.this.refreshList();
                        }
                    });
                }
            }).start();

            return true;
        }

        if (id == R.id.link_categories_unlinked) {
            final ProgressDialog barTransScanDialog = new ProgressDialog(this);
            barTransScanDialog.setTitle("Scan unlinked Transactions");
            barTransScanDialog.setMessage("Scanning...");
            barTransScanDialog.setProgressStyle(barTransScanDialog.STYLE_HORIZONTAL);
            barTransScanDialog.setProgress(0);
            barTransScanDialog.setMax(100);

            barTransScanDialog.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    barTransScanDialog.setMax(transactionItems.size());
                    int foundCount = 0;
                    int linkedCount = 0;

                    for (Transaction t : transactionItems){
                        barTransScanDialog.incrementProgressBy(1);
                        try {
                            Thread.sleep(1);
                        } catch (Exception ex) {

                        }

                        if (t.getCategoryId() == 0) {
                            foundCount++;
                            String desc = t.getDescription();
                            boolean foundDesc = false;
                            for (Category c : CategoryListActivity.categoryItems) {

                                for (String s : c.getSMSDescription()) {
                                    if (desc.contains(s)) {
                                        foundDesc = true;
                                        //update category id
                                        t.setCategoryId(c.getId());
                                        t.Update();
                                        linkedCount++;
                                        break;
                                    }
                                }
                                if (foundDesc) {
                                    break;
                                }
                            }
                        }
                    }
                    barTransScanDialog.dismiss();
                    final String message = "Linked " + linkedCount + " transactions.\nCould not link " + (foundCount-linkedCount) + " transactions";
                    TransactionListActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(TransactionListActivity.this.getApplicationContext(), message, Toast.LENGTH_LONG).show();

                        }
                    });
                    TransactionListActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            TransactionListActivity.this.refreshList();
                        }
                    });
                }
            }).start();
            return true;
        }

        if (id == R.id.backup_database) {
            try {
                File sd = Environment.getExternalStorageDirectory();
                File data = Environment.getDataDirectory();

                if (sd.canWrite()) {
                    String  currentDBPath= "//data//" + "com.thinus.wallet"
                            + "//databases//" + "WalletDB";
                    String backupDBPath  = "/BackupFolder/WalletDB";
                    String backupDBDirPath  = "/BackupFolder";
                    File currentDB = new File(data, currentDBPath);
                    File backupDB = new File(sd, backupDBPath);
                    File backupDBDir = new File(sd, backupDBDirPath);
                    if(!backupDBDir.exists()) {
                        if (!backupDBDir.mkdirs()) {
                            Toast.makeText(getBaseContext(), "Could not make dir " + backupDBDir.getPath(), Toast.LENGTH_LONG).show();
                        }
                    }
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(getBaseContext(), "DB backuped to " + backupDB.toString(),Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
            }
            return true;
        }

        if (id == R.id.restore_database) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            try {
                builder
                        .setTitle("Restore DB")
                        .setMessage("Are you sure?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Yes button clicked, do something
                                try {
                                    File sd = Environment.getExternalStorageDirectory();
                                    File data = Environment.getDataDirectory();

                                    if (sd.canWrite()) {
                                        String currentDBPath = "//data//" + "com.thinus.wallet"
                                                + "//databases//" + "WalletDB";
                                        String backupDBPath = "/BackupFolder/WalletDB";
                                        File backupDB = new File(data, currentDBPath);
                                        File currentDB = new File(sd, backupDBPath);

                                        FileChannel src = new FileInputStream(currentDB).getChannel();
                                        FileChannel dst = new FileOutputStream(backupDB).getChannel();
                                        dst.transferFrom(src, 0, src.size());
                                        src.close();
                                        dst.close();
                                        Toast.makeText(getBaseContext(), "DB Restored from " + backupDB.toString(), Toast.LENGTH_LONG).show();
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            } catch (Exception ex) {
                Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            }

            return true;
        }

        if (id == R.id.create_database) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            try {
                builder
                        .setTitle("Recreate Database")
                        .setMessage("Are you sure?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Yes button clicked, do something
                                try
                                {
                                    db.execSQL("DROP TABLE IF EXISTS [transaction];");
                                    db.execSQL("CREATE TABLE IF NOT EXISTS [transaction] (id INT PRIMARY KEY NOT NULL, amount DOUBLE, categoryid INT, account VARCHAR,description VARCHAR,date VARCHAR, reference VARCHAR, income_expense int);");
                                    db.execSQL("DROP TABLE IF EXISTS [category];");
                                    db.execSQL("CREATE TABLE IF NOT EXISTS [category] (id INT PRIMARY KEY NOT NULL, name VARCHAR, categorytype INT, budget DOUBLE, smsDescription VARCHAR);");
                                    db.execSQL("DROP TABLE IF EXISTS [settings];");
                                    db.execSQL("CREATE TABLE IF NOT EXISTS [settings] (id INT PRIMARY KEY NOT NULL, name VARCHAR,value VARCHAR);");
                                    db.execSQL("INSERT INTO [settings] VALUES(1, 'monthstartonday', '23')");
                                    Toast.makeText(getApplicationContext(), "DB Cleaned", Toast.LENGTH_LONG).show();

                                    transactionItems = new ArrayList<Transaction>();
                                    CategoryListActivity.categoryItems = new ArrayList<Category>();
                                    //CategoryListActivity.AddTestArray();
                                    //AddTestArray();
                                    Toast.makeText(getBaseContext(), "Done", Toast.LENGTH_LONG).show();
                                } catch (Exception ex) {
                                    Toast.makeText(getBaseContext(), ex.toString(), Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            } catch (Exception ex) {
                Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            }

            return true;
        }

        if (id == R.id.clean_trans) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            try {
                builder
                        .setTitle("Wipe transactions")
                        .setMessage("Are you sure?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Yes button clicked, do something
                                try
                                {
                                    db.execSQL("DROP TABLE IF EXISTS [transaction];");
                                    db.execSQL("CREATE TABLE IF NOT EXISTS [transaction] (id INT PRIMARY KEY NOT NULL, amount DOUBLE, categoryid INT, account VARCHAR,description VARCHAR,date VARCHAR, reference VARCHAR, income_expense int);");
                                    Toast.makeText(getApplicationContext(), "Transactions Cleaned", Toast.LENGTH_LONG).show();
                                } catch (Exception ex) {
                                    Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            }

            return true;
        }

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static Transaction getTransactionByID(int id){
        for (Transaction t : transactionItems){
            if (t.getId() == id)
                return t;
        }
        return null;
    }

    public static Transaction getTransactionByReference(String ref){
        for (Transaction t : transactionItems){
            if (t.getReference().equals(ref))
                return t;
        }
        return null;
    }

    public static boolean checkForDuplicateTrans(Transaction sr){
        for (Transaction t : transactionItems){
            if (t.getAmount() == sr.getAmount()){

                if (t.getDate().equals(sr.getDate()))
                    return true;
                Calendar c = Calendar.getInstance();
                c.setTime(sr.getDate());
                c.add(Calendar.DATE, 7);  // number of days to add
                Calendar c2 = Calendar.getInstance();
                c2.setTime(sr.getDate());
                c2.add(Calendar.DATE, -7);  // number of days to add
                if (t.getDate().after(c2.getTime()) && t.getDate().before(c.getTime()))
                    return true;
            }
        }
        return false;
    }

    public static boolean deleteTransactionByID(int id){
        for (Transaction t : transactionItems){
            if (t.getId() == id) {
                t.Delete();
                transactionItems.remove(t);
                return true;
            }
        }
        return false;
    }

    public static Transaction ProcessSMSBody(String msgDate, String msgBody, boolean saveTrans) {
        Transaction sr0 = null;
        try {

            if (msgBody.startsWith("Standard Bank:")) {
                double amount = 0;
                int categoryId = 0;
                String account = "";
                String description = "";
                String date = "";
                int incom_expense = 0;
                if (msgBody.contains("Standard Bank Internet Banking - you logged on")) {
                    //log on sms
                } else if (msgBody.contains("purchased from Acc")) {
                    //Standard Bank: R303.83 purchased from Acc. 2340 at PnP Fam Brentwood Park. Avl bal R129.40 2016-01-01 Query? 0860123107
                    incom_expense = 2;
                    int startIndex = msgBody.indexOf("R") + 1;
                    amount = Double.valueOf(msgBody.substring(startIndex, msgBody.indexOf(" ", startIndex)));
                    startIndex = msgBody.indexOf("purchased from Acc.", startIndex) + 20;
                    account = msgBody.substring(startIndex, msgBody.indexOf(" ", startIndex));
                    startIndex = msgBody.indexOf(" at ", startIndex) + 4;
                    if (msgBody.contains(". Avl bal")) {
                        description = msgBody.substring(startIndex, msgBody.indexOf(". Avl bal", startIndex));
                        startIndex = msgBody.indexOf(". Avl bal R", startIndex) + 12;
                    } else if (msgBody.contains(". Acl bal")) {
                        description = msgBody.substring(startIndex, msgBody.indexOf(". Acl bal", startIndex));
                        startIndex = msgBody.indexOf(". Acl bal R", startIndex) + 12;
                    }
                    startIndex = msgBody.indexOf(" ", startIndex) + 1;
                    date = msgBody.substring(startIndex, startIndex = msgBody.indexOf(" ", startIndex));
                } else if (msgBody.contains("paid to Acc")) {
                    //Standard Bank: R23.00 paid to Acc. 1812 from LARAINE PIETERSEN. Acl bal R-18135.32 2015-12-31 Query? 0860123107
                    incom_expense = 1;
                    int startIndex = msgBody.indexOf("R") + 1;
                    amount = Double.valueOf(msgBody.substring(startIndex, msgBody.indexOf(" ", startIndex)));
                    startIndex = msgBody.indexOf("paid to Acc.", startIndex) + 13;
                    account = msgBody.substring(startIndex, msgBody.indexOf(" ", startIndex));
                    startIndex = msgBody.indexOf(" from ", startIndex) + 6;
                    if (msgBody.contains(". Avl bal")) {
                        description = msgBody.substring(startIndex, msgBody.indexOf(". Avl bal", startIndex));
                        startIndex = msgBody.indexOf(". Avl bal R", startIndex) + 12;
                    } else if (msgBody.contains(". Acl bal")) {
                        description = msgBody.substring(startIndex, msgBody.indexOf(". Acl bal", startIndex));
                        startIndex = msgBody.indexOf(". Acl bal R", startIndex) + 12;
                    }
                    startIndex = msgBody.indexOf(" ", startIndex) + 1;
                    date = msgBody.substring(startIndex, startIndex = msgBody.indexOf(" ", startIndex));
                } else if (msgBody.contains("cash dep. to Acc")) {
                    //Standard Bank: R3300.00 cash dep. to Acc. 1812 from ELI. Acl bal R-55096.55 2015-11-12 Query? 0860123107
                    incom_expense = 1;
                    int startIndex = msgBody.indexOf("R") + 1;
                    amount = Double.valueOf(msgBody.substring(startIndex, msgBody.indexOf(" ", startIndex)));
                    startIndex = msgBody.indexOf("cash dep. to Acc.", startIndex) + 18;
                    account = msgBody.substring(startIndex, msgBody.indexOf(" ", startIndex));
                    startIndex = msgBody.indexOf(" from ", startIndex) + 6;
                    if (msgBody.contains(". Avl bal")) {
                        description = msgBody.substring(startIndex, msgBody.indexOf(". Avl bal", startIndex));
                        startIndex = msgBody.indexOf(". Avl bal R", startIndex) + 12;
                    } else if (msgBody.contains(". Acl bal")) {
                        description = msgBody.substring(startIndex, msgBody.indexOf(". Acl bal", startIndex));
                        startIndex = msgBody.indexOf(". Acl bal R", startIndex) + 12;
                    }
                    startIndex = msgBody.indexOf(" ", startIndex) + 1;
                    date = msgBody.substring(startIndex, startIndex = msgBody.indexOf(" ", startIndex));
                } else if (msgBody.contains("withdrawn from Acc")) {
                    //Standard Bank: R3800.00 withdrawn from Acc. 1812 at SHELL CONSTAN 17H06 601724804. Acl bal R-12213.78 2015-12-22 Query? 0860123107
                    incom_expense = 2;
                    int startIndex = msgBody.indexOf("R") + 1;
                    amount = Double.valueOf(msgBody.substring(startIndex, msgBody.indexOf(" ", startIndex)));
                    startIndex = msgBody.indexOf("withdrawn from Acc.", startIndex) + 20;
                    account = msgBody.substring(startIndex, msgBody.indexOf(" ", startIndex));
                    startIndex = msgBody.indexOf(" at ", startIndex) + 4;
                    if (msgBody.contains(". Avl bal")) {
                        description = msgBody.substring(startIndex, msgBody.indexOf(". Avl bal", startIndex));
                        startIndex = msgBody.indexOf(". Avl bal R", startIndex) + 12;
                    } else if (msgBody.contains(". Acl bal")) {
                        description = msgBody.substring(startIndex, msgBody.indexOf(". Acl bal", startIndex));
                        startIndex = msgBody.indexOf(". Acl bal R", startIndex) + 12;
                    }
                    startIndex = msgBody.indexOf(" ", startIndex) + 1;
                    date = msgBody.substring(startIndex, startIndex = msgBody.indexOf(" ", startIndex));
                } else if (msgBody.contains("Stop Order")) {
                    //Standard Bank: Stop Order R208.87 MTN SP    A1119399  0130825204; from Acc. 1812. 2016-01-01 Query?0860123107
                    incom_expense = 2;
                    int startIndex = msgBody.indexOf("R") + 1;
                    amount = Double.valueOf(msgBody.substring(startIndex, msgBody.indexOf(" ", startIndex)));
                    startIndex = msgBody.indexOf(" ", startIndex) + 1;

                    double amount2 = 0;
                    String description2 = null;
                    if (msgBody.indexOf("; R", startIndex) > 0) {
                        description = msgBody.substring(startIndex, msgBody.indexOf("; R", startIndex));
                        startIndex = msgBody.indexOf("; R", startIndex) + 3;
                        amount2 = Double.valueOf(msgBody.substring(startIndex, msgBody.indexOf(" ", startIndex)));
                        startIndex = msgBody.indexOf(" ", startIndex) + 1;
                        description2 = msgBody.substring(startIndex, msgBody.indexOf("from Acc", startIndex));
                    } else {
                        description = msgBody.substring(startIndex, msgBody.indexOf("from Acc", startIndex));
                    }

                    startIndex = msgBody.indexOf("from Acc.", startIndex) + 10;
                    account = msgBody.substring(startIndex, msgBody.indexOf(" ", startIndex) - 1);
                    startIndex = msgBody.indexOf(" ", startIndex) + 1;
                    date = msgBody.substring(startIndex, startIndex = msgBody.indexOf(" ", startIndex));

                    if (amount2 > 0) {
                        try {
                            sr0 = new Transaction(0,//get next ID
                                    amount2,
                                    0,
                                    account,
                                    description2,
                                    dateFormat.parse(date),
                                    msgBody,
                                    incom_expense);
                            if (saveTrans) {
                                if (sr0 != null) {
                                    sr0.Save();
                                    transactionItems.add(sr0);
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (msgBody.contains("deposited into Acc")) {
                    //Standard Bank: R1500.00 deposited into Acc. 2340 from FUND TRANSFERS           MARSHALLTOWN ZA. Avl bal R786.61 2015-12-31 Query? 0860123107
                    incom_expense = 1;
                    int startIndex = msgBody.indexOf("R") + 1;
                    amount = Double.valueOf(msgBody.substring(startIndex, msgBody.indexOf(" ", startIndex)));
                    startIndex = msgBody.indexOf("deposited into Acc.", startIndex) + 20;
                    account = msgBody.substring(startIndex, msgBody.indexOf(" ", startIndex));
                    startIndex = msgBody.indexOf(" from ", startIndex) + 6;
                    if (msgBody.contains(". Avl bal")) {
                        description = msgBody.substring(startIndex, msgBody.indexOf(". Avl bal", startIndex));
                        startIndex = msgBody.indexOf(". Avl bal R", startIndex) + 12;
                    } else if (msgBody.contains(". Acl bal")) {
                        description = msgBody.substring(startIndex, msgBody.indexOf(". Acl bal", startIndex));
                        startIndex = msgBody.indexOf(". Acl bal R", startIndex) + 12;
                    }
                    startIndex = msgBody.indexOf(" ", startIndex) + 1;
                    date = msgBody.substring(startIndex, startIndex = msgBody.indexOf(" ", startIndex));
                } else if (msgBody.contains("Debit Order")) {
                    //Standard Bank: Debit Order R5212.94 NEDMFCPAAI   EMT70877260001151;  from Acc.1812. 2016-01-02 Query?0860123107
                    incom_expense = 2;
                    int startIndex = msgBody.indexOf("R") + 1;
                    amount = Double.valueOf(msgBody.substring(startIndex, msgBody.indexOf(" ", startIndex)));
                    startIndex = msgBody.indexOf(" ", startIndex) + 1;

                    double amount2 = 0;
                    String description2 = null;
                    if (msgBody.indexOf("; R", startIndex) > 0) {
                        description = msgBody.substring(startIndex, msgBody.indexOf("; R", startIndex));
                        startIndex = msgBody.indexOf("; R", startIndex) + 3;
                        amount2 = Double.valueOf(msgBody.substring(startIndex, msgBody.indexOf(" ", startIndex)));
                        startIndex = msgBody.indexOf(" ", startIndex) + 1;
                        description2 = msgBody.substring(startIndex, msgBody.indexOf("from Acc", startIndex));
                    } else {
                        description = msgBody.substring(startIndex, msgBody.indexOf("from Acc", startIndex));
                    }

                    startIndex = msgBody.indexOf("from Acc.", startIndex) + 9;
                    account = msgBody.substring(startIndex, msgBody.indexOf(" ", startIndex) - 1);
                    startIndex = msgBody.indexOf(" ", startIndex) + 1;
                    date = msgBody.substring(startIndex, startIndex = msgBody.indexOf(" ", startIndex));

                    if (amount2 > 0) {
                        try {
                            sr0 = new Transaction(0,//get next ID
                                    amount2,
                                    0,
                                    account,
                                    description2,
                                    dateFormat.parse(date),
                                    msgBody,
                                    incom_expense);
                            if (saveTrans) {
                                if (sr0 != null) {
                                    sr0.Save();
                                    transactionItems.add(sr0);
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (msgBody.contains("purchased for")) {
                    //Standard Bank: R20.00 purchased for MTN PREPAID  0789862175 Acc. 1812. Acl bal R-14667.53 2015-12-28 Query? 0860123107
                    incom_expense = 2;
                    int startIndex = msgBody.indexOf("R") + 1;
                    amount = Double.valueOf(msgBody.substring(startIndex, msgBody.indexOf(" ", startIndex)));
                    startIndex = msgBody.indexOf("purchased for", startIndex) + 13;
                    description = msgBody.substring(startIndex, msgBody.indexOf(" Acc. ", startIndex));
                    startIndex = msgBody.indexOf(" Acc. ", startIndex) + 6;
                    account = msgBody.substring(startIndex, msgBody.indexOf(" ", startIndex) - 1);
                    if (msgBody.contains(". Avl bal")) {
                        startIndex = msgBody.indexOf(". Avl bal R", startIndex) + 12;
                    } else if (msgBody.contains(". Acl bal")) {
                        startIndex = msgBody.indexOf(". Acl bal R", startIndex) + 12;
                    }
                    startIndex = msgBody.indexOf(" ", startIndex) + 1;
                    date = msgBody.substring(startIndex, startIndex = msgBody.indexOf(" ", startIndex));
                } else if (msgBody.contains("Once-off CNP Payment to")) {
                    //Standard Bank: Once-off CNP Payment to: SANRAL TCH INTERNET for R300.00. Confirmation OTP: 21419. Queries? 086 120 1311. Sent 14 Dec 23:19:25
                } else if (msgBody.contains("transferred to Acc")) {
                    //Standard Bank: R4000.00 transferred to Acc. 1812. Acl bal R7626.12 2015-12-21 Query? 0860123107
                    incom_expense = 1;
                    int startIndex = msgBody.indexOf("R") + 1;
                    amount = Double.valueOf(msgBody.substring(startIndex, msgBody.indexOf(" ", startIndex)));
                    startIndex = msgBody.indexOf("transferred to Acc.", startIndex) + 20;
                    account = msgBody.substring(startIndex, msgBody.indexOf(" ", startIndex) - 1);
                    description = "";
                    if (msgBody.contains(". Avl bal")) {
                        startIndex = msgBody.indexOf(". Avl bal R", startIndex) + 12;
                    } else if (msgBody.contains(". Acl bal")) {
                        startIndex = msgBody.indexOf(". Acl bal R", startIndex) + 12;
                    }
                    startIndex = msgBody.indexOf(" ", startIndex) + 1;
                    date = msgBody.substring(startIndex, startIndex = msgBody.indexOf(" ", startIndex));
                } else if (msgBody.contains("paid from Acc")) {
                    //Standard Bank: R600.00 paid from Acc. 1812 to EDGARS STORES LTD   601724804. Acl bal R-12672.32 2015-12-29 Query? 0860123107. Reply 1 to report fraud
                    incom_expense = 2;
                    int startIndex = msgBody.indexOf("R") + 1;
                    amount = Double.valueOf(msgBody.substring(startIndex, msgBody.indexOf(" ", startIndex)));
                    startIndex = msgBody.indexOf("paid from Acc.", startIndex) + 15;
                    account = msgBody.substring(startIndex, msgBody.indexOf(" ", startIndex));
                    startIndex = msgBody.indexOf(" to ", startIndex) + 4;
                    if (msgBody.contains(". Avl bal")) {
                        description = msgBody.substring(startIndex, msgBody.indexOf(". Avl bal", startIndex));
                        startIndex = msgBody.indexOf(". Avl bal R", startIndex) + 12;
                    } else if (msgBody.contains(". Acl bal")) {
                        description = msgBody.substring(startIndex, msgBody.indexOf(". Acl bal", startIndex));
                        startIndex = msgBody.indexOf(". Acl bal R", startIndex) + 12;
                    }
                    startIndex = msgBody.indexOf(" ", startIndex) + 1;
                    date = msgBody.substring(startIndex, startIndex = msgBody.indexOf(" ", startIndex));
                } else if (msgBody.contains("returned to Acc")) {
                    //Standard Bank: R8.00 returned to Acc. 2340 from GOOGLE *SELLER. Avl bal R107.81 2015-12-08 Query? 0860123107
                    incom_expense = 1;
                    int startIndex = msgBody.indexOf("R") + 1;
                    amount = Double.valueOf(msgBody.substring(startIndex, msgBody.indexOf(" ", startIndex)));
                    startIndex = msgBody.indexOf("returned to Acc.", startIndex) + 17;
                    account = msgBody.substring(startIndex, msgBody.indexOf(" ", startIndex));
                    startIndex = msgBody.indexOf(" from ", startIndex) + 6;
                    if (msgBody.contains(". Avl bal")) {
                        description = msgBody.substring(startIndex, msgBody.indexOf(". Avl bal", startIndex));
                        startIndex = msgBody.indexOf(". Avl bal R", startIndex) + 12;
                    } else if (msgBody.contains(". Acl bal")) {
                        description = msgBody.substring(startIndex, msgBody.indexOf(". Acl bal", startIndex));
                        startIndex = msgBody.indexOf(". Acl bal R", startIndex) + 12;
                    }
                    startIndex = msgBody.indexOf(" ", startIndex) + 1;
                    date = msgBody.substring(startIndex, startIndex = msgBody.indexOf(" ", startIndex));
                } else {
                    incom_expense = 0;
                    try {
                        String fileName2;
                        fileName2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/sms_unknown.txt";
                        File myFile2 = new File(fileName2);
                        myFile2.createNewFile();
                        FileOutputStream fs2 = new FileOutputStream(myFile2);
                        OutputStreamWriter out2 = new OutputStreamWriter(fs2);
                        out2.write(msgDate);
                        out2.write("\n\r");
                        out2.write(msgBody);
                        out2.write("\n\r");
                        out2.close();
                        fs2.close();
                    } catch (IOException ioe) {
                        //Toast.makeText(TransactionListActivity.this.getApplicationContext(), ioe.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                if (amount > 0) {

                    try {
                        sr0 = new Transaction(0,//get next ID
                                amount,
                                0,
                                account,
                                description,
                                dateFormat.parse(date),
                                msgBody,
                                incom_expense);
                        if (saveTrans) {
                            if (sr0 != null) {
                                sr0.Save();
                                transactionItems.add(sr0);
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception ex) {
            String exStr = ex.getMessage();
            try {
                String fileName2;
                fileName2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/sms_unknown.txt";
                File myFile2 = new File(fileName2);
                myFile2.createNewFile();
                FileOutputStream fs2 = new FileOutputStream(myFile2);
                OutputStreamWriter out2 = new OutputStreamWriter(fs2);
                out2.write(msgDate);
                out2.write("\n\r");
                out2.write(msgBody);
                out2.write("\n\r");
                out2.write(exStr);
                out2.write("\n\r");
                out2.close();
                fs2.close();
            } catch (IOException ioe) {
            }

        }
        return sr0;
    }

    private void AddTestArray(){

        Transaction sr1 = null;
        try {
            sr1 = new Transaction(1, 450.52, 1, "Cheque", "ULP95", dateFormat.parse("2014-12-25"),"", 2);
            transactionItems.add(sr1);
            sr1.Save();

            sr1 = new Transaction(2, 3600.52, 2, "Cheque", "Wesbank", dateFormat.parse("2014-12-25"),"", 2);
            transactionItems.add(sr1);
            sr1.Save();

            sr1 = new Transaction(3, 20, 3, "Cheque", "Mcd's", dateFormat.parse("2014-12-26"),"", 2);
            transactionItems.add(sr1);
            sr1.Save();

            sr1 = new Transaction(4, 30000, 4, "Cheque", "Salary", dateFormat.parse("2014-12-26"),"", 1);
            transactionItems.add(sr1);
            sr1.Save();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public static void LoadTransactionsFromDB() {
        Cursor resultSet = TransactionListActivity.db.rawQuery("SELECT * FROM [transaction] ORDER BY Date DESC",null);
        resultSet.moveToFirst();

        while (!resultSet.isAfterLast()) {
            int id = resultSet.getInt(0);
            double amount = resultSet.getDouble(1);
            int categoryId = resultSet.getInt(2);
            String account = resultSet.getString(3);
            String description = resultSet.getString(4);
            if (description.contains("//")) {
                description = description.replace("//","'");
            }
            Date date = null;
            try {
                date = dateFormat.parse(resultSet.getString(5));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //Date date = new Date(resultSet.getLong(5)*1000);
            String reference = resultSet.getString(6);
            if (reference.contains("//")) {
                reference = reference.replace("//","'");
            }
            int income_expence = resultSet.getInt(7);

            Transaction sr1 = null;
            sr1 = new Transaction(id, amount, categoryId, account, description, date, reference, income_expence);
            transactionItems.add(sr1);
            resultSet.moveToNext();
        }
    }

    public static void LoadSettingsFromDB() {
        Cursor resultSet = TransactionListActivity.db.rawQuery("SELECT * FROM [settings]",null);
        resultSet.moveToFirst();

        while (!resultSet.isAfterLast()) {
            int id = resultSet.getInt(0);
            String name = resultSet.getString(1);
            String value = resultSet.getString(2);

            if (name.equals("monthstartonday"))
            {
                monthstartonday = Integer.parseInt(value);
            }
            resultSet.moveToNext();
        }
    }

    public void refreshList() {
        if (!mode.equals("category")) {
            //Toast.makeText(getApplicationContext(), "Refreshing list", Toast.LENGTH_LONG).show();
            baseTransactionListAdapter.getFilter().filter("month", new Filter.FilterListener() {
                public void onFilterComplete(int count) {
                    setTitle("Wallet (" + count + ") - " + monthName + " " + year);
                }
            });
        }
    }

    private void refreshDates() {
        SimpleDateFormat dateFormat0 = new SimpleDateFormat("MMM");
        try {
            if (day >= monthstartonday) {
                if (month < 12) {
                    startDate = dateFormat.parse(year + "-" + month + "-" + monthstartonday);
                    endDate = dateFormat.parse(year + "-" + (month+1) + "-" + (monthstartonday-1));
                } else {
                    startDate = dateFormat.parse(year + "-" + month + "-" + monthstartonday);
                    endDate = dateFormat.parse(year+1 + "-" + (1) + "-" + (monthstartonday-1));
                }
            } else {
                if (month > 1) {
                    startDate = dateFormat.parse(year + "-" + (month-1) + "-" + monthstartonday);
                    endDate = dateFormat.parse(year + "-" + (month) + "-" + (monthstartonday-1));
                } else {
                    startDate = dateFormat.parse((year-1) + "-" + (12) + "-" + monthstartonday);
                    endDate = dateFormat.parse((year) + "-" + (month) + "-" + (monthstartonday-1));
                }
            }
            monthName = dateFormat0.format(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (baseTransactionListAdapter != null) {
            baseTransactionListAdapter.getFilter().filter("month", new Filter.FilterListener() {
                public void onFilterComplete(int count) {
                    setTitle("Wallet (" + count + ") - " + monthName + " " + year);
                }
            });
        }
    }

    private static final int FILE_SELECT_CODE = 0;
    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
}


//