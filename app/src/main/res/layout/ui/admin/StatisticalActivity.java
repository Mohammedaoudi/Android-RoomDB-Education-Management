package com.fh.app_student_management.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.fh.app_student_management.R;

public class StatisticalActivity extends AppCompatActivity {

    private ImageView btnBack;
    private CardView btnToStatisticalLecturer;
    private CardView btnToStatisticalSubject;
    private CardView btnToStatisticalClass;
    private CardView btnToStatisticalScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_statistical);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initStatisticalView();
        handleEventListener();
    }

    private void initStatisticalView() {
        btnBack = findViewById(R.id.btnBack);
        btnToStatisticalLecturer = findViewById(R.id.btnToStatisticalLecturer);
        btnToStatisticalSubject = findViewById(R.id.btnToStatisticalSubject);
        btnToStatisticalClass = findViewById(R.id.btnToStatisticalClass);
        btnToStatisticalScore = findViewById(R.id.btnToStatisticalScore);
    }

    private void handleEventListener() {
        btnBack.setOnClickListener(v -> finish());

        btnToStatisticalLecturer.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatisticalLecturerActivity.class);
            startActivity(intent);
        });

        btnToStatisticalSubject.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatisticalSubjectActivity.class);
            startActivity(intent);
        });

        btnToStatisticalClass.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatisticalClassActivity.class);
            startActivity(intent);
        });

        btnToStatisticalScore.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatisticalScoreActivity.class);
            startActivity(intent);
        });
    }
}