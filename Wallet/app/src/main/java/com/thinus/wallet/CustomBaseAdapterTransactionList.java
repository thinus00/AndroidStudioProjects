package com.thinus.wallet;

/**
 * Created by thinus on 2014/12/26.
 */


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

                // perform your search here using the searchConstraint String.

                constraint = constraint.toString().toLowerCase();
                for (int i = 0; i < TransactionListActivity.transactionItems.size(); i++) {
                    Transaction dataNames = TransactionListActivity.transactionItems.get(i);
                    if ((constraint.equals("all")) ||
                        ((constraint.equals("unlinked")) && (dataNames.getCategoryId() == 0)) ||
                        ((constraint.equals("month")) && (((dataNames.getDate().after(TransactionListActivity.startDate)) && (dataNames.getDate().before(TransactionListActivity.endDate))) || (dataNames.getDate().equals(TransactionListActivity.startDate)) || (dataNames.getDate().equals(TransactionListActivity.endDate)))) ||
                        ((constraint.equals("monthcategory")) && (dataNames.getCategoryId() == TransactionListActivity.catFilterID) && (dataNames.getDate().after(TransactionListActivity.startDate)) && (dataNames.getDate().before(TransactionListActivity.endDate)))){
                            FilteredArrayNames.add(dataNames);
                    }
                }
                if (!constraint.equals("monthcategory")) {
                    TransactionListActivity.transactionItems_month = FilteredArrayNames;
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
