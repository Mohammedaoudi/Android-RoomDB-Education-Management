package com.fh.app_student_management.ui.admin;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.fh.app_student_management.R;
import com.fh.app_student_management.data.AppDatabase;
import com.fh.app_student_management.data.entities.LecturerSubjectCrossRef;
import com.fh.app_student_management.data.entities.Semester;
import com.fh.app_student_management.data.entities.SubjectSemesterCrossRef;
import com.fh.app_student_management.data.entities.User;
import com.fh.app_student_management.data.relations.SubjectWithRelations;
import com.fh.app_student_management.utilities.Constants;
import com.fh.app_student_management.utilities.Utils;

import java.util.ArrayList;
import java.util.List;

public class OpenClassActivity extends AppCompatActivity {

    private ArrayList<SubjectWithRelations> subjects;
    private ArrayList<String> subjectNames;
    private long selectedSubjectId;
    private ArrayList<User> lecturers;
    private ArrayList<String> lecturerNames;
    private long selectedLecturerId;
    private ArrayList<Semester> semesters;
    private ArrayList<String> semesterNames;
    private long selectedSemesterId;

    private ImageView btnBack;
    private EditText edtSubject;
    private EditText edtLecturer;
    private EditText edtSemester;
    private Button btnOpenClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_open_class);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initCreateClassView();
        handleEventListener();
    }

    private void initCreateClassView() {
        btnBack = findViewById(R.id.btnBack);
        edtSubject = findViewById(R.id.edtSubject);
        edtLecturer = findViewById(R.id.edtLecturer);
        edtSemester = findViewById(R.id.edtSemester);
        btnOpenClass = findViewById(R.id.btnOpenClass);

        semesters = new ArrayList<>(AppDatabase.getInstance(this).semesterDAO().getAll());
        semesterNames = new ArrayList<>(semesters.size() + 1);
        semesterNames.add(0, "--- Chọn học kỳ ---");
        for (int i = 0; i < semesters.size(); i++) {
            String startDate = Utils.formatDate("MM/yyyy").format(semesters.get(i).getStartDate());
            String endDate = Utils.formatDate("MM/yyyy").format(semesters.get(i).getEndDate());
            String semesterName = String.format("%s (%s - %s)", semesters.get(i).getName(), startDate, endDate);
            semesterNames.add(semesterName);
        }

        lecturers = new ArrayList<>(AppDatabase.getInstance(this).userDAO().getByRole(Constants.Role.LECTURER));
        lecturerNames = new ArrayList<>(lecturers.size() + 1);
        lecturerNames.add(0, "--- Chọn giảng viên ---");
        for (int i = 0; i < lecturers.size(); i++) {
            lecturerNames.add(lecturers.get(i).getFullName());
        }
    }

    private void handleEventListener() {
        btnBack.setOnClickListener(v -> finish());

        edtSemester.setOnClickListener(v -> showSelectionDialog("Chọn học kỳ", semesterNames, (dialog, which) -> {
            if (which == 0) {
                resetSelections(edtSemester, edtSubject);
            } else {
                selectedSemesterId = semesters.get(which).getId();
                edtSemester.setText(semesterNames.get(which));
            }
        }));

        edtSubject.setOnClickListener(v -> {
            if (edtSemester.getText().toString().isEmpty()) {
                Utils.showToast(this, "Chưa chọn học kỳ");
                return;
            }

            subjects = new ArrayList<>(AppDatabase.getInstance(this).subjectDAO().getBySemester(selectedSemesterId));
            subjectNames = new ArrayList<>(subjects.size() + 1);
            subjectNames.add(0, "--- Chọn môn học ---");
            for (int i = 0; i < subjects.size(); i++) {
                subjectNames.add(subjects.get(i).getSubject().getName());
            }

            showSelectionDialog("Chọn môn học", subjectNames, (dialog, which) -> {
                if (which == 0) {
                    resetSelections(edtSubject);
                } else {
                    selectedSubjectId = subjects.get(which).getSubject().getId();
                    edtSubject.setText(subjectNames.get(which));
                }
            });
        });

        edtLecturer.setOnClickListener(v -> showSelectionDialog("Chọn giảng viên", lecturerNames, (dialog, which) -> {
            if (which == 0) {
                resetSelections(edtLecturer);
            } else {
                selectedLecturerId = AppDatabase.getInstance(this)
                        .lecturerDAO().getByUser(lecturers.get(which).getId()).getId();
                edtLecturer.setText(lecturerNames.get(which));
            }
        }));

        btnOpenClass.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Thông báo")
                .setMessage("Mở lớp?")
                .setPositiveButton("Có", (dialog, which) -> performOpenClass())
                .setNegativeButton("Không", null)
                .show());
    }

    private void performOpenClass() {
        LecturerSubjectCrossRef lecturerSubjectCrossRef = new LecturerSubjectCrossRef();
        lecturerSubjectCrossRef.setLecturerId(selectedLecturerId);
        lecturerSubjectCrossRef.setSubjectId(selectedSubjectId);
        AppDatabase.getInstance(this).crossRefDAO().insertLecturerSubjectCrossRef(lecturerSubjectCrossRef);

        SubjectSemesterCrossRef subjectSemesterCrossRef = new SubjectSemesterCrossRef();
        subjectSemesterCrossRef.setSubjectId(selectedSubjectId);
        subjectSemesterCrossRef.setSemesterId(selectedSemesterId);
        AppDatabase.getInstance(this).crossRefDAO().insertSubjectSemesterCrossRef(subjectSemesterCrossRef);

        Utils.showToast(this, "Tạo lớp thành công");
        finish();
    }

    private void showSelectionDialog(String title, @NonNull List<String> options, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setItems(options.toArray(new CharSequence[0]), listener);
        builder.show();
    }

    private void resetSelections(@NonNull EditText edtSemester, EditText edtSubject) {
        selectedSemesterId = 0;
        edtSemester.setText("");
        resetSelections(edtSubject);
    }

    private void resetSelections(@NonNull EditText edtSubject) {
        selectedSubjectId = 0;
        edtSubject.setText("");
    }
}