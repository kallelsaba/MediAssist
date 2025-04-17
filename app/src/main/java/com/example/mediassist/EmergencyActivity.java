package com.example.mediassist;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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

public class EmergencyActivity extends AppCompatActivity {
    private static final String TAG = "EmergencyActivity";

    private EditText etSearch;
    private LinearLayout contactsContainer;
    private DatabaseHelper dbHelper;
    private long userId;
    private List<Contact> contactList;

    private final ActivityResultLauncher<Intent> contactActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadContacts();
                    Log.d(TAG, "Contact activity result OK, reloading contacts");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Démarrage de EmergencyActivity");

        try {
            setContentView(R.layout.activity_emergency);
            Log.d(TAG, "onCreate: Layout chargé avec succès");

            // Pour le débogage, utilisez un ID utilisateur fixe
            userId = 1; // Remplacez par la récupération réelle de l'ID utilisateur

            // Initialisation de la base de données
            dbHelper = new DatabaseHelper(this);
            Log.d(TAG, "onCreate: DatabaseHelper initialisé");

            // Initialisation des vues
            initializeViews();
            setupListeners();

            // Chargement des contacts depuis la base de données
            loadContacts();
            Log.d(TAG, "onCreate: Contacts chargés");
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans onCreate", e);
            e.printStackTrace();
        }
    }

    private void initializeViews() {
        try {
            ImageView btnBack = findViewById(R.id.btnBack);
            contactsContainer = findViewById(R.id.contactsContainer);
            FloatingActionButton fabAddContact = findViewById(R.id.fabAddContact);

            btnBack.setOnClickListener(v -> finish());
            fabAddContact.setOnClickListener(v -> openAddContactActivity());
            Log.d(TAG, "initializeViews: Vues initialisées avec succès");
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans initializeViews", e);
            e.printStackTrace();
        }
    }

    private void setupListeners() {
        try {
            // Si vous avez une barre de recherche
            etSearch = findViewById(R.id.etSearch);
            if (etSearch != null) {
                etSearch.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        filterContacts(s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) { }
                });
            }
            Log.d(TAG, "setupListeners: Listeners configurés avec succès");
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans setupListeners", e);
            e.printStackTrace();
        }
    }

    private void openAddContactActivity() {
        try {
            Intent intent = new Intent(this, AddContactActivity.class);
            intent.putExtra("USER_ID", userId);
            contactActivityLauncher.launch(intent);
            Log.d(TAG, "openAddContactActivity: AddContactActivity lancée");
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans openAddContactActivity", e);
            e.printStackTrace();
        }
    }

    private void loadContacts() {
        try {
            contactList = dbHelper.getAllContacts(userId);
            displayContacts(contactList);
            Log.d(TAG, "loadContacts: " + (contactList != null ? contactList.size() : 0) + " contacts chargés");
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans loadContacts", e);
            e.printStackTrace();

            // En cas d'erreur, afficher un message à l'utilisateur
            contactsContainer.removeAllViews();
            TextView errorText = new TextView(this);
            errorText.setText("Impossible de charger les contacts. Veuillez réessayer.");
            errorText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            errorText.setPadding(0, 50, 0, 0);
            contactsContainer.addView(errorText);
        }
    }

    private void filterContacts(String query) {
        try {
            if (query.isEmpty()) {
                displayContacts(contactList);
            } else {
                List<Contact> filteredList = dbHelper.searchContacts(query, userId);
                displayContacts(filteredList);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans filterContacts", e);
            e.printStackTrace();
        }
    }

    private void displayContacts(List<Contact> contacts) {
        try {
            // Effacer les contacts existants
            contactsContainer.removeAllViews();

            if (contacts == null || contacts.isEmpty()) {
                // Afficher l'état vide
                TextView emptyText = new TextView(this);
                emptyText.setText("Aucun contact d'urgence trouvé");
                emptyText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                emptyText.setPadding(0, 50, 0, 0);
                contactsContainer.addView(emptyText);
                return;
            }

            // Ajouter les contacts de la base de données
            for (Contact contact : contacts) {
                View contactView = LayoutInflater.from(this).inflate(
                        R.layout.item_contact, contactsContainer, false);

                ImageView ivContact = contactView.findViewById(R.id.ivContact);
                TextView tvContactName = contactView.findViewById(R.id.tvContactName);
                TextView tvContactRelation = contactView.findViewById(R.id.tvContactRelation);
                TextView tvContactPhone = contactView.findViewById(R.id.tvContactPhone);
                Button btnCall = contactView.findViewById(R.id.btnCall);

                tvContactName.setText(contact.getName());
                tvContactRelation.setText(contact.getRelation());
                tvContactPhone.setText("Phone: " + contact.getPhone());

                // Charger l'image du contact si disponible
                if (contact.getImagePath() != null && !contact.getImagePath().isEmpty()) {
                    File imgFile = new File(contact.getImagePath());
                    if (imgFile.exists()) {
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        ivContact.setImageBitmap(myBitmap);
                    }
                }

                // Définir le listener du bouton d'appel
                btnCall.setOnClickListener(v -> callContact(contact.getPhone()));

                // Définir le listener de clic long pour les options d'édition/suppression
                contactView.setOnLongClickListener(v -> {
                    showContactOptions(contact);
                    return true;
                });

                contactsContainer.addView(contactView);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans displayContacts", e);
            e.printStackTrace();
        }
    }

    private void callContact(String phoneNumber) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'appel", e);
            Toast.makeText(this, "Impossible de passer l'appel", Toast.LENGTH_SHORT).show();
        }
    }

    private void showContactOptions(Contact contact) {
        try {
            String[] options = {"Modifier", "Supprimer"};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Options du contact")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            // Modifier le contact
                            openEditContactActivity(contact);
                        } else if (which == 1) {
                            // Supprimer le contact
                            confirmDeleteContact(contact);
                        }
                    })
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans showContactOptions", e);
            e.printStackTrace();
        }
    }

    private void openEditContactActivity(Contact contact) {
        try {
            Log.d(TAG, "Ouverture de EditContactActivity pour le contact ID: " + contact.getId());
            Intent intent = new Intent(this, EditContactActivity.class);
            intent.putExtra("CONTACT_ID", contact.getId());
            intent.putExtra("USER_ID", userId);
            contactActivityLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans openEditContactActivity", e);
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors de l'ouverture de l'écran de modification", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDeleteContact(Contact contact) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Supprimer le contact")
                    .setMessage("Êtes-vous sûr de vouloir supprimer " + contact.getName() + " ?")
                    .setPositiveButton("Supprimer", (dialog, which) -> {
                        deleteContact(contact);
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans confirmDeleteContact", e);
            e.printStackTrace();
        }
    }

    private void deleteContact(Contact contact) {
        try {
            Log.d(TAG, "Tentative de suppression du contact ID: " + contact.getId());
            boolean success = dbHelper.deleteContact(contact.getId());

            if (success) {
                Log.d(TAG, "Contact supprimé avec succès");
                Toast.makeText(this, "Contact supprimé avec succès", Toast.LENGTH_SHORT).show();
                loadContacts(); // Recharger la liste
            } else {
                Log.e(TAG, "Échec de la suppression du contact");
                Toast.makeText(this, "Échec de la suppression du contact", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la suppression du contact", e);
            e.printStackTrace();
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}