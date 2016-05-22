package com.thinus.budget;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by thinus on 2016/03/19.
 */
public class SumaryFragment extends Fragment {

    double recurring = 0;
    double recurringdaytoday = 0;
    double daytoday = 0;
    double income = 0;
    double transfer = 0;
    double total = 0;
    double recurringBudget = 0;
    double recurringdaytodayBudget = 0;
    double daytodayBudget = 0;
    double incomeBudget = 0;
    double transferBudget = 0;
    double totalBudget = 0;
    double recurringDiff = 0;
    double recurringdaytodayDiff = 0;
    double daytodayDiff = 0;
    double incomeDiff = 0;
    double transferDiff = 0;
    double totalDiff = 0;
    double recurringRemain = 0;
    double recurringdaytodayRemain = 0;
    double daytodayRemain = 0;
    double incomeRemain = 0;
    double transferRemain = 0;
    double totalRemain = 0;
    double recurringNotBudget = 0;
    double recurringdaytodayNotBudget = 0;
    double daytodayNotBudget = 0;
    double incomeNotBudget = 0;
    double transferNotBudget = 0;
    double totalNotBudget = 0;

    View rootView;

    public SumaryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_summary, container, false);
        refreshValues();
        return rootView;
    }

    public void refreshValues() {
        recurring = 0;
        recurringdaytoday = 0;
        daytoday = 0;
        income = 0;
        transfer = 0;
        total = 0;
        recurringBudget = 0;
        recurringdaytodayBudget = 0;
        daytodayBudget = 0;
        incomeBudget = 0;
        transferBudget = 0;
        totalBudget = 0;
        recurringDiff = 0;
        recurringdaytodayDiff = 0;
        daytodayDiff = 0;
        incomeDiff = 0;
        transferDiff = 0;
        totalDiff = 0;
        recurringRemain = 0;
        recurringdaytodayRemain = 0;
        daytodayRemain = 0;
        incomeRemain = 0;
        transferRemain = 0;
        totalRemain = 0;
        recurringNotBudget = 0;
        recurringdaytodayNotBudget = 0;
        daytodayNotBudget = 0;
        incomeNotBudget = 0;
        transferNotBudget = 0;
        totalNotBudget = 0;

        if (rootView == null)
            return;

        CategoryFragment.calcBugetTotals();

        for (Category c : MainActivity.categoryItems) {
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
            if (c.getCatType() == Category.CategoryType.RecurringDayToDay) {
                recurringdaytodayBudget = recurringdaytodayBudget + c.getBudget();
                recurringdaytoday = recurringdaytoday + c.getBudgetTotal();
                if (c.getBudget() > 0) {
                    double tmp = c.getBudget() - c.getBudgetTotal();
                    if (tmp > 0) {
                        recurringdaytodayRemain = recurringdaytodayRemain + (tmp);
                    }
                } else {
                    recurringdaytodayNotBudget = recurringdaytodayNotBudget + c.getBudgetTotal();
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

        total = income - recurring - recurringdaytoday - daytoday;
        totalBudget = incomeBudget - recurringBudget - recurringdaytodayBudget - daytodayBudget;
        recurringDiff = recurringBudget - recurring;
        recurringdaytodayDiff = recurringdaytodayBudget - recurringdaytoday;
        daytodayDiff = daytodayBudget - daytoday;
        incomeDiff = income - incomeBudget;
        transferDiff = transferBudget - transfer;
        totalDiff = total - totalBudget;
        totalRemain = recurringRemain + recurringdaytodayRemain + daytodayRemain + incomeRemain;
        totalNotBudget = recurringNotBudget + recurringdaytodayNotBudget + daytodayNotBudget + incomeNotBudget;

        TextView textView_recurring_actual = (TextView) rootView.findViewById(R.id.textView_recurring_actual);
        textView_recurring_actual.setText(String.format("%.2f", recurring));
        TextView textView_recurring_budget = (TextView) rootView.findViewById(R.id.textView_recurring_budget);
        textView_recurring_budget.setText(String.format("%.2f", recurringBudget));
        TextView textView_recurring_diff = (TextView) rootView.findViewById(R.id.textView_recurring_diff);
        textView_recurring_diff.setText(String.format("%.2f", recurringDiff));
        if ((recurringDiff) < 0)
            textView_recurring_diff.setTextColor(0xffff0000);
        else
            textView_recurring_diff.setTextColor(0xff00fa00);

        TextView textView_recurringdaytoday_actual = (TextView) rootView.findViewById(R.id.textView_recurringdaytoday_actual);
        textView_recurringdaytoday_actual.setText(String.format("%.2f", recurringdaytoday));
        TextView textView_recurringdaytoday_budget = (TextView) rootView.findViewById(R.id.textView_recurringdaytoday_budget);
        textView_recurringdaytoday_budget.setText(String.format("%.2f", recurringdaytodayBudget));
        TextView textView_recurringdaytoday_diff = (TextView) rootView.findViewById(R.id.textView_recurringdaytoday_diff);
        textView_recurringdaytoday_diff.setText(String.format("%.2f", recurringdaytodayDiff));
        if ((recurringdaytodayDiff) < 0)
            textView_recurringdaytoday_diff.setTextColor(0xffff0000);
        else
            textView_recurringdaytoday_diff.setTextColor(0xff00fa00);

        TextView textView_daytoday_actual = (TextView) rootView.findViewById(R.id.textView_daytoday_actual);
        textView_daytoday_actual.setText(String.format("%.2f", daytoday));
        TextView textView_daytoday_budget = (TextView) rootView.findViewById(R.id.textView_daytoday_budget);
        textView_daytoday_budget.setText(String.format("%.2f", daytodayBudget));
        TextView textView_daytoday_diff = (TextView) rootView.findViewById(R.id.textView_daytoday_diff);
        textView_daytoday_diff.setText(String.format("%.2f", daytodayDiff));
        if ((daytodayDiff) < 0)
            textView_daytoday_diff.setTextColor(0xffff0000);
        else
            textView_daytoday_diff.setTextColor(0xff00fa00);

        TextView textView_income_actual = (TextView) rootView.findViewById(R.id.textView_income_actual);
        textView_income_actual.setText(String.format("%.2f", income));
        TextView textView_income_budget = (TextView) rootView.findViewById(R.id.textView_income_budget);
        textView_income_budget.setText(String.format("%.2f", incomeBudget));
        TextView textView_income_diff = (TextView) rootView.findViewById(R.id.textView_income_diff);
        textView_income_diff.setText(String.format("%.2f", incomeDiff));
        if ((incomeDiff) < 0)
            textView_income_diff.setTextColor(0xffff0000);
        else
            textView_income_diff.setTextColor(0xff00fa00);

        TextView textView_transfer_actual = (TextView) rootView.findViewById(R.id.textView_transfer_actual);
        textView_transfer_actual.setText(String.format("%.2f", transfer));
        TextView textView_transfer_budget = (TextView) rootView.findViewById(R.id.textView_transfer_budget);
        textView_transfer_budget.setText(String.format("%.2f", transferBudget));
        TextView textView_transfer_diff = (TextView) rootView.findViewById(R.id.textView_transfer_diff);
        textView_transfer_diff.setText(String.format("%.2f", transferDiff));

        TextView textView_total_actual = (TextView) rootView.findViewById(R.id.textView_total_actual);
        textView_total_actual.setText(String.format("%.2f", total));
        if (total < 0)
            textView_total_actual.setTextColor(0xffff0000);
        else
            textView_total_actual.setTextColor(0xff00fa00);

        TextView textView_total_budget = (TextView) rootView.findViewById(R.id.textView_total_budget);
        textView_total_budget.setText(String.format("%.2f", totalBudget));
        if (totalBudget < 0)
            textView_total_budget.setTextColor(0xffff0000);
        else
            textView_total_budget.setTextColor(0xff00fa00);
        TextView textView_total_diff = (TextView) rootView.findViewById(R.id.textView_total_diff);
        textView_total_diff.setText(String.format("%.2f", totalDiff));
        if (totalDiff < 0)
            textView_total_diff.setTextColor(0xffff0000);
        else
            textView_total_diff.setTextColor(0xff00fa00);

        TextView textView_temp = (TextView) rootView.findViewById(R.id.textView_daytoday_label);
        textView_temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onClicky(v);
            }
        });
        onClicky(textView_temp);

        textView_temp = (TextView) rootView.findViewById(R.id.textView_recurring_label);
        textView_temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onClicky(v);
            }
        });

        textView_temp = (TextView) rootView.findViewById(R.id.textView_recurringdaytoday_label);
        textView_temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onClicky(v);
            }
        });

        textView_temp = (TextView) rootView.findViewById(R.id.textView_income_label);
        textView_temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onClicky(v);
            }
        });

        textView_temp = (TextView) rootView.findViewById(R.id.textView_transfer_label);
        textView_temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onClicky(v);
            }
        });

        //return rootView;
    }

    public void onClicky(View v) {
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
            case R.id.textView_recurringdaytoday_label:
                type = "Recurring DtD";
                budget = recurringdaytodayBudget;
                actual = recurringdaytoday;
                diff = recurringdaytodayDiff;
                remain = recurringdaytodayRemain;
                notBudget = recurringdaytodayNotBudget;
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

        TextView typelabel = (TextView) rootView.findViewById(R.id.textView_specific_type1);
        typelabel.setText(type);
        TextView budgetlabel = (TextView) rootView.findViewById(R.id.textView_specific_budget_value);
        budgetlabel.setText(String.format("%.2f", budget));
        TextView actuallabel = (TextView) rootView.findViewById(R.id.textView_specific_actual_value);
        actuallabel.setText(String.format("%.2f", actual));
        TextView difflabel = (TextView) rootView.findViewById(R.id.textView_specific_diff_value);
        difflabel.setText(String.format("%.2f", diff));
        TextView remainlabel = (TextView) rootView.findViewById(R.id.textView_specific_remaining_value);
        remainlabel.setText(String.format("%.2f", remain));
        TextView notBudgetlabel = (TextView) rootView.findViewById(R.id.textView_specific_notbudget_value);
        notBudgetlabel.setText(String.format("%.2f", notBudget));
    }
}
