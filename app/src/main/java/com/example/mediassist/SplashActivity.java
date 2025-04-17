package com.example.mediassist;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_DELAY = 2000; // 2 secondes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Délai avant de passer à WelcomeActivity
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
            finish();
        }, SPLASH_DELAY);
    }
}
