package com.thinus.budget;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by thinus on 2016/03/19.
 */
public class CategoryFragment extends Fragment {

    private CustomBaseAdapterCategoryList baseCategoryListAdapter;

    public CustomBaseAdapterCategoryList getListAdapter () { return baseCategoryListAdapter; }

    public CategoryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_category, container, false);
        final ListView lv1 = (ListView) rootView.findViewById(R.id.listViewCategories);
        baseCategoryListAdapter = new CustomBaseAdapterCategoryList(getActivity(), MainActivity.categoryItems);
        lv1.setAdapter(baseCategoryListAdapter);

        lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Object o = lv1.getItemAtPosition(position);
                Category fullObject = (Category) o;
                MainActivity.catFilterID = fullObject.getId();
                TransactonFragment page = (TransactonFragment) getActivity().getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.container + ":" + 1);
                if (page != null) {
                    if (page.getListAdapter() != null) {
                        page.getListAdapter().getFilter().filter("monthCategory", new Filter.FilterListener() {
                            public void onFilterComplete(int count) {
                                MainActivity.transactionFilteredCount = count;
                                MainActivity.mSectionsPagerAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
                MainActivity.mViewPager.setCurrentItem(1);
            }
        });
        lv1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> a, View v, int position, long id) {
                Object o = lv1.getItemAtPosition(position);
                Category fullObject = (Category) o;
                Intent intent = new Intent(getActivity(), CategoryAddActivity.class);
                intent.putExtra("mode", "edit");
                intent.putExtra("id", fullObject.getId());
                startActivity(intent);
                return true;
            }
        });

        calcBugetTotals();
        //calcSavedTotals();

        return rootView;
    }

    public static void calcBugetTotals() {
        if ((MainActivity.categoryItems != null) && (MainActivity.transactionItems_month != null)) {
            for (int i = 0; i < MainActivity.categoryItems.size(); i++) {
                Category c = MainActivity.categoryItems.get(i);

                double budgetTotal = 0;
                for (int j = 0; j < MainActivity.transactionItems_month.size(); j++) {
                    Transaction t = MainActivity.transactionItems_month.get(j);
                    if (t.getCategoryId() == c.getId()) {
                        if ((c.getCatType() == Category.CategoryType.DayToDay || c.getCatType() == Category.CategoryType.RecurringDayToDay || c.getCatType() == Category.CategoryType.Recurring) && t.getIncomeExpense() == 2)
                            budgetTotal = budgetTotal + t.getAmount();
                        if ((c.getCatType() == Category.CategoryType.DayToDay || c.getCatType() == Category.CategoryType.RecurringDayToDay || c.getCatType() == Category.CategoryType.Recurring) && t.getIncomeExpense() == 1)
                            budgetTotal = budgetTotal - t.getAmount();
                        if ((c.getCatType() == Category.CategoryType.Income) && t.getIncomeExpense() == 2)
                            budgetTotal = budgetTotal - t.getAmount();
                        if ((c.getCatType() == Category.CategoryType.Income) && t.getIncomeExpense() == 1)
                            budgetTotal = budgetTotal + t.getAmount();
                        if ((c.getCatType() == Category.CategoryType.Transfer) && t.getIncomeExpense() == 2)
                            budgetTotal = budgetTotal + t.getAmount();
                        if ((c.getCatType() == Category.CategoryType.Transfer) && t.getIncomeExpense() == 1)
                            budgetTotal = budgetTotal - t.getAmount();
                    }
                }
                c.setBudgetTotal(budgetTotal);
            }
        }
        //calcSavedTotals();
    }

    public static void calcSavedTotals() {
        if ((MainActivity.categoryItems != null) && (MainActivity.transactionItems != null)) {
            for (int i = 0; i < MainActivity.categoryItems.size(); i++) {
                Category c = MainActivity.categoryItems.get(i);
                if (c.getName().startsWith("Save-")) {
                    double savedTotal = 0;
                    for (int j = 0; j < MainActivity.transactionItems.size(); j++) {
                        Transaction t = MainActivity.transactionItems.get(j);
                        if ((t != null ) && (t.getDeleted() == 0)) {


                            for (String smsDesc : c.getSMSDescription()) {
                                if (t.getDescription().toLowerCase().contains(smsDesc.toLowerCase())) {
                                    if (t.getIncomeExpense() == 1)
                                        savedTotal = savedTotal - t.getAmount();
                                    if (t.getIncomeExpense() == 2)
                                        savedTotal = savedTotal + t.getAmount();
                                }
                            }
                        }
                    }
                    c.setSavedTotal(savedTotal);
                }
            }
        }
    }
}
