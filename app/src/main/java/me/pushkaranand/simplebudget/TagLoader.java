package me.pushkaranand.simplebudget;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

class TagLoader extends AsyncTaskLoader<List<Tags>>
{
    private DatabaseHelper dbHelper;
    private ArrayList<Integer> ids;
    private Cursor res;
    private Tags tags;

    TagLoader(Context context)
    {
        super(context);
        dbHelper = DatabaseHelper.getInstance(getContext());
    }

    @Override
    public List<Tags> loadInBackground()
    {
        Log.d("TaskLoader_TAGS: ", "Started working");
        List<Tags> tagsList  = new ArrayList<Tags>();
        Tags n;

        ids = dbHelper.ListTagIds();
        Log.d("TAGS: ", String.valueOf(ids.size()));
        for (Integer id : ids) {
            Integer i;
            String name;
            Double spend, limit;
            res = dbHelper.getTagData(id);
            res.moveToFirst();

            id = res.getInt(res.getColumnIndex(DatabaseHelper.TAG_COLUMN_NAME_ID));
            name = res.getString(res.getColumnIndex(DatabaseHelper.TAG_COLUMN_NAME_NAME));
            spend = res.getDouble(res.getColumnIndex(DatabaseHelper.TAG_COLUMN_NAME_SPEND));
            limit = res.getDouble(res.getColumnIndex(DatabaseHelper.TAG_COLUMN_NAME_LIMIT));

            n = new Tags(id, name, spend,limit);

            tagsList.add(n);
        }
        return tagsList;
    }

    protected void onReleaseResources() {
        res.close();
    }
}
