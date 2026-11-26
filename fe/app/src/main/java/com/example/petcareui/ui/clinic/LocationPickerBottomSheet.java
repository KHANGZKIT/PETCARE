package com.example.petcareui.ui.clinic;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.petcareui.R;
import com.example.petcareui.databinding.DialogLocationPickerBinding;
import com.example.petcareui.utils.UiMotionUtils;
import com.example.petcareui.utils.UserLocationManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;

/**
 * BottomSheet chọn khu vực / định vị GPS đồng bộ theo Design System Warm Tactile của PET CARE.
 * Dùng chung cho cả MainActivity (Trang chủ) và ClinicListActivity (Phòng khám).
 */
public final class LocationPickerBottomSheet {

    public interface OnLocationSelectedListener {
        void onSelectGps();
        void onSelectPreset(@NonNull UserLocationManager.PresetLocation preset);
    }

    private LocationPickerBottomSheet() {
    }

    public static void show(@NonNull Context context, @NonNull OnLocationSelectedListener listener) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        DialogLocationPickerBinding binding = DialogLocationPickerBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(binding.getRoot());

        // Làm trong suốt nền mặc định của Material BottomSheet để bo góc bg_bottom_sheet sắc nét
        dialog.setOnShowListener(d -> {
            View sheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (sheet != null) {
                sheet.setBackgroundColor(Color.TRANSPARENT);
            }
        });

        UserLocationManager.LocationState current = UserLocationManager.getCurrent(context);

        binding.tvLocationSheetSubtitle.setText("Hiển thị các phòng khám thú y gần bạn nhất");
        binding.tvGpsOptionTitle.setText("Dùng vị trí hiện tại");
        binding.tvGpsOptionDesc.setText(current.gpsMode
                ? current.label
                : "Tự động xác định vị trí qua GPS");

        boolean gpsActive = current.gpsMode;
        binding.rowUseCurrentGps.setBackgroundResource(
                gpsActive ? R.drawable.bg_location_gps_active : R.drawable.bg_location_gps_default);
        binding.tvGpsOptionTitle.setTextColor(ContextCompat.getColor(
                context, gpsActive ? R.color.ds_primary : R.color.ds_text_primary));
        binding.ivGpsCheck.setVisibility(gpsActive ? View.VISIBLE : View.GONE);

        UiMotionUtils.attachSpringTouch(binding.btnCloseLocationSheet, binding.rowUseCurrentGps);
        binding.btnCloseLocationSheet.setOnClickListener(v -> dialog.dismiss());
        binding.rowUseCurrentGps.setOnClickListener(v -> {
            dialog.dismiss();
            listener.onSelectGps();
        });

        renderPresetRows(context, binding.containerPresetLocations, current, preset -> {
            dialog.dismiss();
            listener.onSelectPreset(preset);
        });

        dialog.show();
    }

    private interface PresetClickHandler {
        void onClick(UserLocationManager.PresetLocation preset);
    }

    private static void renderPresetRows(@NonNull Context context,
                                         @NonNull LinearLayout container,
                                         @NonNull UserLocationManager.LocationState current,
                                         @NonNull PresetClickHandler onClick) {
        container.removeAllViews();
        float density = context.getResources().getDisplayMetrics().density;
        int padH = Math.round(14 * density);
        int padV = Math.round(13 * density);
        int iconBoxSize = Math.round(36 * density);
        int iconSize = Math.round(16 * density);
        int checkSize = Math.round(18 * density);

        UserLocationManager.PresetLocation[] presets = UserLocationManager.PRESETS;
        for (int i = 0; i < presets.length; i++) {
            UserLocationManager.PresetLocation preset = presets[i];
            boolean isSelected = !current.gpsMode && preset.label.equalsIgnoreCase(current.label);

            LinearLayout row = new LinearLayout(context);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(padH, padV, padH, padV);
            row.setClickable(true);
            row.setFocusable(true);
            if (isSelected) {
                row.setBackgroundColor(ContextCompat.getColor(context, R.color.ds_soft_peach));
            }

            // Icon tile
            FrameLayout iconTile = new FrameLayout(context);
            iconTile.setLayoutParams(new LinearLayout.LayoutParams(iconBoxSize, iconBoxSize));
            iconTile.setBackgroundResource(isSelected
                    ? R.drawable.bg_home_icon_btn_white
                    : R.drawable.bg_shortcut_icon_cream);

            ImageView ivPin = new ImageView(context);
            FrameLayout.LayoutParams pinLp = new FrameLayout.LayoutParams(iconSize, iconSize);
            pinLp.gravity = Gravity.CENTER;
            ivPin.setLayoutParams(pinLp);
            ivPin.setImageResource(R.drawable.ic_map_pin);
            ivPin.setColorFilter(ContextCompat.getColor(
                    context, isSelected ? R.color.ds_primary : R.color.ds_text_secondary));
            iconTile.addView(ivPin);
            row.addView(iconTile);

            // Text block
            LinearLayout textCol = new LinearLayout(context);
            LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            textLp.setMarginStart(Math.round(12 * density));
            textLp.setMarginEnd(Math.round(8 * density));
            textCol.setLayoutParams(textLp);
            textCol.setOrientation(LinearLayout.VERTICAL);

            TextView tvTitle = new TextView(context);
            tvTitle.setText(preset.label);
            tvTitle.setTextSize(14f);
            tvTitle.setIncludeFontPadding(false);
            tvTitle.setTypeface(ResourcesCompat.getFont(
                    context, isSelected ? R.font.open_sans_bold : R.font.open_sans_semibold));
            tvTitle.setTextColor(ContextCompat.getColor(
                    context, isSelected ? R.color.ds_primary : R.color.ds_text_primary));
            textCol.addView(tvTitle);

            TextView tvSub = new TextView(context);
            LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            subLp.topMargin = Math.round(3 * density);
            tvSub.setLayoutParams(subLp);
            tvSub.setText(preset.subtitle);
            tvSub.setTextSize(12f);
            tvSub.setIncludeFontPadding(false);
            tvSub.setTypeface(ResourcesCompat.getFont(context, R.font.open_sans_regular));
            tvSub.setTextColor(ContextCompat.getColor(context, R.color.ds_text_secondary));
            textCol.addView(tvSub);

            row.addView(textCol);

            // Checkmark when selected
            if (isSelected) {
                ImageView ivCheck = new ImageView(context);
                ivCheck.setLayoutParams(new LinearLayout.LayoutParams(checkSize, checkSize));
                ivCheck.setImageResource(R.drawable.ic_check_circle);
                ivCheck.setColorFilter(ContextCompat.getColor(context, R.color.ds_primary));
                row.addView(ivCheck);
            }

            UiMotionUtils.attachSpringTouch(row);
            row.setOnClickListener(v -> onClick.onClick(preset));
            container.addView(row);

            // Hairline divider between rows
            if (i < presets.length - 1) {
                View divider = new View(context);
                LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, Math.round(density)));
                divLp.setMarginStart(padH + iconBoxSize + Math.round(12 * density));
                divLp.setMarginEnd(padH);
                divider.setLayoutParams(divLp);
                divider.setBackgroundColor(ContextCompat.getColor(context, R.color.ds_divider_hairline));
                container.addView(divider);
            }
        }
    }
}
