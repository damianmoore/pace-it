package uk.co.epixstudios.pace_it.paceit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getSharedPreferences("app_preferences", 0);

        Intent intent = new Intent(this, StartActivity.class);

        // Show onboarding screen the first time the app loads
//        if (!settings.getBoolean("onboarding_complete", false)) {
//            intent = new Intent(this, OnboardingActivity.class);
//        }

        startActivity(intent);
        finish();
    }
}
