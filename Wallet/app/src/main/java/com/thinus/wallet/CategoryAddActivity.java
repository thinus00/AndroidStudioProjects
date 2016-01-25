package com.thinus.wallet;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import android.app.Dialog;
import android.widget.Toast;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by thinus on 2015/10/14.
 */
public class CategoryAddActivity extends ActionBarActivity {
    public String mode;
    public int modeid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mode = getIntent().getExtras().getString("mode");
        modeid = getIntent().getExtras().getInt("id");

        setContentView(R.layout.activity_category_add);

        Spinner s = (Spinner) findViewById(R.id.spinner_type);
        ArrayAdapter<String> adapter = new ArrayAdapter<String> (this,android.R.layout.simple_spinner_item, Category.GetCategoryTypeSpinnerList());
        s.setAdapter(adapter);

        if (mode.equals("add")) {

        } else{
            if (mode.equals("edit")) {
                //modeid
                Category cat = CategoryListActivity.getCategoryByID(modeid);
                if (cat != null) {
                    EditText editText_name = (EditText)findViewById(R.id.editText_name);
                    editText_name.setText(cat.getName());
                    Spinner spn = (Spinner) findViewById(R.id.spinner_type);
                    spn.setSelection(cat.getCatIndexFromType(cat.getCatType()));
                    EditText editText_budget = (EditText)findViewById(R.id.editText_budget);
                    editText_budget.setText(String.valueOf(cat.getBudget()));
                    EditText editText_smsdesc = (EditText)findViewById(R.id.editText_smsdesc);
                    editText_smsdesc.setText(cat.getSmsDescriptionString());
                }
            }
        }

        Button buttonSave = (Button)findViewById(R.id.button_save);
        buttonSave.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

                        EditText editText_name = (EditText) findViewById(R.id.editText_name);
                        Spinner s = (Spinner) findViewById(R.id.spinner_type);
                        //EditText editText_type = (EditText)findViewById(R.id.editText_type);
                        EditText editText_budget = (EditText) findViewById(R.id.editText_budget);
                        EditText editText_smsdesc = (EditText)findViewById(R.id.editText_smsdesc);
                        Category.CategoryType ct = null;
                        switch (s.getSelectedItem().toString()) {
                            case "Recurring":
                                ct = Category.CategoryType.Recurring;
                                break;
                            case "Day-to-Day":
                                ct = Category.CategoryType.DayToDay;
                                break;
                            case "Income":
                                ct = Category.CategoryType.Income;
                                break;
                            case "Transfer":
                                ct = Category.CategoryType.Transfer;
                                break;
                        }

                        if (mode.equals("edit")) {
                            Category cat = CategoryListActivity.getCategoryByID(modeid);
                            if (cat != null) {
                                cat.setName(editText_name.getText().toString());
                                cat.setCatType(ct);
                                cat.setBudget(Double.parseDouble(editText_budget.getText().toString()));
                                cat.setSmsDescriptionString(editText_smsdesc.getText().toString());
                                cat.Update();
                            }
                            finish();

                        } else {
                            if (mode.equals("add")) {

                                Category sr1 = null;
                                //int Id, String Name, CategoryType catType, double Budget)
                                sr1 = new Category(0,//get next ID
                                        editText_name.getText().toString(),
                                        ct,
                                        Double.parseDouble(editText_budget.getText().toString()),
                                        ""
                                );

                                sr1.Save();
                                if (!sr1._error.isEmpty())
                                    Toast.makeText(getApplicationContext(), sr1._error, Toast.LENGTH_LONG).show();
                                else
                                    Toast.makeText(getApplicationContext(), sr1.getName() + " saved", Toast.LENGTH_LONG).show();
                                CategoryListActivity.categoryItems.add(sr1);
                                finish();
                            }
                        }
                    }
                }
        );

        Button buttonDelete = (Button)findViewById(R.id.button_delete);
        buttonDelete.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        //Put up the Yes/No message box
                        AlertDialog.Builder builder = new AlertDialog.Builder(CategoryAddActivity.this);
                        try {
                            builder
                                    .setTitle("Delete Category")
                                    .setMessage("Are you sure?")
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            //Yes button clicked, do something
                                            if (CategoryListActivity.deleteCategoryByID(modeid)) {
                                                Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_LONG).show();
                                            }
                                            //Toast.makeText(CategoryAddActivity.this, "Yes button pressed",Toast.LENGTH_SHORT).show();
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_transaction_add, menu);
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
