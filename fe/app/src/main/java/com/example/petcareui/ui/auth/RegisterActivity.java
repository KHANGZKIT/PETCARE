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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.petcareui.MainActivity;
import com.example.petcareui.R;
import com.example.petcareui.databinding.ActivityRegisterBinding;
import com.example.petcareui.utils.TokenManager;
import com.example.petcareui.utils.UiMotionUtils;

/**
 * Màn hình Đăng ký tài khoản (Register) - PETCARE Design System 1.0 (Warm & Cute - Modern Veterinary).
 * - Sử dụng ViewModel và LiveData quan sát kết quả đăng ký và lỗi từ Backend.
 * - Hỗ trợ đầy đủ các trạng thái: Default, Focus, Validation Error, Password Mismatch, Loading, API Error, Registration Success.
 * - Sau khi đăng ký thành công, tự động lưu thông tin xác thực và chuyển vào MainActivity.
 */
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

        UiMotionUtils.applyWindowInsets(binding.getRoot(), binding.btnBack, null);

        setupLoginLinkSpannable();
        setupInputInteractions();
        setupListeners();
        observeViewModel();
    }

    private void setupLoginLinkSpannable() {
        String fullText = "Đã có tài khoản? Đăng nhập";
        SpannableString spannable = new SpannableString(fullText);

        int startIndex = fullText.indexOf("Đăng nhập");
        if (startIndex != -1) {
            int endIndex = startIndex + "Đăng nhập".length();
            int coralDarkColor = ContextCompat.getColor(this, R.color.ds_primary_dark);

            spannable.setSpan(new ForegroundColorSpan(coralDarkColor), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        binding.tvGoToLogin.setText(spannable);
    }

    private void setupInputInteractions() {
        attachFocusHandler(binding.boxFullName, binding.etFullName, binding.layoutErrorFullName);
        attachFocusHandler(binding.boxEmail, binding.etEmail, binding.layoutErrorEmail);
        attachFocusHandler(binding.boxPhone, binding.etPhone, binding.layoutErrorPhone);
        attachFocusHandler(binding.boxPassword, binding.etPassword, null);
        attachFocusHandler(binding.boxConfirmPassword, binding.etConfirmPassword, binding.layoutErrorConfirmPassword);
    }

    private void attachFocusHandler(LinearLayout box, EditText editText, View errorContainer) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            boolean hasError = errorContainer != null && errorContainer.getVisibility() == View.VISIBLE;
            if (!hasError) {
                box.setBackgroundResource(hasFocus ? R.drawable.bg_input_field_focus : R.drawable.bg_input_field);
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (errorContainer != null && errorContainer.getVisibility() == View.VISIBLE) {
                    errorContainer.setVisibility(View.GONE);
                    box.setBackgroundResource(editText.hasFocus() ? R.drawable.bg_input_field_focus : R.drawable.bg_input_field);
                }
                binding.noticeServerFailure.setVisibility(View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupListeners() {
        UiMotionUtils.attachSpringTouch(binding.btnBack, binding.btnRegister, binding.tvGoToLogin);

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
            binding.tvErrorFullName.setText("Vui lòng nhập họ và tên của bạn");
            binding.layoutErrorFullName.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            binding.boxFullName.setBackgroundResource(R.drawable.bg_input_field);
            binding.layoutErrorFullName.setVisibility(View.GONE);
        }

        // 2. Kiểm tra Email
        if (email.isEmpty()) {
            binding.boxEmail.setBackgroundResource(R.drawable.bg_input_field_error);
            binding.ivEmailStatusIcon.setVisibility(View.VISIBLE);
            binding.tvEmailHint.setText("Vui lòng nhập email.");
            binding.layoutErrorEmail.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.boxEmail.setBackgroundResource(R.drawable.bg_input_field_error);
            binding.ivEmailStatusIcon.setVisibility(View.VISIBLE);
            binding.tvEmailHint.setText("Email chưa đúng định dạng (ví dụ: ten@email.com).");
            binding.layoutErrorEmail.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            binding.boxEmail.setBackgroundResource(R.drawable.bg_input_field);
            binding.layoutErrorEmail.setVisibility(View.GONE);
        }

        // 3. Kiểm tra Số điện thoại
        if (phone.isEmpty() || phone.length() < 9 || phone.length() > 11) {
            binding.boxPhone.setBackgroundResource(R.drawable.bg_input_field_error);
            binding.tvErrorPhone.setText("Vui lòng nhập số điện thoại hợp lệ (9 - 11 chữ số).");
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
            binding.tvPasswordHint.setText("Mật khẩu tối thiểu 8 ký tự, gồm ít nhất 1 chữ số.");
            binding.tvPasswordHint.setTextColor(ContextCompat.getColor(this, R.color.ds_error));
            isValid = false;
        } else {
            binding.boxPassword.setBackgroundResource(R.drawable.bg_input_field);
            binding.ivPasswordStatusIcon.setVisibility(View.GONE);
            binding.tvPasswordHint.setText("Mật khẩu đạt yêu cầu.");
            binding.tvPasswordHint.setTextColor(ContextCompat.getColor(this, R.color.ds_success));
        }

        // 5. Kiểm tra Nhập lại mật khẩu
        if (confirmPassword.isEmpty()) {
            binding.boxConfirmPassword.setBackgroundResource(R.drawable.bg_input_field_error);
            binding.tvErrorConfirmPassword.setText("Vui lòng nhập lại mật khẩu.");
            binding.layoutErrorConfirmPassword.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            binding.boxConfirmPassword.setBackgroundResource(R.drawable.bg_input_field_error);
            binding.tvErrorConfirmPassword.setText("Mật khẩu xác nhận chưa khớp.");
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
        if (isPasswordVisible) {
            binding.etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            binding.tvTogglePassword.setText("Ẩn");
        } else {
            binding.etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            binding.tvTogglePassword.setText("Hiện");
        }
        binding.etPassword.setSelection(binding.etPassword.getText().length());
    }

    private void toggleConfirmPasswordVisibility() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        if (isConfirmPasswordVisible) {
            binding.etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            binding.tvToggleConfirmPassword.setText("Ẩn");
        } else {
            binding.etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            binding.tvToggleConfirmPassword.setText("Hiện");
        }
        binding.etConfirmPassword.setSelection(binding.etConfirmPassword.getText().length());
    }

    private void observeViewModel() {
        // Trạng thái Loading
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

        // Trạng thái Server Error
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                binding.tvError.setText(error);
                binding.noticeServerFailure.setVisibility(View.VISIBLE);
                binding.scrollRegisterRoot.smoothScrollTo(0, binding.noticeServerFailure.getTop() - 40);
            } else {
                binding.noticeServerFailure.setVisibility(View.GONE);
            }
        });

        // Trạng thái Thành công (Success)
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
                tokenManager.saveUserPhone(binding.etPhone.getText().toString().trim());

                Toast.makeText(RegisterActivity.this, "Tạo tài khoản thành công! Chào mừng " + response.getFullName(), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
