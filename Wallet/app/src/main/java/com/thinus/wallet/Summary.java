package com.thinus.wallet;

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
        //recurring = Double.valueOf(String.format("%.2f", recurring));
        //daytoday = Double.valueOf(String.format("%.2f", daytoday));
        //income = Double.valueOf(String.format("%.2f", income));
        //transfer = Double.valueOf(String.format("%.2f", transfer));
        //total = Double.valueOf(String.format("%.2f", total));
        //recurringBudget = Double.valueOf(String.format("%.2f", recurringBudget));
        //daytodayBudget = Double.valueOf(String.format("%.2f", daytodayBudget));
        //incomeBudget = Double.valueOf(String.format("%.2f", incomeBudget));
        //transferBudget = Double.valueOf(String.format("%.2f", transferBudget));

        totalBudget = incomeBudget - recurringBudget - daytodayBudget;
        //totalBudget = Double.valueOf(String.format("%.2f", totalBudget));

        TextView textView_recurringvalue = (TextView)findViewById(R.id.textView_recurringvalue);
        textView_recurringvalue.setText(String.format("%.2f", recurring));
        TextView textView_recurringbudget = (TextView)findViewById(R.id.textView_recurringbudget);
        textView_recurringbudget.setText(String.format("%.2f", recurringBudget) + " (" + String.format("%.2f", recurringBudget-recurring) + ")");

        TextView textView_daytodayvalue = (TextView)findViewById(R.id.textView_daytodayvalue);
        textView_daytodayvalue.setText(String.format("%.2f", daytoday));
        TextView textView_daytodaybudget = (TextView)findViewById(R.id.textView_daytodaybudget);
        textView_daytodaybudget.setText(String.format("%.2f", daytodayBudget) + " (" + String.format("%.2f", daytodayBudget-daytoday) + ")");

        TextView textView_incomevalue = (TextView)findViewById(R.id.textView_incomevalue);
        textView_incomevalue.setText(String.format("%.2f", income));
        TextView textView_incomebudget = (TextView)findViewById(R.id.textView_incomebudget);
        textView_incomebudget.setText(String.format("%.2f", incomeBudget) + " (" + String.format("%.2f", income-incomeBudget) + ")");

        TextView textView_transfervalue = (TextView)findViewById(R.id.textView_transfervalue);
        textView_transfervalue.setText(String.format("%.2f", transfer));
        TextView textView_transferbudget = (TextView)findViewById(R.id.textView_transferbudget);
        textView_transferbudget.setText(String.format("%.2f", transferBudget) + " (" + String.format("%.2f", transferBudget-transfer) + ")");

        TextView textView_totalvalue = (TextView)findViewById(R.id.textView_totalvalue);
        textView_totalvalue.setText(String.format("%.2f", total));
        TextView textView_totalbudget = (TextView)findViewById(R.id.textView_totalbudget);
        textView_totalbudget.setText(String.format("%.2f", totalBudget) + " (" + String.format("%.2f", total - totalBudget) + ")");
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
