package me.pushkaranand.simplebudget;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import static android.support.v4.app.NotificationCompat.DEFAULT_ALL;
import static android.support.v4.app.NotificationCompat.PRIORITY_MAX;

public class AddTransactionReminder extends IntentService {
    public static final String PREF = "simple-budget";

    public AddTransactionReminder() {
        super("AddTransactionReminder");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Service: ", "Service AddTransactionReminder Started");

        String msg = "Did you spend any money today??";

        Log.d("Notification: ", "Sending notification. TEXT: " + msg);

        final int NOTIFICATION_ID = 574;
        NotificationManager mNotificationManager;
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);


        Intent notificationIntent = new Intent(this, NewTransaction.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        NOTIFICATION_ID,
                        notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );


        NotificationCompat.Builder mBuilder =
                null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // The id of the channel.
            String id = "my_channel_01";

// The user-visible name of the channel.
            CharSequence name = "Reminder Channel";

// The user-visible description of the channel.
            String description = "Reminds to add daily spend";

            int importance = NotificationManager.IMPORTANCE_LOW;

            NotificationChannel mChannel = new NotificationChannel(id, name, importance);

// Configure the notification channel.
            mChannel.setDescription(description);

            mChannel.enableLights(true);
// Sets the notification light color for notifications posted to this
// channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);

            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            String CHANNEL_ID = "my_channel_01";

            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(mChannel);
            }
            mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_remind)
                    .setLargeIcon(BitmapFactory
                            .decodeResource(this.getResources(), R.drawable.ic_remind))
                    .setContentTitle("Add new transaction")
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(msg))
                    .setContentText(msg)
                    .setPriority(PRIORITY_MAX)
                    .setAutoCancel(true)
                    .setDefaults(DEFAULT_ALL)
                    .addAction(R.drawable.ic_add, "ADD NOW", resultPendingIntent);
        } else {
            mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_remind)
                    .setLargeIcon(BitmapFactory
                            .decodeResource(this.getResources(), R.drawable.ic_remind))
                    .setContentTitle("Add new transaction")
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(msg))
                    .setContentText(msg)
                    .setPriority(PRIORITY_MAX)
                    .setAutoCancel(true)
                    .setDefaults(DEFAULT_ALL)
                    .addAction(R.drawable.ic_add, "ADD NOW", resultPendingIntent);
        }

        if (mNotificationManager != null) {
            Log.d("Notification: ", "sent notification. TEXT: " + msg);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        } else {
            Log.d("Notification: ", "Unable to send. Mngr is null for AddTransactionReminder");
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Intent i = new Intent(this, LimitCheckerService.class);
        startService(i);
    }
}