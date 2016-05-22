package com.thinus.budget;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by thinus on 2016/03/19.
 */
public class Database {



    private static SQLiteDatabase db;

    public static void LoadDatabase(SQLiteDatabase tmpdb) {
        db = tmpdb;

        db.execSQL("CREATE TABLE IF NOT EXISTS [category] (id INT PRIMARY KEY NOT NULL, name VARCHAR, categorytype INT, budget DOUBLE, smsDescription VARCHAR);");
        db.execSQL("CREATE TABLE IF NOT EXISTS [transaction] (id INT PRIMARY KEY NOT NULL, amount DOUBLE, categoryid INT, account VARCHAR,description VARCHAR,date VARCHAR, reference VARCHAR, income_expense int);");
        db.execSQL("CREATE TABLE IF NOT EXISTS [settings] (id INT PRIMARY KEY NOT NULL, name VARCHAR,value VARCHAR);");
    }

    public static String BackupDB() {
        String message = null;
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String  currentDBPath= "//data//" + "com.thinus.budget"
                        + "//databases//" + "WalletDB";
                String backupDBPath  = "/BackupFolder/WalletDB";
                String backupDBDirPath  = "/BackupFolder";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);
                File backupDBDir = new File(sd, backupDBDirPath);
                if(!backupDBDir.exists()) {
                    if (!backupDBDir.mkdirs()) {
                        return "Could not make dir " + backupDBDir.getPath();
                    }
                }
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                return "DB backuped to " + backupDB.toString();
            }
        } catch (Exception e) {
            message = e.toString();
        }
        return message;
    }

    public static String RestoreDB() {
        String message = null;
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//" + "com.thinus.budget"
                        + "//databases//" + "WalletDB";
                String backupDBPath = "/BackupFolder/WalletDB";
                File backupDB = new File(data, currentDBPath);
                File currentDB = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                return "DB Restored from " + backupDB.toString();
            }
        } catch (Exception e) {
            message = e.toString();
        }
        return message;
    }

    public static void LoadSettingsFromDB() {
        Cursor resultSet = db.rawQuery("SELECT * FROM [settings]",null);
        resultSet.moveToFirst();

        while (!resultSet.isAfterLast()) {
            int id = resultSet.getInt(0);
            String name = resultSet.getString(1);
            String value = resultSet.getString(2);

            if (name.equals("monthstartonday"))
            {
                MainActivity.monthstartonday = Integer.parseInt(value);
            }

            // Transactionlist font size
            // Categorylist font size
            resultSet.moveToNext();
        }
    }

    public static String RecreateDB() {
        String message = null;
        try
        {
            db.execSQL("DROP TABLE IF EXISTS [transaction];");
            db.execSQL("CREATE TABLE IF NOT EXISTS [transaction] (id INT PRIMARY KEY NOT NULL, amount DOUBLE, categoryid INT, account VARCHAR,description VARCHAR,date VARCHAR, reference VARCHAR, income_expense int);");
            db.execSQL("DROP TABLE IF EXISTS [category];");
            db.execSQL("CREATE TABLE IF NOT EXISTS [category] (id INT PRIMARY KEY NOT NULL, name VARCHAR, categorytype INT, budget DOUBLE, smsDescription VARCHAR);");
            db.execSQL("DROP TABLE IF EXISTS [settings];");
            db.execSQL("CREATE TABLE IF NOT EXISTS [settings] (id INT PRIMARY KEY NOT NULL, name VARCHAR,value VARCHAR);");
            db.execSQL("INSERT INTO [settings] VALUES(1, 'monthstartonday', '23')");

            MainActivity.transactionItems = new ArrayList<Transaction>();
            MainActivity.categoryItems = new ArrayList<Category>();
            return "DB Cleaned";
        } catch (Exception ex) {
            message = ex.toString();
        }
        return message;
    }

    public static String ClearTransactions() {
        String message = null;

        try {
            db.execSQL("DROP TABLE IF EXISTS [transaction];");
            db.execSQL("CREATE TABLE IF NOT EXISTS [transaction] (id INT PRIMARY KEY NOT NULL, amount DOUBLE, categoryid INT, account VARCHAR,description VARCHAR,date VARCHAR, reference VARCHAR, income_expense int);");
            return "Transactions Cleaned";
        } catch (Exception ex) {
            message = ex.toString();
        }
        return message;
    }

    /*
    Transactions
     */

    public static void LoadTransactionsFromDB() {
        LoadTransactionsFromDB("Date DESC");
    }
    public static void LoadTransactionsFromDB(String sort) {
        Cursor resultSet = db.rawQuery("SELECT * FROM [transaction] ORDER BY " + sort, null);
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
                date = MainActivity.dateFormat.parse(resultSet.getString(5));
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
            MainActivity.transactionItems.add(sr1);
            resultSet.moveToNext();
        }
    }





    public static void SaveTransaction(Transaction tr){
        db.execSQL("INSERT INTO [transaction] VALUES('" +   tr.getId() + "','" +
                                                            tr.getAmount() + "','" +
                                                            tr.getCategoryId() + "','" +
                                                            tr.getAccount() + "','" +
                                                            tr.getDescription() + "','" +
                                                            tr.getDateFormated() + "','" +
                                                            tr.getReference() + "','" +
                                                            tr.getIncomeExpense() + "');");
    }

    public static void UpdateTransaction(Transaction tr) {
        db.execSQL("UPDATE [transaction] SET    amount = '" +  tr.getAmount() +
                                            "', categoryid = '" + tr.getCategoryId() +
                                            "', account = '" + tr.getAccount() +
                                            "', description = '" + tr.getDescription() +
                                            "', date = '" + tr.getDateFormated() +
                                            "', reference = '" + tr.getReference() +
                                            "', income_expense = '" + tr.getIncomeExpense() +
                "' WHERE id = " + tr.getId() + ";");
    }

    public static void DeleteTransaction(Transaction tr) {
        db.execSQL("DELETE FROM [transaction] WHERE id = " + tr.getId() + ";");
    }



    /*
    Categories
    */

    public static void LoadCategoriesFromDB() {
        //'" + _id + "','" + _name + "','" + getCatNameFromType(_catType) + "','" + _budget + "'
        Cursor resultSet = db.rawQuery("Select * from [category] order by categorytype, name",null);
        resultSet.moveToFirst();

        if (resultSet.getCount() == 0) {
            Category sr0 = new Category(-1, "unknown", Category.CategoryType.DayToDay, 0, "");
            MainActivity.categoryItems.add(sr0);
            sr0.Save();
        }
        while (!resultSet.isAfterLast()) {
            int id = resultSet.getInt(0);
            String name = resultSet.getString(1);
            //Category.CategoryType catType = Category.CategoryType.valueOf(resultSet.getString(2));
            Category.CategoryType catType = Category.getCatTypeFromName(resultSet.getString(2));
            double budget = resultSet.getDouble(3);
            String smsDecription = resultSet.getString(4);

            Category sr1 = null;
            sr1 = new Category(id, name, catType, budget, smsDecription);
            MainActivity.categoryItems.add(sr1);
            resultSet.moveToNext();
        }
    }

    public static void SaveCategory(Category cat) {
        db.execSQL("INSERT INTO [category] VALUES('" +  cat.getId() + "','" +
                                                        cat.getName() + "','" +
                                                        cat.getCatName() + "','" +
                                                        cat.getBudget() + "','" +
                                                        cat.getSmsDescriptionString() + "');");
    }

    public static void UpdateCategory(Category cat) {
        db.execSQL("UPDATE [category] SET   name = '" + cat.getName() +
                                        "', categorytype = '" + cat.getCatName() +
                                        "', budget = '" + cat.getBudget() +
                                        "', smsDescription = '" + cat.getSmsDescriptionString() +
                "' WHERE id = " + cat.getId() + ";");
    }

    public static void DeleteCategory(Category cat) {
        db.execSQL("DELETE FROM [category] WHERE id = " + cat.getId() + ";");
    }
}
