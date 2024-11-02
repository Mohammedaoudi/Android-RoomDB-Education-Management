package com.fh.app_student_management.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.fh.app_student_management.R;
import com.fh.app_student_management.fragments.HomeFragment;
import com.fh.app_student_management.fragments.SettingFragment;
import com.fh.app_student_management.utilities.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private Map<String, String> params;

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initBottomNavigationView();
        handleEventListener(params);

        loadFragment(HomeFragment.newInstance(params));
    }

    private void initBottomNavigationView() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        assert bundle != null;
        long userId = bundle.getLong(Constants.USER_ID, 0);

        params = new HashMap<>();
        params.put(Constants.USER_ID, String.valueOf(userId));

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
    }

    private void handleEventListener(Map<String, String> params) {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navHome) {
                loadFragment(HomeFragment.newInstance(params));
                return true;
            } else if (itemId == R.id.navSetting) {
                loadFragment(SettingFragment.newInstance(params));
                return true;
            }

            return super.onOptionsItemSelected(item);
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }
}