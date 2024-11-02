package com.fh.app_student_management.ui.lecturer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fh.app_student_management.R;
import com.fh.app_student_management.adapters.lecturer.ScoreListRecycleViewAdapter;
import com.fh.app_student_management.data.AppDatabase;
import com.fh.app_student_management.data.relations.StudentWithScores;
import com.fh.app_student_management.utilities.Constants;

import java.util.ArrayList;

public class ScoreListActivity extends AppCompatActivity {

    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lecturer_activity_list_score);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutScore), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initScoreView();
        handleEventListener();
    }

    private void initScoreView() {
        Intent intent = getIntent();
        long semesterId = intent.getLongExtra(Constants.SEMESTER_ID, 0);
        long subjectId = intent.getLongExtra(Constants.SUBJECT_ID, 0);
        String subjectName = intent.getStringExtra("subjectName");

        btnBack = findViewById(R.id.btnBack);
        RecyclerView rvScore = findViewById(R.id.lvScore);
        TextView txtSubjectName = findViewById(R.id.txtSubjectName);

        txtSubjectName.setText(subjectName);

        ArrayList<StudentWithScores> students = new ArrayList<>(AppDatabase.getInstance(this)
                .studentDAO().getScoresBySemesterSubject(semesterId, subjectId));
        ScoreListRecycleViewAdapter scoreListRecycleViewAdapter = new ScoreListRecycleViewAdapter(this, getIntent(), students);
        rvScore.setLayoutManager(new LinearLayoutManager(this));
        rvScore.setAdapter(scoreListRecycleViewAdapter);
    }

    @SuppressLint("InflateParams")
    private void handleEventListener() {
        btnBack.setOnClickListener(v -> finish());
    }
}