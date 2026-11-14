package com.example.petcareui.utils;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Hệ thống Hiệu ứng Chuyển cảnh, Tương tác Lò xo (Spring Physics) & Xử lý tràn viền (Edge-to-Edge Insets).
 */
public final class UiMotionUtils {

    private static final int TAG_INITIAL_TOP = 0x7f0b9901;
    private static final int TAG_INITIAL_BOTTOM = 0x7f0b9902;
    private static final int TAG_INITIAL_HEIGHT = 0x7f0b9903;

    private UiMotionUtils() {
    }

    /**
     * Khắc phục triệt để lỗi đè Status Bar (đồng hồ/pin) và cắt chữ ở Bottom Navigation trên Android 13-15 (API 35).
     */
    public static void applyWindowInsets(View rootView, View topTarget, View bottomTarget) {
        if (rootView == null) return;

        final View topView = topTarget != null ? topTarget : rootView;
        final View bottomView = bottomTarget;

        saveInitialMetrics(topView);
        if (bottomView != null) {
            saveInitialMetrics(bottomView);
        }

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            int baseTop = getSavedInt(topView, TAG_INITIAL_TOP, topView.getPaddingTop());
            topView.setPadding(
                    topView.getPaddingLeft(),
                    baseTop + bars.top,
                    topView.getPaddingRight(),
                    topView.getPaddingBottom()
            );

            if (bottomView != null) {
                int baseBottom = getSavedInt(bottomView, TAG_INITIAL_BOTTOM, bottomView.getPaddingBottom());
                int baseHeight = getSavedInt(bottomView, TAG_INITIAL_HEIGHT, -999);
                ViewGroup.LayoutParams lp = bottomView.getLayoutParams();
                if (lp != null && baseHeight > 0) {
                    lp.height = baseHeight + bars.bottom;
                    bottomView.setLayoutParams(lp);
                }
                bottomView.setPadding(
                        bottomView.getPaddingLeft(),
                        bottomView.getPaddingTop(),
                        bottomView.getPaddingRight(),
                        baseBottom + bars.bottom
                );
            }

            return windowInsets;
        });
        ViewCompat.requestApplyInsets(rootView);
    }

    private static void saveInitialMetrics(View view) {
        if (view.getTag(TAG_INITIAL_TOP) == null) {
            view.setTag(TAG_INITIAL_TOP, view.getPaddingTop());
        }
        if (view.getTag(TAG_INITIAL_BOTTOM) == null) {
            view.setTag(TAG_INITIAL_BOTTOM, view.getPaddingBottom());
        }
        if (view.getTag(TAG_INITIAL_HEIGHT) == null && view.getLayoutParams() != null) {
            view.setTag(TAG_INITIAL_HEIGHT, view.getLayoutParams().height);
        }
    }

    private static int getSavedInt(View view, int key, int fallback) {
        Object val = view.getTag(key);
        return (val instanceof Integer) ? (Integer) val : fallback;
    }

    /**
     * Gắn phản hồi chạm lò xo (Tactile Spring Press) cho các thẻ Card, Chip lọc và Nút bấm.
     */
    @SuppressLint("ClickableViewAccessibility")
    public static void attachSpringTouch(View... views) {
        if (views == null) return;
        for (View view : views) {
            if (view == null) continue;
            view.setOnTouchListener((v, event) -> {
                int action = event.getActionMasked();
                if (action == MotionEvent.ACTION_DOWN) {
                    v.animate()
                            .scaleX(0.96f)
                            .scaleY(0.96f)
                            .setDuration(110)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    v.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(240)
                            .setInterpolator(new OvershootInterpolator(2.2f))
                            .start();
                }
                return false;
            });
        }
    }

    /**
     * Hiệu ứng đổ danh sách tuần tự (Staggered Slide-Up & Fade-In) cho các thẻ trong danh sách.
     */
    public static void animateStaggeredChildren(ViewGroup container, long initialDelayMs) {
        if (container == null) return;
        float density = container.getResources().getDisplayMetrics().density;
        float offsetPx = 26f * density;

        int count = container.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = container.getChildAt(i);
            if (child == null || child.getVisibility() != View.VISIBLE) continue;

            child.setAlpha(0f);
            child.setTranslationY(offsetPx);
            child.setScaleX(0.97f);
            child.setScaleY(0.97f);

            child.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay(initialDelayMs + (i * 55L))
                    .setDuration(320)
                    .setInterpolator(new DecelerateInterpolator(1.6f))
                    .start();
        }
    }

    /**
     * Hiệu ứng xuất hiện mượt mà cho Hero Banner và các khối Section trên màn hình.
     */
    public static void animateEntranceSections(View... sections) {
        if (sections == null) return;
        for (int i = 0; i < sections.length; i++) {
            View section = sections[i];
            if (section == null || section.getVisibility() != View.VISIBLE) continue;
            float density = section.getResources().getDisplayMetrics().density;
            section.setAlpha(0f);
            section.setTranslationY(20f * density);
            section.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(i * 65L)
                    .setDuration(340)
                    .setInterpolator(new DecelerateInterpolator(1.5f))
                    .start();
        }
    }

    /**
     * Hiệu ứng nảy nhẹ (Pop) khi người dùng chọn một thẻ Thú cưng, Dịch vụ hoặc Chip.
     */
    public static void animateSelectionPop(View view) {
        if (view == null) return;
        view.setScaleX(0.95f);
        view.setScaleY(0.95f);
        view.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(260)
                .setInterpolator(new OvershootInterpolator(2.6f))
                .start();
    }
}
