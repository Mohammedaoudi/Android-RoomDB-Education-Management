package com.fh.app_student_management.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.fh.app_student_management.R;

import ma.ensa.projet.ui.admin.StatisticalActivity;

public class FeatureActivity extends AppCompatActivity {

    private ImageView btnBack;
    private CardView btnToStatisticalAdmin;
    private CardView btnToOpenClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_activity_feature);
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
        btnToStatisticalAdmin = findViewById(R.id.btnToStatisticalAdmin);
        btnToOpenClass = findViewById(R.id.btnToOpenClass);
    }

    private void handleEventListener() {
        btnBack.setOnClickListener(v -> finish());

        btnToStatisticalAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatisticalActivity.class);
            startActivity(intent);
        });

        btnToOpenClass.setOnClickListener(v -> {
            Intent intent = new Intent(this, OpenClassActivity.class);
            startActivity(intent);
        });
    }
}