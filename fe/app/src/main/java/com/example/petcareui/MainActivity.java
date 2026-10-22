package com.example.petcareui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petcareui.databinding.ActivityMainBinding;
import com.example.petcareui.ui.auth.LoginActivity;
import com.example.petcareui.utils.TokenManager;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);

        // Kiểm tra phiên đăng nhập
        if (!tokenManager.isLoggedIn()) {
            goToLogin();
            return;
        }

        displayUserInfo();
        setupListeners();
    }

    private void displayUserInfo() {
        String name = tokenManager.getUserName();
        String role = tokenManager.getUserRole();

        if (name != null && !name.isEmpty()) {
            binding.tvWelcomeName.setText("Xin chào, " + name + "!");
        }
        if (role != null && !role.isEmpty()) {
            binding.tvUserRole.setText("Vai trò: " + role);
        }
    }

    private void setupListeners() {
        binding.btnLogout.setOnClickListener(v -> {
            tokenManager.clear();
            goToLogin();
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}