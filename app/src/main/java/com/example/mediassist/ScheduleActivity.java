package com.example.mediassist;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ScheduleActivity extends AppCompatActivity {

    private static final String TAG = "ScheduleActivity";
    private static final String CHANNEL_ID = "MediAssistChannel";

    private CalendarView calendarView;
    private LinearLayout reminderContainer;
    private TextView tvNoReminders;
    private TextView tvMonthYear;
    private FloatingActionButton fabAddEvent;
    private FloatingActionButton fabTestNotification;
    private DatabaseHelper dbHelper;
    private long userId = 1; // Valeur par défaut pour les tests
    private String selectedDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        Log.d(TAG, "ScheduleActivity onCreate started");

        // Initialisation de la base de données
        dbHelper = new DatabaseHelper(this);

        // Initialisation des vues
        ImageView btnBack = findViewById(R.id.btnBack);
        calendarView = findViewById(R.id.calendarView);
        reminderContainer = findViewById(R.id.reminderContainer);
        tvNoReminders = findViewById(R.id.tvNoReminders);
        tvMonthYear = findViewById(R.id.tvMonthYear);
        fabAddEvent = findViewById(R.id.fabAddEvent);
        fabTestNotification = findViewById(R.id.fabTestNotification);

        // Créer le canal de notification
        createNotificationChannel();

        // Définir la date initiale du calendrier (aujourd'hui)
        Calendar calendar = Calendar.getInstance();
        selectedDate = dateFormat.format(calendar.getTime());
        calendarView.setDate(calendar.getTimeInMillis());

        Log.d(TAG, "Selected date initialized to: " + selectedDate);

        // Mettre à jour le titre avec le mois et l'année actuels
        tvMonthYear.setText(monthYearFormat.format(calendar.getTime()));

        // Gestion du bouton retour
        btnBack.setOnClickListener(v -> finish());

        // Gestion du bouton d'ajout d'événement
        fabAddEvent.setOnClickListener(v -> showAddEventDialog());

        // Gestion du bouton de test de notification
        fabTestNotification.setOnClickListener(v -> testNotification());

        // Gérer la sélection d'une date dans le calendrier
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar newCal = Calendar.getInstance();
            newCal.set(year, month, dayOfMonth);
            selectedDate = dateFormat.format(newCal.getTime());

            Log.d(TAG, "Date selected: " + selectedDate);

            // Mettre à jour les rappels pour la date sélectionnée
            updateReminders(selectedDate);

            // Mettre à jour le titre si le mois change
            if (calendar.get(Calendar.MONTH) != newCal.get(Calendar.MONTH) ||
                    calendar.get(Calendar.YEAR) != newCal.get(Calendar.YEAR)) {
                calendar.setTime(newCal.getTime());
                tvMonthYear.setText(monthYearFormat.format(calendar.getTime()));
            }
        });

        Log.d(TAG, "ScheduleActivity onCreate completed");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "ScheduleActivity onResume");

        // Recharger les rappels à chaque fois que l'activité devient visible
        updateReminders(selectedDate);
    }

    private void createNotificationChannel() {
        // Créer le canal de notification pour Android 8.0 et versions ultérieures
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "MediAssist Reminders";
            String description = "Channel for medication and appointment reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);

            // Enregistrer le canal auprès du système
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created");
            }
        }
    }

    private void updateReminders(String date) {
        Log.d(TAG, "Updating reminders for date: " + date);

        // Vider le conteneur de rappels
        reminderContainer.removeAllViews();

        // Récupérer tous les événements pour cette date
        List<ScheduleEvent> events = new ArrayList<>();

        // 1. Récupérer les médicaments
        List<Medication> medications = dbHelper.getAllMedications(userId);
        for (Medication med : medications) {
            ScheduleEvent event = new ScheduleEvent();
            event.setId(med.getId());
            event.setTitle(med.getName());
            event.setTime(med.getTime());
            event.setDate(date); // Tous les médicaments sont affichés pour chaque jour
            event.setNote("Dosage: " + med.getDosage());
            event.setType("medication");
            event.setColorRes(R.drawable.circle_purple);
            events.add(event);

            // Programmer une notification pour ce médicament
            scheduleNotification(event);
        }

        // 2. Récupérer les rendez-vous pour cette date spécifique
        List<Appointment> appointments = dbHelper.getAllAppointments(userId);
        for (Appointment appt : appointments) {
            if (date.equals(appt.getDate())) {
                ScheduleEvent event = new ScheduleEvent();
                event.setId(appt.getId());
                event.setTitle(appt.getTitle());
                // Utiliser une heure par défaut si non spécifiée
                event.setTime(appt.getDate().contains(" ") ?
                        appt.getDate().split(" ")[1] : "09:00");
                event.setDate(date);
                event.setNote(appt.getDetails());
                event.setType("appointment");

                // Définir la couleur en fonction de la catégorie
                if ("doctor".equalsIgnoreCase(appt.getCategory())) {
                    event.setColorRes(R.drawable.circle_blue);
                } else if ("analysis".equalsIgnoreCase(appt.getCategory())) {
                    event.setColorRes(R.drawable.circle_green);
                } else {
                    event.setColorRes(R.drawable.circle_blue);
                }

                events.add(event);

                // Programmer une notification pour ce rendez-vous
                scheduleNotification(event);
            }
        }

        Log.d(TAG, "Found " + events.size() + " events for date " + date);

        if (!events.isEmpty()) {
            tvNoReminders.setVisibility(View.GONE);

            // Trier les événements par heure
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                events.sort((e1, e2) -> {
                    try {
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        Date time1 = timeFormat.parse(e1.getTime());
                        Date time2 = timeFormat.parse(e2.getTime());
                        return time1.compareTo(time2);
                    } catch (ParseException e) {
                        return 0;
                    }
                });
            }

            for (ScheduleEvent event : events) {
                View reminderView = LayoutInflater.from(this).inflate(R.layout.item_reminder, reminderContainer, false);

                View colorIndicator = reminderView.findViewById(R.id.colorIndicator);
                TextView tvTime = reminderView.findViewById(R.id.tvTime);
                TextView tvTitle = reminderView.findViewById(R.id.tvTitle);
                TextView tvNote = reminderView.findViewById(R.id.tvNote);
                ImageView ivMore = reminderView.findViewById(R.id.ivMore);

                colorIndicator.setBackgroundResource(event.getColorRes());
                tvTime.setText(event.getTime());
                tvTitle.setText(event.getTitle());

                if (event.getNote() != null && !event.getNote().isEmpty()) {
                    tvNote.setText(event.getNote());
                    tvNote.setVisibility(View.VISIBLE);
                } else {
                    tvNote.setVisibility(View.GONE);
                }

                // Configurer le bouton "more" pour afficher plus d'informations
                ivMore.setOnClickListener(v -> showEventDetails(event));

                reminderContainer.addView(reminderView);

                // Ajouter un espace entre les rappels
                View spacer = new View(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        (int) getResources().getDimension(R.dimen.reminder_spacing));
                reminderContainer.addView(spacer, params);
            }

            // Supprimer le dernier espace s'il existe
            if (reminderContainer.getChildCount() > 0) {
                reminderContainer.removeViewAt(reminderContainer.getChildCount() - 1);
            }
        } else {
            tvNoReminders.setVisibility(View.VISIBLE);
        }
    }

    private void showEventDetails(ScheduleEvent event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_reminder_details, null);

        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvDialogTime = dialogView.findViewById(R.id.tvDialogTime);
        TextView tvDialogNote = dialogView.findViewById(R.id.tvDialogNote);
        TextView tvDialogDescription = dialogView.findViewById(R.id.tvDialogDescription);
        View colorIndicator = dialogView.findViewById(R.id.dialogColorIndicator);

        tvDialogTitle.setText(event.getTitle());
        tvDialogTime.setText("Time: " + event.getTime());
        colorIndicator.setBackgroundResource(event.getColorRes());

        if (event.getNote() != null && !event.getNote().isEmpty()) {
            tvDialogNote.setText("Note: " + event.getNote());
            tvDialogNote.setVisibility(View.VISIBLE);
        } else {
            tvDialogNote.setVisibility(View.GONE);
        }

        // Description supplémentaire (exemple)
        String description = getDescriptionForEvent(event);
        if (description != null) {
            tvDialogDescription.setText(description);
            tvDialogDescription.setVisibility(View.VISIBLE);
        } else {
            tvDialogDescription.setVisibility(View.GONE);
        }

        builder.setView(dialogView);
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

        // Option pour supprimer l'événement
        builder.setNegativeButton("Delete", (dialog, which) -> {
            deleteEvent(event);
            dialog.dismiss();
        });

        // Option pour configurer une notification
        builder.setNeutralButton("Test Notification", (dialog, which) -> {
            showNotification(event);
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String getDescriptionForEvent(ScheduleEvent event) {
        // Exemple de descriptions supplémentaires pour certains types d'événements
        if ("medication".equals(event.getType())) {
            return "Taking your medication at the scheduled time ensures consistent levels in your bloodstream for optimal effectiveness.";
        } else if ("appointment".equals(event.getType())) {
            if (event.getTitle().toLowerCase().contains("doctor")) {
                return "Regular check-ups with your doctor help monitor your overall health and adjust your treatment plan as needed.";
            } else if (event.getTitle().toLowerCase().contains("blood") ||
                    event.getTitle().toLowerCase().contains("analysis")) {
                return "Regular blood tests help track your health indicators and adjust treatment as needed.";
            }
        }
        return null;
    }

    private void deleteEvent(ScheduleEvent event) {
        try {
            boolean success = false;

            if ("medication".equals(event.getType())) {
                success = dbHelper.deleteMedication(event.getId());
            } else if ("appointment".equals(event.getType())) {
                success = dbHelper.deleteAppointment(event.getId());
            }

            if (success) {
                Toast.makeText(this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                // Recharger les événements et mettre à jour l'affichage
                updateReminders(selectedDate);
            } else {
                Toast.makeText(this, "Failed to delete event", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting event", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void scheduleNotification(ScheduleEvent event) {
        try {
            // Créer l'intent pour la notification
            Intent intent = new Intent(this, NotificationReceiver.class);
            intent.putExtra("title", event.getTitle());
            intent.putExtra("note", event.getNote());
            intent.putExtra("time", event.getTime());
            intent.putExtra("type", event.getType());
            intent.putExtra("id", (int) event.getId());

            // Créer un PendingIntent unique pour cette notification
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    (int) event.getId(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Calculer le temps pour la notification
            Calendar calendar = Calendar.getInstance();
            try {
                // Extraire l'heure et les minutes
                String[] timeParts = event.getTime().split(":");
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);

                // Configurer le calendrier avec la date et l'heure de l'événement
                calendar.setTime(new Date());
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);

                // Si l'heure est déjà passée aujourd'hui, programmer pour demain
                if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing time: " + event.getTime(), e);
                // En cas d'erreur, utiliser l'heure actuelle + 1 minute
                calendar.add(Calendar.MINUTE, 1);
            }

            Log.d(TAG, "Scheduling notification for " + event.getTitle() + " at " +
                    calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) +
                    " on " + calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.MONTH) + 1));

            // Configurer l'alarme
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                try {
                    // Vérifier si l'application peut programmer des alarmes exactes (Android 12+)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (!alarmManager.canScheduleExactAlarms()) {
                            Log.w(TAG, "Permission to schedule exact alarms not granted");
                            // Utiliser une alarme inexacte comme solution de repli
                            alarmManager.set(
                                    AlarmManager.RTC_WAKEUP,
                                    calendar.getTimeInMillis(),
                                    pendingIntent
                            );
                            return;
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                calendar.getTimeInMillis(),
                                pendingIntent
                        );
                        Log.d(TAG, "Notification scheduled with setExactAndAllowWhileIdle");
                    } else {
                        alarmManager.setExact(
                                AlarmManager.RTC_WAKEUP,
                                calendar.getTimeInMillis(),
                                pendingIntent
                        );
                        Log.d(TAG, "Notification scheduled with setExact");
                    }
                } catch (SecurityException e) {
                    Log.e(TAG, "SecurityException: " + e.getMessage());
                    // Utiliser une alarme inexacte comme solution de repli
                    alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                    Log.d(TAG, "Notification scheduled with set (fallback)");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling notification", e);
        }
    }

    private void showAddEventDialog() {
        // Dans une implémentation réelle, vous créeriez un dialogue avec des onglets
        // pour ajouter soit un médicament, soit un rendez-vous
        // Pour simplifier, nous allons juste rediriger vers les activités existantes

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Event");
        String[] options = {"Add Medication", "Add Appointment"};

        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Add Medication
                    Intent medicationIntent = new Intent(ScheduleActivity.this, AddMedicationActivity.class);
                    medicationIntent.putExtra("USER_ID", userId);
                    startActivity(medicationIntent);
                    break;
                case 1: // Add Appointment
                    Intent appointmentIntent = new Intent(ScheduleActivity.this, AddAppointmentActivity.class);
                    appointmentIntent.putExtra("USER_ID", userId);
                    // Passer la date sélectionnée
                    appointmentIntent.putExtra("SELECTED_DATE", selectedDate);
                    startActivity(appointmentIntent);
                    break;
            }
        });

        builder.show();
    }

    // Méthode pour tester une notification immédiatement
    private void testNotification() {
        Log.d(TAG, "Testing notification");

        // Créer un événement de test
        ScheduleEvent testEvent = new ScheduleEvent();
        testEvent.setId(9999);
        testEvent.setTitle("Test Notification");
        testEvent.setNote("This is a test notification");
        testEvent.setTime(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
        testEvent.setDate(selectedDate);
        testEvent.setType("medication");
        testEvent.setColorRes(R.drawable.circle_purple);

        // Afficher la notification immédiatement
        showNotification(testEvent);

        Toast.makeText(this, "Test notification sent", Toast.LENGTH_SHORT).show();
    }

    // Méthode pour afficher une notification immédiatement
    private void showNotification(ScheduleEvent event) {
        Intent intent = new Intent(this, ScheduleActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Créer l'intent pour l'action "Compléter"
        Intent completeIntent = new Intent(this, NotificationActionReceiver.class);
        completeIntent.setAction("COMPLETE_ACTION");
        completeIntent.putExtra("notification_id", (int) event.getId());
        PendingIntent completePendingIntent = PendingIntent.getBroadcast(
                this,
                (int) event.getId(),
                completeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Créer l'intent pour l'action "Reporter"
        Intent snoozeIntent = new Intent(this, NotificationActionReceiver.class);
        snoozeIntent.setAction("SNOOZE_ACTION");
        snoozeIntent.putExtra("notification_id", (int) event.getId());
        snoozeIntent.putExtra("title", event.getTitle());
        snoozeIntent.putExtra("note", event.getNote());
        snoozeIntent.putExtra("time", event.getTime());
        snoozeIntent.putExtra("type", event.getType());
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                this,
                (int) event.getId() + 1000,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(event.getTitle())
                .setContentText(event.getNote() != null ? event.getNote() : "It's time for your medication")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[] { 0, 500, 250, 500 })
                .setAutoCancel(true)
                .addAction(R.drawable.ic_check, "Complete", completePendingIntent)
                .addAction(R.drawable.ic_snooze, "Snooze 10 min", snoozePendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify((int) event.getId(), builder.build());
            Log.d(TAG, "Notification displayed with ID: " + event.getId());
        } else {
            Log.e(TAG, "NotificationManager is null");
        }
    }
}