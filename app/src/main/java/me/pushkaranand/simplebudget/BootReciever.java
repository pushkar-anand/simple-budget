package me.pushkaranand.simplebudget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;
import java.util.Objects;

public class BootReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), "android.intent.action.BOOT_COMPLETED")) {
            startRemindingService(context);
        }
    }

    private void startRemindingService(Context context) {
        Log.d("NOTIFIER: ", "Starting");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 21);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(context, AddTransactionReminder.class);

        PendingIntent pendingIntent = PendingIntent.getService(context,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
        } else {
            Log.e("NotifyAlarm", "alarmMngr is null");
        }
    }
}
