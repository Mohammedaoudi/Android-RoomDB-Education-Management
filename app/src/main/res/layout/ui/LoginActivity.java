package com.fh.app_student_management.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
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

public class LoginActivity extends AppCompatActivity {

    private LinearLayout layoutLogin;
    private EditText edtEmail;
    private EditText edtPassword;
    private CheckBox chkRememberPassword;
    private Button btnLogin;
    private TextView btnToRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initLoginView();
        handleEventListener();
    }

    @SuppressLint("SetTextI18n")
    private void initLoginView() {
        layoutLogin = findViewById(R.id.layoutLogin);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        chkRememberPassword = findViewById(R.id.chkRememberPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnToRegister = findViewById(R.id.txtRegister);

        // TODO: TEMP
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(new CharSequence[]{"Admin", "Chuyên viên", "Giảng viên"}, (dialog, which) -> {
            switch (which) {
                case 0:
                    edtEmail.setText("admin@gmail.com");
                    edtPassword.setText("admin@123");
                    break;
                case 1:
                    edtEmail.setText("specialist1@gmail.com");
                    edtPassword.setText("user@123");
                    break;
                case 2:
                    edtEmail.setText("lecturer1@gmail.com");
                    edtPassword.setText("user@123");
                    break;
            }
        });
        builder.show();
        chkRememberPassword.setChecked(true);
    }

    private void handleEventListener() {
        layoutLogin.setOnClickListener(v -> {
            if (v.getId() == R.id.layoutLogin) {
                InputMethodManager inm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                try {
                    inm.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
                } catch (Exception ex) {
                    // Ignore
                }
            }
        });

        btnLogin.setOnClickListener(v -> performLogin());

        btnToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void performLogin() {
        if (!validateInputs()) return;

        User user = AppDatabase.getInstance(this).userDAO().getByEmail(edtEmail.getText().toString().trim());

        if (user == null ||
                !Utils.verifyPassword(edtPassword.getText().toString().trim(), user.getPassword())) {
            Utils.showToast(this, "Email hoặc mật khẩu không chính xác!");
            return;
        }

        if (chkRememberPassword.isChecked()) {
            SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putLong(Constants.USER_ID, user.getId());

            editor.apply();
        }

        Intent intent = new Intent(this, HomeActivity.class);
        Bundle bundle = new Bundle();
        bundle.putLong(Constants.USER_ID, user.getId());
        intent.putExtras(bundle);

        startActivity(intent);
        finish();
    }

    private boolean validateInputs() {
        return validateNotEmpty(R.id.edtEmail, "Email không được để trống")
                && validateEmail(R.id.edtEmail)
                && validateNotEmpty(R.id.edtPassword, "Mật khẩu không được để trống");
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