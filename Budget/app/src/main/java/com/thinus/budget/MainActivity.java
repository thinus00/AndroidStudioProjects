package com.thinus.budget;


import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.EditText;
import android.widget.Filter;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends ActionBarActivity {

    public static SectionsPagerAdapter mSectionsPagerAdapter;
    public static ViewPager mViewPager;

    public static NotificationManager notificationManager;

    private Menu menu;

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat dateFormatCSV = new SimpleDateFormat("yyyyMMdd");

    public static int monthstartonday = 23;
    public static int day = 0;
    public static int month = 0;
    public static int year = 0;
    public static String monthName;
    public static Date startDate;
    public static Date endDate;

    private String search_Text = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                try {
                    String fileName2;
                    fileName2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/sms_exception3.txt";
                    File myFile2 = new File(fileName2);
                    if (!myFile2.exists()) myFile2.createNewFile();
                    FileWriter fw = new FileWriter(myFile2, true);
                    PrintWriter pw = new PrintWriter(fw);
                    paramThrowable.printStackTrace(pw);
                    pw.close();

                    new Thread() {
                        @Override
                        public void run() {
                            Looper.prepare();
                            Toast.makeText(getApplicationContext(), "Application crashed", Toast.LENGTH_LONG).show();
                            Looper.loop();
                        }
                    }.start();

                    try {
                        Thread.sleep(4000); // Let the Toast display before app will get shutdown
                    } catch (InterruptedException e) {
                        // Ignored.
                    }
                    System.exit(1);
                } catch (IOException ioe) {
                    //Toast.makeText(TransactionListActivity.this.getApplicationContext(), ioe.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        setContentView(R.layout.activity_main);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.

        Database.LoadDatabase(openOrCreateDatabase("WalletDB", Context.MODE_PRIVATE, null));

        transactionItems = new ArrayList<Transaction>();
        categoryItems = new ArrayList<Category>();
        try {
            Database.LoadCategoriesFromDB();
            Database.LoadTransactionsFromDB();
            Database.LoadSettingsFromDB();
        } catch (Exception ex) {
            Toast.makeText(MainActivity.this.getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Date date = new Date();
        DateFormat dateFormat0 = new SimpleDateFormat("dd");
        day = Integer.parseInt(dateFormat0.format(date));
        dateFormat0 = new SimpleDateFormat("MM");
        month = Integer.parseInt(dateFormat0.format(date));
        dateFormat0 = new SimpleDateFormat("yyyy");
        year = Integer.parseInt(dateFormat0.format(date));
        refreshDates();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            // This method will be invoked when a new page becomes selected.
            @Override
            public void onPageSelected(int position) {
                invalidateOptionsMenu();
//                switch (position) {
//                    case 0:
//                        break;
//                    case 1:
//                        break;
//                    case 2:
//                        break;
//                }
//                mSectionsPagerAdapter.notifyDataSetChanged();
            }

            // This method will be invoked when the current page is scrolled
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Code goes here
            }

            // Called when the scroll state changes:
            // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
            @Override
            public void onPageScrollStateChanged(int state) {
                // Code goes here
            }
        });

        mViewPager.setCurrentItem(1);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        int pageNum = mViewPager.getCurrentItem();
        switch (pageNum) {
            case 0:
                break;
            case 1:
                menu.findItem(R.id.add_transaction).setVisible(true);
                menu.findItem(R.id.show_unlinked).setVisible(true);
                menu.findItem(R.id.add_transaction).setVisible(true);
                menu.findItem(R.id.show_all).setVisible(true);
                menu.findItem(R.id.add_category).setVisible(false);
                menu.findItem(R.id.show_only_actuals).setVisible(false);
                menu.findItem(R.id.show_only_budgets).setVisible(false);
                menu.findItem(R.id.show_budgetnotspent).setVisible(false);
                menu.findItem(R.id.show_remaining).setVisible(false);
                menu.findItem(R.id.sort_ammount).setVisible(true);
                break;
            case 2:
                menu.findItem(R.id.add_transaction).setVisible(false);
                menu.findItem(R.id.show_unlinked).setVisible(false);
                menu.findItem(R.id.add_transaction).setVisible(false);
                menu.findItem(R.id.show_all).setVisible(true);
                menu.findItem(R.id.add_category).setVisible(true);
                menu.findItem(R.id.show_only_actuals).setVisible(true);
                menu.findItem(R.id.show_only_budgets).setVisible(true);
                menu.findItem(R.id.show_budgetnotspent).setVisible(true);
                menu.findItem(R.id.show_remaining).setVisible(true);
                menu.findItem(R.id.sort_ammount).setVisible(false);
                break;

        }
        return true;
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

        if (id == R.id.action_search) {
            search_Text = "";

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Search");

            // Set up the input
            final EditText input = new EditText(this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    search_Text = input.getText().toString();
                    TransactonFragment page = (TransactonFragment)getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.container + ":" + 1);
                    if (page != null) {
                        if (page.getListAdapter() != null) {
                            page.getListAdapter().getFilter().filter("search " + search_Text, new Filter.FilterListener() {
                                public void onFilterComplete(int count) {
                                    transactionFilteredCount = count;
                                    mSectionsPagerAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();


            return true;
        }

        if (id == R.id.action_refresh) {
            Toast.makeText(getApplicationContext(), "Refreshing list", Toast.LENGTH_LONG).show();
            categoryItems.clear();
            transactionItems.clear();
            Database.LoadCategoriesFromDB();
            Database.LoadTransactionsFromDB();

            refreshList();
            return true;
        }


        if (id == R.id.add_transaction) {
            Intent intent = new Intent(MainActivity.this, TransactionAddActivity.class);
            intent.putExtra("mode", "add");
            intent.putExtra("id", 0);
            this.startActivity(intent);
            return true;
        }

        if (id == R.id.show_unlinked) {
            TransactonFragment page = (TransactonFragment)getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.container + ":" + 1);
            if (page != null) {
                if (page.getListAdapter() != null) {
                    page.getListAdapter().getFilter().filter("unlinked", new Filter.FilterListener() {
                        public void onFilterComplete(int count) {
                            transactionFilteredCount = count;
                            mSectionsPagerAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
            return true;
        }

        if (id == R.id.show_duplicates) {
            transactionItems.clear();
            Database.LoadTransactionsFromDB("amount, date");
            TransactonFragment page = (TransactonFragment)getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.container + ":" + 1);
            if (page != null) {
                if (page.getListAdapter() != null) {
                    page.getListAdapter().getFilter().filter("duplicates", new Filter.FilterListener() {
                        public void onFilterComplete(int count) {
                            transactionFilteredCount = count;
                            mSectionsPagerAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
            return true;
        }

        if (id == R.id.show_all) {
            TransactonFragment page = (TransactonFragment)getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.container + ":" + 1);
            if (page != null) {
                if (page.getListAdapter() != null) {
                    page.getListAdapter().getFilter().filter("all", new Filter.FilterListener() {
                        public void onFilterComplete(int count) {
                            transactionFilteredCount = count;
                            CategoryFragment pageCat = (CategoryFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.container + ":" + 2);
                            if (pageCat != null) {
                                if (pageCat.getListAdapter() != null) {
                                    pageCat.calcBugetTotals();
                                    pageCat.getListAdapter().getFilter().filter("all");
                                }
                            }
                            mSectionsPagerAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
            return true;
        }



        if (id == R.id.add_category) {
            Intent intent = new Intent(MainActivity.this, CategoryAddActivity.class);
            intent.putExtra("mode", "add");
            intent.putExtra("id", 0);
            this.startActivity(intent);
            return true;
        }

        if (id == R.id.show_only_actuals) {
            CategoryFragment pageCat = (CategoryFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.container + ":" + 2);
            if (pageCat != null) {
                if (pageCat.getListAdapter() != null) {
                    pageCat.calcBugetTotals();
                    pageCat.getListAdapter().getFilter().filter("actuals");
                }
            }
            return true;
        }

        if (id == R.id.show_only_budgets) {
            CategoryFragment pageCat = (CategoryFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.container + ":" + 2);
            if (pageCat != null) {
                if (pageCat.getListAdapter() != null) {
                    pageCat.calcBugetTotals();
                    pageCat.getListAdapter().getFilter().filter("budgets");
                }
            }
            return true;
        }

        if (id == R.id.show_remaining) {
            ShowRemaining = !ShowRemaining;
            MenuItem mi = (MenuItem)menu.findItem(R.id.show_remaining);
            if (ShowRemaining) {
                mi.setTitle(getResources().getString(R.string.show_actuals));
            } else {
                mi.setTitle(getResources().getString(R.string.show_remaining));
            }
            refreshList();
            return true;
        }

        if (id == R.id.show_budgetnotspent) {
            CategoryFragment pageCat = (CategoryFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.container + ":" + 2);
            if (pageCat != null) {
                if (pageCat.getListAdapter() != null) {
                    pageCat.calcBugetTotals();
                    pageCat.getListAdapter().getFilter().filter("budgetsnotspent");
                }
            }
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
                            MainActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(MainActivity.this.getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        //TransactionListActivity.this.refreshList();
                    }
                    barSMSScanDialog.dismiss();
                    final String message = "Found " + (transactionItems.size()-transactionItemscount) + " transactions";
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(MainActivity.this.getApplicationContext(), message, Toast.LENGTH_LONG).show();
                        }
                    });
                    transactionItems.clear();
                    Database.LoadTransactionsFromDB();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            MainActivity.this.refreshList();
                        }
                    });
                }
            }).start();

            return true;
        }

        if (id == R.id.sort_ammount) {
            transactionItems.clear();
            Database.LoadTransactionsFromDB("amount");
            TransactonFragment page = (TransactonFragment)getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.container + ":" + 1);
            if (page != null) {
                if (page.getListAdapter() != null) {
                    page.getListAdapter().getFilter().filter("month", new Filter.FilterListener() {
                        public void onFilterComplete(int count) {
                            transactionFilteredCount = count;
                            mSectionsPagerAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
            return true;
        }



        if (id == R.id.backup_database) {
            Toast.makeText(getApplicationContext(), Database.BackupDB(), Toast.LENGTH_LONG).show();
            return true;
        }
        if (id == R.id.restore_database) {
            Toast.makeText(getApplicationContext(), Database.RestoreDB(), Toast.LENGTH_LONG).show();
            return true;
        }

        if (id == R.id.action_settings) {
            return true;
        }


        return super.onOptionsItemSelected(item);
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
                            int transactionItemsDupcount = 0;

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
                                                    } else {
                                                        transactionItemsDupcount++;
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
                            final String message = "Found " + transactionItemscount + " transactions (" + transactionItemsDupcount + " dup)";
                            MainActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(MainActivity.this.getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                }
                            });
                            transactionItems.clear();
                            Database.LoadTransactionsFromDB();
                            MainActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    MainActivity.this.refreshList();
                                }
                            });
                        }
                    }).start();

                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private static final int FILE_SELECT_CODE = 0;
    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"),FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
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

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            Fragment fragment = new Fragment();
            switch (position) {
                case 0:
                    fragment = new SumaryFragment();
                    break;
                case 1:
                    fragment = new TransactonFragment();
                    break;
                case 2:
                    fragment = new CategoryFragment();
                    break;
            }
            return fragment;

        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Summary";
                case 1:
                    return "Transactions (" + transactionFilteredCount + "/" + MainActivity.transactionFilteredUnlinkedCount + "-" + MainActivity.transactionUnlinkedCount + ") - " + monthName + " " + year;
                case 2:
                    return "Categories";
            }
            return "n/a";
        }
    }

    public void refreshList() {
        TransactonFragment page = (TransactonFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.container + ":" + 1);
        if (page != null) {
            if (page.getListAdapter() != null) {
                page.getListAdapter().getFilter().filter("month", new Filter.FilterListener() {
                    public void onFilterComplete(int count) {
                        transactionFilteredCount = count;
                        CategoryFragment pageCat = (CategoryFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.container + ":" + 2);
                        if (pageCat != null) {
                            if (pageCat.getListAdapter() != null) {
                                pageCat.calcBugetTotals();
                                pageCat.getListAdapter().getFilter().filter("all");
                            }
                            SumaryFragment pageSum = (SumaryFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.container + ":" + 0);
                            if (pageSum != null) {
                                pageSum.refreshValues();
                            }
                        }
                        mSectionsPagerAdapter.notifyDataSetChanged();
                    }
                });
            }
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

        refreshList();
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



    /*
    Category
    */
    public static ArrayList<Category> categoryItems;
    public static int catFilterID = 0;
    //public static boolean catFilterON = false;

    public static boolean ShowRemaining = false;

    private static ArrayList<String> categoryItemsspinner;
    public static ArrayList<String> GetCategorycategoryItemsSpinner() {
        if (categoryItemsspinner==null) {
            categoryItemsspinner = new ArrayList<String>();
        }
        categoryItemsspinner.clear();

        for (int i = 0; i < categoryItems.size(); i++)
        {
            categoryItemsspinner.add((categoryItems.get(i)).getName());
        }

        return categoryItemsspinner;
    }

    public static int GetCategoryIndexForSpinnerFromId(int id) {
        for (int i = 0; i < categoryItems.size(); i++)
        {
            if (((Category)categoryItems.get(i)).getId() == id)
                return i;
        }

        return 0;
    }

    public static String getCategoryName(int Id){
        for (Category s : categoryItems){

            if (s.getId() == (Id))
                return s.getName();
        }
        return "Category not found";
    }

    public static String getCategoryNameType(int Id){
        for (Category s : categoryItems){
            if (s.getId() == (Id))
                return s.getCatType() + " -> " + s.getName();
        }
        return "Category not found";
    }

    public static int getCategoryIDName(String name){
        for (Category s : categoryItems){
            if (s.getName().equals(name))
                return s.getId();
        }
        return 0;
    }

    public static Category getCategoryByID(int id){
        for (Category s : categoryItems){
            if (s.getId() == id)
                return s;
        }
        return null;
    }

    public static boolean deleteCategoryByID(int id){
        for (Category c : categoryItems){
            if (c.getId() == id) {
                c.Delete();
                categoryItems.remove(c);
                return true;
            }
        }
        return false;
    }



    /*
    Transactions
     */
    public static ArrayList<Transaction> transactionItems;
    public static ArrayList<Transaction> transactionItems_month;
    public static int transactionFilteredCount = 0;
    public static int transactionFilteredUnlinkedCount = 0;
    public static int transactionUnlinkedCount = 0;

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


}
