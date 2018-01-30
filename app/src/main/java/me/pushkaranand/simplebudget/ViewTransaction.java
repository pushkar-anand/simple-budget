package me.pushkaranand.simplebudget;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ViewTransaction extends AppCompatActivity implements LoaderManager.LoaderCallbacks
{
    private static final int TRANSACTION_LOADER = 1;
    private static final int TAGS_LOADER = 2;

    private Integer txn_id = null;
    private List<Tags> tagsList;
    private Transactions txn;

    private Boolean isInEditMode = false;
    private Boolean isLoaded = false;
    //For view transaction
    private TextView viewAmt, viewTag, viewDate, viewNote;

    //For edit transaction
    private Spinner editCrDr, editTag;
    private EditText editDate;
    private TextInputEditText editAmt, editNote;
    private int old;

    

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_transaction);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewAmt = findViewById(R.id.amountTxt);
        viewTag = findViewById(R.id.tagTxt);
        viewDate = findViewById(R.id.dateTxt);
        viewNote = findViewById(R.id.notesTxt);

        editCrDr = findViewById(R.id.CrDrSpinner);
        editTag = findViewById(R.id.TagSpin);
        editDate = findViewById(R.id.DateSel);
        editAmt = findViewById(R.id.amountEdt);
        editNote = findViewById(R.id.NotesEdt);


        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onClick(View view) {
                if (isLoaded) {
                    if (!isInEditMode) {
                        isInEditMode = true;
                        fab.setImageResource(R.drawable.ic_action_done);
                        enableEditMode();
                    } else if (isInEditMode) {
                        isInEditMode = false;
                        fab.setImageResource(R.drawable.ic_action_edit);
                        disableEditMode();
                    }
                } else {
                    Toast.makeText(ViewTransaction.this, "Contents not loaded. Please click again in few moments", Toast.LENGTH_SHORT).show();
                }
            }
        });

        try {
            //noinspection ConstantConditions
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            e.printStackTrace();
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        Intent intent = getIntent();
        txn_id = intent.getIntExtra("TXN_ID",0);
        if(txn_id != 0)
        {
            getLoaderManager().initLoader(TRANSACTION_LOADER, null,this).forceLoad();
            getLoaderManager().initLoader(TAGS_LOADER, null,this).forceLoad();
            Log.d("TXN_ID", String.valueOf(txn_id));
        }
        else
        {
            Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void enableEditMode()
    {
        Log.d("EDIT", "Edit mode enabled");
        findViewById(R.id.viewLayout).setVisibility(View.GONE);
        findViewById(R.id.editLayout).setVisibility(View.VISIBLE);
    }

    private void disableEditMode()
    {
        Log.d("EDIT", "Edit mode disabled");
        findViewById(R.id.editLayout).setVisibility(View.GONE);
        findViewById(R.id.viewLayout).setVisibility(View.VISIBLE);
        saveUpdatedData();
        Intent i = new Intent(this, ViewTransaction.class);
        i.putExtra("TXN_ID", txn_id);
        startActivity(i);
        finish();
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args)
    {
        if (id == TRANSACTION_LOADER)
        {
            Log.e("TEST", "Create Loader");
            return new SingleTransactionLoader(this, txn_id);
        }
        if(id == TAGS_LOADER)
        {
            return new TagLoader(this);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader loader, Object o)
    {
        int id = loader.getId();
        isLoaded = true;

        if(id == TRANSACTION_LOADER)
        {
            Transactions transaction = (Transactions) o;

            Log.d("LOADER", " LoadFinished");

            if (transaction != null )
            {

                Log.i("LOADER_DATA", transaction.getTxn_type());
                //process the data
                txn = transaction;
                updateTransactionViewData();
            }
            else
            {
                Log.e("LOADER_DATA ", "NULL OR EMPTY");
            }
        }
        if(id == TAGS_LOADER)
        {
            //noinspection unchecked
            tagsList = (List<Tags>) o;
            updateSpinners();
        }
    }

    @Override
    public void onLoaderReset(Loader loader)
    {
        //setListAdapter(null);
    }

    private void updateTransactionViewData() {

        /*//For view transaction
        private TextView viewAmt, viewTag, viewDate, viewNote;

        //For edit transaction
        private Spinner editCrDr, editTag;
        private EditText editDate;
        private TextInputEditText editAmt, editNote;*/

        String x = (txn.getTxn_type().equals("DEBIT")) ? "-" : "+";
        String y = x + String.valueOf(txn.getTxn_amount());

        viewAmt.setText(y);
        viewTag.setText(txn.getTxn_category());
        viewDate.setText(txn.getTxn_date());
        viewNote.setText(txn.getTxn_notes());

        editAmt.setText(String.valueOf(txn.getTxn_amount()));
        editNote.setText(txn.getTxn_notes());
        editDate.setText(txn.getTxn_date());

    }

    private void updateSpinners() {
        ArrayList<String> lst = new ArrayList<>();

        try {
            for (int i = 0; i < tagsList.size(); i++) {
                String tagName = tagsList.get(i).getTagName();
                if (tagName.equals(txn.getTxn_category())) {
                    old = i;
                }
                lst.add(tagName);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            Toast.makeText(this, "Some error occurred", Toast.LENGTH_SHORT).show();
        }


        ArrayAdapter<String> SpinAdapter, CrDrApadter;

        SpinAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, lst);
        SpinAdapter.setNotifyOnChange(true);

        SpinAdapter.setDropDownViewResource(R.layout.spinner_item);
        editTag.setAdapter(SpinAdapter);
        editTag.setSelection(old);

        ArrayList<String> l = new ArrayList<>();
        l.add("CREDIT");
        l.add("DEBIT");

        CrDrApadter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, l);
        CrDrApadter.setDropDownViewResource(R.layout.spinner_item);

        editCrDr.setAdapter(CrDrApadter);

        if (txn.getTxn_type().equals("CREDIT")) {
            editCrDr.setSelection(0);
        } else {
            editCrDr.setSelection(1);
        }
    }

    private void saveUpdatedData() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);

        String type = editCrDr.getSelectedItem().toString();

        String tag = editTag.getSelectedItem().toString();
        String old_tag = txn.getTxn_category();
        String tag_id = String.valueOf(editTag.getSelectedItemId() + 1);
        Integer old_tag_id = old + 1;

        Double amount = Double.valueOf(editAmt.getText().toString());
        Double old_amount = txn.getTxn_amount();

        String note = editNote.getText().toString();

        String date = editDate.getText().toString();

        String[] arr = date.split("-");

        Log.d("DATE: ", Arrays.toString(arr));

        Integer year = Integer.valueOf(arr[2]);
        String month = arr[1];

        databaseHelper.updateTransaction(txn_id, type, amount, tag, date, year, month, note, old_tag, old_amount, Integer.valueOf(tag_id), old_tag_id);
    }
}
