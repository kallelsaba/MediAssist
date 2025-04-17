package com.example.mediassist;

import static android.content.ContentValues.TAG;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddMedicationActivity extends AppCompatActivity {

    private EditText etMedicationName, etDosage, etTime;
    private Spinner spinnerType, spinnerFrequency;
    private ImageView ivMedicationPhoto, ivAddPhoto;
    private Button btnAddMedication;
    private FrameLayout photoContainer;
    private Uri selectedImageUri = null;
    private String imagePath = "";
    private DatabaseHelper dbHelper;
    private long userId;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        try {
                            ivMedicationPhoto.setImageURI(selectedImageUri);
                            ivMedicationPhoto.setVisibility(View.VISIBLE);
                            ivAddPhoto.setVisibility(View.GONE);

                            // Save the image to app's private storage
                            imagePath = saveImageToInternalStorage(selectedImageUri);
                        } catch (Exception e) {
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);
        Log.d(TAG, "onCreate: Starting AddMedicationActivity");

        // Demander les permissions nécessaires
        requestPermissions();

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
        setupSpinners();
        setupListeners();
    }

    private void requestPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                // Android 13+
                if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
                }
            } else {
                // Android 6-12
                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED ||
                        checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{
                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, PERMISSION_REQUEST_CODE);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission granted");
            } else {
                Log.d(TAG, "Permission denied");
                Toast.makeText(this, "Permission denied. Some features may not work properly.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeViews() {
        etMedicationName = findViewById(R.id.etMedicationName);
        etDosage = findViewById(R.id.etDosage);
        etTime = findViewById(R.id.etTime);
        spinnerType = findViewById(R.id.spinnerType);
        spinnerFrequency = findViewById(R.id.spinnerFrequency);
        ivMedicationPhoto = findViewById(R.id.ivMedicationPhoto);
        ivAddPhoto = findViewById(R.id.ivAddPhoto);
        btnAddMedication = findViewById(R.id.btnAddMedication);
        photoContainer = findViewById(R.id.photoContainer);

        // Back button
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupSpinners() {
        // Setup Type Spinner
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.medication_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        // Setup Frequency Spinner
        ArrayAdapter<CharSequence> frequencyAdapter = ArrayAdapter.createFromResource(this,
                R.array.medication_frequencies, android.R.layout.simple_spinner_item);
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(frequencyAdapter);
    }

    private void setupListeners() {
        // Time picker
        etTime.setOnClickListener(v -> showTimePickerDialog());

        // Photo selection
        photoContainer.setOnClickListener(v -> openGallery());

        // Add medication button
        btnAddMedication.setOnClickListener(v -> saveMedication());
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, selectedMinute) -> {
                    Calendar selectedTime = Calendar.getInstance();
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, selectedMinute);

                    SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
                    etTime.setText(timeFormat.format(selectedTime.getTime()));
                },
                hour,
                minute,
                false);
        timePickerDialog.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private String saveImageToInternalStorage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream != null) {
                // Create a unique filename
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String imageFileName = "JPEG_" + timeStamp + ".jpg";

                // Get the directory for the app's private pictures directory
                File storageDir = new File(getFilesDir(), "medication_images");
                if (!storageDir.exists()) {
                    storageDir.mkdirs();
                }

                File imageFile = new File(storageDir, imageFileName);

                // Save the image
                OutputStream outputStream = new FileOutputStream(imageFile);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();
                outputStream.close();

                return imageFile.getAbsolutePath();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show();
        }
        return "";
    }

    private void saveMedication() {
        String name = etMedicationName.getText().toString().trim();
        String type = spinnerType.getSelectedItem().toString();
        String frequency = spinnerFrequency.getSelectedItem().toString();
        String dosage = etDosage.getText().toString().trim();
        String time = etTime.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            etMedicationName.setError("Name is required");
            etMedicationName.requestFocus();
            return;
        }

        if (dosage.isEmpty()) {
            etDosage.setError("Dosage is required");
            etDosage.requestFocus();
            return;
        }

        if (time.isEmpty()) {
            etTime.setError("Time is required");
            etTime.requestFocus();
            return;
        }

        // Save to database
        long medicationId = dbHelper.insertMedication(name, type, frequency, dosage, time, imagePath, userId);

        if (medicationId != -1) {
            Toast.makeText(this, "Medication added successfully", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Failed to add medication", Toast.LENGTH_SHORT).show();
        }
    }
}