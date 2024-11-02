package com.fh.app_student_management.ui.admin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fh.app_student_management.R;
import ma.ensa.projet.adapters.admin.SubjectListRecycleViewAdapter;
import com.fh.app_student_management.data.AppDatabase;
import com.fh.app_student_management.data.entities.Major;
import com.fh.app_student_management.data.entities.Subject;
import com.fh.app_student_management.data.relations.SubjectWithRelations;
import com.fh.app_student_management.utilities.Utils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;

public class SubjectListActivity extends AppCompatActivity {

    private Class selectedClass;
    private ArrayList<Class> classes;
    private String[] classNames;
    private Major selectedMajor;
    private ArrayList<Major> majors;
    private String[] majorNames;
    private SubjectListRecycleViewAdapter subjectListRecycleViewAdapter;

    private LinearLayout layoutSubject;
    private ImageView btnBack;
    private SearchView searchViewSubject;
    private Button btnAddSubject;

    private BottomSheetDialog bottomSheetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_list_subject);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutSubject), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initSubjectListView();
        handleEventListener();
    }

    private void initSubjectListView() {
        layoutSubject = findViewById(R.id.layoutSubject);
        btnBack = findViewById(R.id.btnBack);
        searchViewSubject = findViewById(R.id.searchViewSubject);
        btnAddSubject = findViewById(R.id.btnAddSubject);
        bottomSheetDialog = new BottomSheetDialog(this);

        classes = new ArrayList<>(AppDatabase.getInstance(this).classDAO().getAll());
        classNames = new String[classes.size()];
        for (int i = 0; i < classes.size(); i++) {
            classNames[i] = classes.get(i).getName();
        }

        majors = new ArrayList<>(AppDatabase.getInstance(this).majorDAO().getAll());
        majorNames = new String[majors.size()];
        for (int i = 0; i < majors.size(); i++) {
            majorNames[i] = majors.get(i).getName();
        }

        ArrayList<SubjectWithRelations> subjects = new ArrayList<>(AppDatabase.getInstance(this)
                .subjectDAO().getAllWithRelations());
        RecyclerView rvSubject = findViewById(R.id.rvSubject);
        subjectListRecycleViewAdapter = new SubjectListRecycleViewAdapter(this, subjects);
        rvSubject.setLayoutManager(new LinearLayoutManager(this));
        rvSubject.setAdapter(subjectListRecycleViewAdapter);
    }

    @SuppressLint("InflateParams")
    private void handleEventListener() {
        layoutSubject.setOnClickListener(v -> {
            if (v.getId() == R.id.layoutSubject) {
                searchViewSubject.clearFocus();
            }
        });

        btnBack.setOnClickListener(v -> finish());

        searchViewSubject.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                subjectListRecycleViewAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                subjectListRecycleViewAdapter.getFilter().filter(newText);
                return false;
            }
        });

        btnAddSubject.setOnClickListener(v -> showAddSubjectDialog());
    }

    @SuppressLint("InflateParams")
    private void showAddSubjectDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.admin_bottom_sheet_add_subject, null);
        bottomSheetDialog.setContentView(view);
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) view.getParent());
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setSkipCollapsed(true);
        bottomSheetDialog.show();

        view.findViewById(R.id.edtClass).setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Chọn lớp")
                .setItems(classNames, (dialog, which) -> {
                    selectedClass = classes.get(which);
                    ((EditText) view.findViewById(R.id.edtClass)).setText(classNames[which]);
                })
                .show());

        view.findViewById(R.id.edtMajor).setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Chọn ngành")
                .setItems(majorNames, (dialog, which) -> {
                    selectedMajor = majors.get(which);
                    ((EditText) view.findViewById(R.id.edtMajor)).setText(majorNames[which]);
                })
                .show());

        view.findViewById(R.id.btnAddSubject).setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Thông báo")
                .setMessage("Thêm môn học mới?")
                .setPositiveButton("Có", (dialog, which) -> performAddSubject(view))
                .setNegativeButton("Không", null)
                .show());
    }

    private void performAddSubject(View view) {
        if (!validateInputs(view)) return;

        SubjectWithRelations subjectWithRelations = new SubjectWithRelations();
        subjectWithRelations.setSubject(new Subject());

        subjectWithRelations.getSubject().setName(((EditText) view.findViewById(R.id.edtSubjectName)).getText().toString());
        subjectWithRelations.getSubject().setCredits(Integer.parseInt(((EditText) view.findViewById(R.id.edtSubjectCredits)).getText().toString()));
        subjectWithRelations.getSubject().setClassId(selectedClass.getId());
        subjectWithRelations.getSubject().setMajorId(selectedMajor.getId());
        subjectWithRelations.setClazz(selectedClass);
        subjectWithRelations.setMajor(selectedMajor);

        subjectListRecycleViewAdapter.addSubject(subjectWithRelations);
        bottomSheetDialog.dismiss();
        Utils.showToast(this, "Thêm thành công");
    }

    private boolean validateInputs(View view) {
        return validateNotEmpty(view, R.id.edtSubjectName, "Tên môn không được để trống")
                && validateNotEmpty(view, R.id.edtSubjectCredits, "Số tín chỉ không được để trống")
                && validateNotEmpty(view, R.id.edtClass, "Lớp không được để trống")
                && validateNotEmpty(view, R.id.edtMajor, "Ngành không được để trống");
    }

    private boolean validateNotEmpty(@NonNull View view, int viewId, String errorMessage) {
        EditText editText = view.findViewById(viewId);
        if (editText == null || editText.getText().toString().trim().isEmpty()) {
            Utils.showToast(this, errorMessage);
            return false;
        }
        return true;
    }
}