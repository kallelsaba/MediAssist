package com.example.mediassist;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class AppointmentActivity extends AppCompatActivity {
    private static final String TAG = "AppointmentActivity";

    private LinearLayout appointmentsContainer;
    private DatabaseHelper dbHelper;
    private long userId;
    private List<Appointment> appointmentList;

    private final ActivityResultLauncher<Intent> addAppointmentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadAppointments();
                    Log.d(TAG, "Appointment added, reloading appointments");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);
        Log.d(TAG, "onCreate: Starting AppointmentActivity");

        // Pour le débogage, utilisez un ID utilisateur fixe
        userId = 1; // Remplacez par la récupération réelle de l'ID utilisateur

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);
        Log.d(TAG, "Database helper initialized");

        // Initialize views
        appointmentsContainer = findViewById(R.id.appointmentsContainer);
        FloatingActionButton fabAddAppointment = findViewById(R.id.fabAddAppointment);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Add appointment button
        fabAddAppointment.setOnClickListener(v -> openAddAppointmentActivity());

        // Load appointments
        loadAppointments();
    }

    private void openAddAppointmentActivity() {
        try {
            Intent intent = new Intent(this, AddAppointmentActivity.class);
            intent.putExtra("USER_ID", userId);
            addAppointmentLauncher.launch(intent);
            Log.d(TAG, "Opening AddAppointmentActivity");
        } catch (Exception e) {
            Log.e(TAG, "Error opening AddAppointmentActivity", e);
            Toast.makeText(this, "Error opening add appointment screen", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAppointments() {
        try {
            appointmentList = dbHelper.getAllAppointments(userId);
            displayAppointments();
            Log.d(TAG, "Loaded " + (appointmentList != null ? appointmentList.size() : 0) + " appointments");
        } catch (Exception e) {
            Log.e(TAG, "Error loading appointments", e);
            e.printStackTrace();

            // En cas d'erreur, afficher un message à l'utilisateur
            appointmentsContainer.removeAllViews();
            TextView errorText = new TextView(this);
            errorText.setText("Impossible de charger les rendez-vous. Veuillez réessayer.");
            errorText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            errorText.setPadding(0, 50, 0, 0);
            appointmentsContainer.addView(errorText);
        }
    }

    private void displayAppointments() {
        try {
            // Effacer les rendez-vous existants
            appointmentsContainer.removeAllViews();

            if (appointmentList == null || appointmentList.isEmpty()) {
                // Afficher l'état vide
                TextView emptyText = new TextView(this);
                emptyText.setText("Aucun rendez-vous trouvé");
                emptyText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                emptyText.setPadding(0, 50, 0, 0);
                appointmentsContainer.addView(emptyText);
                return;
            }

            // Ajouter les rendez-vous de la base de données
            for (Appointment appointment : appointmentList) {
                View appointmentView = LayoutInflater.from(this).inflate(
                        R.layout.item_appointment, appointmentsContainer, false);

                TextView tvAppointmentTitle = appointmentView.findViewById(R.id.tvAppointmentTitle);
                TextView tvAppointmentDetails = appointmentView.findViewById(R.id.tvAppointmentDetails);
                TextView tvAppointmentDate = appointmentView.findViewById(R.id.tvAppointmentDate);
                TextView tvAppointmentLocation = appointmentView.findViewById(R.id.tvAppointmentLocation);
                View appointmentCard = appointmentView.findViewById(R.id.appointmentCard);

                tvAppointmentTitle.setText(appointment.getTitle());
                tvAppointmentDetails.setText(appointment.getDetails());
                tvAppointmentDate.setText("Date: " + appointment.getDate());
                tvAppointmentLocation.setText(appointment.getLocation());

                // Définir la couleur de l'indicateur en fonction de la catégorie
                View indicator = appointmentView.findViewById(R.id.appointmentIndicator);
                if ("doctor".equalsIgnoreCase(appointment.getCategory())) {
                    indicator.setBackgroundResource(R.drawable.circle_indicator_doctor);
                } else if ("analysis".equalsIgnoreCase(appointment.getCategory())) {
                    indicator.setBackgroundResource(R.drawable.circle_indicator_analysis);
                } else {
                    indicator.setBackgroundResource(R.drawable.circle_indicator_default);
                }

                // Configurer le clic long pour supprimer
                appointmentCard.setOnLongClickListener(v -> {
                    showDeleteDialog(appointment);
                    return true;
                });

                appointmentsContainer.addView(appointmentView);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying appointments", e);
            e.printStackTrace();
        }
    }

    private void showDeleteDialog(Appointment appointment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Supprimer le rendez-vous")
                .setMessage("Êtes-vous sûr de vouloir supprimer ce rendez-vous ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    deleteAppointment(appointment);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void deleteAppointment(Appointment appointment) {
        try {
            boolean success = dbHelper.deleteAppointment(appointment.getId());
            if (success) {
                Toast.makeText(this, "Rendez-vous supprimé", Toast.LENGTH_SHORT).show();
                loadAppointments();
            } else {
                Toast.makeText(this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting appointment", e);
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}