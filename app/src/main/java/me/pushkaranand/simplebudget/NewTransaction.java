package me.pushkaranand.simplebudget;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("unused")
public class NewTransaction extends AppCompatActivity implements LoaderManager.LoaderCallbacks
{
    private static final int TAGS_LOADER = 2;
    private RadioGroup crDr;
    private EditText amount;
    private EditText notes;
    private Button dateBtn;
    private String catg;
    private int pos;
    private ArrayAdapter<String> SpinAdapter;
    private DatabaseHelper databaseHelper;
    private int year, month, day;
    private final DatePickerDialog.OnDateSetListener myDateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0,
                                      int arg1, int arg2, int arg3) {
                    // arg1 = year
                    // arg2 = month
                    // arg3 = day
                    showDate(arg1, arg2 + 1, arg3);
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_transaction);

        databaseHelper = DatabaseHelper.getInstance(this);

        crDr = findViewById(R.id.CrDr);

        amount = findViewById(R.id.Tamount);
        notes = findViewById(R.id.Tnotes);

        Spinner catSpin = findViewById(R.id.catSpinner);

        dateBtn = findViewById(R.id.dateButton);


        String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String dateBtnTxt = "Date: "+date;
        dateBtn.setText(dateBtnTxt);

        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH)+1;
        day = calendar.get(Calendar.DAY_OF_MONTH);

        String[] categories = new String[]{"Select Tag"};
        ArrayList<String> lst = new ArrayList<>(Arrays.asList(categories));
        getLoaderManager().initLoader(TAGS_LOADER, null, this).forceLoad();

        SpinAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item,lst)
        {
            @Override
            public boolean isEnabled(int position)
            {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0)
                {
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                return view;
            }
        };
        SpinAdapter.setNotifyOnChange(true);

        SpinAdapter.setDropDownViewResource(R.layout.spinner_item);
        catSpin.setAdapter(SpinAdapter);

        catSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                // If user change the default selection
                // First item is disable and it is used for hint
                pos = position;
                if(position > 0)
                {
                    // Notify the selected item text
                    Toast.makeText(getApplicationContext(), "Selected : " + selectedItemText, Toast.LENGTH_SHORT).show();
                    catg = selectedItemText;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

    }

    public void setDate(View v)
    {
        showDialog(999);
        //new DatePickerDialog(this,myDateListener, year,month,day);
    }

    @Override
    protected Dialog onCreateDialog(int id)
    {
        if (id == 999)
        {
            return new DatePickerDialog(this,
                    myDateListener, year, month-1, day);
        }
        return null;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args)
    {
        if(id == TAGS_LOADER)
        {
            return new TagLoader(this);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader loader, Object o)
    {
        List<Tags> data;
        List<String> s = new ArrayList<>();
        int id = loader.getId();
        if(id == TAGS_LOADER)
        {
            if(o != null)
            {
                Log.d("TAG_LOADER", "Loader finished updating spinner.");
                //noinspection unchecked
                data = (List<Tags>) o;
                for (Tags d:  data)
                {
                    s.add(d.getTagName());
                }
                SpinAdapter.addAll(s);
                SpinAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader loader)
    {
        //setListAdapter(null);
    }

    public void SaveTransaction(View view)
    {
        int selectedId = crDr.getCheckedRadioButtonId();
        if (selectedId != -1)
        {
            RadioButton txn_rd =  findViewById(selectedId);
            String txn_type = txn_rd.getText().toString().toUpperCase();

            String xz = amount.getText().toString().trim();
            if (xz.isEmpty() || xz.length() == 0 || xz.equals(""))
            {
                Toast.makeText(this, "Enter amount", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Double txn_amount = Double.parseDouble(xz);

                String n = notes.getText().toString().trim();
                String txn_notes = (n.isEmpty() || n.length() == 0 || n.equals(""))?null:n;

                String txn_date = String.valueOf(day)+"-"+String.valueOf(month)+"-"+String.valueOf(year);
                Integer txn_year=year;
                String txn_month = String.valueOf(month);

                if(pos>0)
                {
                    String txn_category = catg;
                    Integer id = pos;

                    Double currSpend = databaseHelper.getTagSpend(id);
                    Double limit = databaseHelper.getTagLimit(id);


                    //noinspection ConstantConditions
                    if (currSpend + txn_amount >= limit && (limit != 0 || limit < 0) && txn_type.equals("DEBIT"))
                    {
                        Toast.makeText(this,
                                "You are exceeding the limit for " + txn_category,
                                Toast.LENGTH_LONG)
                                .show();
                    } else {

                        if (databaseHelper.newTransaction(txn_type, txn_amount, txn_category, txn_date,
                                txn_year, txn_month, txn_notes, id)) {
                            Intent intent = new Intent(this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Error adding transaction", Toast.LENGTH_SHORT).show();
                        }
                    }

                }
                else {
                    Toast.makeText(this, "Select category", Toast.LENGTH_SHORT).show();
                }
            }
        }
        else
        {
            Toast.makeText(this, "Select debit/credit", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    private void showDate(int y, int m, int d)
    {
        String newDateBtnTxt = "Date: "+String.valueOf(d)+"-"+String.valueOf(m)+"-"+String.valueOf(y);
        dateBtn.setText(newDateBtnTxt);
        year=y;
        month=m;
        day=d;
    }
}
