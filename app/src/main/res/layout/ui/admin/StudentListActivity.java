package com.fh.app_student_management.ui.admin;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.fh.app_student_management.data.entities.AcademicYear;
import com.fh.app_student_management.data.entities.Major;
import com.fh.app_student_management.data.entities.Semester;
import com.fh.app_student_management.data.entities.Student;
import com.fh.app_student_management.data.entities.User;
import com.fh.app_student_management.data.relations.StudentWithRelations;
import com.fh.app_student_management.data.relations.SubjectWithRelations;
import com.fh.app_student_management.utilities.Constants;
import com.fh.app_student_management.utilities.Utils;
import com.fh.app_student_management.utilities.Validator;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StudentListActivity extends AppCompatActivity {

    private ArrayList<Semester> semesters;
    private ArrayList<String> semesterNames;
    private long selectedSemesterId;
    private ArrayList<SubjectWithRelations> subjects;
    private ArrayList<String> subjectNames;
    private long selectedSubjectId;
    private ArrayList<Major> majors;
    private ArrayList<String> majorNames;
    private long selectedMajorId;
    private ArrayList<Class> classes;
    private ArrayList<String> classNames;
    private long selectedClassId;
    private ArrayList<AcademicYear> academicYears;
    private ArrayList<String> academicYearNames;
    private long selectedAcademicYearId;
    private StudentListRecycleViewAdapter studentListRecycleViewAdapter;

    private RelativeLayout layoutStudent;
    private ImageView btnBack;
    private SearchView searchViewStudent;
    private Button btnAddStudent;
    private ImageView btnFilter;

    private BottomSheetDialog bottomSheetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_list_student);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutStudent), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initAddStudentView();
        handleEventListener();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            ImageView avatar = bottomSheetDialog.findViewById(R.id.avatar);
            if (avatar != null) {
                avatar.setImageURI(uri);
            }
        }
    }

    private void initAddStudentView() {
        layoutStudent = findViewById(R.id.layoutStudent);
        btnBack = findViewById(R.id.btnBack);
        searchViewStudent = findViewById(R.id.searchViewStudent);
        btnAddStudent = findViewById(R.id.btnAddStudent);
        btnFilter = findViewById(R.id.btnFilter);
        bottomSheetDialog = new BottomSheetDialog(this);

        semesters = new ArrayList<>(AppDatabase.getInstance(this).semesterDAO().getAll());
        semesterNames = new ArrayList<>(semesters.size() + 1);
        semesterNames.add(0, "--- Chọn học kỳ ---");
        for (int i = 0; i < semesters.size(); i++) {
            String startDate = Utils.formatDate("MM/yyyy").format(semesters.get(i).getStartDate());
            String endDate = Utils.formatDate("MM/yyyy").format(semesters.get(i).getEndDate());
            String semesterName = String.format("%s (%s - %s)", semesters.get(i).getName(), startDate, endDate);
            semesterNames.add(semesterName);
        }

        majors = new ArrayList<>(AppDatabase.getInstance(this).majorDAO().getAll());
        majorNames = new ArrayList<>(majors.size() + 1);
        majorNames.add(0, "--- Chọn ngành ---");
        for (int i = 0; i < majors.size(); i++) {
            majorNames.add(majors.get(i).getName());
        }

        classes = new ArrayList<>(AppDatabase.getInstance(this).classDAO().getAll());
        classNames = new ArrayList<>(classes.size() + 1);
        classNames.add(0, "--- Chọn lớp ---");
        for (int i = 0; i < classes.size(); i++) {
            classNames.add(classes.get(i).getName());
        }

        academicYears = new ArrayList<>(AppDatabase.getInstance(this).academicYearDAO().getAll());
        academicYearNames = new ArrayList<>(academicYears.size() + 1);
        academicYearNames.add(0, "--- Chọn khóa học ---");
        for (int i = 0; i < academicYears.size(); i++) {
            academicYearNames.add(academicYears.get(i).getName());
        }

        ArrayList<StudentWithRelations> students = new ArrayList<>(AppDatabase.getInstance(this)
                .studentDAO().getAllWithRelations());
        studentListRecycleViewAdapter = new StudentListRecycleViewAdapter(this, students);
        RecyclerView rvStudent = findViewById(R.id.rvStudent);
        rvStudent.setLayoutManager(new LinearLayoutManager(this));
        rvStudent.setAdapter(studentListRecycleViewAdapter);
    }

    @SuppressLint("InflateParams")
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

        btnAddStudent.setOnClickListener(v -> showAddStudentDialog());

        btnFilter.setOnClickListener(v -> showFilterStudentDialog());
    }

    @SuppressLint("InflateParams")
    private void showAddStudentDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.admin_bottom_sheet_add_student, null);
        bottomSheetDialog.setContentView(view);
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) view.getParent());
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setSkipCollapsed(true);
        behavior.setDraggable(false);
        bottomSheetDialog.show();

        view.findViewById(R.id.iconCamera).setOnClickListener(v -> ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start());

        view.findViewById(R.id.edtDob).setOnClickListener(this::showDatePickerDialog);

        view.findViewById(R.id.edtMajor).setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Chọn lớp")
                .setItems(majorNames.toArray(new CharSequence[0]), (dialog, which) -> {
                    selectedMajorId = majors.get(which - 1).getId();
                    ((EditText) view.findViewById(R.id.edtMajor)).setText(majorNames.get(which));
                })
                .show());

        view.findViewById(R.id.edtClass).setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Chọn ngành")
                .setItems(classNames.toArray(new CharSequence[0]), (dialog, which) -> {
                    selectedClassId = classes.get(which - 1).getId();
                    ((EditText) view.findViewById(R.id.edtClass)).setText(classNames.get(which));
                })
                .show());

        view.findViewById(R.id.edtAcademicYear).setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Chọn năm học")
                .setItems(academicYearNames.toArray(new CharSequence[0]), (dialog, which) -> {
                    selectedAcademicYearId = academicYears.get(which - 1).getId();
                    ((EditText) view.findViewById(R.id.edtAcademicYear)).setText(academicYearNames.get(which));
                })
                .show());

        view.findViewById(R.id.btnAddStudent).setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Thông báo")
                .setMessage("Thêm sinh viên mới?")
                .setPositiveButton("Có", (dialog, which) -> performAddStudent(view))
                .setNegativeButton("Không", null)
                .show());
    }

    private void performAddStudent(View view) {
        if (!validateInputs(view)) return;

        StudentWithRelations studentWithRelations = new StudentWithRelations();
        studentWithRelations.setUser(new User());
        studentWithRelations.setStudent(new Student());

        try {
            studentWithRelations.getUser().setAvatar(Utils.getBytesFromBitmap(Utils.getBitmapFromView(view.findViewById(R.id.avatar))));
            studentWithRelations.getUser().setEmail(((EditText) view.findViewById(R.id.edtEmail)).getText().toString());
            studentWithRelations.getUser().setPassword(Utils.hashPassword("123456"));
            studentWithRelations.getUser().setFullName(((EditText) view.findViewById(R.id.edtFullName)).getText().toString());
            studentWithRelations.getUser().setDob(Utils.formatDate("dd/MM/YYYY").parse(((EditText) view.findViewById(R.id.edtDob)).getText().toString()));
            studentWithRelations.getUser().setAddress(((EditText) view.findViewById(R.id.edtAddress)).getText().toString());
            studentWithRelations.getUser().setRole(Constants.Role.STUDENT);
            studentWithRelations.getStudent().setMajorId(selectedMajorId);
            studentWithRelations.getStudent().setClassId(selectedClassId);
            studentWithRelations.getStudent().setAcademicYearId(selectedAcademicYearId);

            int genderId = ((RadioGroup) view.findViewById(R.id.radioGroupGender)).getCheckedRadioButtonId();
            studentWithRelations.getUser().setGender(genderId == R.id.radioButtonMale ? Constants.MALE : Constants.FEMALE);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        studentListRecycleViewAdapter.addStudent(studentWithRelations);
        bottomSheetDialog.dismiss();
    }

    private void showDatePickerDialog(View view) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (v, year, month, day) -> {
            String date = day + "/" + (month + 1) + "/" + year;
            ((TextView) view).setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private boolean validateInputs(View view) {
        return validateNotEmpty(view, R.id.edtEmail, "Email không được để trống")
                && validateEmail(view, R.id.edtEmail)
                && validateNotEmpty(view, R.id.edtFullName, "Họ và tên không được để trống")
                && validateNotEmpty(view, R.id.edtDob, "Ngày sinh không được để trống")
                && validateNotEmpty(view, R.id.edtAddress, "Địa chỉ không được để trống")
                && validateNotEmpty(view, R.id.edtMajor, "Ngành không được để trống")
                && validateNotEmpty(view, R.id.edtClass, "Lớp không được để trống")
                && validateNotEmpty(view, R.id.edtAcademicYear, "Năm học không được để trống");
    }

    private boolean validateNotEmpty(@NonNull View view, int viewId, String errorMessage) {
        EditText editText = view.findViewById(viewId);
        if (editText == null || editText.getText().toString().trim().isEmpty()) {
            Utils.showToast(this, errorMessage);
            return false;
        }
        return true;
    }

    private boolean validateEmail(@NonNull View view, int viewId) {
        EditText editText = view.findViewById(viewId);
        if (editText != null && !Validator.isValidEmail(editText.getText().toString())) {
            Utils.showToast(this, "Email không hợp lệ");
            return false;
        }
        return true;
    }

    @SuppressLint("InflateParams")
    private void showFilterStudentDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.admin_bottom_sheet_filter_student, null);
        bottomSheetDialog.setContentView(view);
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) view.getParent());
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setSkipCollapsed(true);
        behavior.setDraggable(false);
        bottomSheetDialog.show();

        EditText edtSemester = view.findViewById(R.id.edtSemester);
        EditText edtClass = view.findViewById(R.id.edtClass);
        EditText edtSubject = view.findViewById(R.id.edtSubject);

        edtSemester.setOnClickListener(v -> showSelectionDialog("Chọn học kỳ", semesterNames, (dialog, which) -> {
                    if (which == 0) {
                        resetSelections(edtSemester, edtClass, edtSubject);
                    } else {
                        selectedSemesterId = semesters.get(which - 1).getId();
                        edtSemester.setText(semesterNames.get(which));
                        resetSelections(edtClass, edtSubject);
                    }
                }
        ));

        edtClass.setOnClickListener(v -> {
            if (edtSemester.getText().toString().isEmpty()) {
                Utils.showToast(this, "Chưa chọn học kỳ");
                return;
            }

            showSelectionDialog("Chọn lớp", classNames, (dialog, which) -> {
                        if (which == 0) {
                            resetSelections(edtClass, edtSubject);
                        } else {
                            selectedClassId = classes.get(which - 1).getId();
                            edtClass.setText(classNames.get(which));
                            resetSelections(edtSubject);
                        }
                    }
            );
        });

        edtSubject.setOnClickListener(v -> {
            if (edtSemester.getText().toString().isEmpty()) {
                Utils.showToast(this, "Chưa chọn học kỳ");
                return;
            }

            if (edtClass.getText().toString().isEmpty()) {
                Utils.showToast(this, "Chưa chọn lớp");
                return;
            }

            subjects = new ArrayList<>(AppDatabase.getInstance(this)
                    .subjectDAO().getBySemesterClass(selectedSemesterId, selectedClassId));
            subjectNames = new ArrayList<>(subjects.size() + 1);
            subjectNames.add(0, "--- Chọn môn học ---");
            for (SubjectWithRelations subject : subjects) {
                subjectNames.add(subject.getSubject().getName());
            }

            showSelectionDialog("Chọn môn học", subjectNames, (dialog, which) -> {
                        if (which == 0) {
                            resetSelections(edtSubject);
                        } else {
                            selectedSubjectId = subjects.get(which - 1).getSubject().getId();
                            edtSubject.setText(subjectNames.get(which));
                        }
                    }
            );
        });

        view.findViewById(R.id.btnConfirm).setOnClickListener(v -> updateStudentList());
    }

    private void updateStudentList() {
        List<StudentWithRelations> students = null;

        if (selectedSemesterId > 0) {
            if (selectedClassId > 0) {
                if (selectedSubjectId > 0) {
                    students = AppDatabase.getInstance(this)
                            .studentDAO().getBySemesterClassSubject(selectedSemesterId, selectedClassId, selectedSubjectId);
                } else {
                    students = AppDatabase.getInstance(this).studentDAO().getBySemesterClass(selectedSemesterId, selectedClassId);
                }
            } else {
                students = AppDatabase.getInstance(this).studentDAO().getBySemester(selectedSemesterId);
            }
        }

        if (students != null) {
            studentListRecycleViewAdapter.setFilteredList(new ArrayList<>(students));
        } else {
            studentListRecycleViewAdapter.resetFilteredList();
        }
        searchViewStudent.setQuery("", false);
        searchViewStudent.clearFocus();
        bottomSheetDialog.dismiss();
    }

    private void showSelectionDialog(String title, @NonNull List<String> options, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setItems(options.toArray(new CharSequence[0]), listener);
        builder.show();
    }

    private void resetSelections(@NonNull EditText edtSemester, EditText edtClass, EditText edtSubject) {
        studentListRecycleViewAdapter.resetFilteredList();
        edtSemester.setText("");
        selectedSemesterId = 0;
        resetSelections(edtClass, edtSubject);
    }

    private void resetSelections(@NonNull EditText edtClass, EditText edtSubject) {
        edtClass.setText("");
        selectedClassId = 0;
        resetSelections(edtSubject);
    }

    private void resetSelections(@NonNull EditText edtSubject) {
        edtSubject.setText("");
        selectedSubjectId = 0;
        subjectNames = null;
    }
}