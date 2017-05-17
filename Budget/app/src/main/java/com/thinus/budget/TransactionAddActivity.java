package com.thinus.budget;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.app.Dialog;
import android.widget.Filter;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class TransactionAddActivity extends ActionBarActivity {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public String mode;
    public int modeid;
    public int cID;
    private boolean possible = false;
    private boolean linkto = false;
    private Transaction trans = null;

    private boolean addCat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_add);

        addCat = false;
        mode = getIntent().getExtras().getString("mode");
        modeid = getIntent().getExtras().getInt("id");

        Spinner s = (Spinner) findViewById(R.id.spinner_category);
        ArrayAdapter<String> adapter = new ArrayAdapter<String> (this,android.R.layout.simple_spinner_item, MainActivity.GetCategorycategoryItemsSpinner());
        s.setAdapter(adapter);

        ArrayList<String> typeList = new ArrayList<String>();
        typeList.add("Unknown");
        typeList.add("Income");
        typeList.add("Expense");
        Spinner s2 = (Spinner) findViewById(R.id.spinner_income_expense);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String> (this,android.R.layout.simple_spinner_item, typeList);
        s2.setAdapter(adapter2);

        if (mode.equals("add")) {
            final Calendar calendar = Calendar.getInstance();
            int yy = calendar.get(Calendar.YEAR);
            int mm = calendar.get(Calendar.MONTH)+1;
            int dd = calendar.get(Calendar.DAY_OF_MONTH);
            EditText _editText_date = (EditText)findViewById(R.id.editText_date);
            _editText_date.setText(yy+"-"+mm+"-"+dd);
        } else{
            if (mode.equals("edit") || mode.equals("possible") || mode.equals("linkto")) {

                if (mode.equals("edit")) {
                    trans = MainActivity.getTransactionByID(modeid);
                } else {
                    if (mode.equals("linkto")) {
                        for (Transaction t : MainActivity.transactionItemsPossibleLinkTo) {
                            if (t.getId() == modeid) {
                                trans = t;
                                linkto = true;
                                break;
                            }
                        }
                    } else {
                        for (Transaction t : MainActivity.transactionItemsPossibleLink) {
                            if (t.getId() == modeid) {
                                trans = t;
                                possible = true;

                                break;
                            }
                        }
                    }
                }
                if (trans != null) {
                    EditText editText_amount = (EditText) findViewById(R.id.editText_amount);
                    editText_amount.setText(String.valueOf(trans.getAmount()));
                    //EditText editText_category = (EditText)findViewById(R.id.editText_category);
                    EditText editText_account = (EditText) findViewById(R.id.editText_account);
                    editText_account.setText(trans.getAccount());
                    EditText editText_description = (EditText) findViewById(R.id.editText_description);
                    editText_description.setText(trans.getDescription());
                    EditText editText_date = (EditText) findViewById(R.id.editText_date);
                    editText_date.setText(dateFormat.format(trans.getDate()));
                    EditText editText_reference = (EditText) findViewById(R.id.editText_reference);
                    editText_reference.setText(trans.getReference());
                    Spinner spn = (Spinner) findViewById(R.id.spinner_category);
                    spn.setSelection(MainActivity.GetCategoryIndexForSpinnerFromId(trans.getCategoryId()));
                    Spinner spn2 = (Spinner) findViewById(R.id.spinner_income_expense);
                    spn2.setSelection(trans.getIncomeExpense());
                }
            }
        }




        Button button = (Button)findViewById(R.id.button_save);
        if (linkto)
            button.setVisibility(View.INVISIBLE);
        else {
            button.setOnClickListener(
                    new Button.OnClickListener() {
                        public void onClick(View v) {
                            EditText editText_amount = (EditText) findViewById(R.id.editText_amount);
                            //EditText editText_category = (EditText)findViewById(R.id.editText_category);
                            EditText editText_account = (EditText) findViewById(R.id.editText_account);
                            EditText editText_description = (EditText) findViewById(R.id.editText_description);
                            EditText editText_date = (EditText) findViewById(R.id.editText_date);
                            EditText editText_reference = (EditText) findViewById(R.id.editText_reference);

                            Spinner s = (Spinner) findViewById(R.id.spinner_category);
                            cID = MainActivity.getCategoryIDName(s.getSelectedItem().toString());
                            Spinner s2 = (Spinner) findViewById(R.id.spinner_income_expense);
                            int tID = s2.getSelectedItemPosition();

                            int deleted = 0;
                            String sms = editText_reference.getText().toString();
                            String csv_prov = "";
                            String csv_hist = "";
                            Date date_created = Calendar.getInstance().getTime();
                            if (possible) {
                                for (Transaction t : MainActivity.transactionItemsPossibleLink) {
                                    if (t.getId() == modeid) {
                                        sms = t.getSms();
                                        csv_hist = t.getCSV_hist();
                                        csv_prov = t.getCSV_prov();

                                        break;
                                    }
                                }
                            }
                            int split = 0;
                            int split_trans_id1 = 0;
                            int split_trans_id2 = 0;

                            String yy;
                            String mm;
                            String dd;
                            try {
                                String[] yymmdd = editText_date.getText().toString().split("-");
                                yy = yymmdd[0];
                                mm = yymmdd[1];
                                dd = yymmdd[2];
                            } catch (Exception ex) {
                                yy = "2010";
                                mm = "01";
                                dd = "01";
                            }

                            if (mode.equals("edit") && !possible) {
                                Transaction trans = MainActivity.getTransactionByID(modeid);
                                if (trans != null) {
                                    if (trans.getCategoryId() != cID) {
                                        trans.setCategoryId(cID);
                                        AlertDialog.Builder builder = new AlertDialog.Builder(TransactionAddActivity.this);
                                        try {
                                            builder
                                                    .setTitle("AutoLink Category")
                                                    .setMessage("Are you sure?")
                                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            //Yes button clicked, do something
                                                            Transaction trans = MainActivity.getTransactionByID(modeid);
                                                            //trans.setCategoryId(cID);
                                                            Category c = MainActivity.getCategoryByID(cID);
                                                            c.addSmsDescription(trans.getDescription());
                                                            c.Update();
                                                            Toast.makeText(getApplicationContext(), "Linked " + trans.getDescription() + " to Category " + c.getName(), Toast.LENGTH_LONG).show();
                                                        }
                                                    })
                                                    .setNegativeButton("No", null)
                                                    .show();
                                        } catch (Exception ex) {
                                            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    } else {

                                        trans.setAmount(Double.parseDouble(editText_amount.getText().toString()));
                                        trans.setCategoryId(cID);
                                        trans.setAccount(editText_account.getText().toString());
                                        trans.setDescription(editText_description.getText().toString());
                                        trans.setReference(editText_reference.getText().toString());
                                        trans.setIncomeExpense(tID);
                                        try {
                                            trans.setDate(dateFormat.parse(yy + "-" + mm + "-" + dd));
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        trans.Update();
                                        finish();
                                    }
                                }

                            } else {
                                if (mode.equals("add") || possible) {
                                    Transaction sr1 = null;
                                    try {
                                        sr1 = new Transaction(0,//get next ID
                                                Double.parseDouble(editText_amount.getText().toString()),
                                                cID,
                                                editText_account.getText().toString(),
                                                editText_description.getText().toString(),
                                                dateFormat.parse(yy + "-" + mm + "-" + dd),
                                                editText_reference.getText().toString(),
                                                tID,
                                                deleted,
                                                sms,
                                                csv_prov,
                                                csv_hist,
                                                date_created,
                                                split,
                                                split_trans_id1,
                                                split_trans_id1);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    sr1.Save();
                                    if (!sr1._error.isEmpty())
                                        Toast.makeText(getApplicationContext(), sr1._error, Toast.LENGTH_LONG).show();
                                    else
                                        Toast.makeText(getApplicationContext(), sr1.getDescription() + " saved", Toast.LENGTH_LONG).show();

                                    MainActivity.transactionItems.add(sr1);
                                    if (possible) {
                                        for (Transaction t : MainActivity.transactionItemsPossibleLink) {
                                            if (t.getId() == modeid) {
                                                MainActivity.transactionItemsPossibleLink.remove(t);
                                                t.Delete();
                                                break;
                                            }
                                        }
                                    }
                                }
                                finish();
                            }
                        }
                    }
            );
        }

        Button buttonDelete = (Button)findViewById(R.id.button_delete);
        if (linkto)
            buttonDelete.setVisibility(View.INVISIBLE);
        else {
            buttonDelete.setOnClickListener(
                    new Button.OnClickListener() {
                        public void onClick(View v) {
                            //Put up the Yes/No message box
                            AlertDialog.Builder builder = new AlertDialog.Builder(TransactionAddActivity.this);
                            try {
                                builder
                                        .setTitle("Delete Transaction")
                                        .setMessage("Are you sure?")
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                //Yes button clicked, do something
                                                if (possible) {
                                                    for (Transaction t : MainActivity.transactionItemsPossibleLink) {
                                                        if (t.getId() == modeid) {
                                                            MainActivity.transactionItemsPossibleLink.remove(t);
                                                            break;
                                                        }
                                                    }
                                                } else {
                                                    if (MainActivity.deleteTransactionByID(modeid)) {
                                                        //Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                                finish();
                                            }
                                        })
                                        .setNegativeButton("No", null)                        //Do nothing on no
                                        .show();
                            } catch (Exception ex) {
                                Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
            );
        }

        Button buttonLink = (Button)findViewById(R.id.button_link);
        if (linkto) {
            buttonLink.setVisibility(View.VISIBLE);
            buttonLink.setOnClickListener(
                    new Button.OnClickListener() {
                        public void onClick(View v) {
                            Transaction possibleT = null;
                            for (Transaction t : MainActivity.transactionItemsPossibleLink) {
                                if (t.getId() == trans.getLinkToId()) {
                                    possibleT = t;
                                    break;
                                }
                            }
                            if (possibleT == null) {
                                for (Transaction t : MainActivity.transactionItems) {
                                    if (t.getId() == trans.getLinkToId()) {
                                        possibleT = t;
                                        break;
                                    }
                                }
                            }

                            Transaction mainT = MainActivity.getTransactionByID(trans.getId());
                            if (!possibleT.getCSV_hist().isEmpty())
                                mainT.setCSVHist(possibleT.getCSV_hist());
                            if (!possibleT.getCSV_prov().isEmpty())
                                mainT.setCSVProv(possibleT.getCSV_prov());
                            if (!possibleT.getSms().isEmpty())
                                mainT.setSMS(possibleT.getSms());
                            mainT.Update();
                            MainActivity.transactionItemsPossibleLink.remove(possibleT);
                            possibleT.Delete();
                            TransactonFragment.returnToPossibles = true;
                            finish();
                        }
                    }
            );
        }
        else {
            buttonLink.setVisibility(View.VISIBLE);
            buttonLink.setOnClickListener(
                    new Button.OnClickListener() {
                        public void onClick(View v) {
                            Transaction fullObject = trans;

                                MainActivity.transactionItemsPossibleLinkTo.clear();
                                MainActivity.checkForDuplicateTrans(fullObject, true);

                                if (TransactonFragment.getListAdapter() != null) {
                                    TransactonFragment.getListAdapter().getFilter().filter("linkto", new Filter.FilterListener() {
                                        public void onFilterComplete(int count) {
                                            MainActivity.transactionFilteredCount = count;
                                            MainActivity.mSectionsPagerAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            finish();
                        }
                    }
            );
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Spinner s = (Spinner) findViewById(R.id.spinner_category);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, MainActivity.GetCategorycategoryItemsSpinner());
        s.setAdapter(adapter);

        if (mode.equals("edit")) {
            Transaction trans = MainActivity.getTransactionByID(modeid);
            if (trans != null) {
                s.setSelection(MainActivity.GetCategoryIndexForSpinnerFromId(trans.getCategoryId()));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_transaction_add, menu);
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

        if (id == R.id.add_category) {
            Intent intent = new Intent(TransactionAddActivity.this, CategoryAddActivity.class);
            intent.putExtra("mode", "add");
            intent.putExtra("id", 0);
            this.startActivity(intent);
            addCat = true;
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void selectDate(View view){
        DialogFragment newFragment = new SelectDateFragment();
        newFragment.show(getFragmentManager(), "DatePicker");
    }

    public void populateSetDate(int year, int month, int day){
        EditText _editText_date = (EditText)findViewById(R.id.editText_date);
        _editText_date.setText(year+"-"+month+"-"+day);
    }

    public class SelectDateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener{
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            final Calendar calendar = Calendar.getInstance();
            int yy = calendar.get(Calendar.YEAR);
            int mm = calendar.get(Calendar.MONTH);
            int dd = calendar.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, yy,mm,dd);
        }
        public void onDateSet(DatePicker view, int yy, int mm, int dd)
        {
            populateSetDate(yy,mm+1,dd);
        }
    }
}
