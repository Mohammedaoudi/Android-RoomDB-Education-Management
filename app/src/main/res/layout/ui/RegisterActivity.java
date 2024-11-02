package com.fh.app_student_management.ui;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.fh.app_student_management.R;
import com.fh.app_student_management.data.AppDatabase;
import com.fh.app_student_management.data.entities.User;
import com.fh.app_student_management.utilities.Constants;
import com.fh.app_student_management.utilities.Utils;
import com.fh.app_student_management.utilities.Validator;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private LinearLayout layoutRegister;
    private EditText edtFullName;
    private EditText edtEmail;
    private EditText edtPassword;
    private EditText edtConfirmPassword;
    private Button btnRegister;
    private TextView btnToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initRegisterView();
        handleEventListener();
    }

    private void initRegisterView() {
        layoutRegister = findViewById(R.id.layoutRegister);
        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnToLogin = findViewById(R.id.txtLogin);
    }

    private void handleEventListener() {
        layoutRegister.setOnClickListener(v -> {
            if (v.getId() == R.id.layoutRegister) {
                InputMethodManager inm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                try {
                    inm.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
                } catch (Exception ex) {
                    // Ignore
                }
            }
        });

        btnRegister.setOnClickListener(v -> performRegister());

        btnToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void performRegister() {
        if (!validateInputs()) return;

        if (!edtPassword.getText().toString().equals(edtConfirmPassword.getText().toString())) {
            Utils.showToast(this, "Mật khẩu không khớp!");
            return;
        }

        User user = new User();
        user.setFullName(edtFullName.getText().toString().trim());
        user.setEmail(edtEmail.getText().toString().trim());
        user.setPassword(Utils.hashPassword(edtPassword.getText().toString()));
        user.setAvatar(Utils.getBytesFromDrawable(this, R.drawable.default_avatar));
        user.setRole(Constants.Role.SPECIALIST);

        try {
            AppDatabase.getInstance(this).userDAO().insert(user);
        } catch (SQLiteConstraintException ex) {
            Utils.showToast(this, "Email đã tồn tại!");
            return;
        } catch (Exception ex) {
            Utils.showToast(this, "Đăng ký thất bại!");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thông báo");
        builder.setMessage("Đăng ký thành công. Vui lòng đăng nhập để tiếp tục.");
        builder.setPositiveButton("OK", (dialog, which) -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
        builder.show();
    }

    private boolean validateInputs() {
        return validateNotEmpty(R.id.edtFullName, "Họ và tên không được để trống")
                && validateNotEmpty(R.id.edtEmail, "Email không được để trống")
                && validateEmail(R.id.edtEmail)
                && validateNotEmpty(R.id.edtPassword, "Mật khẩu không được để trống")
                && validateNotEmpty(R.id.edtConfirmPassword, "Xác nhận mật khẩu không được để trống");
    }

    private boolean validateNotEmpty(int viewId, String errorMessage) {
        EditText editText = findViewById(viewId);
        if (editText == null || editText.getText().toString().trim().isEmpty()) {
            Utils.showToast(this, errorMessage);
            return false;
        }
        return true;
    }

    private boolean validateEmail(int viewId) {
        EditText editText = findViewById(viewId);
        if (editText != null && !Validator.isValidEmail(editText.getText().toString())) {
            Utils.showToast(this, "Email không hợp lệ");
            return false;
        }
        return true;
    }
}