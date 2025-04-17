package com.example.mediassist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    private Button btnProfile, btnMedications, btnAppointments, btnPrescriptions, btnSchedule, btnEmergency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnProfile = findViewById(R.id.btnProfile);
        btnMedications = findViewById(R.id.btnMedications);
        btnAppointments = findViewById(R.id.btnAppointments);
        btnPrescriptions = findViewById(R.id.btnPrescriptions);
        btnSchedule = findViewById(R.id.btnSchedule);
        btnEmergency = findViewById(R.id.btnEmergency);

        btnProfile.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
        btnMedications.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, MedicationActivity.class)));
        btnAppointments.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, AppointmentActivity.class)));
        btnPrescriptions.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, PrescriptionActivity.class)));
        btnSchedule.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, ScheduleActivity.class)));
        btnEmergency.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, EmergencyActivity.class)));
    }
}


