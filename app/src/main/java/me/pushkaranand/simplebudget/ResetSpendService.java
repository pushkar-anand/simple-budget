package me.pushkaranand.simplebudget;

import android.app.IntentService;
import android.content.Intent;
import android.widget.Toast;


public class ResetSpendService extends IntentService {


    public ResetSpendService() {
        super("ResetSpendService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
        databaseHelper.resetSpends();
        Toast.makeText(this, "Spends have been reset", Toast.LENGTH_SHORT).show();
    }


}
