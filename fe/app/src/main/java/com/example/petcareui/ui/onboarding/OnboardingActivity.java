package com.example.petcareui.ui.onboarding;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.petcareui.MainActivity;
import com.example.petcareui.R;
import com.example.petcareui.databinding.ActivityOnboardingBinding;
import com.example.petcareui.ui.auth.LoginActivity;
import com.example.petcareui.ui.auth.RegisterActivity;
import com.example.petcareui.ui.staff.StaffDashboardActivity;
import com.example.petcareui.utils.TokenManager;
import com.example.petcareui.utils.UiMotionUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Màn hình Intro / Onboarding 2 trang với ViewPager2 theo phong cách Modern Veterinary Minimalist:
 * - Nền #FAF8F6, Card minh họa Soft Peach #FCE9E2 (bo góc 32dp, không viền), CTA Coral #B95740.
 * - Lưu cờ is_onboarding_completed trong SharedPreferences để người dùng cũ vào thẳng Login / Main.
 */
public class OnboardingActivity extends AppCompatActivity {

    private ActivityOnboardingBinding binding;
    private TokenManager tokenManager;
    private ViewPager2.OnPageChangeCallback pageChangeCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        tokenManager = TokenManager.getInstance(this);

        // 1. Nếu đã đăng nhập trước đó -> chuyển thẳng vào màn hình chính theo vai trò
        if (tokenManager.isLoggedIn()) {
            Intent intent = "STAFF".equalsIgnoreCase(tokenManager.getUserRole())
                    ? new Intent(this, StaffDashboardActivity.class)
                    : new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // 2. Nếu đã hoàn thành Onboarding trước đó -> bỏ qua Intro, chuyển thẳng đến LoginActivity
        if (tokenManager.isOnboardingCompleted()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        UiMotionUtils.applyWindowInsets(binding.getRoot(), binding.layoutTopBar, binding.layoutBottomControls);

        setupViewPager();
        setupLoginPromptSpannable();
        setupActions();
        updatePageState(0, false);
    }

    private void setupViewPager() {
        List<OnboardingItem> pages = Arrays.asList(
                new OnboardingItem(
                        R.drawable.mascot_corgi_wave,
                        "Chăm sóc bé cưng nhẹ nhàng hơn mỗi ngày",
                        "Lưu giữ hồ sơ sức khỏe và theo dõi lịch trình của từng bé tại một nơi duy nhất."
                ),
                new OnboardingItem(
                        R.drawable.mascot_corgi_booking,
                        "Đặt lịch khám nhanh, không lo chờ đợi",
                        "Chọn phòng khám gần bạn, xem khung giờ trống thực tế và đặt hẹn chỉ trong 30 giây."
                )
        );

        OnboardingAdapter adapter = new OnboardingAdapter(pages);
        binding.viewPagerOnboarding.setAdapter(adapter);

        pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updatePageState(position, true);
            }
        };
        binding.viewPagerOnboarding.registerOnPageChangeCallback(pageChangeCallback);
    }

    private void setupLoginPromptSpannable() {
        String fullText = "Đã có tài khoản? Đăng nhập";
        SpannableString spannable = new SpannableString(fullText);

        int startIndex = fullText.indexOf("Đăng nhập");
        if (startIndex != -1) {
            int endIndex = startIndex + "Đăng nhập".length();
            int coralDarkColor = ContextCompat.getColor(this, R.color.ds_primary_dark);
            spannable.setSpan(new ForegroundColorSpan(coralDarkColor), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        binding.tvLoginPrompt.setText(spannable);
    }

    private void setupActions() {
        UiMotionUtils.attachSpringTouch(
                binding.tvSkip,
                binding.btnNext,
                binding.btnGetStarted,
                binding.tvLoginPrompt
        );

        // Bấm 'Bỏ qua' ở trang 1 -> hoàn thành Onboarding và sang LoginActivity
        binding.tvSkip.setOnClickListener(v -> finishOnboardingToLogin());

        // Bấm 'Tiếp tục' ở trang 1 -> trượt mượt sang trang 2 (index 1)
        binding.btnNext.setOnClickListener(v -> binding.viewPagerOnboarding.setCurrentItem(1, true));

        // Bấm 'Bắt đầu ngay' ở trang 2 -> hoàn thành Onboarding và mở Đăng ký (đặt Login làm nền back-stack)
        binding.btnGetStarted.setOnClickListener(v -> finishOnboardingToRegister());

        // Bấm 'Đăng nhập' ở trang 2 -> hoàn thành Onboarding và chuyển thẳng sang LoginActivity
        binding.tvLoginPrompt.setOnClickListener(v -> finishOnboardingToLogin());
    }

    private void updatePageState(int position, boolean animate) {
        if (animate) {
            AutoTransition transition = new AutoTransition();
            transition.setDuration(180L);
            TransitionManager.beginDelayedTransition(binding.layoutBottomControls, transition);
        }

        boolean isFirstPage = (position == 0);

        // Nút 'Bỏ qua' góc trên: hiện ở trang 1, biến mất hoàn toàn ở trang 2 (giữ nguyên khung để không giật ViewPager2)
        binding.tvSkip.setVisibility(isFirstPage ? View.VISIBLE : View.INVISIBLE);
        binding.tvSkip.setEnabled(isFirstPage);

        // Dot Indicators: viên thuốc dài Coral #B95740 (24dp) cho trang hiện tại, chấm tròn #E5DFD9 (8dp) cho trang còn lại
        setDotStyle(binding.dotIndicator1, isFirstPage);
        setDotStyle(binding.dotIndicator2, !isFirstPage);

        ConstraintLayout.LayoutParams dotParams =
                (ConstraintLayout.LayoutParams) binding.layoutDotIndicators.getLayoutParams();
        dotParams.horizontalBias = isFirstPage ? 0.0f : 0.5f;
        dotParams.topMargin = dpToPx(isFirstPage ? 37 : 4);
        binding.layoutDotIndicators.setLayoutParams(dotParams);

        // Nút điều hướng dưới
        binding.btnNext.setVisibility(isFirstPage ? View.VISIBLE : View.GONE);
        binding.layoutPage2Actions.setVisibility(isFirstPage ? View.GONE : View.VISIBLE);
    }

    private void setDotStyle(View dotView, boolean active) {
        ViewGroup.LayoutParams lp = dotView.getLayoutParams();
        lp.width = dpToPx(active ? 24 : 8);
        lp.height = dpToPx(8);
        dotView.setLayoutParams(lp);
        dotView.setBackgroundResource(active
                ? R.drawable.bg_onboarding_dot_active
                : R.drawable.bg_onboarding_dot_inactive);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void finishOnboardingToLogin() {
        tokenManager.setOnboardingCompleted(true);
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_scale_out);
        finish();
    }

    private void finishOnboardingToRegister() {
        tokenManager.setOnboardingCompleted(true);
        Intent loginIntent = new Intent(this, LoginActivity.class);
        Intent registerIntent = new Intent(this, RegisterActivity.class);
        startActivities(new Intent[]{loginIntent, registerIntent});
        overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_scale_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (binding != null && pageChangeCallback != null) {
            binding.viewPagerOnboarding.unregisterOnPageChangeCallback(pageChangeCallback);
        }
        super.onDestroy();
    }
}
