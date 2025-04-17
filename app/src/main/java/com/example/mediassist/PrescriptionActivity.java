package com.example.mediassist;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PrescriptionActivity extends AppCompatActivity {
    private static final String TAG = "PrescriptionActivity";

    private LinearLayout prescriptionsContainer;
    private DatabaseHelper dbHelper;
    private long userId;
    private List<Prescription> prescriptionList;

    private final ActivityResultLauncher<Intent> addPrescriptionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadPrescriptions();
                    Log.d(TAG, "Prescription added, reloading prescriptions");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription);
        Log.d(TAG, "onCreate: Starting PrescriptionActivity");

        // Pour le débogage, utilisez un ID utilisateur fixe
        userId = 1; // Remplacez par la récupération réelle de l'ID utilisateur

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);
        Log.d(TAG, "Database helper initialized");

        // Initialize views
        prescriptionsContainer = findViewById(R.id.prescriptionsContainer);
        FloatingActionButton fabAddPrescription = findViewById(R.id.fabAddPrescription);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Add prescription button
        fabAddPrescription.setOnClickListener(v -> openAddPrescriptionActivity());

        // Load prescriptions
        loadPrescriptions();
    }

    private void openAddPrescriptionActivity() {
        try {
            Intent intent = new Intent(this, AddPrescriptionActivity.class);
            intent.putExtra("USER_ID", userId);
            addPrescriptionLauncher.launch(intent);
            Log.d(TAG, "Opening AddPrescriptionActivity");
        } catch (Exception e) {
            Log.e(TAG, "Error opening AddPrescriptionActivity", e);
            Toast.makeText(this, "Error opening add prescription screen", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadPrescriptions() {
        try {
            prescriptionList = dbHelper.getAllPrescriptions(userId);
            displayPrescriptions();
            Log.d(TAG, "Loaded " + (prescriptionList != null ? prescriptionList.size() : 0) + " prescriptions");
        } catch (Exception e) {
            Log.e(TAG, "Error loading prescriptions", e);
            e.printStackTrace();

            // En cas d'erreur, afficher un message à l'utilisateur
            prescriptionsContainer.removeAllViews();
            TextView errorText = new TextView(this);
            errorText.setText("Impossible de charger les ordonnances. Veuillez réessayer.");
            errorText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            errorText.setPadding(0, 50, 0, 0);
            prescriptionsContainer.addView(errorText);
        }
    }

    private void displayPrescriptions() {
        try {
            // Effacer les ordonnances existantes
            prescriptionsContainer.removeAllViews();

            if (prescriptionList == null || prescriptionList.isEmpty()) {
                // Afficher l'état vide
                TextView emptyText = new TextView(this);
                emptyText.setText("Aucune ordonnance trouvée");
                emptyText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                emptyText.setPadding(0, 50, 0, 0);
                prescriptionsContainer.addView(emptyText);
                return;
            }

            // Ajouter les ordonnances de la base de données
            for (Prescription prescription : prescriptionList) {
                View prescriptionView = LayoutInflater.from(this).inflate(
                        R.layout.item_prescription, prescriptionsContainer, false);

                TextView tvPrescriptionTitle = prescriptionView.findViewById(R.id.tvPrescriptionTitle);
                ImageView imgPrescription = prescriptionView.findViewById(R.id.imgPrescription);
                Button btnShare = prescriptionView.findViewById(R.id.btnShare);
                Button btnDelete = prescriptionView.findViewById(R.id.btnDelete);

                tvPrescriptionTitle.setText(prescription.getTitle());

                // Charger l'image de l'ordonnance si disponible
                if (prescription.getImagePath() != null && !prescription.getImagePath().isEmpty()) {
                    File imgFile = new File(prescription.getImagePath());
                    if (imgFile.exists()) {
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        imgPrescription.setImageBitmap(myBitmap);
                    }
                }

                // Image click to enlarge
                imgPrescription.setOnClickListener(v -> showFullScreenImage(prescription.getImagePath()));

                // Share button
                btnShare.setOnClickListener(v -> sharePrescription(prescription));

                // Delete button
                btnDelete.setOnClickListener(v -> confirmDeletePrescription(prescription));

                prescriptionsContainer.addView(prescriptionView);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying prescriptions", e);
            e.printStackTrace();
        }
    }

    private void showFullScreenImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            Toast.makeText(this, "Image non disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_fullscreen_image);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        }

        ImageView fullScreenImage = dialog.findViewById(R.id.fullScreenImage);

        // Load image from path
        File imgFile = new File(imagePath);
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            fullScreenImage.setImageBitmap(myBitmap);
        } else {
            fullScreenImage.setImageResource(R.drawable.ic_launcher_foreground);
        }

        fullScreenImage.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void sharePrescription(Prescription prescription) {
        if (prescription.getImagePath() == null || prescription.getImagePath().isEmpty()) {
            // Fallback to sharing just text if image is not available
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, prescription.getTitle());
            startActivity(Intent.createChooser(shareIntent, "Share via"));
            return;
        }

        try {
            // Get the bitmap from file
            File imageFile = new File(prescription.getImagePath());
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

            // Save bitmap to cache directory
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "prescription.jpg");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.close();

            // Get URI using FileProvider
            Uri imageUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider", // Use your app's package name
                    file
            );

            // Create share intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/jpeg");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, prescription.getTitle());
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Start share activity
            startActivity(Intent.createChooser(shareIntent, "Share prescription via"));

        } catch (IOException e) {
            e.printStackTrace();
            // Fallback to sharing just text if image fails
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, prescription.getTitle());
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        }
    }

    private void confirmDeletePrescription(Prescription prescription) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Supprimer l'ordonnance")
                    .setMessage("Êtes-vous sûr de vouloir supprimer cette ordonnance ?")
                    .setPositiveButton("Supprimer", (dialog, which) -> {
                        deletePrescription(prescription);
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error in confirmDeletePrescription", e);
            e.printStackTrace();
        }
    }

    private void deletePrescription(Prescription prescription) {
        try {
            Log.d(TAG, "Attempting to delete prescription ID: " + prescription.getId());
            boolean success = dbHelper.deletePrescription(prescription.getId());

            if (success) {
                Log.d(TAG, "Prescription deleted successfully");
                Toast.makeText(this, "Ordonnance supprimée avec succès", Toast.LENGTH_SHORT).show();
                loadPrescriptions(); // Recharger la liste
            } else {
                Log.e(TAG, "Failed to delete prescription");
                Toast.makeText(this, "Échec de la suppression de l'ordonnance", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting prescription", e);
            e.printStackTrace();
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}