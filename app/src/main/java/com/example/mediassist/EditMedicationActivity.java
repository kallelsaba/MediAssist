package com.example.mediassist;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.TextView;
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

public class EditMedicationActivity extends AppCompatActivity {
    private static final String TAG = "EditMedicationActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;

    private EditText etMedicationName, etDosage, etTime;
    private Spinner spinnerType, spinnerFrequency;
    private ImageView ivMedicationPhoto, ivAddPhoto;
    private Button btnUpdateMedication;
    private FrameLayout photoContainer;
    private Uri selectedImageUri = null;
    private String imagePath = "";
    private DatabaseHelper dbHelper;
    private Medication medication;
    private long medicationId;
    private long userId;

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
                            Log.d(TAG, "Image saved to: " + imagePath);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to load image", e);
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);
        Log.d(TAG, "onCreate: Starting EditMedicationActivity");

        // Demander les permissions nécessaires
        requestPermissions();

        // Get medication ID from intent
        medicationId = getIntent().getLongExtra("MEDICATION_ID", -1);
        userId = getIntent().getLongExtra("USER_ID", -1);

        Log.d(TAG, "Medication ID received: " + medicationId);
        Log.d(TAG, "User ID received: " + userId);

        if (medicationId == -1) {
            Toast.makeText(this, "Error: Medication not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);
        Log.d(TAG, "Database helper initialized");

        // Get medication data
        medication = dbHelper.getMedicationById(medicationId);
        if (medication == null) {
            Toast.makeText(this, "Error: Medication not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        initializeViews();
        setupSpinners();
        populateFields();
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
        try {
            // Change title to "Edit Medication"
            TextView tvTitle = findViewById(R.id.tvTitle);
            if (tvTitle != null) {
                tvTitle.setText("Modifier le médicament");
            }

            etMedicationName = findViewById(R.id.etMedicationName);
            etDosage = findViewById(R.id.etDosage);
            etTime = findViewById(R.id.etTime);
            spinnerType = findViewById(R.id.spinnerType);
            spinnerFrequency = findViewById(R.id.spinnerFrequency);
            ivMedicationPhoto = findViewById(R.id.ivMedicationPhoto);
            ivAddPhoto = findViewById(R.id.ivAddPhoto);
            btnUpdateMedication = findViewById(R.id.btnAddMedication);
            btnUpdateMedication.setText("Mettre à jour");
            photoContainer = findViewById(R.id.photoContainer);

            // Back button
            ImageView btnBack = findViewById(R.id.btnBack);
            btnBack.setOnClickListener(v -> finish());

            Log.d(TAG, "Views initialized");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            e.printStackTrace();
        }
    }

    private void setupSpinners() {
        try {
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

            Log.d(TAG, "Spinners setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up spinners", e);
            e.printStackTrace();
        }
    }

    private void populateFields() {
        try {
            Log.d(TAG, "Populating fields with medication data: " + medication.getName());

            etMedicationName.setText(medication.getName());
            etDosage.setText(medication.getDosage());
            etTime.setText(medication.getTime());

            // Set spinner selections
            setSpinnerSelection(spinnerType, medication.getType());
            setSpinnerSelection(spinnerFrequency, medication.getFrequency());

            // Load image if available
            if (medication.getImagePath() != null && !medication.getImagePath().isEmpty()) {
                File imgFile = new File(medication.getImagePath());
                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    ivMedicationPhoto.setImageBitmap(myBitmap);
                    ivMedicationPhoto.setVisibility(View.VISIBLE);
                    ivAddPhoto.setVisibility(View.GONE);
                    imagePath = medication.getImagePath();
                    Log.d(TAG, "Loaded image from: " + imagePath);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error populating fields", e);
            e.printStackTrace();
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        try {
            ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).toString().equals(value)) {
                    spinner.setSelection(i);
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting spinner selection", e);
            e.printStackTrace();
        }
    }

    private void setupListeners() {
        try {
            // Time picker
            etTime.setOnClickListener(v -> showTimePickerDialog());

            // Photo selection
            photoContainer.setOnClickListener(v -> openGallery());

            // Update medication button
            btnUpdateMedication.setOnClickListener(v -> updateMedication());

            Log.d(TAG, "Listeners setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up listeners", e);
            e.printStackTrace();
        }
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
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening gallery", e);
            Toast.makeText(this, "Error opening gallery", Toast.LENGTH_SHORT).show();
        }
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
                    boolean dirCreated = storageDir.mkdirs();
                    Log.d(TAG, "Directory created: " + dirCreated);
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

                Log.d(TAG, "Image saved successfully to: " + imageFile.getAbsolutePath());
                return imageFile.getAbsolutePath();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error saving image", e);
            e.printStackTrace();
            Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show();
        }
        return "";
    }

    private void updateMedication() {
        Log.d(TAG, "Attempting to update medication");

        String name = etMedicationName.getText().toString().trim();
        String type = spinnerType.getSelectedItem().toString();
        String frequency = spinnerFrequency.getSelectedItem().toString();
        String dosage = etDosage.getText().toString().trim();
        String time = etTime.getText().toString().trim();

        // Log all values for debugging
        Log.d(TAG, "Name: " + name);
        Log.d(TAG, "Type: " + type);
        Log.d(TAG, "Frequency: " + frequency);
        Log.d(TAG, "Dosage: " + dosage);
        Log.d(TAG, "Time: " + time);
        Log.d(TAG, "Image Path: " + imagePath);
        Log.d(TAG, "Medication ID: " + medicationId);

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

        try {
            // Update in database
            boolean success = dbHelper.updateMedication(medicationId, name, type, frequency, dosage, time, imagePath);

            Log.d(TAG, "Update result: " + success);

            if (success) {
                Toast.makeText(this, "Medication updated successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to update medication", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Database update failed");
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception during medication update", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}