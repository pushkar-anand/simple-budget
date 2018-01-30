package me.pushkaranand.simplebudget;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

class ResetSpends extends AsyncTask<Context, Void, Void> {

    @Override
    protected Void doInBackground(Context... contexts) {

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(contexts[0]);
        databaseHelper.resetSpends();
        Toast.makeText(contexts[0], "Spends have been reset", Toast.LENGTH_SHORT).show();
        return null;
    }
}
