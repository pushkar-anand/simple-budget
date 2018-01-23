package me.pushkaranand.simplebudget;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks {

    public static final String PREF = "simple-budget";
    private static final int REQUEST_INVITE = 0;
    private static final int ADD_TRANS = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int TRANSACTIONS_LOADER = 1;
    private static final int TAGS_LOADER = 2;
    TextView blncView;
    SharedPreferences sharedPreferences;
    Double availableBalance = 00.00;
    RecyclerView recyclerView;
    List<Transactions> TList;
    DatabaseHelper databaseHelper;
    String[] spinData;
    private TransactionsAdapter transactionsAdapter;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewTransaction.class);
                startActivity(intent);
                finish();
            }
        });

        mAdView = findViewById(R.id.adViewMain);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        sharedPreferences = this.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean("FIRST_RUN", true))
        {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("FIRST_RUN", false);
            editor.apply();
            createDefaultTags();
        }

        databaseHelper = DatabaseHelper.getInstance(this);
        //availableBalance = 00.00;//databaseHelper.getAvailableBalance();

        blncView = findViewById(R.id.avlBlnc);

        spinData = new String[]{"All", "Today", "Yesterday", "This Week", "This Month"};

        Spinner selectorSpinner = findViewById(R.id.selectorSpinner);
        ArrayAdapter<String> selectorAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, spinData);
        selectorSpinner.setAdapter(selectorAdapter);

        selectorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String selectedItemText = (String) adapterView.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), "Selected : " + selectedItemText, Toast.LENGTH_SHORT).show();
                Bundle b = new Bundle();
                b.putString("Item", selectedItemText);
                getLoaderManager().initLoader(1, b, MainActivity.this).forceLoad();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {

            }
        });


        recyclerView = findViewById(R.id.TlistR);
        TList = new ArrayList<>();
        transactionsAdapter = new TransactionsAdapter(this, TList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //recyclerView.hasFixedSize();
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(transactionsAdapter);

        getLoaderManager().initLoader(TRANSACTIONS_LOADER, null, this).forceLoad();

        //new PrepareData(this);
    }

    public void createDefaultTags()
    {
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        db.initiateTagTable();
    }

    public void updateBalance(String s)
    {

        blncView.setText(s);
    }

    @Override
    protected void onResume() {
        super.onResume();
        availableBalance = 00.00;
    }

    @Override
    protected void onPause() {
        super.onPause();
        availableBalance = 00.00;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args)
    {
        if (id == TRANSACTIONS_LOADER)
        {
            Log.e("TEST", "Create Loader");
            if (args == null) {
                Log.d("LoaderCreate: ", "Bundle is null");
                return new PrepareData(this);
            } else {
                String s = args.getString("Item");
                return new PrepareData(this, s);
            }
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
        if(id == TRANSACTIONS_LOADER)
        {
            Pair<ArrayList<Transactions>, Double> data = (Pair<ArrayList<Transactions>, Double>) o;

            Log.d("LOADER", " LoadFinished");

            if (data != null && !data.first.isEmpty()) {
                TList = data.first;
                Log.i("LOADER_DATA", TList.get(0).getTxn_type());

                //List<Transactions> f = new ArrayList<>();
                //recyclerView.setAdapter(new TransactionsAdapter(f));
                transactionsAdapter.updateData(TList);
                transactionsAdapter.notifyDataSetChanged();
                updateBalance(String.valueOf(data.second));
                //setListAdapter(new PrepareData(this, data));

            } else {
                Log.e("LOADER_DATA ", "NULL OR EMPTY");
            }
        }
        if(id == TAGS_LOADER)
        {

            //
        }
    }

    @Override
    public void onLoaderReset(Loader loader)
    {
        //setListAdapter(null);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
        } else if (id == R.id.action_about) {
            new LibsBuilder()
                    //provide a style (optional) (LIGHT, DARK, LIGHT_DARK_TOOLBAR)
                    .withActivityStyle(Libs.ActivityStyle.DARK)
                    //start the activity
                    .start(this);
        } else if (id == R.id.action_backup) {
            Intent b = new Intent(this, BackupActivity.class);
            startActivity(b);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        Intent intent;
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home)
        {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_search)
        {
            Intent i = new Intent(this, SearchActivity.class);
            startActivity(i);
        }
        else if (id == R.id.nav_tags)
        {
            Intent i = new Intent(this, TagsActivity.class);
            startActivity(i);

        } else if (id == R.id.nav_settings)
        {
            intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_share)
        {
            Intent i = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                    .setMessage(getString(R.string.invitation_message))
                    .setDeepLink(Uri.parse(getString(R.string.app_playstore_link)))
                    .setCallToActionText(getString(R.string.invitation_cta))
                    .build();
            startActivityForResult(i, REQUEST_INVITE);
            /*intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT,
                    "Hey check out this app at: "+R.string.app_playstore_link);
            intent.setType("text/plain");
            startActivity(intent);*/
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode= " + requestCode + ", resultCode= " + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.d(TAG, "onActivityResult: sent invitation " + id);
                }
            } else {
                Toast.makeText(this, "Some error occurred", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == ADD_TRANS) {
            if (resultCode == RESULT_OK) {
                String message = data.getStringExtra("MESSAGE");
                Toast.makeText(this, "Added Transaction", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Added " + message);
            }

        }
    }

    private static class PrepareData extends AsyncTaskLoader<Pair<ArrayList<Transactions>, Double>> {
        ArrayList<Integer> ids;
        Cursor res;
        Transactions txn;
        String listType = null;

        PrepareData(Context context) {
            super(context);
        }

        PrepareData(Context context, String s) {
            super(context);
            listType = s;
        }

        @Override
        public Pair<ArrayList<Transactions>, Double> loadInBackground()
        {
            //super.loadInBackground();
            Double balance = 0.0;
            ArrayList<Transactions> arrayList = new ArrayList<>();
            DatabaseHelper db;
            Log.d("TaskLoader: ", "Started working");
            db = DatabaseHelper.getInstance(getContext());
            ids = db.ListTransactionIds();
            Log.d("TRANSACTIONS: ", String.valueOf(ids.size()));

            for (Integer id : ids)
            {
                res = db.getData(id);
                res.moveToFirst();

                String txn_date, txn_category, txn_type, txn_notes;
                Double txn_amount;
                Integer txn_id;

                txn_id = res.getInt(res.getColumnIndex(DatabaseHelper.COLUMN_NAME_ID));
                txn_date = res.getString(res.getColumnIndex(DatabaseHelper.COLUMN_NAME_DATE));
                txn_type = res.getString(res.getColumnIndex(DatabaseHelper.COLUMN_NAME_TYPE));
                txn_amount = res.getDouble(res.getColumnIndex(DatabaseHelper.COLUMN_NAME_AMOUNT));
                txn_category = res.getString(res.getColumnIndex(DatabaseHelper.COLUMN_NAME_CATEGORY));
                txn_notes = res.getString(res.getColumnIndex(DatabaseHelper.COLUMN_NAME_NOTES));

                txn = new Transactions(txn_id, txn_date, txn_category, txn_type, txn_notes, txn_amount);

                String log = String.valueOf(id) + " " + txn_date + " " + txn_type + " " + String.valueOf(txn_amount);

                Log.i("TRANSACTION: ", log);

                if (txn_type.equals("CREDIT")) {
                    balance += txn_amount;
                } else if (txn_type.equals("DEBIT")) {
                    balance -= txn_amount;
                }
                if (listType == null || listType.equals("All"))
                {
                    arrayList.add(txn);
                }
                else
                {
                    if (listType.equals("Today") && isToday(txn_date))
                    {
                        arrayList.add(txn);
                    }
                    else if (listType.equals("Yesterday") && isYesterday(txn_date))
                    {
                        arrayList.add(txn);
                    }
                    else if(listType.equals("This Week") && isThisWeek(txn_date))
                    {
                        arrayList.add(txn);
                    }
                    else if(listType.equals("This Month") && isThisMonth(txn_date))
                    {
                        arrayList.add(txn);
                    }
                }
            }
            Log.d("TaskLoader: ", "Returning");

            return new Pair<>(arrayList,balance);
        }

        private boolean isToday(String d) {
            Calendar calendar = Calendar.getInstance();
            Integer year = calendar.get(Calendar.YEAR);

            Integer month = calendar.get(Calendar.MONTH) + 1;
            Integer day = calendar.get(Calendar.DAY_OF_MONTH);

            String date = String.valueOf(day) + "-" + String.valueOf(month) + "-" + String.valueOf(year);

            return Objects.equals(date, d);
        }

        private boolean isYesterday(String d) {
            String date;
            Calendar calendar = Calendar.getInstance();
            Integer year = calendar.get(Calendar.YEAR);

            Integer month = calendar.get(Calendar.MONTH) + 1;
            Integer day = calendar.get(Calendar.DAY_OF_MONTH);

            if (day == 1 && month == 1) {
                date = String.valueOf(31) + "-" + String.valueOf(12) + "-" + String.valueOf(year - 1);
            } else if (day == 1 && month == 3 && year % 4 == 0) {
                date = String.valueOf(29) + "-" + String.valueOf(2) + "-" + String.valueOf(year);
            } else if (day == 1 && month == 3) {
                date = String.valueOf(28) + "-" + String.valueOf(2) + "-" + String.valueOf(year);
            } else if (day == 1 && (month == 2 || month == 4 || month == 6 || month == 9 || month == 11)) {
                date = String.valueOf(31) + "-" + String.valueOf(month - 1) + "-" + String.valueOf(year);
            } else if (day == 1) {
                date = String.valueOf(30) + "-" + String.valueOf(month - 1) + "-" + String.valueOf(year);
            } else {
                date = String.valueOf(day - 1) + "-" + String.valueOf(month) + "-" + String.valueOf(year);
            }

            return date.equals(d);
        }

        private boolean isThisWeek(String d) {
            return false;
        }

        private boolean isThisMonth(String d) {
            String date;
            Calendar calendar = Calendar.getInstance();

            Integer month = calendar.get(Calendar.MONTH) + 1;

            String[] s = d.split("-");

            return String.valueOf(month).equals(s[1]);
        }
        protected void onReleaseResources() {
            res.close();
        }
    }
}
