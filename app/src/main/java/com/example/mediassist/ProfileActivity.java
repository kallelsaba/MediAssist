package com.example.mediassist;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvUsername, tvAge, tvGender, tvBloodGroup, tvWeight, tvHeight, tvAllergies, tvPhone, tvAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Bouton retour
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // Ferme l'activité pour revenir en arrière
            }
        });

        // TextViews
        tvUsername = findViewById(R.id.tvUsername);
        tvAge = findViewById(R.id.tvAge);
        tvGender = findViewById(R.id.tvGender);
        tvBloodGroup = findViewById(R.id.tvBloodGroup);
        tvWeight = findViewById(R.id.tvWeight);
        tvHeight = findViewById(R.id.tvHeight);
        tvAllergies = findViewById(R.id.tvAllergies);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);

        // Exemple de données statiques
        tvUsername.setText("Mamie_Sousou");
        tvAge.setText("     70");
        tvGender.setText("      female");
        tvBloodGroup.setText("      A+");
        tvWeight.setText("      50 Kg");
        tvHeight.setText("      160 cm");
        tvAllergies.setText("       None");
        tvPhone.setText("       +21698756340");
        tvAddress.setText("     123, Main Street");
    }
}

