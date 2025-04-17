package com.example.mediassist;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddAppointmentActivity extends AppCompatActivity {
    private static final String TAG = "AddAppointmentActivity";

    private EditText etAppointmentTitle;
    private EditText etAppointmentNotes;
    private TextView tvAppointmentDate;
    private RadioGroup rgCategory;
    private RadioButton rbDoctor;
    private RadioButton rbAnalysis;
    //private Button btnAddCategory;
    private Button btnCreateEvent;
    private ImageView btnDatePicker;
    private EditText etLocation;

    private DatabaseHelper dbHelper;
    private long userId;
    private Calendar selectedDate = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_appointment);
        Log.d(TAG, "onCreate: Starting AddAppointmentActivity");

        // Get user ID from intent
        userId = getIntent().getLongExtra("USER_ID", -1);
        Log.d(TAG, "User ID received: " + userId);

        // For testing purposes, if userId is -1, set a default value
        if (userId == -1) {
            userId = 1; // Default user ID for testing
            Log.d(TAG, "Using default user ID: " + userId);
        }

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);
        Log.d(TAG, "Database helper initialized");

        // Initialize views
        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        try {
            etAppointmentTitle = findViewById(R.id.etAppointmentTitle);
            etAppointmentNotes = findViewById(R.id.etAppointmentNotes);
            tvAppointmentDate = findViewById(R.id.tvAppointmentDate);
            rgCategory = findViewById(R.id.rgCategory);
            rbDoctor = findViewById(R.id.rbDoctor);
            rbAnalysis = findViewById(R.id.rbAnalysis);
            //btnAddCategory = findViewById(R.id.btnAddCategory);
            btnCreateEvent = findViewById(R.id.btnCreateEvent);
            btnDatePicker = findViewById(R.id.btnDatePicker);
            etLocation = findViewById(R.id.etLocation);

            // Back button
            ImageView btnBack = findViewById(R.id.btnBack);
            btnBack.setOnClickListener(v -> finish());

            // Set default date
            tvAppointmentDate.setText(dateFormat.format(selectedDate.getTime()));

            Log.d(TAG, "Views initialized");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            e.printStackTrace();
        }
    }

    private void setupListeners() {
        try {
            // Date picker
            btnDatePicker.setOnClickListener(v -> showDatePickerDialog());
            tvAppointmentDate.setOnClickListener(v -> showDatePickerDialog());

            // Add category button
            //btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());

            // Create event button
            btnCreateEvent.setOnClickListener(v -> createAppointment());

            Log.d(TAG, "Listeners setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up listeners", e);
            e.printStackTrace();
        }
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    tvAppointmentDate.setText(dateFormat.format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    /*
    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        builder.setView(dialogView);

        EditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);
        Button btnSaveCategory = dialogView.findViewById(R.id.btnSaveCategory);
        Button btnCancelCategory = dialogView.findViewById(R.id.btnCancelCategory);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnSaveCategory.setOnClickListener(v -> {
            String categoryName = etCategoryName.getText().toString().trim();
            if (!categoryName.isEmpty()) {
                addNewCategoryRadioButton(categoryName);
                dialog.dismiss();
            } else {
                etCategoryName.setError("Veuillez entrer un nom de catégorie");
            }
        });

        btnCancelCategory.setOnClickListener(v -> dialog.dismiss());
    }

    private void addNewCategoryRadioButton(String categoryName) {
        RadioButton radioButton = new RadioButton(this);
        radioButton.setText(categoryName);
        radioButton.setId(View.generateViewId());
        rgCategory.addView(radioButton);
        radioButton.setChecked(true);
    }*/

    private void createAppointment() {
        Log.d(TAG, "Attempting to create appointment");

        String title = etAppointmentTitle.getText().toString().trim();
        String notes = etAppointmentNotes.getText().toString().trim();
        String date = tvAppointmentDate.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        // Get selected category
        int selectedId = rgCategory.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedId);
        String category = selectedRadioButton != null ? selectedRadioButton.getText().toString().toLowerCase() : "";

        // Details will be empty for now, can be customized based on category if needed
        String details = "";
        if ("doctor".equalsIgnoreCase(category)) {
            details = "Doctor Appointment";
        } else if ("analysis".equalsIgnoreCase(category)) {
            details = "Medical Analysis";
        } else {
            details = "Medical Appointment";
        }

        // Log all values for debugging
        Log.d(TAG, "Title: " + title);
        Log.d(TAG, "Notes: " + notes);
        Log.d(TAG, "Date: " + date);
        Log.d(TAG, "Location: " + location);
        Log.d(TAG, "Category: " + category);
        Log.d(TAG, "Details: " + details);
        Log.d(TAG, "User ID: " + userId);

        // Validation
        if (title.isEmpty()) {
            etAppointmentTitle.setError("Titre requis");
            etAppointmentTitle.requestFocus();
            return;
        }

        if (date.isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner une date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedRadioButton == null) {
            Toast.makeText(this, "Veuillez sélectionner une catégorie", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Insert into database
            long appointmentId = dbHelper.insertAppointment(title, details, date, location, category, notes, userId);

            Log.d(TAG, "Insert result: " + appointmentId);

            if (appointmentId != -1) {
                Toast.makeText(this, "Rendez-vous ajouté avec succès", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Échec de l'ajout du rendez-vous", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Database insert failed");
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception during appointment addition", e);
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}