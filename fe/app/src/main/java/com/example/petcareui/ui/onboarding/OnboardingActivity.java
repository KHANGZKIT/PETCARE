package com.example.petcareui.ui.onboarding;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.petcareui.R;
import com.example.petcareui.databinding.ActivityOnboardingBinding;
import com.example.petcareui.ui.auth.LoginActivity;
import com.example.petcareui.utils.TokenManager;
import com.example.petcareui.utils.UiMotionUtils;

/**
 * Màn hình giới thiệu / Onboarding cao cấp của PetCare:
 * - Tone màu ấm áp Coral Terracotta (#D97B60) và Soft Cream (#FFF9F5).
 * - Hình ảnh cún cưng lấp ló trên mép thẻ trắng bo tròn 36dp.
 * - Nút bấm "Bắt đầu" nổi bật và link "Đăng nhập".
 */
public class OnboardingActivity extends AppCompatActivity {

    private ActivityOnboardingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Nếu người dùng đã đăng nhập từ trước, chuyển thẳng vào Dashboard
        TokenManager tokenManager = TokenManager.getInstance(this);
        if (tokenManager.isLoggedIn()) {
            String role = tokenManager.getUserRole();
            Intent intent;
            if ("STAFF".equalsIgnoreCase(role)) {
                intent = new Intent(this, com.example.petcareui.ui.staff.StaffDashboardActivity.class);
            } else {
                intent = new Intent(this, com.example.petcareui.MainActivity.class);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        UiMotionUtils.applyWindowInsets(binding.getRoot(), binding.layoutHeaderBranding, binding.cardWelcomeContent);

        setupLoginTextSpannable();
        setupActions();
    }

    private void setupLoginTextSpannable() {
        String fullText = "Đã có tài khoản? Đăng nhập";
        SpannableString spannable = new SpannableString(fullText);

        int startIndex = fullText.indexOf("Đăng nhập");
        if (startIndex != -1) {
            int endIndex = startIndex + "Đăng nhập".length();
            int coralColor = ContextCompat.getColor(this, R.color.coral_terracotta);

            spannable.setSpan(new ForegroundColorSpan(coralColor), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        binding.tvLoginPrompt.setText(spannable);
    }

    private void setupActions() {
        UiMotionUtils.attachSpringTouch(binding.btnGetStarted, binding.tvLoginPrompt);

        binding.btnGetStarted.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_scale_out);
        });

        binding.tvLoginPrompt.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_scale_out);
        });
    }
}
