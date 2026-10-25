package com.example.petcareui.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.petcareui.MainActivity;
import com.example.petcareui.R;
import com.example.petcareui.databinding.ActivityRegisterBinding;
import com.example.petcareui.utils.TokenManager;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private AuthViewModel viewModel;
    private TokenManager tokenManager;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        // Nút quay lại trên App Bar
        binding.btnBack.setOnClickListener(v -> finish());

        // Toggle xem/ẩn mật khẩu
        binding.tvTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
        binding.tvToggleConfirmPassword.setOnClickListener(v -> toggleConfirmPasswordVisibility());

        // Chuyển sang màn hình Đăng nhập
        binding.tvGoToLogin.setOnClickListener(v -> finish());

        // Xử lý tạo tài khoản
        binding.btnRegister.setOnClickListener(v -> {
            if (validateInputs()) {
                String fullName = binding.etFullName.getText().toString().trim();
                String email = binding.etEmail.getText().toString().trim();
                String phone = binding.etPhone.getText().toString().trim();
                String password = binding.etPassword.getText().toString().trim();

                binding.noticeServerFailure.setVisibility(View.GONE);
                viewModel.register(fullName, email, phone, password);
            }
        });
    }

    /**
     * Kiểm tra dữ liệu nhập form (Validation).
     * Mặc định form không hiện lỗi đỏ, chỉ kích hoạt khi bấm nút Tạo tài khoản hoặc người dùng nhập sai.
     */
    private boolean validateInputs() {
        boolean isValid = true;

        String fullName = binding.etFullName.getText() != null ? binding.etFullName.getText().toString().trim() : "";
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";
        String confirmPassword = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString().trim() : "";

        // 1. Kiểm tra Họ và tên
        if (fullName.isEmpty() || fullName.length() < 2) {
            binding.boxFullName.setBackgroundResource(R.drawable.bg_input_field_error);
            binding.layoutErrorFullName.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            binding.boxFullName.setBackgroundResource(R.drawable.bg_input_field);
            binding.layoutErrorFullName.setVisibility(View.GONE);
        }

        // 2. Kiểm tra Email
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.boxEmail.setBackgroundResource(R.drawable.bg_input_field_error);
            binding.ivEmailStatusIcon.setVisibility(View.VISIBLE);
            binding.tvEmailHint.setText("Email chưa hợp lệ");
            binding.tvEmailHint.setTextColor(ContextCompat.getColor(this, R.color.status_destructive_ink));
            isValid = false;
        } else {
            binding.boxEmail.setBackgroundResource(R.drawable.bg_input_field);
            binding.ivEmailStatusIcon.setVisibility(View.GONE);
            binding.tvEmailHint.setText("Dùng email này để nhận thông báo về lịch hẹn");
            binding.tvEmailHint.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
        }

        // 3. Kiểm tra Số điện thoại
        if (phone.isEmpty() || phone.length() < 9) {
            binding.boxPhone.setBackgroundResource(R.drawable.bg_input_field_error);
            binding.layoutErrorPhone.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            binding.boxPhone.setBackgroundResource(R.drawable.bg_input_field);
            binding.layoutErrorPhone.setVisibility(View.GONE);
        }

        // 4. Kiểm tra Mật khẩu (ít nhất 8 ký tự, gồm ít nhất 1 chữ số)
        boolean hasDigit = password.matches(".*\\d.*");
        if (password.length() < 8 || !hasDigit) {
            binding.boxPassword.setBackgroundResource(R.drawable.bg_input_field_error);
            binding.ivPasswordStatusIcon.setVisibility(View.VISIBLE);
            binding.tvPasswordHint.setText("Ít nhất 8 ký tự, gồm một chữ số");
            binding.tvPasswordHint.setTextColor(ContextCompat.getColor(this, R.color.status_destructive_ink));
            isValid = false;
        } else {
            binding.boxPassword.setBackgroundResource(R.drawable.bg_input_field);
            binding.ivPasswordStatusIcon.setVisibility(View.GONE);
            binding.tvPasswordHint.setText("Ít nhất 8 ký tự, gồm một chữ số");
            binding.tvPasswordHint.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
        }

        // 5. Kiểm tra Nhập lại mật khẩu
        if (confirmPassword.isEmpty() || !confirmPassword.equals(password)) {
            binding.boxConfirmPassword.setBackgroundResource(R.drawable.bg_input_field_error);
            binding.layoutErrorConfirmPassword.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            binding.boxConfirmPassword.setBackgroundResource(R.drawable.bg_input_field);
            binding.layoutErrorConfirmPassword.setVisibility(View.GONE);
        }

        return isValid;
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        toggleInputType(binding.etPassword, binding.tvTogglePassword, isPasswordVisible);
    }

    private void toggleConfirmPasswordVisibility() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        toggleInputType(binding.etConfirmPassword, binding.tvToggleConfirmPassword, isConfirmPasswordVisible);
    }

    private void toggleInputType(EditText editText, TextView toggleView, boolean isVisible) {
        if (isVisible) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            toggleView.setText("Ẩn");
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggleView.setText("Hiện");
        }
        editText.setSelection(editText.getText().length());
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.tvBtnRegisterText.setText(isLoading ? "Đang tạo tài khoản…" : "Tạo tài khoản");
            binding.btnRegister.setEnabled(!isLoading);
            binding.etFullName.setEnabled(!isLoading);
            binding.etEmail.setEnabled(!isLoading);
            binding.etPhone.setEnabled(!isLoading);
            binding.etPassword.setEnabled(!isLoading);
            binding.etConfirmPassword.setEnabled(!isLoading);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                binding.tvError.setText(error);
                binding.noticeServerFailure.setVisibility(View.VISIBLE);
            } else {
                binding.noticeServerFailure.setVisibility(View.GONE);
            }
        });

        viewModel.getRegisterResult().observe(this, response -> {
            if (response != null && response.getToken() != null) {
                tokenManager.saveAuthData(
                        response.getToken(),
                        response.getUserId(),
                        response.getFullName(),
                        response.getEmail(),
                        response.getRole(),
                        response.getClinicId()
                );

                Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
