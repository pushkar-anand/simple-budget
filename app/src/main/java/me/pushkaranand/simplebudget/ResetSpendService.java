package me.pushkaranand.simplebudget;

import android.app.IntentService;
import android.content.Intent;


public class ResetSpendService extends IntentService {


    public ResetSpendService() {
        super("ResetSpendService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);
        databaseHelper.resetSpends();
    }


}
