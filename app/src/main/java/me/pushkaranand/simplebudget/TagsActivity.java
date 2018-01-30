package me.pushkaranand.simplebudget;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import java.util.ArrayList;
import java.util.List;

public class TagsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks {
    private static final int REQUEST_INVITE = 0;
    private static final int ADD_TRANS = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int TAGS_LOADER = 2;
    private TagAdapter tagAdapter;
    private List<Tags> tagsList;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tags);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //MobileAds.initialize(this, getResources().getString(R.string.ad_id));

        AdView mAdView = findViewById(R.id.adTagAct);
        AdRequest adRequest = new AdRequest.Builder()
                .build();

        mAdView.loadAd(adRequest);

        databaseHelper = DatabaseHelper.getInstance(this);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
                               {
                                   @Override
                                   public void onClick(View view)
                                   {
                                       LayoutInflater li = LayoutInflater.from(TagsActivity.this);
                                       @SuppressLint("InflateParams") View prompt = li.inflate(R.layout.new_tag_dialog, null);

                                       final EditText n = prompt.findViewById(R.id.tagNameEdt);
                                       final EditText l = prompt.findViewById(R.id.tagLimitEdt);

                                       AlertDialog.Builder builder = new AlertDialog.Builder(TagsActivity.this);
                                       builder.setView(prompt);

                                       builder.setCancelable(false)
                                               .setPositiveButton("Save", new DialogInterface.OnClickListener()
                                               {
                                                   @Override
                                                   public void onClick(DialogInterface dialogInterface, int i)
                                                   {
                                                       if(TextUtils.isEmpty(n.getText()) || TextUtils.isEmpty(l.getText()))
                                                       {
                                                           Toast.makeText(TagsActivity.this, "Input name and limit", Toast.LENGTH_SHORT).show();
                                                       }
                                                       else
                                                       {
                                                           databaseHelper.newTag(n.getText().toString(),0.0,Double.valueOf(l.getText().toString()));
                                                           dialogInterface.dismiss();
                                                           Intent x = new Intent(TagsActivity.this, TagsActivity.class);
                                                           startActivity(x);
                                                           finish();
                                                           //getLoaderManager().restartLoader(TAGS_LOADER,null,TagsActivity.this);
                                                       }

                                                   }
                                               })
                                               .setNegativeButton("Cancel", new
                                                       DialogInterface.OnClickListener()
                                                       {
                                                           @Override
                                                           public void onClick(DialogInterface dialogInterface, int i)
                                                           {
                                                               dialogInterface.cancel();
                                                           }
                                                       }
                                               );
                                       AlertDialog alertDialog = builder.create();

                                       alertDialog.show();
                                   }
                               });


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        RecyclerView recyclerView = findViewById(R.id.rlTagView);
        tagsList = new ArrayList<>();
        tagAdapter = new TagAdapter(tagsList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(tagAdapter);

        getLoaderManager().initLoader(TAGS_LOADER, null, this).forceLoad();
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
        getMenuInflater().inflate(R.menu.menu, menu);
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_about) {
            new LibsBuilder()
                    //provide a style (optional) (LIGHT, DARK, LIGHT_DARK_TOOLBAR)
                    .withActivityStyle(Libs.ActivityStyle.DARK)
                    //start the activity
                    .start(this);
        } else if (id == R.id.action_backup) {
            Intent b = new Intent(this, BackupActivity.class);
            startActivity(b);
        } else if (id == R.id.action_reset_spend) {
            Intent i = new Intent(this, ResetSpendService.class);
            startService(i);
        }

        return super.onOptionsItemSelected(item);
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
        int id = loader.getId();
        if(id == TAGS_LOADER)
        {
            //noinspection unchecked
            tagsList = (List<Tags>) o;
            tagAdapter.updateTagAdapter(tagsList);
            tagAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader loader)
    {
        //setListAdapter(null);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;
        // Handle navigation view item clicks here.
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
}
