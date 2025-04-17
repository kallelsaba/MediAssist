package com.example.mediassist;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class NotificationActionReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationActionReceiver";
    private static final int SNOOZE_TIME_MS = 10 * 60 * 1000; // 10 minutes

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int notificationId = intent.getIntExtra("notification_id", 0);

        Log.d(TAG, "Action received: " + action + " for notification ID: " + notificationId);

        if ("COMPLETE_ACTION".equals(action)) {
            // Marquer comme terminé et fermer la notification
            dismissNotification(context, notificationId);
            Toast.makeText(context, "Marked as completed", Toast.LENGTH_SHORT).show();
        } else if ("SNOOZE_ACTION".equals(action)) {
            // Reporter la notification
            dismissNotification(context, notificationId);

            // Récupérer les données pour recréer la notification plus tard
            String title = intent.getStringExtra("title");
            String note = intent.getStringExtra("note");
            String time = intent.getStringExtra("time");
            String type = intent.getStringExtra("type");

            // Programmer une nouvelle notification
            scheduleSnoozeNotification(context, notificationId, title, note, time, type);

            Toast.makeText(context, "Snoozed for 10 minutes", Toast.LENGTH_SHORT).show();
        }
    }

    private void dismissNotification(Context context, int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }

    private void scheduleSnoozeNotification(Context context, int notificationId, String title, String note, String time, String type) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("note", note);
        intent.putExtra("time", time);
        intent.putExtra("type", type);
        intent.putExtra("id", notificationId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            long triggerTime = SystemClock.elapsedRealtime() + SNOOZE_TIME_MS;

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            triggerTime,
                            pendingIntent
                    );
                } else {
                    alarmManager.setExact(
                            AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            triggerTime,
                            pendingIntent
                    );
                }
            } catch (SecurityException e) {
                Log.e(TAG, "SecurityException: " + e.getMessage());
                // Fallback to inexact alarm
                alarmManager.set(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            }
        }
    }
}