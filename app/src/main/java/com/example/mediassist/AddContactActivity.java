package com.example.mediassist;

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
import java.util.Date;
import java.util.Locale;

public class AddContactActivity extends AppCompatActivity {
    private static final String TAG = "AddContactActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;

    private EditText etContactName, etPhone;
    private Spinner spinnerRelation;
    private ImageView ivContactPhoto, ivAddPhoto;
    private Button btnAddContact;
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
                            ivContactPhoto.setImageURI(selectedImageUri);
                            ivContactPhoto.setVisibility(View.VISIBLE);
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
        setContentView(R.layout.activity_add_contact);
        Log.d(TAG, "onCreate: Starting AddContactActivity");

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
        try {
            etContactName = findViewById(R.id.etContactName);
            etPhone = findViewById(R.id.etPhone);
            spinnerRelation = findViewById(R.id.spinnerRelation);
            ivContactPhoto = findViewById(R.id.ivContactPhoto);
            ivAddPhoto = findViewById(R.id.ivAddPhoto);
            btnAddContact = findViewById(R.id.btnAddContact);
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
            // Setup Relation Spinner
            ArrayAdapter<CharSequence> relationAdapter = ArrayAdapter.createFromResource(this,
                    R.array.contact_relations, android.R.layout.simple_spinner_item);
            relationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerRelation.setAdapter(relationAdapter);

            Log.d(TAG, "Spinners setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up spinners", e);
            e.printStackTrace();
        }
    }

    private void setupListeners() {
        try {
            // Photo selection
            photoContainer.setOnClickListener(v -> openGallery());

            // Add contact button
            btnAddContact.setOnClickListener(v -> addContact());

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
                File storageDir = new File(getFilesDir(), "contact_images");
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

    private void addContact() {
        Log.d(TAG, "Attempting to add contact");

        String name = etContactName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String relation = spinnerRelation.getSelectedItem().toString();

        // Log all values for debugging
        Log.d(TAG, "Name: " + name);
        Log.d(TAG, "Phone: " + phone);
        Log.d(TAG, "Relation: " + relation);
        Log.d(TAG, "Image Path: " + imagePath);
        Log.d(TAG, "User ID: " + userId);

        // Validation
        if (name.isEmpty()) {
            etContactName.setError("Name is required");
            etContactName.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return;
        }

        try {
            // Insert into database
            long contactId = dbHelper.insertContact(name, phone, relation, imagePath, userId);

            Log.d(TAG, "Insert result: " + contactId);

            if (contactId != -1) {
                Toast.makeText(this, "Contact added successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to add contact", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Database insert failed");
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception during contact addition", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}