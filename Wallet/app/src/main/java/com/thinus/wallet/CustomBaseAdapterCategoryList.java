package com.thinus.wallet;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by thinus on 2014/12/27.
 */

public class CustomBaseAdapterCategoryList extends BaseAdapter {
    private static ArrayList<Category> searchArrayList;

    private LayoutInflater mInflater;

    public CustomBaseAdapterCategoryList(Context context, ArrayList<Category> results) {
        searchArrayList = results;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return searchArrayList.size();
    }

    public Object getItem(int position) {
        return searchArrayList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.custom_category_item_view, null);
            holder = new ViewHolder();
            holder.txtName = (TextView) convertView.findViewById(R.id.name);
            holder.pBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
            holder.txtBudget = (TextView) convertView.findViewById(R.id.budget);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //);setText(searchArrayList.get(position).getCatType().toString()
        holder.txtName.setText(searchArrayList.get(position).getName());
        holder.pBar.setMax(100);
        double budgetTotal = 0;
        for (int i = 0; i < TransactionListActivity.transactionItems_month.size(); i++) {
            if (TransactionListActivity.transactionItems_month.get(i).getCategoryId() == searchArrayList.get(position).getId()) {
                if ((searchArrayList.get(position).getCatType() == Category.CategoryType.DayToDay || searchArrayList.get(position).getCatType() == Category.CategoryType.Recurring) && TransactionListActivity.transactionItems_month.get(i).getIncomeExpense() == 2)
                    budgetTotal = budgetTotal + TransactionListActivity.transactionItems_month.get(i).getAmount();
                if ((searchArrayList.get(position).getCatType() == Category.CategoryType.DayToDay || searchArrayList.get(position).getCatType() == Category.CategoryType.Recurring) && TransactionListActivity.transactionItems_month.get(i).getIncomeExpense() == 1)
                    budgetTotal = budgetTotal - TransactionListActivity.transactionItems_month.get(i).getAmount();
                if ((searchArrayList.get(position).getCatType() == Category.CategoryType.Income) && TransactionListActivity.transactionItems_month.get(i).getIncomeExpense() == 2)
                    budgetTotal = budgetTotal - TransactionListActivity.transactionItems_month.get(i).getAmount();
                if ((searchArrayList.get(position).getCatType() == Category.CategoryType.Income) && TransactionListActivity.transactionItems_month.get(i).getIncomeExpense() == 1)
                    budgetTotal = budgetTotal + TransactionListActivity.transactionItems_month.get(i).getAmount();
                if ((searchArrayList.get(position).getCatType() == Category.CategoryType.Transfer) && TransactionListActivity.transactionItems_month.get(i).getIncomeExpense() == 2)
                    budgetTotal = budgetTotal + TransactionListActivity.transactionItems_month.get(i).getAmount();
                if ((searchArrayList.get(position).getCatType() == Category.CategoryType.Transfer) && TransactionListActivity.transactionItems_month.get(i).getIncomeExpense() == 1)
                    budgetTotal = budgetTotal - TransactionListActivity.transactionItems_month.get(i).getAmount();
            }
        }
        budgetTotal = Double.valueOf(String.format("%.2f", budgetTotal));
        holder.pBar.setProgress((int)Math.floor((budgetTotal/(searchArrayList.get(position).getBudget()))*100));
        if ((searchArrayList.get(position).getBudget() == 0 && budgetTotal == 0) || (searchArrayList.get(position).getCatType() == Category.CategoryType.Transfer)) {
        // keep color black
            holder.txtBudget.setTextColor(-1979711488);
        } else {
            if (budgetTotal <= searchArrayList.get(position).getBudget()) {
                holder.txtBudget.setTextColor(Color.rgb(0, 255, 0));
            } else {
                holder.txtBudget.setTextColor(Color.rgb(255, 0, 0));
            }
        }
        holder.txtBudget.setText(String.valueOf(budgetTotal) + " of " + String.valueOf(searchArrayList.get(position).getBudget()));

        Category c = CategoryListActivity.getCategoryByID(searchArrayList.get(position).getId());
        c.setBudgetTotal(budgetTotal);

        return convertView;
    }

    static class ViewHolder {
        TextView txtName;
        ProgressBar pBar;
        TextView txtBudget;
    }
}
