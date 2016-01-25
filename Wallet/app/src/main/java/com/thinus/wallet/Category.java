package com.thinus.wallet;

import java.util.ArrayList;

/**
 * Created by thinus on 2014/12/27.
 */

public class Category {

    public enum CategoryType {
        Recurring,
        DayToDay,
        Income,
        Transfer
    }

    private static int catId = 1;

    public String _error = "";

    private static ArrayList<String> array_spinner;
    public static ArrayList<String> GetCategoryTypeSpinnerList() {
        if (array_spinner==null) {
            array_spinner = new ArrayList<String>();
        }
        array_spinner.clear();
        array_spinner.add("Recurring");
        array_spinner.add("Day-to-Day");
        array_spinner.add("Income");
        array_spinner.add("Transfer");

        return array_spinner;
    }

    private String getCatNameFromType(CategoryType ct)
    {
        ArrayList<String> cSpinnger = GetCategoryTypeSpinnerList();
        CategoryType[] ctList = CategoryType.values();
        for (int i = 0; i < ctList.length; i++)
        {
            if (ctList[i] == ct)
            {
                return cSpinnger.get(i);
            }
        }
        return "Not Found";
    }

    public int getCatIndexFromType(CategoryType ct)
    {
        ArrayList<String> cSpinnger = GetCategoryTypeSpinnerList();
        CategoryType[] ctList = CategoryType.values();
        for (int i = 0; i < ctList.length; i++)
        {
            if (ctList[i] == ct)
            {
                return i;
            }
        }
        return 0;
    }

    public static CategoryType getCatTypeFromName(String ct)
    {
        ArrayList<String> cSpinnger = GetCategoryTypeSpinnerList();
        CategoryType[] ctList = CategoryType.values();

        for (int i = 0; i < cSpinnger.size()-1; i++)
        {
            if (cSpinnger.get(i).equals(ct))
            {
                return ctList[i];
            }
        }
        return CategoryType.Transfer;
    }

    public static String getSmsDescriptionString(ArrayList<String> tmpList) {
        String listString = "";
        for (int i = 0; i < tmpList.size(); i++)
        {
            if (listString.length() > 0) {
                listString = listString + ";";
            }
            listString = listString + tmpList.get(i).replace(";","//");
        }
        return listString;
    }

    public String getSmsDescriptionString() {
        String listString = "";
        for (int i = 0; i < _smsDescriptions.size(); i++)
        {
            if (listString.length() > 0) {
                listString = listString + ";";
            }
            listString = listString + _smsDescriptions.get(i).replace(";","//");
        }
        return listString;
    }

    public void setSmsDescriptionString(String listString) {
        _smsDescriptions.clear();

        if (listString.length() > 0) {
            String[] strArray = listString.split(";");
            for (int i = 0; i < strArray.length; i++)
            {
                _smsDescriptions.add(strArray[i].replace("//",";"));
            }
        }
    }

    private int _id;
    private String _name;
    private CategoryType _catType;
    private double _budget;
    private double _budgetTotal;
    private ArrayList<String> _smsDescriptions;

    public int getId()
    {
        return _id;
    }
    public String getName()
    {
        return _name;
    }
    public CategoryType getCatType()
    {
        return _catType;
    }
    public double getBudget()
    {
        return _budget;
    }
    public double getBudgetTotal()
    {
        return _budgetTotal;
    }
    public ArrayList<String> getSMSDescription()
    {
        return _smsDescriptions;
    }
    public int getSmsDescriptionCount() { return _smsDescriptions.size(); }
    public String getSmsDescription(int pos)
    {
        return _smsDescriptions.get(pos);
    }
    public String getCatName()
    {
        return getCatNameFromType(_catType);
    }

    public void setName(String tmpName)
    {
        _name = tmpName;
    }
    public void setCatType(CategoryType tmpCatType)
    {
        _catType = tmpCatType;
    }
    public void setBudget(double tmpBudget)
    {
        _budget = tmpBudget;
    }
    public void setBudgetTotal(double tmpBudget)
    {
        _budgetTotal = tmpBudget;
    }
    public void addSmsDescription(String SmsDescription)
    {
        if (!_smsDescriptions.contains(SmsDescription))
            _smsDescriptions.add(SmsDescription);
    }

    Category(int Id, String Name, CategoryType catType, double Budget, String smsDescriptionString)
    {
        if (Id == 0) {
            _id = getNextID();
        } else if (Id == -1) {
            _id = 0;
        } else {
            _id = Id;
        }


        if (catId <= _id)
            catId = _id + 1;

        _name = Name;
        _catType = catType;
        _budget = Budget;
        _smsDescriptions = new ArrayList<String>();
        if (smsDescriptionString.length() > 0) {
            String[] strArray = smsDescriptionString.split(";");
            for (int i = 0; i < strArray.length; i++)
            {
                _smsDescriptions.add(strArray[i].replace("//",";"));
            }
        }
    }

    private int getNextID(){
        //look for max categoryId and add 1
        return catId++;
    }

    public void Save()
    {
        try {
            TransactionListActivity.db.execSQL("INSERT INTO [category] VALUES('" + _id + "','" + _name + "','" + getCatNameFromType(_catType) + "','" + _budget + "','" + getSmsDescriptionString(_smsDescriptions) + "');");
        } catch (Exception ex) {
            _error = ex.toString();
        }
    }

    public void Update()
    {
        try {
            TransactionListActivity.db.execSQL("UPDATE [category] SET name = '" + _name + "', categorytype = '" + getCatNameFromType(_catType) + "', budget = '" + _budget + "', smsDescription = '" + getSmsDescriptionString(_smsDescriptions) + "' WHERE id = " + _id + ";");
        } catch (Exception ex) {
            _error = ex.toString();
        }
    }

    public void Delete()
    {
        try {
            TransactionListActivity.db.execSQL("DELETE FROM [category] WHERE id = " + _id + ";");
        } catch (Exception ex) {
            _error = ex.toString();
        }
    }
}
