package com.example.mediassist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class NotificationDetailActivity extends AppCompatActivity {
    ImageView ivMedication;
    TextView tvTime, tvTitle, tvMedicationName, tvInstructions;
    Button btnComplete, btnSnooze, btnSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_notification);

        // Liaison des vues
        ivMedication = findViewById(R.id.ivMedication);
        tvTime = findViewById(R.id.tvNotificationTime);
        tvTitle = findViewById(R.id.tvNotificationTitle);
        tvMedicationName = findViewById(R.id.tvMedicationName);
        tvInstructions = findViewById(R.id.tvInstructions);
        btnComplete = findViewById(R.id.btnComplete);
        btnSnooze = findViewById(R.id.btnSnooze);
        btnSkip = findViewById(R.id.btnSkip);

        // Récupération des données
        Intent intent = getIntent();
        String medicationName = intent.getStringExtra("medicationName");
        String instructions = intent.getStringExtra("instructions");
        int imageRes = intent.getIntExtra("imageRes", R.drawable.doctor); // Une image par défaut
        String time = intent.getStringExtra("time");

        // Affectation des valeurs
        tvTime.setText(time);
        tvMedicationName.setText(medicationName);
        tvInstructions.setText(instructions);
        ivMedication.setImageResource(imageRes);

        // Actions
        btnComplete.setOnClickListener(v -> {
            Toast.makeText(this, "Marked as complete", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnSnooze.setOnClickListener(v -> {
            Toast.makeText(this, "Snoozed for 10 minutes", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnSkip.setOnClickListener(v -> {
            Toast.makeText(this, "Skipped", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
