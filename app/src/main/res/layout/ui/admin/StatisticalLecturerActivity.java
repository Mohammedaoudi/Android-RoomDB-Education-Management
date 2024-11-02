package com.fh.app_student_management.ui.admin;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fh.app_student_management.R;
import com.fh.app_student_management.adapters.admin.LecturerStatisticalRecycleViewAdapter;
import com.fh.app_student_management.data.AppDatabase;
import com.fh.app_student_management.data.entities.Semester;
import com.fh.app_student_management.data.relations.LecturerAndUser;
import com.fh.app_student_management.data.relations.StatisticalOfLecturer;
import com.fh.app_student_management.utilities.Utils;

import java.util.ArrayList;
import java.util.List;

public class StatisticalLecturerActivity extends AppCompatActivity {

    private ArrayList<Semester> semesters;
    private ArrayList<String> semesterNames;
    private long selectedSemesterId;
    private ArrayList<LecturerAndUser> lecturers;
    private ArrayList<String> lecturerNames;
    private long selectedLecturerId;

    private ImageView btnBack;
    private EditText edtSemester;
    private EditText edtLecturer;
    private LinearLayout titleTable;
    private TextView txtLecturerName;
    private TextView txtSemesterName;
    private RecyclerView rvLecturer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_statistical_lecturer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initStatisticalLecturerView();
        handleEventListener();
    }

    private void initStatisticalLecturerView() {
        btnBack = findViewById(R.id.btnBack);
        edtSemester = findViewById(R.id.edtSemester);
        edtLecturer = findViewById(R.id.edtLecturer);
        titleTable = findViewById(R.id.titleTable);
        txtLecturerName = findViewById(R.id.txtLecturerName);
        txtSemesterName = findViewById(R.id.txtSemesterName);
        rvLecturer = findViewById(R.id.rvLecturer);

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

        edtSemester.setOnClickListener(v -> showSelectionDialog("Chọn học kỳ", semesterNames, (dialog, which) -> {
            if (which == 0) {
                resetSelections(edtSemester, edtLecturer);
            } else {
                selectedSemesterId = semesters.get(which - 1).getId();
                edtSemester.setText(semesterNames.get(which));
                resetSelections(edtLecturer);
            }
        }));

        edtLecturer.setOnClickListener(v -> {
            if (edtSemester.getText().toString().isEmpty()) {
                Utils.showToast(this, "Chưa chọn học kỳ");
                return;
            }

            lecturers = new ArrayList<>(AppDatabase.getInstance(this)
                    .lecturerDAO().getAllLecturerAndUserBySemester(selectedSemesterId));
            lecturerNames = new ArrayList<>(lecturers.size() + 1);
            lecturerNames.add(0, "--- Chọn giảng viên ---");
            for (int i = 0; i < lecturers.size(); i++) {
                lecturerNames.add(lecturers.get(i).getUser().getFullName());
            }

            showSelectionDialog("Chọn giảng viên", lecturerNames, (dialog, which) -> {
                if (which == 0) {
                    resetSelections(edtLecturer);
                } else {
                    selectedLecturerId = lecturers.get(which - 1).getLecturer().getId();
                    edtLecturer.setText(lecturerNames.get(which));
                    titleTable.setVisibility(View.VISIBLE);
                    txtLecturerName.setText(lecturerNames.get(which));
                    txtSemesterName.setText(edtSemester.getText().toString());
                    updateStatistical();
                }
            });
        });
    }

    private void updateStatistical() {
        List<StatisticalOfLecturer> statisticalOfLecturers = new ArrayList<>();
        if (selectedSemesterId > 0 && selectedLecturerId > 0) {
            statisticalOfLecturers = AppDatabase.getInstance(this)
                    .statisticalDAO().getStatisticalOfLecturer(selectedSemesterId, selectedLecturerId);
        }
        rvLecturer.setLayoutManager(new LinearLayoutManager(this));
        rvLecturer.setAdapter(new LecturerStatisticalRecycleViewAdapter(this, new ArrayList<>(statisticalOfLecturers)));
    }

    private void showSelectionDialog(String title, @NonNull List<String> options, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setItems(options.toArray(new CharSequence[0]), listener);
        builder.show();
    }

    private void resetSelections(@NonNull EditText edtSemester, EditText edtLecturer) {
        titleTable.setVisibility(View.GONE);
        selectedSemesterId = 0;
        edtSemester.setText("");
        resetSelections(edtLecturer);
    }

    private void resetSelections(@NonNull EditText edtLecturer) {
        titleTable.setVisibility(View.GONE);
        selectedLecturerId = 0;
        edtLecturer.setText("");
        lecturerNames = null;
        updateStatistical();
    }
}