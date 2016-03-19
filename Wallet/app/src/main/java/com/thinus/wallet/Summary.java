package com.thinus.wallet;

import android.content.res.ColorStateList;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


public class Summary extends ActionBarActivity {

    double recurring = 0;
    double daytoday = 0;
    double income = 0;
    double transfer = 0;
    double total = 0;
    double recurringBudget = 0;
    double daytodayBudget = 0;
    double incomeBudget = 0;
    double transferBudget = 0;
    double totalBudget = 0;
    double recurringDiff = 0;
    double daytodayDiff = 0;
    double incomeDiff = 0;
    double transferDiff = 0;
    double totalDiff = 0;
    double recurringRemain = 0;
    double daytodayRemain = 0;
    double incomeRemain = 0;
    double transferRemain = 0;
    double totalRemain = 0;
    double recurringNotBudget = 0;
    double daytodayNotBudget = 0;
    double incomeNotBudget = 0;
    double transferNotBudget = 0;
    double totalNotBudget = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
/*
        for (Transaction s : TransactionListActivity.transactionItems_month){
            if (CategoryListActivity.getCategoryByID(s.getCategoryId()).getCatType() == Category.CategoryType.Recurring)
                recurring = recurring + s.getAmount();
            if (CategoryListActivity.getCategoryByID(s.getCategoryId()).getCatType() == Category.CategoryType.DayToDay)
                daytoday = daytoday + s.getAmount();
            if (CategoryListActivity.getCategoryByID(s.getCategoryId()).getCatType() == Category.CategoryType.Income)
                income = income + s.getAmount();
            if (CategoryListActivity.getCategoryByID(s.getCategoryId()).getCatType() == Category.CategoryType.Transfer)
                transfer = transfer + s.getAmount();
        }
*/
        for (Category c : CategoryListActivity.categoryItems) {
            if (c.getCatType() == Category.CategoryType.Recurring) {
                recurringBudget = recurringBudget + c.getBudget();
                recurring = recurring + c.getBudgetTotal();
                if (c.getBudget() > 0) {
                    double tmp = c.getBudget() - c.getBudgetTotal();
                    if (tmp > 0) {
                        recurringRemain = recurringRemain + (tmp);
                    }
                } else {
                    recurringNotBudget = recurringNotBudget + c.getBudgetTotal();
                }
            }
            if (c.getCatType() == Category.CategoryType.DayToDay) {
                daytodayBudget = daytodayBudget + c.getBudget();
                daytoday = daytoday + c.getBudgetTotal();
                if (c.getBudget() > 0) {
                    double tmp = c.getBudget() - c.getBudgetTotal();
                    if (tmp > 0) {
                        daytodayRemain = daytodayRemain + (tmp);
                    }
                } else {
                    daytodayNotBudget = daytodayNotBudget + c.getBudgetTotal();
                }
            }
            if (c.getCatType() == Category.CategoryType.Income) {
                incomeBudget = incomeBudget + c.getBudget();
                income = income + c.getBudgetTotal();
                if (c.getBudget() > 0) {
                    double tmp = c.getBudget() - c.getBudgetTotal();
                    if (tmp > 0) {
                        incomeRemain = incomeRemain + (tmp);
                    }
                } else {
                    incomeNotBudget = incomeNotBudget + c.getBudgetTotal();
                }
            }
            if (c.getCatType() == Category.CategoryType.Transfer) {
                transferBudget = transferBudget + c.getBudget();
                transfer = transfer + c.getBudgetTotal();
                if (c.getBudget() > 0) {
                    double tmp = c.getBudget() - c.getBudgetTotal();
                    if (tmp > 0) {
                        transferRemain = transferRemain + (tmp);
                    }
                } else {
                    transferNotBudget = transferNotBudget + c.getBudgetTotal();
                }
            }

        }

        total = income-recurring-daytoday;
        totalBudget = incomeBudget - recurringBudget - daytodayBudget;
        recurringDiff = recurringBudget-recurring;
        daytodayDiff = daytodayBudget-daytoday;
        incomeDiff = income-incomeBudget;
        transferDiff = transferBudget-transfer;
        totalDiff = total-totalBudget;
        totalRemain = recurringRemain + daytodayRemain + incomeRemain ;
        totalNotBudget = recurringNotBudget + daytodayNotBudget + incomeNotBudget;

        TextView textView_recurring_actual = (TextView)findViewById(R.id.textView_recurring_actual);
        textView_recurring_actual.setText(String.format("%.2f", recurring));
        TextView textView_recurring_budget = (TextView)findViewById(R.id.textView_recurring_budget);
        textView_recurring_budget.setText(String.format("%.2f", recurringBudget));
        TextView textView_recurring_diff = (TextView)findViewById(R.id.textView_recurring_diff);
        textView_recurring_diff.setText(String.format("%.2f", recurringDiff));
        if ((recurringDiff)<0)
            textView_recurring_diff.setTextColor(0xffff0000);
        else
            textView_recurring_diff.setTextColor(0xff00fa00);

