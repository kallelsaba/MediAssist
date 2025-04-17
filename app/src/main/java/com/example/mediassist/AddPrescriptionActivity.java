package com.example.mediassist;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import java.util.Date;
import java.util.Locale;

public class AddPrescriptionActivity extends AppCompatActivity {
    private static final String TAG = "AddPrescriptionActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;

    private EditText etPrescriptionTitle;
    private ImageView ivPrescriptionPhoto, ivAddPhoto;
    private Button btnAddPrescription;
    private FrameLayout photoContainer;
    private Uri selectedImageUri = null;
    private String imagePath = "";
    private DatabaseHelper dbHelper;
    private long userId;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        try {
                            ivPrescriptionPhoto.setImageURI(selectedImageUri);
                            ivPrescriptionPhoto.setVisibility(View.VISIBLE);
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
        setContentView(R.layout.activity_add_prescription);
        Log.d(TAG, "onCreate: Starting AddPrescriptionActivity");

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
            etPrescriptionTitle = findViewById(R.id.etPrescriptionTitle);
            ivPrescriptionPhoto = findViewById(R.id.ivPrescriptionPhoto);
            ivAddPhoto = findViewById(R.id.ivAddPhoto);
            btnAddPrescription = findViewById(R.id.btnAddPrescription);
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

    private void setupListeners() {
        try {
            // Photo selection
            photoContainer.setOnClickListener(v -> openGallery());

            // Add prescription button
            btnAddPrescription.setOnClickListener(v -> addPrescription());

            Log.d(TAG, "Listeners setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up listeners", e);
            e.printStackTrace();
        }
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
                File storageDir = new File(getFilesDir(), "prescription_images");
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

    private void addPrescription() {
        Log.d(TAG, "Attempting to add prescription");

        String title = etPrescriptionTitle.getText().toString().trim();

        // Log all values for debugging
        Log.d(TAG, "Title: " + title);
        Log.d(TAG, "Image Path: " + imagePath);
        Log.d(TAG, "User ID: " + userId);

        // Validation
        if (title.isEmpty()) {
            etPrescriptionTitle.setError("Title is required");
            etPrescriptionTitle.requestFocus();
            return;
        }

        if (imagePath.isEmpty()) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Insert into database
            long prescriptionId = dbHelper.insertPrescription(title, imagePath, userId);

            Log.d(TAG, "Insert result: " + prescriptionId);

            if (prescriptionId != -1) {
                Toast.makeText(this, "Prescription added successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to add prescription", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Database insert failed");
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception during prescription addition", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
