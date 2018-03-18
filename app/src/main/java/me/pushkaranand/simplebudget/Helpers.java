package me.pushkaranand.simplebudget;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;

class Helpers {
    static final String PREF = "simple-budget";
    static final Integer DAILY_REMINDER_INTENT = 100;

    static boolean isDebug() {
        if (BuildConfig.DEBUG) {
            Log.d("DEBUG: ", "In debug mode");
            return true;
        }
        return false;
    }

    static void setDailyReminderAlarm(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Helpers.PREF, Context.MODE_PRIVATE);
        Integer hour = sharedPreferences.getInt("DAILY_REMINDER_HOUR", 21);
        Integer min = sharedPreferences.getInt("DAILY_REMINDER_MIN", 0);

        Log.d("setDailyReminderAlarm: ", "Starting");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(context, AddTransactionReminder.class);

        PendingIntent pendingIntent = PendingIntent.getService(context,
                Helpers.DAILY_REMINDER_INTENT,
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