        TextView textView_daytoday_actual = (TextView)findViewById(R.id.textView_daytoday_actual);
        textView_daytoday_actual.setText(String.format("%.2f", daytoday));
        TextView textView_daytoday_budget = (TextView)findViewById(R.id.textView_daytoday_budget);
        textView_daytoday_budget.setText(String.format("%.2f", daytodayBudget));
        TextView textView_daytoday_diff = (TextView)findViewById(R.id.textView_daytoday_diff);
        textView_daytoday_diff.setText(String.format("%.2f", daytodayDiff));
        if ((daytodayDiff)<0)
            textView_daytoday_diff.setTextColor(0xffff0000);
        else
            textView_daytoday_diff.setTextColor(0xff00fa00);

        TextView textView_income_actual = (TextView)findViewById(R.id.textView_income_actual);
        textView_income_actual.setText(String.format("%.2f", income));
        TextView textView_income_budget = (TextView)findViewById(R.id.textView_income_budget);
        textView_income_budget.setText(String.format("%.2f", incomeBudget));
        TextView textView_income_diff = (TextView)findViewById(R.id.textView_income_diff);
        textView_income_diff.setText(String.format("%.2f", incomeDiff));
        if ((incomeDiff)<0)
            textView_income_diff.setTextColor(0xffff0000);
        else
            textView_income_diff.setTextColor(0xff00fa00);

        TextView textView_transfer_actual = (TextView)findViewById(R.id.textView_transfer_actual);
        textView_transfer_actual.setText(String.format("%.2f", transfer));
        TextView textView_transfer_budget = (TextView)findViewById(R.id.textView_transfer_budget);
        textView_transfer_budget.setText(String.format("%.2f", transferBudget));
        TextView textView_transfer_diff = (TextView)findViewById(R.id.textView_transfer_diff);
        textView_transfer_diff.setText(String.format("%.2f", transferDiff));

        TextView textView_total_actual = (TextView)findViewById(R.id.textView_total_actual);
        textView_total_actual.setText(String.format("%.2f", total));
        if (total<0)
            textView_total_actual.setTextColor(0xffff0000);
        else
            textView_total_actual.setTextColor(0xff00fa00);

        TextView textView_total_budget = (TextView)findViewById(R.id.textView_total_budget);
        textView_total_budget.setText(String.format("%.2f", totalBudget));
        if (totalBudget<0)
            textView_total_budget.setTextColor(0xffff0000);
        else
            textView_total_budget.setTextColor(0xff00fa00);
        TextView textView_total_diff = (TextView)findViewById(R.id.textView_total_diff);
        textView_total_diff.setText(String.format("%.2f", totalDiff));
        if (totalDiff<0)
            textView_total_diff.setTextColor(0xffff0000);
        else
            textView_total_diff.setTextColor(0xff00fa00);

        TextView textView_temp = (TextView)findViewById(R.id.textView_daytoday_label);
        onClick(textView_temp);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_summary, menu);
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

    public void onClick(View v) {
        String type = "";
        double budget = 0;
        double actual = 0;
        double diff = 0;
        double remain = 0;
        double notBudget = 0;
        switch (v.getId()) {
            case R.id.textView_recurring_label:
                type = "Recurring";
                budget = recurringBudget;
                actual = recurring;
                diff = recurringDiff;
                remain = recurringRemain;
                notBudget = recurringNotBudget;
                break;
            case R.id.textView_daytoday_label:
                type = "Day-to-Day";
                budget = daytodayBudget;
                actual = daytoday;
                diff = daytodayDiff;
                remain = daytodayRemain;
                notBudget = daytodayNotBudget;
                break;
            case R.id.textView_income_label:
                type = "Income";
                budget = incomeBudget;
                actual = income;
                diff = incomeDiff;
                remain = incomeRemain;
                notBudget = incomeNotBudget;
                break;
            case R.id.textView_transfer_label:
                type = "Transfers";
                budget = transferBudget;
                actual = transfer;
                diff = transferDiff;
                remain = transferRemain;
                notBudget = transferNotBudget;
                break;
            case R.id.textView_total_label:
                type = "Total";
                budget = totalBudget;
                actual = total;
                diff = totalDiff;
                remain = totalRemain;
                notBudget = totalNotBudget;
                break;
        }

        TextView typelabel = (TextView) findViewById(R.id.textView_specific_type1);
        typelabel.setText(type);
        TextView budgetlabel = (TextView) findViewById(R.id.textView_specific_budget_value);
        budgetlabel.setText(String.format("%.2f", budget));
        TextView actuallabel = (TextView) findViewById(R.id.textView_specific_actual_value);
        actuallabel.setText(String.format("%.2f", actual));
        TextView difflabel = (TextView) findViewById(R.id.textView_specific_diff_value);
        difflabel.setText(String.format("%.2f", diff));
        TextView remainlabel = (TextView) findViewById(R.id.textView_specific_remaining_value);
        remainlabel.setText(String.format("%.2f", remain));
        TextView notBudgetlabel = (TextView) findViewById(R.id.textView_specific_notbudget_value);
        notBudgetlabel.setText(String.format("%.2f", notBudget));


    }
}
