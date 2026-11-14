package com.example.petcareui.ui.auth;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.petcareui.MainActivity;
import com.example.petcareui.R;
import com.example.petcareui.databinding.ActivityLoginBinding;
import com.example.petcareui.ui.staff.StaffDashboardActivity;
import com.example.petcareui.utils.TokenManager;
import com.example.petcareui.utils.UiMotionUtils;

/**
 * Màn hình Đăng nhập (Login) - PETCARE Design System 1.0 (Warm & Cute - Modern Veterinary).
 * - Sử dụng ViewModel và LiveData quan sát kết quả đăng nhập và lỗi từ Backend.
 * - Hỗ trợ đầy đủ các trạng thái: Default, Focus, Validation Error, Loading, API Error, Success.
 * - Điều hướng theo vai trò (CUSTOMER -> MainActivity, STAFF -> StaffDashboardActivity).
 */
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

        // Nếu đã đăng nhập trước đó thì chuyển thẳng theo vai trò
        if (tokenManager.isLoggedIn()) {
            navigateByRole(tokenManager.getUserRole());
            return;
        }

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        UiMotionUtils.applyWindowInsets(binding.getRoot(), binding.layoutLoginHeader, null);

        setupRegisterLinkSpannable();
        setupInputInteractions();
        setupListeners();
        observeViewModel();
    }

    private void setupRegisterLinkSpannable() {
        String fullText = "Chưa có tài khoản? Đăng ký";
        SpannableString spannable = new SpannableString(fullText);

        int startIndex = fullText.indexOf("Đăng ký");
        if (startIndex != -1) {
            int endIndex = startIndex + "Đăng ký".length();
            int coralDarkColor = ContextCompat.getColor(this, R.color.ds_primary_dark);

            spannable.setSpan(new ForegroundColorSpan(coralDarkColor), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        binding.tvGoToRegister.setText(spannable);
    }

    private void setupInputInteractions() {
        // Focus state styling for Email box
        binding.etEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (binding.layoutErrorEmail.getVisibility() != View.VISIBLE) {
                binding.boxEmail.setBackgroundResource(hasFocus ? R.drawable.bg_input_field_focus : R.drawable.bg_input_field);
            }
        });

        // Focus state styling for Password box
        binding.etPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (binding.layoutErrorPassword.getVisibility() != View.VISIBLE) {
                binding.boxPassword.setBackgroundResource(hasFocus ? R.drawable.bg_input_field_focus : R.drawable.bg_input_field);
            }
        });

        // Clear inline errors on text edit
        binding.etEmail.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (binding.layoutErrorEmail.getVisibility() == View.VISIBLE) {
                    binding.layoutErrorEmail.setVisibility(View.GONE);
                    binding.boxEmail.setBackgroundResource(binding.etEmail.hasFocus() ? R.drawable.bg_input_field_focus : R.drawable.bg_input_field);
                }
                binding.noticeError.setVisibility(View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (binding.layoutErrorPassword.getVisibility() == View.VISIBLE) {
                    binding.layoutErrorPassword.setVisibility(View.GONE);
                    binding.boxPassword.setBackgroundResource(binding.etPassword.hasFocus() ? R.drawable.bg_input_field_focus : R.drawable.bg_input_field);
                }
                binding.noticeError.setVisibility(View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupListeners() {
        UiMotionUtils.attachSpringTouch(binding.btnLogin, binding.tvGoToRegister);

        // Toggle ẩn/hiện mật khẩu
        binding.tvTogglePassword.setOnClickListener(v -> togglePasswordVisibility());

        // Nút Đăng nhập
        binding.btnLogin.setOnClickListener(v -> {
            if (validateInputs()) {
                String email = binding.etEmail.getText().toString().trim();
                String password = binding.etPassword.getText().toString().trim();

                binding.noticeError.setVisibility(View.GONE);
                viewModel.login(email, password);
            }
        });

        // Chuyển sang màn hình Đăng ký
        binding.tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_scale_out);
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

        // Kiểm tra Email
        if (email.isEmpty()) {
            binding.boxEmail.setBackgroundResource(R.drawable.bg_input_field_error);
            binding.tvErrorEmail.setText("Vui lòng nhập email của bạn.");
            binding.layoutErrorEmail.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.boxEmail.setBackgroundResource(R.drawable.bg_input_field_error);
            binding.tvErrorEmail.setText("Email chưa đúng định dạng (ví dụ: ten@email.com).");
            binding.layoutErrorEmail.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            binding.layoutErrorEmail.setVisibility(View.GONE);
            binding.boxEmail.setBackgroundResource(R.drawable.bg_input_field);
        }

        // Kiểm tra Mật khẩu
        if (password.isEmpty()) {
            binding.boxPassword.setBackgroundResource(R.drawable.bg_input_field_error);
            binding.tvErrorPassword.setText("Vui lòng nhập mật khẩu.");
            binding.layoutErrorPassword.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            binding.layoutErrorPassword.setVisibility(View.GONE);
            binding.boxPassword.setBackgroundResource(R.drawable.bg_input_field);
        }

        return isValid;
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

    private void observeViewModel() {
        // Trạng thái Loading
        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.tvBtnLoginText.setText(isLoading ? "Đang đăng nhập…" : "Đăng nhập");
            binding.btnLogin.setEnabled(!isLoading);
            binding.etEmail.setEnabled(!isLoading);
            binding.etPassword.setEnabled(!isLoading);
        });

        // Trạng thái API Error
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                binding.tvError.setText(error);
                binding.noticeError.setVisibility(View.VISIBLE);
                // Cuộn nhẹ lên nếu cần để người dùng nhìn thấy banner lỗi
                binding.scrollLoginRoot.smoothScrollTo(0, binding.noticeError.getTop() - 40);
            } else {
                binding.noticeError.setVisibility(View.GONE);
            }
        });

        // Trạng thái Thành công (Success)
        viewModel.getLoginResult().observe(this, response -> {
            if (response != null && response.getToken() != null) {
                tokenManager.saveAuthData(
                        response.getToken(),
                        response.getUserId(),
                        response.getFullName(),
                        response.getEmail(),
                        response.getRole(),
                        response.getClinicId()
                );

                Toast.makeText(LoginActivity.this, "Chào mừng " + response.getFullName() + "!", Toast.LENGTH_SHORT).show();
                navigateByRole(response.getRole());
            }
        });
    }

    private void navigateByRole(String role) {
        Intent intent;
        if ("STAFF".equalsIgnoreCase(role)) {
            intent = new Intent(LoginActivity.this, StaffDashboardActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, MainActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
