package com.fh.app_student_management.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.fh.app_student_management.R;
import com.fh.app_student_management.data.AppDatabase;
import com.fh.app_student_management.data.entities.User;
import com.fh.app_student_management.utilities.Constants;
import com.fh.app_student_management.utilities.Utils;

import java.util.Objects;

public class ChangePasswordActivity extends AppCompatActivity {

    private User user;

    private LinearLayout layoutChangePassword;
    private EditText edtOldPassword;
    private EditText edtNewPassword;
    private EditText edtConfirmNewPassword;
    private Button btnSavePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initChangePasswordView();
        handleEventListener();
    }

    private void initChangePasswordView() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        assert bundle != null;
        long userId = bundle.getLong(Constants.USER_ID, 0);
        user = AppDatabase.getInstance(this).userDAO().getById(userId);

        layoutChangePassword = findViewById(R.id.layoutChangePassword);
        edtOldPassword = findViewById(R.id.edtOldPassword);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmNewPassword = findViewById(R.id.edtConfirmNewPassword);
        btnSavePassword = findViewById(R.id.btnSavePassword);
    }

    private void handleEventListener() {
        layoutChangePassword.setOnClickListener(v -> {
            if (v.getId() == R.id.layoutChangePassword) {
                InputMethodManager inm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                try {
                    inm.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
                } catch (Exception ex) {
                    // Ignore
                }
            }
        });

        btnSavePassword.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Thông báo")
                .setMessage("Xác nhận thay đổi mật khẩu?")
                .setPositiveButton("Có", (dialog, which) -> performChangePassword())
                .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                .show());
    }

    private void performChangePassword() {
        if (!validateInputs()) return;

        if (!Utils.verifyPassword(edtOldPassword.getText().toString(), user.getPassword())) {
            Utils.showToast(this, "Mật khẩu cũ không đúng");
            return;
        }

        if (!edtNewPassword.getText().toString().equals(edtConfirmNewPassword.getText().toString())) {
            Utils.showToast(this, "Mật khẩu mới không khớp");
            return;
        }

        user.setPassword(Utils.hashPassword(edtNewPassword.getText().toString()));
        AppDatabase.getInstance(this).userDAO().update(user);
        Utils.showToast(this, "Đổi mật khẩu thành công");

        SharedPreferences preferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(Constants.USER_ID);
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean validateInputs() {
        return validateNotEmpty(R.id.edtOldPassword, "Mật khẩu cũ không được để trống")
                && validateNotEmpty(R.id.edtNewPassword, "Mật khẩu mới không được để trống")
                && validateNotEmpty(R.id.edtConfirmNewPassword, "Xác nhận mật khẩu mới không được để trống");
    }

    private boolean validateNotEmpty(int viewId, String errorMessage) {
        EditText editText = findViewById(viewId);
        if (editText == null || editText.getText().toString().trim().isEmpty()) {
            Utils.showToast(this, errorMessage);
            return false;
        }
        return true;
    }
}