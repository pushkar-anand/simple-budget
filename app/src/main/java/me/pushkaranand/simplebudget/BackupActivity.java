package me.pushkaranand.simplebudget;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@SuppressWarnings("unused")
public class BackupActivity extends AppCompatActivity {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final int REQUEST_CODE_SIGN_IN = 0;

    private static final String PREF = "simple-budget";

    private static final int PERMISSION_REQUEST_STORAGE = 123;

    private SharedPreferences sharedPreferences;
    private DriveResourceClient mDriveResourceClient;
    private boolean accountConnected = false;

    private TextView textView;

    private Button b;

    private View mLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);


        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                Toast.makeText(this, "Device does'nt support play services", Toast.LENGTH_SHORT).show();
            }

        }


        sharedPreferences = this.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        GoogleSignInClient mGoogleSignInClient = buildGoogleSignInClient();
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);

        b = findViewById(R.id.restoreBTN);


        textView = findViewById(R.id.LstBackupTxt);


        long now = System.currentTimeMillis();

        String sfTime = sharedPreferences.getString("lastDbBackupTime", "Never");
        Date date = null;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            date = format.parse(sfTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        CharSequence relativeTimeSpanString = "Never";
        if (date != null) {
            relativeTimeSpanString = DateUtils.getRelativeTimeSpanString(date.getTime(), now, DateUtils.DAY_IN_MILLIS);
            Log.d("DATE ", relativeTimeSpanString.toString());
        } else {
            Log.d("DATE", "date is null");
        }
        String x = getString(R.string.last_backup) + relativeTimeSpanString.toString();

        textView.setText(x);

        mLayout = findViewById(R.id.bckCL);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PLAY_SERVICES_RESOLUTION_REQUEST:
                if (resultCode == RESULT_OK) {
                    recreate();
                } else {
                    Toast.makeText(this, "Could'nt get play service", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;

            case REQUEST_CODE_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    accountConnected = true;
                    DriveClient mDriveClient = Drive.getDriveClient(this,
                            GoogleSignIn.getLastSignedInAccount(this));
                    mDriveResourceClient = Drive.getDriveResourceClient(this,
                            GoogleSignIn.getLastSignedInAccount(this));
                }

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void startBackup(View view) {
        if (!accountConnected) {
            Toast.makeText(this, "Please sign in using google", Toast.LENGTH_SHORT).show();
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(this);

        final File currentDB = this.getDatabasePath(DatabaseHelper.DATABASE_NAME);

        Log.d("DATABASE: ", currentDB.getAbsolutePath());
        Log.d("DATABASE: ", currentDB.getName());

        progressDialog.setMessage("Backing Up!!!!");
        progressDialog.show();

        final Task<DriveFolder> appFolderTask = mDriveResourceClient.getAppFolder();
        final Task<DriveContents> createContentsTask = mDriveResourceClient.createContents();

        Tasks.whenAll(appFolderTask, createContentsTask)
                .continueWithTask(new Continuation<Void, Task<DriveFile>>() {
                    @Override
                    public Task<DriveFile> then(@NonNull Task<Void> task) throws Exception {
                        DriveFolder parent = appFolderTask.getResult();
                        DriveContents contents = createContentsTask.getResult();

                        InputStream inputStream = null;

                        try {
                            inputStream = new FileInputStream(currentDB);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        OutputStream outputStream = contents.getOutputStream();
                        int c;
                        byte[] buf = new byte[8192];
                        if (inputStream != null) {

                            while ((c = inputStream.read(buf, 0, buf.length)) > 0) {
                                outputStream.write(buf, 0, c);
                                outputStream.flush();
                            }
                            outputStream.close();
                        } else {
                            Toast.makeText(BackupActivity.this, "Some error occurred", Toast.LENGTH_SHORT).show();
                        }

                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setMimeType("application/x-sqlite3")
                                .setTitle(currentDB.getName())
                                .build();


                        return mDriveResourceClient.createFile(parent, changeSet, contents);
                    }
                })
                .addOnSuccessListener(this, new OnSuccessListener<DriveFile>() {
                    @Override
                    public void onSuccess(DriveFile driveFile) {
                        progressDialog.dismiss();

                        String driveFileId = driveFile.getDriveId().encodeToString();

                        String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("dbBackupDriveFileID", driveFileId);
                        editor.putString("lastDbBackupTime", dateTime);
                        editor.apply();

                        Log.d("DRIVE_FILE", driveFileId);

                        String d = getString(R.string.last_backup) + dateTime;
                        b.setEnabled(true);

                        textView.setText(d);

                        Toast.makeText(BackupActivity.this, "Backup Successful. File " + driveFileId, Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.e("DRIVE ", "Unable to create file", e);
                        Toast.makeText(BackupActivity.this, "Unable to backup", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void startRestore(View view) {
        if (!accountConnected) {
            Toast.makeText(this, "Please sign in using google", Toast.LENGTH_SHORT).show();
            return;
        }

        int EXTERNAL_WRITE_PERMISSION = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (EXTERNAL_WRITE_PERMISSION != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Snackbar.make(mLayout, "Write permission is required",
                            Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request the permission
                            ActivityCompat.requestPermissions(BackupActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    PERMISSION_REQUEST_STORAGE);
                        }
                    }).show();

                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_STORAGE);
                }
            }
        }
        EXTERNAL_WRITE_PERMISSION = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (EXTERNAL_WRITE_PERMISSION == PackageManager.PERMISSION_GRANTED) {
            Log.d("RESTORE: ", "Started restore");

            if (sharedPreferences.contains("dbBackupDriveFileID") && sharedPreferences.contains("lastDbBackupTime")) {

                final Query query = new Query.Builder()
                        .addFilter(Filters.eq(SearchableField.TITLE, DatabaseHelper.DATABASE_NAME))
                        .build();

                final Task<DriveFolder> appFolderTask = mDriveResourceClient.getAppFolder();

                appFolderTask.continueWithTask(new Continuation<DriveFolder, Task<MetadataBuffer>>() {
                    @Override
                    public Task<MetadataBuffer> then(@NonNull Task<DriveFolder> task) throws Exception {
                        DriveFolder appFolder = appFolderTask.getResult();


                        return mDriveResourceClient.queryChildren(appFolder, query);
                    }
                }).addOnSuccessListener(new OnSuccessListener<MetadataBuffer>() {
                    @Override
                    public void onSuccess(MetadataBuffer metadata) {
                        if (metadata.getCount() != 0) {
                            Date fileDate = metadata.get(0).getModifiedDate();

                            String sfTime = sharedPreferences.getString("lastDbBackupTime", "Never");

                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                            Date date;

                            try {
                                date = format.parse(sfTime);
                            } catch (ParseException e) {
                                Toast.makeText(BackupActivity.this, "Some error occurred", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                                return;
                            }
                            try {

                                if (fileDate.after(date)) {
                                    searchDriveAndRestore();
                                } else {
                                    restoreWithDriveIdFromPref();
                                }
                            } catch (NullPointerException e) {
                                Toast.makeText(BackupActivity.this, "Some error occurred", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        } else {
                            restoreWithDriveIdFromPref();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });


                restoreWithDriveIdFromPref();

            } else {
                searchDriveAndRestore();
            }
        } else {
            Toast.makeText(this, "Storage permission is required to restore database", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_STORAGE);
            Toast.makeText(this, "Please allow storage permission", Toast.LENGTH_LONG).show();
        }

    }

    private void restoreWithDriveIdFromPref() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Restoring");
        progressDialog.show();

        String driveFileID = sharedPreferences.getString("dbBackupDriveFileID", "");

        DriveFile driveFile = DriveId.decodeFromString(driveFileID).asDriveFile();

        final Task<DriveContents> openFileTask = mDriveResourceClient.openFile(driveFile, DriveFile.MODE_READ_ONLY);

        openFileTask.continueWithTask(new Continuation<DriveContents, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                Log.d("RESTORE: ", "open File task");

                DriveContents driveContents = task.getResult();
                InputStream inputStream = driveContents.getInputStream();

                byte[] buf = new byte[8192];

                int c;
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    DatabaseHelper databaseHelper = DatabaseHelper.getInstance(BackupActivity.this);

                    Log.d("RESTORE: ", "External DIR mounted");

                    String databaseFullPath = getDatabasePath(DatabaseHelper.DATABASE_NAME).getAbsolutePath();

                    Log.d("RESTORE: ", databaseFullPath);


                    FileOutputStream outputStream;
                    outputStream = new FileOutputStream(databaseFullPath, false);

                    while ((c = inputStream.read(buf, 0, buf.length)) > 0) {
                        outputStream.write(buf, 0, c);
                        outputStream.flush();
                    }
                    outputStream.close();
                    progressDialog.dismiss();
                    Toast.makeText(BackupActivity.this, "Restore Successful", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(BackupActivity.this, "Some error occurred", Toast.LENGTH_SHORT).show();
                    Log.d("RESTORE: ", "External DIR not mounted");
                }

                return mDriveResourceClient.discardContents(driveContents);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Toast.makeText(BackupActivity.this, "Some error occurred", Toast.LENGTH_SHORT).show();
                Log.d("RESTORE: ", "Failed");

            }
        });
    }

    private void searchDriveAndRestore() {
        final Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, DatabaseHelper.DATABASE_NAME))
                .build();

        final Task<DriveFolder> appFolderTask = mDriveResourceClient.getAppFolder();

        appFolderTask.continueWithTask(new Continuation<DriveFolder, Task<MetadataBuffer>>() {
            @Override
            public Task<MetadataBuffer> then(@NonNull Task<DriveFolder> task) throws Exception {
                DriveFolder appFolder = appFolderTask.getResult();


                return mDriveResourceClient.queryChildren(appFolder, query);
            }
        }).addOnSuccessListener(new OnSuccessListener<MetadataBuffer>() {
            @Override
            public void onSuccess(MetadataBuffer metadata) {
                int count;
                count = metadata.getCount();
                if (count != 0) {
                    DriveId driveFileID = metadata.get(0).getDriveId();

                    DriveFile backupFile = driveFileID.asDriveFile();

                    final Task<DriveContents> openFileTask = mDriveResourceClient.openFile(backupFile, DriveFile.MODE_READ_ONLY);

                    openFileTask.continueWithTask(new Continuation<DriveContents, Task<Void>>() {
                        @Override
                        public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                            Log.d("RESTORE: ", "open File task");

                            DriveContents driveContents = task.getResult();

                            InputStream inputStream = driveContents.getInputStream();

                            byte[] buf = new byte[8192];

                            int c;
                            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                                DatabaseHelper databaseHelper = DatabaseHelper.getInstance(BackupActivity.this);

                                Log.d("RESTORE: ", "External DIR mounted");


                                String databaseFullPath = getDatabasePath(DatabaseHelper.DATABASE_NAME).getAbsolutePath();


                                Log.d("RESTORE: ", databaseFullPath);


                                FileOutputStream outputStream;
                                outputStream = new FileOutputStream(databaseFullPath, false);

                                while ((c = inputStream.read(buf, 0, buf.length)) > 0) {
                                    outputStream.write(buf, 0, c);
                                    outputStream.flush();
                                }
                                outputStream.close();
                                Toast.makeText(BackupActivity.this, "Restore Successful", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(BackupActivity.this, "Some error occurred", Toast.LENGTH_SHORT).show();
                                Log.d("RESTORE: ", "External DIR not mounted");
                            }

                            return mDriveResourceClient.discardContents(driveContents);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(BackupActivity.this, "Some error occurred", Toast.LENGTH_SHORT).show();
                            Log.d("RESTORE: ", "Failed");

                        }
                    });
                    metadata.release();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_APPFOLDER)
                        .build();
        return GoogleSignIn.getClient(this, signInOptions);
    }

    public void resetDatabase(View view) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Resetting");
        progressDialog.show();

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);
        if (databaseHelper.resetDatabase()) {
            databaseHelper.initiateTagTable();
            progressDialog.dismiss();
            Toast.makeText(this, "Reset Successful.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            progressDialog.dismiss();
            Toast.makeText(this, "Some error occurred. Try again Later", Toast.LENGTH_SHORT).show();
        }
    }

}
