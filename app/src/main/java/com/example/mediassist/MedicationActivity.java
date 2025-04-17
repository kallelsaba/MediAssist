package com.example.mediassist;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.util.List;

public class MedicationActivity extends AppCompatActivity {
    private static final String TAG = "MedicationActivity";

    private EditText etSearch;
    private LinearLayout medicationsContainer;
    private DatabaseHelper dbHelper;
    private long userId;
    private List<Medication> medicationList;

    private final ActivityResultLauncher<Intent> medicationActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadMedications();
                    Log.d(TAG, "Medication activity result OK, reloading medications");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Démarrage de MedicationActivity");

        try {
            setContentView(R.layout.activity_medication);
            Log.d(TAG, "onCreate: Layout chargé avec succès");

            // Pour le débogage, utilisez un ID utilisateur fixe
            userId = 1; // Remplacez par la récupération réelle de l'ID utilisateur

            // Initialisation de la base de données
            dbHelper = new DatabaseHelper(this);
            Log.d(TAG, "onCreate: DatabaseHelper initialisé");

            // Initialisation des vues
            initializeViews();
            setupListeners();

            // Chargement des médicaments depuis la base de données
            loadMedications();
            Log.d(TAG, "onCreate: Médicaments chargés");
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans onCreate", e);
            e.printStackTrace();
        }
    }

    private void initializeViews() {
        try {
            ImageView btnBack = findViewById(R.id.btnBack);
            etSearch = findViewById(R.id.etSearch);
            medicationsContainer = findViewById(R.id.medicationsContainer);
            FloatingActionButton fabAddMedication = findViewById(R.id.fabAddMedication);

            btnBack.setOnClickListener(v -> finish());
            fabAddMedication.setOnClickListener(v -> openAddMedicationActivity());
            Log.d(TAG, "initializeViews: Vues initialisées avec succès");
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans initializeViews", e);
            e.printStackTrace();
        }
    }

    private void setupListeners() {
        try {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterMedications(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) { }
            });
            Log.d(TAG, "setupListeners: Listeners configurés avec succès");
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans setupListeners", e);
            e.printStackTrace();
        }
    }

    private void openAddMedicationActivity() {
        try {
            Intent intent = new Intent(this, AddMedicationActivity.class);
            intent.putExtra("USER_ID", userId);
            medicationActivityLauncher.launch(intent);
            Log.d(TAG, "openAddMedicationActivity: AddMedicationActivity lancée");
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans openAddMedicationActivity", e);
            e.printStackTrace();
        }
    }

    private void loadMedications() {
        try {
            medicationList = dbHelper.getAllMedications(userId);
            displayMedications(medicationList);
            Log.d(TAG, "loadMedications: " + (medicationList != null ? medicationList.size() : 0) + " médicaments chargés");
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans loadMedications", e);
            e.printStackTrace();

            // En cas d'erreur, afficher un message à l'utilisateur
            medicationsContainer.removeAllViews();
            TextView errorText = new TextView(this);
            errorText.setText("Impossible de charger les médicaments. Veuillez réessayer.");
            errorText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            errorText.setPadding(0, 50, 0, 0);
            medicationsContainer.addView(errorText);
        }
    }

    private void filterMedications(String query) {
        try {
            if (query.isEmpty()) {
                displayMedications(medicationList);
            } else {
                List<Medication> filteredList = dbHelper.searchMedications(query, userId);
                displayMedications(filteredList);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans filterMedications", e);
            e.printStackTrace();
        }
    }

    private void displayMedications(List<Medication> medications) {
        try {
            // Effacer les exemples statiques existants
            medicationsContainer.removeAllViews();

            if (medications == null || medications.isEmpty()) {
                // Afficher l'état vide
                TextView emptyText = new TextView(this);
                emptyText.setText("Aucun médicament trouvé");
                emptyText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                emptyText.setPadding(0, 50, 0, 0);
                medicationsContainer.addView(emptyText);
                return;
            }

            // Ajouter les médicaments de la base de données
            for (Medication medication : medications) {
                View medicationView = LayoutInflater.from(this).inflate(
                        R.layout.item_medication, medicationsContainer, false);

                ImageView ivMedication = medicationView.findViewById(R.id.ivMedication);
                TextView tvMedicationName = medicationView.findViewById(R.id.tvMedicationName);
                TextView tvMedicationDosage = medicationView.findViewById(R.id.tvMedicationDosage);
                Button btnDetails = medicationView.findViewById(R.id.btnDetails);

                tvMedicationName.setText(medication.getName());
                tvMedicationDosage.setText(medication.getDosage() + " " + medication.getFrequency());

                // Charger l'image du médicament si disponible
                if (medication.getImagePath() != null && !medication.getImagePath().isEmpty()) {
                    File imgFile = new File(medication.getImagePath());
                    if (imgFile.exists()) {
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        ivMedication.setImageBitmap(myBitmap);
                    }
                }

                // Définir le listener du bouton de détails
                btnDetails.setOnClickListener(v -> showMedicationDetails(medication));

                // Définir le listener de clic long pour les options d'édition/suppression
                medicationView.setOnLongClickListener(v -> {
                    showMedicationOptions(medication);
                    return true;
                });

                medicationsContainer.addView(medicationView);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans displayMedications", e);
            e.printStackTrace();
        }
    }

    private void showMedicationDetails(Medication medication) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(medication.getName())
                    .setMessage("Type: " + medication.getType() + "\n" +
                            "Dosage: " + medication.getDosage() + "\n" +
                            "Fréquence: " + medication.getFrequency() + "\n" +
                            "Heure: " + medication.getTime())
                    .setPositiveButton("Fermer", null)
                    .setNeutralButton("Modifier", (dialog, which) -> {
                        openEditMedicationActivity(medication);
                    })
                    .setNegativeButton("Supprimer", (dialog, which) -> {
                        confirmDeleteMedication(medication);
                    })
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans showMedicationDetails", e);
            e.printStackTrace();
        }
    }

    private void showMedicationOptions(Medication medication) {
        try {
            String[] options = {"Modifier", "Supprimer"};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Options du médicament")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            // Modifier le médicament
                            openEditMedicationActivity(medication);
                        } else if (which == 1) {
                            // Supprimer le médicament
                            confirmDeleteMedication(medication);
                        }
                    })
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans showMedicationOptions", e);
            e.printStackTrace();
        }
    }

    private void openEditMedicationActivity(Medication medication) {
        try {
            Log.d(TAG, "Ouverture de EditMedicationActivity pour le médicament ID: " + medication.getId());
            Intent intent = new Intent(this, EditMedicationActivity.class);
            intent.putExtra("MEDICATION_ID", medication.getId());
            intent.putExtra("USER_ID", userId);
            medicationActivityLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans openEditMedicationActivity", e);
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors de l'ouverture de l'écran de modification", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDeleteMedication(Medication medication) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Supprimer le médicament")
                    .setMessage("Êtes-vous sûr de vouloir supprimer " + medication.getName() + " ?")
                    .setPositiveButton("Supprimer", (dialog, which) -> {
                        deleteMedication(medication);
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans confirmDeleteMedication", e);
            e.printStackTrace();
        }
    }

    private void deleteMedication(Medication medication) {
        try {
            Log.d(TAG, "Tentative de suppression du médicament ID: " + medication.getId());
            boolean success = dbHelper.deleteMedication(medication.getId());

            if (success) {
                Log.d(TAG, "Médicament supprimé avec succès");
                Toast.makeText(this, "Médicament supprimé avec succès", Toast.LENGTH_SHORT).show();
                loadMedications(); // Recharger la liste
            } else {
                Log.e(TAG, "Échec de la suppression du médicament");
                Toast.makeText(this, "Échec de la suppression du médicament", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la suppression du médicament", e);
            e.printStackTrace();
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}