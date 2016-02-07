package com.thinus.wallet;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class CategoryListActivity extends ActionBarActivity {

    public static ArrayList<Category> categoryItems;

    public static boolean ShowRemaining = false;

    private static ArrayList<String> categoryItemsspinner;
    public static ArrayList<String> GetCategorycategoryItemsSpinner() {
        if (categoryItemsspinner==null) {
            categoryItemsspinner = new ArrayList<String>();
        }
        categoryItemsspinner.clear();

        for (int i = 0; i < categoryItems.size(); i++)
        {

            categoryItemsspinner.add(((Category)categoryItems.get(i)).getName());
        }

        return categoryItemsspinner;
    }

    public static int GetCategoryIndexForSpinnerFromId(int id) {
        for (int i = 0; i < categoryItems.size(); i++)
        {
            if (((Category)categoryItems.get(i)).getId() == id)
                return i;
        }

        return 0;
    }

    private CustomBaseAdapterCategoryList baseCategoryListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);

        final ListView lv1 = (ListView) findViewById(R.id.listViewCategories);
        baseCategoryListAdapter = new CustomBaseAdapterCategoryList(this, categoryItems);
        lv1.setAdapter(baseCategoryListAdapter);

        lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Object o = lv1.getItemAtPosition(position);
                Category fullObject = (Category)o;
                Intent intent = new Intent(CategoryListActivity.this, TransactionListActivity.class);
                intent.putExtra("mode", "category");
                intent.putExtra("id", fullObject.getId());
                startActivity(intent);
            }
        });
        lv1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> a, View v, int position, long id) {
                Object o = lv1.getItemAtPosition(position);
                Category fullObject = (Category)o;
                Intent intent = new Intent(CategoryListActivity.this, CategoryAddActivity.class);
                intent.putExtra("mode", "edit");
                intent.putExtra("id", fullObject.getId());
                startActivity(intent);
                return true;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_category_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.add_category) {
            Intent intent = new Intent(CategoryListActivity.this, CategoryAddActivity.class);
            intent.putExtra("mode", "add");
            intent.putExtra("id", 0);
            this.startActivity(intent);
            return true;
        }

        if (id == R.id.show_only_actuals) {
            baseCategoryListAdapter.getFilter().filter("actuals", null);
            return true;
        }

        if (id == R.id.show_only_budgets) {
            baseCategoryListAdapter.getFilter().filter("budgets", null);
            return true;
        }

        if (id == R.id.show_all) {
            baseCategoryListAdapter.getFilter().filter("all", null);
            return true;
        }

        if (id == R.id.show_remaining) {
            ShowRemaining = !ShowRemaining;
            refreshList();
            return true;
        }

        if (id == R.id.refresh) {
            refreshList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    public static String getCategoryName(int Id){
        for (Category s : categoryItems){

            if (s.getId() == (Id))
                return s.getName();
        }
        return "Category not found";
    }

    public static String getCategoryNameType(int Id){
        for (Category s : categoryItems){
            if (s.getId() == (Id))
                return s.getCatType() + " -> " + s.getName();
        }
        return "Category not found";
    }

    public static int getCategoryIDName(String name){
        for (Category s : categoryItems){
            if (s.getName().equals(name))
                return s.getId();
        }
        return 0;
    }

    public static Category getCategoryByID(int id){
        for (Category s : categoryItems){
            if (s.getId() == id)
                return s;
        }
        return null;
    }

    public static boolean deleteCategoryByID(int id){
        for (Category c : categoryItems){
            if (c.getId() == id) {
                c.Delete();
                categoryItems.remove(c);
                return true;
            }
        }
        return false;
    }

    public static void AddTestArray() {
        Category sr1 = null;
        sr1 = new Category(0, "Unknown", Category.CategoryType.Transfer, 0, "");
        categoryItems.add(sr1);
        sr1.Save();

        sr1 = new Category(1, "Fuel", Category.CategoryType.DayToDay, 3000, "");
        categoryItems.add(sr1);
        sr1.Save();

        sr1 = new Category(2, "Car", Category.CategoryType.Recurring, 3652, "");
        categoryItems.add(sr1);
        sr1.Save();

        sr1 = new Category(3, "Fast Food", Category.CategoryType.DayToDay, 500, "");
        categoryItems.add(sr1);
        sr1.Save();

        sr1 = new Category(4, "Salary", Category.CategoryType.Income, 10000, "");
        categoryItems.add(sr1);
        sr1.Save();
    }

    public static void LoadCategoriesFromDB() {
        //'" + _id + "','" + _name + "','" + getCatNameFromType(_catType) + "','" + _budget + "'
        Cursor resultSet = TransactionListActivity.db.rawQuery("Select * from [category] order by categorytype, name",null);
        resultSet.moveToFirst();

        if (resultSet.getCount() == 0) {
            Category sr0 = new Category(-1, "unknown", Category.CategoryType.DayToDay, 0, "");
            categoryItems.add(sr0);
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
            categoryItems.add(sr1);
            resultSet.moveToNext();
        }
    }

    public void refreshList()
    {
        Toast.makeText(getApplicationContext(), "Refreshing list", Toast.LENGTH_LONG).show();
        baseCategoryListAdapter.notifyDataSetChanged();
    }

    private void refreshDates() {
        SimpleDateFormat dateFormat0 = new SimpleDateFormat("MMM");
        try {
            if (TransactionListActivity.day > TransactionListActivity.monthstartonday) {
                if (TransactionListActivity.month < 12) {
                    TransactionListActivity.startDate = TransactionListActivity.dateFormat.parse(TransactionListActivity.year + "-" + TransactionListActivity.month + "-" + TransactionListActivity.monthstartonday);
                    TransactionListActivity.endDate = TransactionListActivity.dateFormat.parse(TransactionListActivity.year + "-" + (TransactionListActivity.month+1) + "-" + (TransactionListActivity.monthstartonday-1));
                } else {
                    TransactionListActivity.startDate = TransactionListActivity.dateFormat.parse(TransactionListActivity.year + "-" + TransactionListActivity.month + "-" + TransactionListActivity.monthstartonday);
                    TransactionListActivity.endDate = TransactionListActivity.dateFormat.parse(TransactionListActivity.year+1 + "-" + (1) + "-" + (TransactionListActivity.monthstartonday-1));
                }
            } else {
                if (TransactionListActivity.month > 1) {
                    TransactionListActivity.startDate = TransactionListActivity.dateFormat.parse(TransactionListActivity.year + "-" + (TransactionListActivity.month-1) + "-" + TransactionListActivity.monthstartonday);
                    TransactionListActivity.endDate = TransactionListActivity.dateFormat.parse(TransactionListActivity.year + "-" + (TransactionListActivity.month) + "-" + (TransactionListActivity.monthstartonday-1));
                } else {
                    TransactionListActivity.startDate = TransactionListActivity.dateFormat.parse((TransactionListActivity.year-1) + "-" + (12) + "-" + TransactionListActivity.monthstartonday);
                    TransactionListActivity.endDate = TransactionListActivity.dateFormat.parse((TransactionListActivity.year) + "-" + (TransactionListActivity.month) + "-" + (TransactionListActivity.monthstartonday-1));
                }
            }
            TransactionListActivity.monthName = dateFormat0.format(TransactionListActivity.endDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


}
