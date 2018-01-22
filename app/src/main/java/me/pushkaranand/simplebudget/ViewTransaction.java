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
import java.util.List;


public class ViewTransaction extends AppCompatActivity implements LoaderManager.LoaderCallbacks
{
    private static final int TRANSACTION_LOADER = 1;
    private static final int TAGS_LOADER = 2;

    Integer txn_id = null;
    List<Tags> tagsList;
    Transactions txn;

    Boolean isInEditMode = false, isLoaded = false;
    //For view transaction
    private TextView viewAmt, viewTag, viewDate, viewNote;

    //For edit transaction
    private Spinner editCrDr, editTag;
    private EditText editDate;
    private TextInputEditText editAmt, editNote;

    

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
            @Override
            public void onClick(View view)
            {
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

    public void enableEditMode()
    {
        Log.d("EDIT", "Edit mode enabled");
        findViewById(R.id.viewLayout).setVisibility(View.GONE);
        findViewById(R.id.editLayout).setVisibility(View.VISIBLE);
    }

    public void disableEditMode()
    {
        Log.d("EDIT", "Edit mode disabled");
        findViewById(R.id.editLayout).setVisibility(View.GONE);
        findViewById(R.id.viewLayout).setVisibility(View.VISIBLE);
        saveUpdatedData();
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
        ArrayList<String> lst = new ArrayList<String>();
        int pos = 0;

        for (int i = 0; i < tagsList.size(); i++) {
            String tagName = tagsList.get(i).getTagName();
            if (tagName.equals(txn.getTxn_category())) {
                pos = i;
            }
            lst.add(tagName);
        }

        ArrayAdapter<String> SpinAdapter;

        SpinAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, lst);
        SpinAdapter.setNotifyOnChange(true);

        SpinAdapter.setDropDownViewResource(R.layout.spinner_item);
        editTag.setAdapter(SpinAdapter);
        editTag.setSelection(pos);

    }

    private void saveUpdatedData() {

    }

}
