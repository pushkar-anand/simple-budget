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


@SuppressWarnings({"unused", "SameReturnValue"})
class DatabaseHelper extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "simple-budget.db";
    private static final int DATABASE_VERSION = 1;
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

    boolean resetDatabase() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_TAG);

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

        sqLiteDatabase.execSQL(createQuery);

        createQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_TAG
                + "("
                + TAG_COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TAG_COLUMN_NAME_NAME + " TEXT  NOT NULL, "
                + TAG_COLUMN_NAME_SPEND + " REAL  NOT NULL, "
                + TAG_COLUMN_NAME_LIMIT + " REAL"
                + ")"
        ;

        Log.d("SQL_CREATE: ", createQuery);
        sqLiteDatabase.execSQL(createQuery);
        return true;
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

    boolean newTransaction(String type, Double amount, String category, String date, Integer year, String month, @Nullable String notes, Integer tag_id) {
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

    void updateTransaction(Integer id, String type, Double amount, String category, String date, Integer year, String month, @Nullable String notes, String old_category, Double old_amount, Integer tag_id, Integer old_tag_id) {
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

        if (!Objects.equals(old_category, category) && !Objects.equals(old_amount, amount)) {
            updateTagSpend(old_tag_id, -old_amount);
            updateTagSpend(tag_id, amount);
        } else if (!Objects.equals(old_category, category) && Objects.equals(old_amount, amount)) {
            updateTagSpend(tag_id, amount);
            updateTagSpend(old_tag_id, -amount);
        } else if (Objects.equals(old_category, category) && !Objects.equals(old_amount, amount)) {
            updateTagSpend(tag_id, amount - old_amount);
        }

    }


    Integer deleteTransaction(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Integer n;

        Cursor cursor = getData(id);
        cursor.moveToFirst();
        String tName = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_CATEGORY));
        Double amt = cursor.getDouble(cursor.getColumnIndex(COLUMN_NAME_AMOUNT));
        String type = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_TYPE));

        cursor.close();

        n = db.delete(TABLE_NAME, COLUMN_NAME_ID + " = ?", new String[]{Integer.toString(id)});

        if (Objects.equals(type, "DEBIT")) {

            String query = "SELECT * FROM " + TABLE_NAME_TAG + " WHERE " + TAG_COLUMN_NAME_NAME + "=\"" + tName + "\"";

            cursor = db.rawQuery(query, null);
            cursor.moveToFirst();
            Integer ix = cursor.getInt(cursor.getColumnIndex(TAG_COLUMN_NAME_ID));
            updateTagSpend(ix, -amt);
            cursor.close();
        }

        return n;
    }

    ArrayList<Integer> ListTransactionIds() {
        ArrayList<Integer> array_list = new ArrayList<>();

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
        double Balance, Credit = 0, Debit = 0;

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

    void initiateTagTable() {
        newTag("Food and Dining", 0.0, (double) 0);
        newTag("Entertainment", 0.0, (double) 0);
        newTag("Transportation", 0.0, (double) 0);
        newTag("Stationary", 0.0, (double) 0);
        newTag("Rations", 0.0, (double) 0);
        newTag("Shopping", 0.0, (double) 0);
        newTag("Salary", 0.0, (double) 0);
        newTag("Bills and Utilities", 0.0, (double) 0);
        newTag("Gifts and Donation", 0.0, (double) 0);
        newTag("Health and Fitness", 0.0, (double) 0);
        newTag("Personal", 0.0, (double) 0);
    }


    void newTag(String name, Double spend, @Nullable Double limit) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(TAG_COLUMN_NAME_NAME, name);
        contentValues.put(TAG_COLUMN_NAME_SPEND, spend);
        contentValues.put(TAG_COLUMN_NAME_LIMIT, limit);

        db.insert(TABLE_NAME_TAG, null, contentValues);
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


    void updateTag(Integer id, String name, Double spend, @Nullable Double limit) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(TAG_COLUMN_NAME_NAME, name);
        contentValues.put(TAG_COLUMN_NAME_SPEND, spend);
        contentValues.put(TAG_COLUMN_NAME_LIMIT, limit);

        db.update(TABLE_NAME_TAG, contentValues, TAG_COLUMN_NAME_ID + " = ?", new String[]{Integer.toString(id)});
    }

    public Integer deleteTag(Integer id) {

        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME_TAG, TAG_COLUMN_NAME_ID + " = ?", new String[]{Integer.toString(id)});
    }


    ArrayList<Integer> ListTagIds() {
        ArrayList<Integer> array_list = new ArrayList<>();

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


    Double getTagSpend(Integer tag_id) {
        Cursor res = this.getTagData(tag_id);
        res.moveToFirst();
        Double spend = res.getDouble(res.getColumnIndex(DatabaseHelper.TAG_COLUMN_NAME_SPEND));
        res.close();
        return spend;
    }

    Double getTagLimit(Integer tag_id) {
        Cursor res = this.getTagData(tag_id);
        res.moveToFirst();
        Double limit = res.getDouble(res.getColumnIndex(DatabaseHelper.TAG_COLUMN_NAME_LIMIT));
        res.close();
        return limit;
    }

    void updateTagSpend(Integer tag_id, Double new_spend) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TAG_COLUMN_NAME_SPEND, new_spend + getTagSpend(tag_id));

        db.update(TABLE_NAME_TAG, contentValues, TAG_COLUMN_NAME_ID + " = ?", new String[]{Integer.toString(tag_id)});
    }

    private void resetTagSpend(Integer tag_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TAG_COLUMN_NAME_SPEND, 0);

        db.update(TABLE_NAME_TAG, contentValues, TAG_COLUMN_NAME_ID + " = ?", new String[]{Integer.toString(tag_id)});
    }

    void resetSpends() {
        ArrayList<Integer> ids = ListTagIds();

        for (Integer id : ids) {
            resetTagSpend(id);
        }
    }


}