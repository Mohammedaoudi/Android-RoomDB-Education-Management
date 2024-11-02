package com.fh.app_student_management.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fh.app_student_management.R;
import com.fh.app_student_management.adapters.admin.SubjectStatisticalRecycleViewAdapter;
import com.fh.app_student_management.data.AppDatabase;
import com.fh.app_student_management.data.entities.Semester;
import com.fh.app_student_management.data.relations.StatisticalOfSubject;
import com.fh.app_student_management.utilities.Utils;

import java.util.ArrayList;
import java.util.List;

public class StatisticalSubjectActivity extends AppCompatActivity {

    private ArrayList<Semester> semesters;
    private ArrayList<String> semesterNames;
    private long selectedSemesterId;
    private String selectedSemesterName;

    private ImageView btnBack;
    private EditText edtSemester;
    private LinearLayout titleTable;
    private TextView txtSemesterName;
    private TextView txtSubjectCount;
    private RecyclerView rvSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_statistical_subject);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initStatisticalSubjectView();
        handleEventListener();
    }

    private void initStatisticalSubjectView() {
        btnBack = findViewById(R.id.btnBack);
        edtSemester = findViewById(R.id.edtSemester);
        titleTable = findViewById(R.id.titleTable);
        txtSemesterName = findViewById(R.id.txtSemesterName);
        txtSubjectCount = findViewById(R.id.txtSubjectCount);
        rvSubject = findViewById(R.id.rvSubject);

        titleTable.setVisibility(View.GONE);

        semesters = new ArrayList<>(AppDatabase.getInstance(this).semesterDAO().getAll());
        semesterNames = new ArrayList<>(semesters.size() + 1);
        semesterNames.add(0, "--- Chọn học kỳ ---");
        for (int i = 0; i < semesters.size(); i++) {
            String startDate = Utils.formatDate("MM/yyyy").format(semesters.get(i).getStartDate());
            String endDate = Utils.formatDate("MM/yyyy").format(semesters.get(i).getEndDate());
            String semesterName = String.format("%s (%s - %s)", semesters.get(i).getName(), startDate, endDate);
            semesterNames.add(semesterName);
        }
    }

    private void handleEventListener() {
        btnBack.setOnClickListener(v -> finish());

        edtSemester.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Chọn học kỳ")
                .setItems(semesterNames.toArray(new CharSequence[0]), (dialog, which) -> {
                    if (which == 0) {
                        selectedSemesterId = 0;
                        edtSemester.setText("");
                        updateStatistical();
                    } else {
                        selectedSemesterId = semesters.get(which - 1).getId();
                        selectedSemesterName = semesterNames.get(which);
                        edtSemester.setText(semesterNames.get(which));
                        updateStatistical();
                    }
                }).show());
    }

    private void updateStatistical() {
        List<StatisticalOfSubject> statisticalOfSubjects = new ArrayList<>();
        if (selectedSemesterId > 0) {
            statisticalOfSubjects = AppDatabase.getInstance(this).statisticalDAO().getStatisticalOfSubject(selectedSemesterId);
        }
        rvSubject.setLayoutManager(new LinearLayoutManager(this));
        rvSubject.setAdapter(new SubjectStatisticalRecycleViewAdapter(this, new ArrayList<>(statisticalOfSubjects)));
        txtSemesterName.setText(selectedSemesterName);
        txtSubjectCount.setText(String.valueOf(statisticalOfSubjects.size()));
        titleTable.setVisibility(View.VISIBLE);
    }
}