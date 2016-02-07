package com.thinus.wallet;

import android.content.res.ColorStateList;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;


public class Summary extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

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
        total = income-recurring-daytoday;
        for (Category s : CategoryListActivity.categoryItems) {
            if (s.getCatType() == Category.CategoryType.Recurring)
                recurringBudget = recurringBudget + s.getBudget();
            if (s.getCatType() == Category.CategoryType.DayToDay)
                daytodayBudget = daytodayBudget + s.getBudget();
            if (s.getCatType() == Category.CategoryType.Income)
                incomeBudget = incomeBudget + s.getBudget();
            if (s.getCatType() == Category.CategoryType.Transfer)
                transferBudget = transferBudget + s.getBudget();

        }

        totalBudget = incomeBudget - recurringBudget - daytodayBudget;

        TextView textView_recurring_actual = (TextView)findViewById(R.id.textView_recurring_actual);
        textView_recurring_actual.setText(String.format("%.2f", recurring));
        TextView textView_recurring_budget = (TextView)findViewById(R.id.textView_recurring_budget);
        textView_recurring_budget.setText(String.format("%.2f", recurringBudget));
        TextView textView_recurring_diff = (TextView)findViewById(R.id.textView_recurring_diff);
        textView_recurring_diff.setText(String.format("%.2f", recurringBudget-recurring));
        if ((recurringBudget-recurring)<0)
            textView_recurring_diff.setTextColor(0xffff0000);
        else
            textView_recurring_diff.setTextColor(0xff00fa00);

        TextView textView_daytoday_actual = (TextView)findViewById(R.id.textView_daytoday_actual);
        textView_daytoday_actual.setText(String.format("%.2f", daytoday));
        TextView textView_daytoday_budget = (TextView)findViewById(R.id.textView_daytoday_budget);
        textView_daytoday_budget.setText(String.format("%.2f", daytodayBudget));
        TextView textView_daytoday_diff = (TextView)findViewById(R.id.textView_daytoday_diff);
        textView_daytoday_diff.setText(String.format("%.2f", daytodayBudget-daytoday));
        if ((daytodayBudget-daytoday)<0)
            textView_daytoday_diff.setTextColor(0xffff0000);
        else
            textView_daytoday_diff.setTextColor(0xff00fa00);

        TextView textView_income_actual = (TextView)findViewById(R.id.textView_income_actual);
        textView_income_actual.setText(String.format("%.2f", income));
        TextView textView_income_budget = (TextView)findViewById(R.id.textView_income_budget);
        textView_income_budget.setText(String.format("%.2f", incomeBudget));
        TextView textView_income_diff = (TextView)findViewById(R.id.textView_income_diff);
        textView_income_diff.setText(String.format("%.2f", income-incomeBudget));
        if ((income-incomeBudget)<0)
            textView_income_diff.setTextColor(0xffff0000);
        else
            textView_income_diff.setTextColor(0xff00fa00);

        TextView textView_transfer_actual = (TextView)findViewById(R.id.textView_transfer_actual);
        textView_transfer_actual.setText(String.format("%.2f", transfer));
        TextView textView_transfer_budget = (TextView)findViewById(R.id.textView_transfer_budget);
        textView_transfer_budget.setText(String.format("%.2f", transferBudget));
        TextView textView_transfer_diff = (TextView)findViewById(R.id.textView_transfer_diff);
        textView_transfer_diff.setText(String.format("%.2f", transferBudget-transfer));

        TextView textView_total_actual = (TextView)findViewById(R.id.textView_total_actual);
        textView_total_actual.setText(String.format("%.2f", total));
        TextView textView_total_budget = (TextView)findViewById(R.id.textView_total_budget);
        textView_total_budget.setText(String.format("%.2f", totalBudget));
        if (totalBudget<0)
            textView_total_budget.setTextColor(0xffff0000);
        else
            textView_total_budget.setTextColor(0xff00fa00);
        TextView textView_total_diff = (TextView)findViewById(R.id.textView_total_diff);
        textView_total_diff.setText(String.format("%.2f", total-totalBudget));
        if (total-totalBudget<0)
            textView_total_diff.setTextColor(0xffff0000);
        else
            textView_total_diff.setTextColor(0xff00fa00);
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
}
