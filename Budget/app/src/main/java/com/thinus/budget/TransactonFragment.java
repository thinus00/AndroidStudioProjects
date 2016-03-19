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
import android.widget.TextView;

/**
 * Created by thinus on 2016/03/19.
 */
public class TransactonFragment extends Fragment {

    private CustomBaseAdapterTransactionList baseTransactionListAdapter;

    public CustomBaseAdapterTransactionList getListAdapter () { return baseTransactionListAdapter; }

    public TransactonFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_transaction, container, false);
        final ListView lv1 = (ListView) rootView.findViewById(R.id.listViewTransactions);
        baseTransactionListAdapter = new CustomBaseAdapterTransactionList(getActivity(), MainActivity.transactionItems);
        lv1.setAdapter(baseTransactionListAdapter);

        baseTransactionListAdapter.getFilter().filter("month", new Filter.FilterListener() {
            public void onFilterComplete(int count) {
                MainActivity.transactionFilteredCount = count;
                MainActivity.mSectionsPagerAdapter.notifyDataSetChanged();
            }
        });


        lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Object o = lv1.getItemAtPosition(position);
                Transaction fullObject = (Transaction) o;
                Intent intent = new Intent(getActivity(), TransactionAddActivity.class);
                intent.putExtra("mode", "edit");
                intent.putExtra("id", fullObject.getId());
                startActivity(intent);
            }
        });

        return rootView;
    }
}
