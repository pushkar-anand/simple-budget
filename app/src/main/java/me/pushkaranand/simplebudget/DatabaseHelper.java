package me.pushkaranand.simplebudget;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Objects;


class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "simple-budget.db";
    private static final String TABLE_NAME = "transactions";
    private static final String TABLE_NAME_TAG = "tags";
    private static final String NAME_PREFIX = "transaction_";

    static final String COLUMN_NAME_ID = NAME_PREFIX + "id";
    static final String COLUMN_NAME_TYPE = NAME_PREFIX + "type";
    static final String COLUMN_NAME_AMOUNT = NAME_PREFIX + "amount";
    static final String COLUMN_NAME_CATEGORY = NAME_PREFIX + "category";
    static final String COLUMN_NAME_DATE = NAME_PREFIX + "date";
    static final String COLUMN_NAME_NOTES = NAME_PREFIX + "notes";
    private static final String COLUMN_NAME_YEAR = NAME_PREFIX + "year";
    private static final String COLUMN_NAME_MONTH = NAME_PREFIX + "month";
    private static final String NAME_PREFIX_TAG = "tag_";
    static final String TAG_COLUMN_NAME_ID = NAME_PREFIX_TAG + "id";
    static final String TAG_COLUMN_NAME_NAME = NAME_PREFIX_TAG + "name";
    static final String TAG_COLUMN_NAME_SPEND = NAME_PREFIX_TAG + "spend";
    static final String TAG_COLUMN_NAME_LIMIT = NAME_PREFIX_TAG + "limit";
    private static DatabaseHelper sInstance;


    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    static synchronized DatabaseHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME
                + "("
                + COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME_TYPE + " TEXT  NOT NULL, "
                + COLUMN_NAME_AMOUNT + " REAL  NOT NULL, "
                + COLUMN_NAME_CATEGORY + " TEXT  NOT NULL, "
                + COLUMN_NAME_DATE + " TEXT  NOT NULL, "
                + COLUMN_NAME_YEAR + " INT  NOT NULL, "
                + COLUMN_NAME_MONTH + " TEXT  NOT NULL, "
                + COLUMN_NAME_NOTES + " TEXT"
                + ")";

        Log.d("SQL_CREATE: ", createQuery);

        db.execSQL(createQuery);

        createQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_TAG
                + "("
                + TAG_COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TAG_COLUMN_NAME_NAME + " TEXT  NOT NULL, "
                + TAG_COLUMN_NAME_SPEND + " REAL  NOT NULL, "
                + TAG_COLUMN_NAME_LIMIT + " REAL"
                + ")"
        ;
        Log.d("SQL_CREATE: ", createQuery);
        db.execSQL(createQuery);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String deleteQuery = "DROP TABLE IF EXISTS" + TABLE_NAME;
        db.execSQL(deleteQuery);
        deleteQuery = "DROP TABLE IF EXISTS" + TABLE_NAME_TAG;
        db.execSQL(deleteQuery);
        onCreate(db);
    }

    //transaction related

    boolean newTransaction(String type, Double amount, String category, String date, Integer year, String month, @Nullable String notes, Integer tag_id)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_NAME_TYPE, type);
        contentValues.put(COLUMN_NAME_AMOUNT, amount);
        contentValues.put(COLUMN_NAME_CATEGORY, category);
        contentValues.put(COLUMN_NAME_DATE, date);
        contentValues.put(COLUMN_NAME_YEAR, year);
        contentValues.put(COLUMN_NAME_MONTH, month);
        contentValues.put(COLUMN_NAME_NOTES, notes);

        db.insert(TABLE_NAME, null, contentValues);

        if (type.equals("DEBIT")) {
            updateTagSpend(tag_id, amount);
        }

        return true;
    }

    Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME_ID + "=" + id + "";

        return db.rawQuery(query, null);
    }

    public int numberOfTransactions() {
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, TABLE_NAME);
    }

    public boolean updateTransaction(Integer id, String type, double amount, String category, String date, Integer year, String month, @Nullable String notes) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_NAME_TYPE, type);
        contentValues.put(COLUMN_NAME_AMOUNT, amount);
        contentValues.put(COLUMN_NAME_CATEGORY, category);
        contentValues.put(COLUMN_NAME_DATE, date);
        contentValues.put(COLUMN_NAME_YEAR, year);
        contentValues.put(COLUMN_NAME_MONTH, month);
        contentValues.put(COLUMN_NAME_NOTES, notes);

        db.update(TABLE_NAME, contentValues, COLUMN_NAME_ID + " = ?", new String[]{Integer.toString(id)});

        return true;
    }

    public Integer deleteTransaction(Integer id) {

        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, COLUMN_NAME_ID + " = ?", new String[]{Integer.toString(id)});
    }

    ArrayList<Integer> ListTransactionIds() {
        ArrayList<Integer> array_list = new ArrayList<Integer>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor res = db.rawQuery(query, null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            array_list.add(res.getInt(res.getColumnIndex(COLUMN_NAME_ID)));
            res.moveToNext();
        }
        res.close();
        return array_list;
    }

    public double getAvailableBalance() {
        double Balance = 0, Credit = 0, Debit = 0;

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor res = db.rawQuery(query, null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            if (Objects.equals(res.getString(res.getColumnIndex(COLUMN_NAME_TYPE)), "CREDIT")) {
                Credit += res.getDouble(res.getColumnIndex(COLUMN_NAME_AMOUNT));
            } else if (Objects.equals(res.getString(res.getColumnIndex(COLUMN_NAME_TYPE)), "DEBIT")) {
                Debit += res.getDouble(res.getColumnIndex(COLUMN_NAME_AMOUNT));
            }
        }
        res.close();


        Balance = Credit - Debit;
        return Balance;
    }


    boolean newTag(String name, Double spend, @Nullable Double limit) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(TAG_COLUMN_NAME_NAME, name);
        contentValues.put(TAG_COLUMN_NAME_SPEND, spend);
        contentValues.put(TAG_COLUMN_NAME_LIMIT, limit);

        db.insert(TABLE_NAME_TAG, null, contentValues);
        return true;
    }


    Cursor getTagData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME_TAG + " WHERE " + TAG_COLUMN_NAME_ID + "=" + id + "";

        return db.rawQuery(query, null);
    }

    public int numberOfTags() {
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, TABLE_NAME_TAG);
    }


    boolean updateTag(Integer id, String name, Double spend, @Nullable Double limit) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(TAG_COLUMN_NAME_NAME, name);
        contentValues.put(TAG_COLUMN_NAME_SPEND, spend);
        contentValues.put(TAG_COLUMN_NAME_LIMIT, limit);

        db.update(TABLE_NAME_TAG, contentValues, TAG_COLUMN_NAME_ID + " = ?", new String[]{Integer.toString(id)});
        return true;
    }


    public Integer deleteTag(Integer id) {

        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME_TAG, TAG_COLUMN_NAME_ID + " = ?", new String[]{Integer.toString(id)});
    }


    ArrayList<Integer> ListTagIds() {
        ArrayList<Integer> array_list = new ArrayList<Integer>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME_TAG;
        Cursor res = db.rawQuery(query, null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            array_list.add(res.getInt(res.getColumnIndex(TAG_COLUMN_NAME_ID)));
            res.moveToNext();
        }
        res.close();
        return array_list;
    }

    private Double getTagSpend(Integer tag_id) {
        Cursor res = this.getTagData(tag_id);
        res.moveToFirst();
        Double spend = res.getDouble(res.getColumnIndex(DatabaseHelper.TAG_COLUMN_NAME_SPEND));
        res.close();
        return spend;
    }

    void updateTagSpend(Integer tag_id, Double new_spend) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TAG_COLUMN_NAME_SPEND, new_spend + getTagSpend(tag_id));

        db.update(TABLE_NAME_TAG, contentValues, TAG_COLUMN_NAME_ID + " = ?", new String[]{Integer.toString(tag_id)});
    }
}