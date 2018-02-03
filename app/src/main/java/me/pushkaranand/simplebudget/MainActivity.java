package me.pushkaranand.simplebudget;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.DialogInterface;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("unused")
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks {

    private static final String PREF = "simple-budget";
    private static final int REQUEST_INVITE = 0;
    private static final int ADD_TRANS = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int TRANSACTIONS_LOADER = 1;
    private static final int TAGS_LOADER = 2;

    private TextView blncView;
    private List<Transactions> TList;
    private TransactionsAdapter transactionsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        AdView mAdView = findViewById(R.id.adViewMain);
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

        SharedPreferences sharedPreferences = this.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean("FIRST_RUN", true)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("FIRST_RUN", false);
            editor.putInt("RESET_DATE", 1);
            editor.apply();
            createDefaultTags();
        } else if (!sharedPreferences.contains("RESET_DATE")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("RESET_DATE", 1);
            editor.apply();
        }

        blncView = findViewById(R.id.avlBlnc);

        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        int MaxHeight = metrics.heightPixels;

        RecyclerView recyclerView = findViewById(R.id.TlistR);

        TList = new ArrayList<>();
        transactionsAdapter = new TransactionsAdapter(this, TList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(transactionsAdapter);

        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
        params.height = MaxHeight / 2;
        recyclerView.setLayoutParams(params);

        getLoaderManager().initLoader(TRANSACTIONS_LOADER, null, this).forceLoad();

        startRemindingService();

    }

    private void createDefaultTags() {
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        db.initiateTagTable();
    }

    private void updateBalance(String s) {

        blncView.setText(s);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        if (id == TRANSACTIONS_LOADER) {
            Log.e("TEST", "Create Transaction Loader ");
            return new PrepareData(this);

        }
        if(id == TAGS_LOADER) {
            return new TagLoader(this);
        }
        return null;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void onLoadFinished(Loader loader, Object o) {
        int id = loader.getId();
        if (id == TRANSACTIONS_LOADER) {
            @SuppressWarnings("unchecked")
            Pair<ArrayList<Transactions>, Double> data = (Pair<ArrayList<Transactions>, Double>) o;

            Log.d("LOADER", " LoadFinished");

            if (data != null && !data.first.isEmpty()) {
                TList = sortTransactionByDate(data.first);
                Log.i("LOADER_DATA", TList.get(0).getTxn_type());

                transactionsAdapter.updateData(TList);
                transactionsAdapter.notifyDataSetChanged();
                updateBalance(String.valueOf(data.second));

            } else {
                Log.e("LOADER_DATA ", "NULL OR EMPTY");
            }
        }
        if(id == TAGS_LOADER) {


        }

        sendAlertIfRequired();
    }

    @Override
    public void onLoaderReset(Loader loader) {
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_about) {
            new LibsBuilder()
                    .withActivityStyle(Libs.ActivityStyle.DARK)
                    .start(this);
        } else if (id == R.id.action_backup) {
            Intent b = new Intent(this, BackupActivity.class);
            startActivity(b);
        } else if (id == R.id.action_reset_spend) {
            Intent i = new Intent(this, ResetSpendService.class);
            startService(i);

        } else if (id == R.id.action_feedback) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setCancelable(false)
                    .setTitle("Feedback")
                    .setMessage("Are you satisfied with this app??")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            Toast.makeText(MainActivity.this, "Please rate this application with 5 stars",
                                    Toast.LENGTH_LONG).show();
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("market://details?id=" + getResources().getString(R.string.appID))));
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                                startActivity(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse(getResources().getString(R.string.app_playstore_link))));
                            }

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            Intent Email = new Intent(Intent.ACTION_SEND);
                            Email.setType("text/email");
                            Email.putExtra(Intent.EXTRA_EMAIL, new String[]{"anandpushkar088@gmail.com"});
                            Email.putExtra(Intent.EXTRA_SUBJECT, "Feedback for app Simple Budget");
                            Email.putExtra(Intent.EXTRA_TEXT, "Dear ...," + "");
                            startActivity(Intent.createChooser(Email, "Send Feedback:"));
                        }
                    });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();


        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;

        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_search) {
            Intent i = new Intent(this, SearchActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_tags) {
            Intent i = new Intent(this, TagsActivity.class);
            startActivity(i);

        } else if (id == R.id.nav_settings) {
            intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_share) {
            Intent i = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                    .setMessage(getString(R.string.invitation_message))
                    .setDeepLink(Uri.parse(getString(R.string.app_playstore_link)))
                    .setCallToActionText(getString(R.string.invitation_cta))
                    .build();
            startActivityForResult(i, REQUEST_INVITE);
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

    private void startRemindingService() {
        Log.d("FUNCTION : ", "Starting startRemindingService");

        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, 21);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(this, AddTransactionReminder.class);

        PendingIntent pendingIntent = PendingIntent.getService(this,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);


        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
        } else {
            Log.e("NotifyAlarm", "alarmMngr is null");
        }
    }

    private void sendAlertIfRequired() {
        Intent intent = new Intent(this, LimitCheckerService.class);
        startService(intent);
    }

    private ArrayList<Transactions> sortTransactionByDate(ArrayList<Transactions> toSort) {
        Log.d("SORT ", "in sortTransactionByDateCall");
        Collections.sort(toSort, new Comparator<Transactions>() {
            int returnVal;

            @Override
            public int compare(Transactions t1, Transactions t2) {
                Log.d("SORT ", "comparing " + t1.getTxn_id() + " & " + t2.getTxn_id());
                String ds1, ds2;

                Date d1 = null, d2 = null;

                ds1 = t1.getTxn_date();
                ds2 = t2.getTxn_date();

                Log.d("SORT ", "dates " + ds1 + " & " + ds2);

                if (ds1.equals(ds2)) {
                    Log.d("SORT ", "ds1 and ds2 are equal");
                    returnVal = t2.getTxn_id().compareTo(t1.getTxn_id());
                    return returnVal;
                } else {
                    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

                    try {
                        d1 = format.parse(ds1);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    try {
                        d2 = format.parse(ds2);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    returnVal = d2.compareTo(d1);
                    return returnVal;
                }
            }
        });


        return toSort;
    }


    @SuppressWarnings("SameReturnValue")
    private boolean isUpdateAvailable() {
        return false;
    }

    @SuppressWarnings("EmptyMethod")
    private void sendUpdateNotification() {

    }

    @SuppressWarnings("unused")
    private static class PrepareData extends AsyncTaskLoader<Pair<ArrayList<Transactions>, Double>> {
        ArrayList<Integer> ids;
        Cursor res;
        Transactions txn;

        PrepareData(Context context) {
            super(context);
        }

        @Override
        public Pair<ArrayList<Transactions>, Double> loadInBackground() {
            Double balance = 0.0;
            ArrayList<Transactions> arrayList = new ArrayList<>();
            DatabaseHelper db;
            Log.d("TaskLoader: ", "Started working");
            db = DatabaseHelper.getInstance(getContext());
            ids = db.ListTransactionIds();
            Log.d("TRANSACTIONS: ", String.valueOf(ids.size()));

            for (Integer id : ids) {
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

                arrayList.add(txn);
            }
            Log.d("TaskLoader: ", "Returning");

            return new Pair<>(arrayList,balance);
        }

        protected void onReleaseResources() {
            res.close();
        }
    }
}
