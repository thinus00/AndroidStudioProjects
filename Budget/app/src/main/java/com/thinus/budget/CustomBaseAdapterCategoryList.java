package com.thinus.budget;

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

public class CustomBaseAdapterCategoryList extends BaseAdapter implements Filterable {
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
        Category c = searchArrayList.get(position);
        holder.txtName.setText(c.getName());
        holder.pBar.setMax(100);
        double budgetTotal = c.getBudgetTotal();

//        if (budgetTotal != 0.0) {
//            budgetTotal = Double.valueOf(String.format("%.2f", budgetTotal));
//        }
        holder.pBar.setProgress((int)Math.floor((budgetTotal/(c.getBudget()))*100));
        if ((c.getBudget() == 0 && budgetTotal == 0) || (c.getCatType() == Category.CategoryType.Transfer)) {
        // keep color black
            holder.txtBudget.setTextColor(-1979711488);
        } else {
            if (c.getCatType() == Category.CategoryType.Income) {
                if (budgetTotal >= c.getBudget()) {
                    holder.txtBudget.setTextColor(Color.rgb(0, 255, 0));
                } else {
                    holder.txtBudget.setTextColor(Color.rgb(255, 0, 0));
                }
            } else {
                if (budgetTotal <= c.getBudget()) {
                    holder.txtBudget.setTextColor(Color.rgb(0, 255, 0));
                } else {
                    holder.txtBudget.setTextColor(Color.rgb(255, 0, 0));
                }
            }
        }
        if (MainActivity.ShowRemaining)
            holder.txtBudget.setText(String.format("%.2f", c.getBudget()-budgetTotal));
        else
            holder.txtBudget.setText(String.format("%.2f", budgetTotal) + " of " + String.format("%.2f", c.getBudget()));

        return convertView;
    }

    static class ViewHolder {
        TextView txtName;
        ProgressBar pBar;
        TextView txtBudget;
    }

    @Override
    public Filter getFilter() {

        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                searchArrayList = (ArrayList<Category>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();
                ArrayList<Category> FilteredArrayNames = new ArrayList<Category>();

                // perform your search here using the searchConstraint String.

                constraint = constraint.toString().toLowerCase();
                for (int i = 0; i < MainActivity.categoryItems.size(); i++) {
                    Category dataNames = MainActivity.categoryItems.get(i);

                    boolean all = constraint.equals("all");
                    boolean budgets = constraint.equals("budgets") && (dataNames.getBudget() != 0);
                    boolean budgetsnotspent = constraint.equals("budgetsnotspent") && (dataNames.getBudget() != 0) && (dataNames.getBudget() > dataNames.getBudgetTotal());
                    boolean actuals = constraint.equals("actuals") && (dataNames.getBudgetTotal() != 0);

                    if ((all) ||
                        (budgets) ||
                        (budgetsnotspent) ||
                        (actuals)) {

                        FilteredArrayNames.add(dataNames);
                    }
                }
                results.count = FilteredArrayNames.size();
                results.values = FilteredArrayNames;

                return results;
            }
        };

        return filter;
    }

}
