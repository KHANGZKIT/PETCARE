package com.example.petcareui.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.petcareui.MainActivity;
import com.example.petcareui.databinding.ActivityLoginBinding;
import com.example.petcareui.utils.TokenManager;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel viewModel;
    private TokenManager tokenManager;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);

        // Nếu đã đăng nhập trước đó thì chuyển thẳng vào màn hình chính
        if (tokenManager.isLoggedIn()) {
            goToMainActivity();
            return;
        }

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        // Toggle ẩn/hiện mật khẩu
        binding.tvTogglePassword.setOnClickListener(v -> togglePasswordVisibility());

        // Nút Đăng nhập
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
            String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";
            
            if (email.isEmpty()) {
                showError("Vui lòng nhập email.");
                return;
            }
            if (password.isEmpty()) {
                showError("Vui lòng nhập mật khẩu.");
                return;
            }

            // Ẩn thông báo lỗi trước khi gửi request
            binding.noticeError.setVisibility(View.GONE);
            viewModel.login(email, password);
        });

        // Nút Google
        binding.btnGoogleLogin.setOnClickListener(v -> {
            Toast.makeText(this, "Đăng nhập Google sẽ sớm khả dụng", Toast.LENGTH_SHORT).show();
        });

        // Quên mật khẩu
        binding.tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng quên mật khẩu đang được phát triển", Toast.LENGTH_SHORT).show();
        });

        // Chuyển sang màn hình Đăng ký
        binding.tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            binding.etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            binding.tvTogglePassword.setText("Ẩn");
        } else {
            binding.etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            binding.tvTogglePassword.setText("Hiện");
        }
        // Giữ con trỏ ở cuối văn bản
        binding.etPassword.setSelection(binding.etPassword.getText().length());
    }

    private void showError(String message) {
        binding.tvError.setText(message);
        binding.noticeError.setVisibility(View.VISIBLE);
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.tvBtnLoginText.setText(isLoading ? "Đang đăng nhập…" : "Đăng nhập");
            binding.btnLogin.setEnabled(!isLoading);
            binding.etEmail.setEnabled(!isLoading);
            binding.etPassword.setEnabled(!isLoading);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
            } else {
                binding.noticeError.setVisibility(View.GONE);
            }
        });

        viewModel.getLoginResult().observe(this, response -> {
            if (response != null && response.getToken() != null) {
                // Lưu token và thông tin người dùng vào SharedPreferences
                tokenManager.saveAuthData(
                        response.getToken(),
                        response.getUserId(),
                        response.getFullName(),
                        response.getEmail(),
                        response.getRole(),
                        response.getClinicId()
                );

                Toast.makeText(LoginActivity.this, "Chào mừng " + response.getFullName() + "!", Toast.LENGTH_SHORT).show();
                goToMainActivity();
            }
        });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
