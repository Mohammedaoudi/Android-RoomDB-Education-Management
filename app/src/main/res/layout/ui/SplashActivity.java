package com.fh.app_student_management.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.fh.app_student_management.R;
import com.fh.app_student_management.data.AppDatabase;
import com.fh.app_student_management.utilities.Constants;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        handler = new Handler();

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        boolean insertedDefaultValues = sharedPreferences.getBoolean(Constants.PREF_INSERT_DEFAULT_VALUES, false);

        if (!insertedDefaultValues) {
            AppDatabase.insertDefaultValue(this);
            sharedPreferences.edit().putBoolean(Constants.PREF_INSERT_DEFAULT_VALUES, true).apply();
        }

        runnable = () -> {
            boolean isFirstTimeLaunch = sharedPreferences.getBoolean(Constants.IS_FIRST_TIME_LAUNCH, false);
            long userId = sharedPreferences.getLong(Constants.USER_ID, 0);

            Class<?> targetActivity = LoginActivity.class;

            targetActivity = !isFirstTimeLaunch ? OnboardingActivity.class : targetActivity;
            targetActivity = userId > 0 ? HomeActivity.class : targetActivity;

            Intent intent = new Intent(this, targetActivity);

            if (userId > 0) {
                Bundle bundle = new Bundle();
                bundle.putLong(Constants.USER_ID, userId);
                intent.putExtras(bundle);
            }

            startActivity(intent);
            finish();
        };

        handler.postDelayed(runnable, 1500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}