package com.thinus.budget;

/**
 * Created by thinus on 2014/12/26.
 */


import android.graphics.Color;
import android.util.Log;
import android.widget.BaseAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import android.content.Context;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.thinus.budget.MainActivity;
import com.thinus.budget.R;
import com.thinus.budget.Transaction;


public class CustomBaseAdapterTransactionList extends BaseAdapter implements Filterable {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static ArrayList<Transaction> searchArrayList;

    private LayoutInflater mInflater;

    public CustomBaseAdapterTransactionList(Context context, ArrayList<Transaction> results) {
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
            convertView = mInflater.inflate(R.layout.custom_list_item_view, null);
            holder = new ViewHolder();
            holder.txtCategory = (TextView) convertView.findViewById(R.id.category);
            holder.txtDescription = (TextView) convertView.findViewById(R.id.description);
            holder.txtAmount = (TextView) convertView.findViewById(R.id.amount);
            holder.txtDate = (TextView) convertView.findViewById(R.id.date);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //holder.txtCategory.setText(searchArrayList.get(position).getCategoryName());
        holder.txtCategory.setText(searchArrayList.get(position).getCategoryNameType());
        holder.txtDescription.setText(searchArrayList.get(position).getDescription());
        holder.txtAmount.setText("R" + String.valueOf(searchArrayList.get(position).getAmount()));
        holder.txtDate.setText(dateFormat.format(searchArrayList.get(position).getDate()));
        if (searchArrayList.get(position).getIncomeExpense() == 1)
            holder.txtAmount.setTextColor(Color.rgb(0, 255, 0));
        else
            holder.txtAmount.setTextColor(-1979711488);
        return convertView;
    }

    static class ViewHolder {
        TextView txtCategory;
        TextView txtDescription;
        TextView txtAmount;
        TextView txtDate;
    }

    @Override
    public Filter getFilter() {

        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                searchArrayList = (ArrayList<Transaction>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();
                ArrayList<Transaction> FilteredArrayNames = new ArrayList<Transaction>();
                MainActivity.transactionFilteredUnlinkedCount = 0;
                MainActivity.transactionUnlinkedCount = 0;
                // perform your search here using the searchConstraint String.

                constraint = constraint.toString().toLowerCase();
                for (int i = 0; i < MainActivity.transactionItems.size(); i++) {
                    Transaction dataNames = MainActivity.transactionItems.get(i);
                    if ((constraint.equals("all")) ||
                        ((constraint.equals("unlinked")) && (dataNames.getCategoryId() == 0)) ||
                        ((constraint.toString().startsWith("search")) && (
                                (dataNames.getReference().toLowerCase().contains(constraint.toString().substring(7).toLowerCase())) ||
                                (dataNames.getDescription().toLowerCase().contains(constraint.toString().substring(7).toLowerCase())) ||
                                (String.valueOf(dataNames.getAmount()).toLowerCase().contains(constraint.toString().substring(7).toLowerCase())) ||
                                (dataNames.getCategoryName().toLowerCase().contains(constraint.toString().substring(7).toLowerCase())))) ||
                        ((constraint.equals("month") || constraint.equals("duplicates")) && (((dataNames.getDate().after(MainActivity.startDate)) && (dataNames.getDate().before(MainActivity.endDate))) || (dataNames.getDate().equals(MainActivity.startDate)) || (dataNames.getDate().equals(MainActivity.endDate)))) ||
                        ((constraint.equals("monthcategory")) && (dataNames.getCategoryId() == MainActivity.catFilterID) && (((dataNames.getDate().after(MainActivity.startDate)) && (dataNames.getDate().before(MainActivity.endDate))) || (dataNames.getDate().equals(MainActivity.startDate)) || (dataNames.getDate().equals(MainActivity.endDate))))){
                            if (dataNames.getCategoryId() == 0)
                                MainActivity.transactionFilteredUnlinkedCount++;
                            FilteredArrayNames.add(dataNames);
                    }
                    if (dataNames.getCategoryId() == 0)
                        MainActivity.transactionUnlinkedCount++;
                }
                if (constraint.equals("duplicates")) {
                    for (int ii = 0; ii < FilteredArrayNames.size(); ii++) {
                        boolean foundDup = false;
                        Transaction dataNames2 = FilteredArrayNames.get(ii);
                        for (int iii = 0; iii < FilteredArrayNames.size(); iii++) {
                            Transaction dataNames3 = FilteredArrayNames.get(iii);
                            if ((dataNames2.getId() != dataNames3.getId()) && (dataNames2.getAmount() == dataNames3.getAmount())) {
                                foundDup = true;
                                break;
                            }
                        }
                        if (!foundDup) {
                            FilteredArrayNames.remove(dataNames2);
                            ii--;
                        }
                    }
                }
                if (!constraint.equals("monthcategory")) {
                    MainActivity.transactionItems_month = FilteredArrayNames;
                }
                results.count = FilteredArrayNames.size();
                results.values = FilteredArrayNames;

                //Log.e("VALUES", results.values.toString());

                return results;
            }
        };

        return filter;
    }

}
