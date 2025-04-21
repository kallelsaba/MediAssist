package com.example.mediassist;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";
    private static final String CHANNEL_ID = "MediAssistChannel";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "NotificationReceiver.onReceive called");

        createNotificationChannel(context);

        // Extraire les données
        String title = intent.getStringExtra("title");
        String note = intent.getStringExtra("note");
        String time = intent.getStringExtra("time");
        String type = intent.getStringExtra("type");
        int id = intent.getIntExtra("id", 0);

        Log.d(TAG, "Showing notification for: " + title + " at " + time + " (ID: " + id + ")");

        // ✅ Intent vers l'interface personnalisée NotificationDetailActivity
        Intent notificationIntent = new Intent(context, NotificationDetailActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationIntent.putExtra("medicationName", title);
        notificationIntent.putExtra("instructions", note);
        notificationIntent.putExtra("time", time);
        notificationIntent.putExtra("imageRes", R.drawable.doctor); // Change selon ton besoin

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                id,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Intent action "Complete"
        Intent completeIntent = new Intent(context, NotificationActionReceiver.class);
        completeIntent.setAction("COMPLETE_ACTION");
        completeIntent.putExtra("notification_id", id);
        PendingIntent completePendingIntent = PendingIntent.getBroadcast(
                context,
                id,
                completeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Intent action "Snooze"
        Intent snoozeIntent = new Intent(context, NotificationActionReceiver.class);
        snoozeIntent.setAction("SNOOZE_ACTION");
        snoozeIntent.putExtra("notification_id", id);
        snoozeIntent.putExtra("title", title);
        snoozeIntent.putExtra("note", note);
        snoozeIntent.putExtra("time", time);
        snoozeIntent.putExtra("type", type);

        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                id + 1000,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Son et icône
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Bitmap largeIcon = "medication".equals(type)
                ? BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_medication)
                : null;

        // Création de la notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(note != null ? note : "Il est temps de prendre votre médicament")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setSound(defaultSoundUri)
                .setVibrate(new long[]{0, 500, 250, 500})
                .setAutoCancel(true)
                .addAction(R.drawable.ic_check, "Complete", completePendingIntent)
                .addAction(R.drawable.ic_snooze, "Snooze 10 min", snoozePendingIntent);

        if (largeIcon != null) {
            builder.setLargeIcon(largeIcon);
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(id, builder.build());
            Log.d(TAG, "Notification displayed with ID: " + id);
        } else {
            Log.e(TAG, "NotificationManager is null");
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "MediAssist Reminders";
            String description = "Channel for medication and appointment reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 250, 500});

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created");
            }
        }
    }
}
