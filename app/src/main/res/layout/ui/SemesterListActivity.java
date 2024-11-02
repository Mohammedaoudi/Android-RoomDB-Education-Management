package com.fh.app_student_management.ui;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fh.app_student_management.R;
import com.fh.app_student_management.adapters.SemesterRecyclerViewAdapter;
import com.fh.app_student_management.data.AppDatabase;
import com.fh.app_student_management.data.entities.Semester;

import java.util.ArrayList;

public class SemesterListActivity extends AppCompatActivity {

    private LinearLayout layoutSemester;
    private ImageView btnBack;
    private SearchView searchViewSemester;

    private SemesterRecyclerViewAdapter semesterRecycleViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_semester);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutSemester), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initSemesterView();
        handleEventListener();
    }

    private void initSemesterView() {
        layoutSemester = findViewById(R.id.layoutSemester);
        btnBack = findViewById(R.id.btnBack);
        searchViewSemester = findViewById(R.id.searchViewSemester);
        RecyclerView rvSemester = findViewById(R.id.rvSemester);

        ArrayList<Semester> semesters = new ArrayList<>(AppDatabase.getInstance(this).semesterDAO().getAll());

        semesterRecycleViewAdapter = new SemesterRecyclerViewAdapter(this, getIntent(), semesters);
        rvSemester.setLayoutManager(new LinearLayoutManager(this));
        rvSemester.setAdapter(semesterRecycleViewAdapter);
    }

    private void handleEventListener() {
        layoutSemester.setOnClickListener(v -> {
            if (v.getId() == R.id.layoutSemester) {
                searchViewSemester.clearFocus();
            }
        });

        btnBack.setOnClickListener(v -> finish());

        searchViewSemester.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                semesterRecycleViewAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                semesterRecycleViewAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }
}