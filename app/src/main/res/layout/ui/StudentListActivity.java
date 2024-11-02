package com.fh.app_student_management.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fh.app_student_management.R;
import com.fh.app_student_management.adapters.admin.StudentListRecycleViewAdapter;
import com.fh.app_student_management.data.AppDatabase;
import com.fh.app_student_management.data.entities.StudentSemesterCrossRef;
import com.fh.app_student_management.data.entities.StudentSubjectCrossRef;
import com.fh.app_student_management.data.relations.StudentWithRelations;
import com.fh.app_student_management.utilities.Constants;

import java.util.ArrayList;

public class StudentListActivity extends AppCompatActivity {

    private long semesterId;
    private long classId;
    private long subjectId;
    private StudentListRecycleViewAdapter studentListRecycleViewAdapter;

    private RelativeLayout layoutStudent;
    private ImageView btnBack;
    private SearchView searchViewStudent;
    private Button btnAddStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list_student);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutStudent), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initAddStudentView();
        handleEventListener();
    }

    private void initAddStudentView() {
        Intent intent = getIntent();
        semesterId = intent.getLongExtra(Constants.SEMESTER_ID, 0);
        classId = intent.getLongExtra(Constants.CLASS_ID, 0);
        subjectId = intent.getLongExtra(Constants.SUBJECT_ID, 0);
        boolean isStudentClass = intent.getBooleanExtra("isStudentClass", false);
        boolean isStudentSubject = intent.getBooleanExtra("isStudentSubject", false);

        layoutStudent = findViewById(R.id.layoutStudent);
        btnBack = findViewById(R.id.btnBack);
        searchViewStudent = findViewById(R.id.searchViewStudent);
        btnAddStudent = findViewById(R.id.btnAddStudent);

        ArrayList<StudentWithRelations> students = new ArrayList<>();
        if (isStudentClass) {
            students = new ArrayList<>(AppDatabase.getInstance(this).studentDAO().getByClass(classId));
        }
        if (isStudentSubject) {
            students = new ArrayList<>(AppDatabase.getInstance(this)
                    .studentDAO().getBySemesterClassSubject(semesterId, classId, subjectId));
        }

        studentListRecycleViewAdapter = new StudentListRecycleViewAdapter(this, students);
        RecyclerView rvStudent = findViewById(R.id.rvStudent);
        rvStudent.setLayoutManager(new LinearLayoutManager(this));
        rvStudent.setAdapter(studentListRecycleViewAdapter);
    }

    private void handleEventListener() {
        layoutStudent.setOnClickListener(v -> {
            if (v.getId() == R.id.layoutStudent) {
                searchViewStudent.clearFocus();
            }
        });

        btnBack.setOnClickListener(v -> finish());

        searchViewStudent.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                studentListRecycleViewAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                studentListRecycleViewAdapter.getFilter().filter(newText);
                return false;
            }
        });

        btnAddStudent.setOnClickListener(v -> {
            Intent intent = getIntent();
            boolean isStudentClass = intent.getBooleanExtra("isStudentClass", false);
            boolean isStudentSubject = intent.getBooleanExtra("isStudentSubject", false);

            ArrayList<StudentWithRelations> students = new ArrayList<>();
            if (isStudentClass) {
                students = new ArrayList<>(AppDatabase.getInstance(this).studentDAO().getAllWithRelationsExclusiveClass(classId));
            }
            if (isStudentSubject) {
                ArrayList<StudentWithRelations> filteredStudents = studentListRecycleViewAdapter.getFilteredList();
                students = new ArrayList<>(AppDatabase.getInstance(this).studentDAO().getAllWithRelations());
                ArrayList<StudentWithRelations> uniqueStudents = new ArrayList<>(students);

                for (StudentWithRelations filteredStudent : filteredStudents) {
                    uniqueStudents.removeIf(student -> student.getStudent().getId() == filteredStudent.getStudent().getId());
                }
                students = new ArrayList<>(uniqueStudents);
            }
            ArrayList<String> studentNames = new ArrayList<>();
            for (StudentWithRelations student : students) {
                studentNames.add(student.getUser().getFullName());
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Chọn sinh viên");
            ArrayList<StudentWithRelations> finalStudents = students;
            builder.setItems(studentNames.toArray(new String[0]), (dialog, which) -> {
                StudentWithRelations studentWithRelations = finalStudents.get(which);
                if (isStudentClass) {
                    studentWithRelations.getStudent().setClassId(classId);
                    AppDatabase.getInstance(this).studentDAO().update(studentWithRelations.getStudent());
                }

                if (isStudentSubject) {
                    StudentSemesterCrossRef studentSemesterCrossRef = new StudentSemesterCrossRef();
                    studentSemesterCrossRef.setStudentId(studentWithRelations.getStudent().getId());
                    studentSemesterCrossRef.setSemesterId(semesterId);
                    AppDatabase.getInstance(this).crossRefDAO().insertStudentSemesterCrossRef(studentSemesterCrossRef);

                    StudentSubjectCrossRef studentSubjectCrossRef = new StudentSubjectCrossRef();
                    studentSubjectCrossRef.setStudentId(studentWithRelations.getStudent().getId());
                    studentSubjectCrossRef.setSubjectId(subjectId);
                    AppDatabase.getInstance(this).crossRefDAO().insertStudentSubjectCrossRef(studentSubjectCrossRef);
                }

                studentListRecycleViewAdapter.updateStudentList(studentWithRelations);
            });
            builder.show();
        });
    }
}