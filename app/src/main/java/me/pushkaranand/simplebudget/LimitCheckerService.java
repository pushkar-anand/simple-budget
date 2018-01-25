package me.pushkaranand.simplebudget;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;

import static android.support.v4.app.NotificationCompat.DEFAULT_ALL;
import static android.support.v4.app.NotificationCompat.PRIORITY_MAX;


public class LimitCheckerService extends IntentService {
    public LimitCheckerService() {
        super("LimitCheckerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);

        ArrayList<Integer> tagIds = databaseHelper.ListTagIds();

        for (Integer tagID : tagIds) {
            Double d;
            Cursor res = databaseHelper.getTagData(tagID);
            res.moveToFirst();

            String name = res.getString(res.getColumnIndex(DatabaseHelper.TAG_COLUMN_NAME_NAME));
            d = res.getDouble(res.getColumnIndex(DatabaseHelper.TAG_COLUMN_NAME_LIMIT));

            Double spend = res.getDouble(res.getColumnIndex(DatabaseHelper.TAG_COLUMN_NAME_SPEND));
            Double limit = (d != 0) ? d : null;

            if (limit != null) {
                Double percentage = (spend / limit) * 100;
                if (percentage > 80) {
                    sendNotification(name, tagID);
                }
            }
            res.close();
        }
    }

    private void sendNotification(String tagName, Integer notificationId) {
        Log.d("Notification: ", "Sending notification for " + tagName);

        String msg = "You have spent more than 80% for " + tagName;

        Log.d("Notification: ", "Sending notification. TEXT: " + msg);

        NotificationManager mNotificationManager;
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);


        NotificationCompat.Builder mBuilder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            String id = "my_channel_02";

            CharSequence name = "Tracker Channel";

            String description = "Reminds when user spend is nearing limit";

            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel mChannel = new NotificationChannel(id, name, importance);

            mChannel.setDescription(description);

            mChannel.enableLights(true);

            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            String CHANNEL_ID = "my_channel_02";

            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(mChannel);
            }
            mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_limit)
                    .setLargeIcon(BitmapFactory
                            .decodeResource(this.getResources(), R.drawable.ic_limit))
                    .setContentTitle("Reaching Limit for " + tagName)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(msg))
                    .setContentText(msg)
                    .setPriority(PRIORITY_MAX)
                    .setAutoCancel(true)
                    .setDefaults(DEFAULT_ALL);
        } else {
            mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_limit)
                    .setLargeIcon(BitmapFactory
                            .decodeResource(this.getResources(), R.drawable.ic_limit))
                    .setContentTitle("Reaching Limit for " + tagName)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(msg))
                    .setContentText(msg)
                    .setPriority(PRIORITY_MAX)
                    .setAutoCancel(true)
                    .setDefaults(DEFAULT_ALL);
        }

        if (mNotificationManager != null) {
            Log.d("Notification: ", "sent notification. TEXT: " + msg);
            mNotificationManager.notify(notificationId, mBuilder.build());
        } else {
            Log.d("Notification: ", "Unable to send. Mngr is null for LimitChecker");
        }
    }


}
