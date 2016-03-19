package com.thinus.budget;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by thinus on 2014/12/26.
 */
public class Transaction {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");;
    public String _error = "";

    private static int transId = 0;

    private int _id;
    private double _amount;
    private int _categoryId;
    private String _account;
    private String _description;
    private Date _date;
    private String _reference;
    private int _income_expense; //unknown = 0 income = 1 expense = 2

    public int getId()
    {
        return _id;
    }
    public double getAmount()
    {
        return _amount;
    }
    public int getCategoryId()
    {
        return _categoryId;
    }
    public String getAccount()
    {
        return _account;
    }
    public String getDescription()
    {
        return _description;
    }
    public Date getDate()
    {
        return _date;
    }
    public String getDateFormated() { return dateFormat.format(_date); }
    public String getReference()
    {
        return _reference;
    }
    public int getIncomeExpense()
    {
        return _income_expense;
    }
    public String getIncomeExpenseString() {
        if (_income_expense == 1) {
            return "Income";
        } else if (_income_expense == 2) {
            return "Expense";
        } else {
            return "Unknown";
        }
    }

    public void setAmount(double tmpAmount)
    {
        _amount = tmpAmount;
    }
    public void setCategoryId(int tmpCategoryId)
    {
        _categoryId = tmpCategoryId;
    }
    public void setAccount(String tmpAccount)
    {
        _account = tmpAccount;
    }
    public void setDescription(String tmpDescription)
    {
        _description = tmpDescription;
    }
    public void setDate(Date tmpDate)
    {
        _date = tmpDate;
    }
    public void setReference(String tmpReference)
    {
        _reference = tmpReference;
    }
    public void setIncomeExpense(int tmpIncomeExpense)
    {
        _income_expense = tmpIncomeExpense;
    }

    public String getCategoryName()
    {
        return MainActivity.getCategoryName(_categoryId);
    }

    public String getCategoryNameType()
    {
        return MainActivity.getCategoryNameType(_categoryId);
    }

    Transaction(int Id, double amount, int categoryId, String account, String description, Date date, String reference, int income_expense)
    {
        _error = "";

        if (Id == 0) {
            _id = getNextID();
        } else {
            _id = Id;
        }
        if (transId <= _id)
            transId = _id + 1;

        _amount = amount;
        _categoryId = categoryId;
        _account = account;
        _description = description;
        _date = date;
        _reference = reference;
        _income_expense = income_expense;
    }

    private int getNextID(){
        //look for max categoryId and add 1
        return transId++;
    }

    public void Save()
    {
        try {
            if (_categoryId == 0) {
                //try to autolink
                String desc = _description;
                boolean foundDesc = false;
                for (Category c : MainActivity.categoryItems) {
                    for (String s : c.getSMSDescription()) {
                        if (desc.contains(s)) {
                            foundDesc = true;
                            //update category id
                            _categoryId = c.getId();
                            break;
                        }
                    }
                    if (foundDesc) {
                        break;
                    }
                }
            }

            if (_description.contains("'")) {
                _description = _description.replace("'","//");
            }
            if (_reference.contains("'")) {
                _reference = _reference.replace("'","//");
            }

            Database.SaveTransaction(this);

        } catch (Exception ex) {
            _error = ex.toString();
        }
    }

    public void Update()
    {
        try {
            if (_description.contains("'")) {
                _description = _description.replace("'","//");
            }
            if (_reference.contains("'")) {
                _reference = _reference.replace("'","//");
            }
            Database.UpdateTransaction(this);

        } catch (Exception ex) {
            _error = ex.toString();
        }
    }

    public void Delete()
    {
        try {
            Database.DeleteTransaction(this);
        } catch (Exception ex) {
            _error = ex.toString();
        }
    }
}
