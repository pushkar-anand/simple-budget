package me.pushkaranand.simplebudget;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;


@SuppressWarnings("unused")
class SingleTransactionLoader extends AsyncTaskLoader
{
    private final Integer id;
    private final DatabaseHelper databaseHelper;
    private  Cursor res;

    SingleTransactionLoader(Context context, Integer id) {
        super(context);
        this.id = id;
        databaseHelper = DatabaseHelper.getInstance(context);
    }

    @Override
    public Transactions loadInBackground() {
        Transactions txn;
        res = databaseHelper.getData(id);
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

        return txn;
    }

    protected void onReleaseResources() {
        res.close();
    }
}
