package com.thinus.budget;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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
    public static boolean returnToPossibles = false;
    private static CustomBaseAdapterTransactionList baseTransactionListAdapter;

    public static CustomBaseAdapterTransactionList getListAdapter () { return baseTransactionListAdapter; }

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
                if (fullObject.getType() == 1) {
                    intent.putExtra("mode", "possible");
                } else {
                    if (fullObject.getType() == 2) {
                        intent.putExtra("mode", "linkto");
                    } else {
                        intent.putExtra("mode", "edit");
                    }
                }
                intent.putExtra("id", fullObject.getId());
                startActivity(intent);
            }
        });

        lv1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> a, View v, int position, long id) {
                Object o = lv1.getItemAtPosition(position);
                Transaction fullObject = (Transaction) o;

                if (fullObject.getType() == 1) {
                    MainActivity.transactionItemsPossibleLinkTo.clear();
                    MainActivity.checkForDuplicateTrans(fullObject);

                    if (getListAdapter() != null) {
                        getListAdapter().getFilter().filter("linkto", new Filter.FilterListener() {
                            public void onFilterComplete(int count) {
                                MainActivity.transactionFilteredCount = count;
                                MainActivity.mSectionsPagerAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }

                return true;
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        if (returnToPossibles) {
            MainActivity.transactionItemsPossibleLink.clear();
            Database.LoadTransactionsFromDB(1);
            returnToPossibles = false;
            if (getListAdapter() != null) {
                getListAdapter().getFilter().filter("possibles", new Filter.FilterListener() {
                    public void onFilterComplete(int count) {
                        MainActivity.transactionFilteredCount = count;
                        MainActivity.mSectionsPagerAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
        super.onResume();
    }
}
