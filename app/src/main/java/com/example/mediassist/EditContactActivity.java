package com.example.mediassist;

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
import java.util.Date;
import java.util.Locale;

public class EditContactActivity extends AppCompatActivity {
    private static final String TAG = "EditContactActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;

    private EditText etContactName, etPhone;
    private Spinner spinnerRelation;
    private ImageView ivContactPhoto, ivAddPhoto;
    private Button btnUpdateContact;
    private FrameLayout photoContainer;
    private Uri selectedImageUri = null;
    private String imagePath = "";
    private DatabaseHelper dbHelper;
    private Contact contact;
    private long contactId;
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
        Log.d(TAG, "onCreate: Starting EditContactActivity");

        // Demander les permissions nécessaires
        requestPermissions();

        // Get contact ID from intent
        contactId = getIntent().getLongExtra("CONTACT_ID", -1);
        userId = getIntent().getLongExtra("USER_ID", -1);

        Log.d(TAG, "Contact ID received: " + contactId);
        Log.d(TAG, "User ID received: " + userId);

        if (contactId == -1) {
            Toast.makeText(this, "Error: Contact not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);
        Log.d(TAG, "Database helper initialized");

        // Get contact data
        contact = dbHelper.getContactById(contactId);
        if (contact == null) {
            Toast.makeText(this, "Error: Contact not found", Toast.LENGTH_SHORT).show();
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
            // Change title to "Edit Contact"
            TextView tvTitle = findViewById(R.id.tvTitle);
            if (tvTitle != null) {
                tvTitle.setText("Modifier le contact");
            }

            etContactName = findViewById(R.id.etContactName);
            etPhone = findViewById(R.id.etPhone);
            spinnerRelation = findViewById(R.id.spinnerRelation);
            ivContactPhoto = findViewById(R.id.ivContactPhoto);
            ivAddPhoto = findViewById(R.id.ivAddPhoto);
            btnUpdateContact = findViewById(R.id.btnAddContact);
            btnUpdateContact.setText("Mettre à jour");
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

    private void populateFields() {
        try {
            Log.d(TAG, "Populating fields with contact data: " + contact.getName());

            etContactName.setText(contact.getName());
            etPhone.setText(contact.getPhone());

            // Set spinner selection
            setSpinnerSelection(spinnerRelation, contact.getRelation());

            // Load image if available
            if (contact.getImagePath() != null && !contact.getImagePath().isEmpty()) {
                File imgFile = new File(contact.getImagePath());
                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    ivContactPhoto.setImageBitmap(myBitmap);
                    ivContactPhoto.setVisibility(View.VISIBLE);
                    ivAddPhoto.setVisibility(View.GONE);
                    imagePath = contact.getImagePath();
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
            // Photo selection
            photoContainer.setOnClickListener(v -> openGallery());

            // Update contact button
            btnUpdateContact.setOnClickListener(v -> updateContact());

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

    private void updateContact() {
        Log.d(TAG, "Attempting to update contact");

        String name = etContactName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String relation = spinnerRelation.getSelectedItem().toString();

        // Log all values for debugging
        Log.d(TAG, "Name: " + name);
        Log.d(TAG, "Phone: " + phone);
        Log.d(TAG, "Relation: " + relation);
        Log.d(TAG, "Image Path: " + imagePath);
        Log.d(TAG, "Contact ID: " + contactId);

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
            // Update in database
            boolean success = dbHelper.updateContact(contactId, name, phone, relation, imagePath);

            Log.d(TAG, "Update result: " + success);

            if (success) {
                Toast.makeText(this, "Contact updated successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to update contact", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Database update failed");
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception during contact update", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}