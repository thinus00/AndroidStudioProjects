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
    private int _deleted; //no = 0 yes = 1
    private String _sms;
    private String _csv_prov;
    private String _csv_hist;
    private Date _date_created;
    private int _split; //no = 0 yes = 1
    private int _split_trans_id1;
    private int _split_trans_id2;
    private int _type; //0=normal,1=possible,2=linkto
    private int _linktoid;
    private double _possibleLinkToSimilarity;

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
    public int getDeleted()
    {
        return _deleted;
    }
    public String getSms()
    {
        return _sms;
    }
    public String getCSV_prov()
    {
        return _csv_prov;
    }
    public String getCSV_hist()
    {
        return _csv_hist;
    }
    public Date getDateCreated()
    {
        return _date_created;
    }
    public int getSplit()
    {
        return _split;
    }
    public int getSplitTransID1()
    {
        return _split_trans_id1;
    }
    public int getSplitTransID2()
    {
        return _split_trans_id2;
    }
    public int getType()
    {
        return _type;
    }
    public int getLinkToId()
    {
        return _linktoid;
    }
    public double getPossibleLinktoSim()
    {
        return _possibleLinkToSimilarity;
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
    public void setDeleted(int tmpDeleted)
    {
        _deleted = tmpDeleted;
    }
    public void setSMS(String tmpSMS)
    {
        _sms = tmpSMS;
        updateRef();
    }
    public void setCSVProv(String tmpCSVProv)
    {
        _csv_prov = tmpCSVProv;
        updateRef();
    }
    public void setCSVHist(String tmpCSVHist)
    {
        _csv_hist = tmpCSVHist;
        updateRef();
    }
    public void setDateCreated(Date tmpDateCreated)
    {
        _date_created = tmpDateCreated;
    }
    public void setSplit(int tmpSplit)
    {
        _split = tmpSplit;
    }
    public void setSplitTransID1(int tmpSplitTransID1)
    {
        _split_trans_id1 = tmpSplitTransID1;
    }
    public void setSplitTransID2(int tmpSplitTransID2)
    {
        _split_trans_id2 = tmpSplitTransID2;
    }
    public void setType(int tmpType)
    {
        _type = tmpType;
    }
    public void setLinkToId(int tmpLinkToId)
    {
        _linktoid = tmpLinkToId;
    }
    public void setPossibleLinktoSim(double tmpPossibleLinkToSim)
    {
        _possibleLinkToSimilarity = tmpPossibleLinkToSim;
    }

    public String getCategoryName()
    {
        return MainActivity.getCategoryName(_categoryId);
    }

    public String getCategoryNameType()
    {
        return MainActivity.getCategoryNameType(_categoryId);
    }

    Transaction(int Id, double amount, int categoryId, String account, String description, Date date, String reference, int income_expense, int deleted, String sms, String csv_prov, String csv_hist, Date dateCreated, int split, int split_transid1, int split_transid2)
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
        //_reference = reference;
        _income_expense = income_expense;
        _deleted = deleted;
        _sms = sms;
        _csv_prov = csv_prov;
        _csv_hist = csv_hist;
        _date_created = dateCreated;
        _split = split;
        _split_trans_id1 = split_transid1;
        _split_trans_id2 = split_transid2;
        _type = 0;
        updateRef();
    }

    private void updateRef() {
        if (!_csv_hist.isEmpty()) {
            _reference = _csv_hist;
        } else {
            if (!_csv_prov.isEmpty()) {
                _reference = _csv_prov;
            } else {
                if (!_sms.isEmpty()) {
                    _reference = _sms;
                } else {
                    _reference = "";
                }
            }
        }
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
            //if (_reference.contains("'")) {
            //    _reference = _reference.replace("'","//");
            //}
            if (_sms.contains("'")) {
                _sms = _sms.replace("'","//");
            }
            if (_csv_prov.contains("'")) {
                _csv_prov = _csv_prov.replace("'","//");
            }
            if (_csv_hist.contains("'")) {
                _csv_hist = _csv_hist.replace("'","//");
            }

            Database.SaveTransaction(this);

        } catch (Exception ex) {
            _error = ex.toString();
        }
    }

    public void Update()
    {
        try {
            updateRef();
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
