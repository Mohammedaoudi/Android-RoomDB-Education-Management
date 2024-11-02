package com.fh.app_student_management.ui.admin;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
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
import com.fh.app_student_management.adapters.admin.UserListRecycleViewAdapter;
import com.fh.app_student_management.data.AppDatabase;
import com.fh.app_student_management.data.entities.Lecturer;
import com.fh.app_student_management.data.entities.User;
import com.fh.app_student_management.data.relations.LecturerAndUser;
import com.fh.app_student_management.utilities.Constants;
import com.fh.app_student_management.utilities.Utils;
import com.fh.app_student_management.utilities.Validator;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.stream.Collectors;

public class UserListActivity extends AppCompatActivity {

    private long userId;
    private Constants.Role selectedRole;
    private UserListRecycleViewAdapter userListRecycleViewAdapter;

    private LinearLayout layoutLecturer;
    private ImageView btnBack;
    private SearchView searchViewLecturer;
    private Button btnAddUser;

    private BottomSheetDialog bottomSheetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_list_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutLecturer), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initLecturerListView();
        handleEventListeners();
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
        userListRecycleViewAdapter.onActivityResult(requestCode, resultCode, data);
    }

    private void initLecturerListView() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        assert bundle != null;
        userId = bundle.getLong(Constants.USER_ID, 0);

        layoutLecturer = findViewById(R.id.layoutLecturer);
        btnBack = findViewById(R.id.btnBack);
        searchViewLecturer = findViewById(R.id.searchViewLecturer);
        btnAddUser = findViewById(R.id.btnAddUser);
        bottomSheetDialog = new BottomSheetDialog(this);

        ArrayList<User> users = AppDatabase.getInstance(this)
                .userDAO()
                .getAllRoleWithoutSelectedRole(Constants.Role.STUDENT)
                .stream()
                .filter(user -> user.getId() != userId).collect(Collectors.toCollection(ArrayList::new));
        RecyclerView rvLecturer = findViewById(R.id.rvLecturer);
        userListRecycleViewAdapter = new UserListRecycleViewAdapter(this, users);
        rvLecturer.setLayoutManager(new LinearLayoutManager(this));
        rvLecturer.setAdapter(userListRecycleViewAdapter);
    }

    private void handleEventListeners() {
        layoutLecturer.setOnClickListener(v -> {
            if (v.getId() == R.id.layoutLecturer) {
                searchViewLecturer.clearFocus();
            }
        });

        btnBack.setOnClickListener(v -> finish());

        searchViewLecturer.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                userListRecycleViewAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                userListRecycleViewAdapter.getFilter().filter(newText);
                return false;
            }
        });

        btnAddUser.setOnClickListener(v -> showAddLecturerDialog());
    }

    @SuppressLint("InflateParams")
    private void showAddLecturerDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.admin_bottom_sheet_add_user, null);
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

        String[] roleNames = {"Quản trị viên", "Chuyên viên", "Giảng viên"};
        view.findViewById(R.id.txtRole).setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Chọn vai trò")
                .setItems(roleNames, (dialog, which) -> {
                    ((EditText) view.findViewById(R.id.txtRole)).setText(roleNames[which]);

                    switch (which) {
                        case 0:
                            selectedRole = Constants.Role.ADMIN;
                            ((LinearLayout) view.findViewById(R.id.edtSpecialization).getParent()).setVisibility(View.GONE);
                            ((LinearLayout) view.findViewById(R.id.edtDegree).getParent()).setVisibility(View.GONE);
                            ((LinearLayout) view.findViewById(R.id.edtCertificate).getParent()).setVisibility(View.GONE);
                            break;
                        case 1:
                            selectedRole = Constants.Role.SPECIALIST;
                            ((LinearLayout) view.findViewById(R.id.edtSpecialization).getParent()).setVisibility(View.GONE);
                            ((LinearLayout) view.findViewById(R.id.edtDegree).getParent()).setVisibility(View.GONE);
                            ((LinearLayout) view.findViewById(R.id.edtCertificate).getParent()).setVisibility(View.GONE);
                            break;
                        case 2:
                            selectedRole = Constants.Role.LECTURER;
                            ((LinearLayout) view.findViewById(R.id.edtSpecialization).getParent()).setVisibility(View.VISIBLE);
                            ((LinearLayout) view.findViewById(R.id.edtDegree).getParent()).setVisibility(View.VISIBLE);
                            ((LinearLayout) view.findViewById(R.id.edtCertificate).getParent()).setVisibility(View.VISIBLE);
                    }
                })
                .show());

        view.findViewById(R.id.edtDob).setOnClickListener(this::showDatePickerDialog);

        view.findViewById(R.id.btnAddUser).setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Thông báo")
                .setMessage("Thêm người dùng mới?")
                .setPositiveButton("Có", (dialog, which) -> performAddUser(view))
                .setNegativeButton("Không", null)
                .show());
    }

    private void performAddUser(View view) {
        if (!validateInputs(view)) return;

        LecturerAndUser lecturerAndUser = new LecturerAndUser();
        lecturerAndUser.setUser(new User());
        lecturerAndUser.setLecturer(null);

        try {
            lecturerAndUser.getUser().setAvatar(Utils.getBytesFromBitmap(Utils.getBitmapFromView(view.findViewById(R.id.avatar))));
            lecturerAndUser.getUser().setEmail(((EditText) view.findViewById(R.id.edtEmail)).getText().toString());
            lecturerAndUser.getUser().setPassword(Utils.hashPassword("123456"));
            lecturerAndUser.getUser().setFullName(((EditText) view.findViewById(R.id.edtFullName)).getText().toString());
            lecturerAndUser.getUser().setDob(Utils.formatDate("dd/MM/YYYY").parse(((EditText) view.findViewById(R.id.edtDob)).getText().toString()));
            lecturerAndUser.getUser().setAddress(((EditText) view.findViewById(R.id.edtAddress)).getText().toString());
            lecturerAndUser.getUser().setRole(selectedRole);
            int genderId = ((RadioGroup) view.findViewById(R.id.radioGroupGender)).getCheckedRadioButtonId();
            lecturerAndUser.getUser().setGender(genderId == R.id.radioButtonMale ? Constants.MALE : Constants.FEMALE);

            if (selectedRole == Constants.Role.LECTURER) {
                lecturerAndUser.setLecturer(new Lecturer());
                lecturerAndUser.getLecturer().setSpecialization(((EditText) view.findViewById(R.id.edtSpecialization)).getText().toString());
                lecturerAndUser.getLecturer().setDegree(((EditText) view.findViewById(R.id.edtDegree)).getText().toString());
                lecturerAndUser.getLecturer().setCertificate(((EditText) view.findViewById(R.id.edtCertificate)).getText().toString());
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        userListRecycleViewAdapter.addUser(lecturerAndUser);
        bottomSheetDialog.dismiss();
        Utils.showToast(this, "Thêm thành công");
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
                && validateNotEmpty(view, R.id.txtRole, "Vai trò không được trống")
                && validateNotEmpty(view, R.id.edtDob, "Ngày sinh không được để trống")
                && validateNotEmpty(view, R.id.edtAddress, "Địa chỉ không được để trống")
                && validateNotEmpty(view, R.id.edtSpecialization, "Chuyên ngành không được để trống")
                && validateNotEmpty(view, R.id.edtDegree, "Bằng cấp không được để trống")
                && validateNotEmpty(view, R.id.edtCertificate, "Chứng chỉ không được để trống");
    }

    private boolean validateNotEmpty(@NonNull View view, int viewId, String errorMessage) {
        EditText editText = view.findViewById(viewId);
        if (((LinearLayout) editText.getParent()).getVisibility() == View.VISIBLE && editText.getText().toString().trim().isEmpty()) {
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
}