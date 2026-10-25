package com.example.petcareui.ui.booking;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.petcareui.R;
import com.example.petcareui.data.model.ClinicItem;
import com.example.petcareui.data.model.ServiceItem;
import com.example.petcareui.databinding.ActivitySelectServiceBinding;
import com.example.petcareui.databinding.ItemServiceSelectableBinding;
import com.example.petcareui.ui.clinic.ClinicListActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình C06: Chọn dịch vụ (Select Service) - Bước 1/4.
 * Bám sát nguyên mẫu React C06ChooseService và Design System Spec.
 */
public class SelectServiceActivity extends AppCompatActivity {

    public static final String EXTRA_CLINIC = "extra_clinic";
    public static final String EXTRA_SELECTED_SERVICE = "extra_selected_service";

    private ActivitySelectServiceBinding binding;
    private ClinicItem clinicItem;

    private final List<ServiceItem> services = new ArrayList<>();
    private final List<ItemServiceSelectableBinding> serviceBindings = new ArrayList<>();
    private int selectedServiceIndex = 1; // Mặc định chọn dịch vụ thứ 2 (Tiêm phòng) theo prototype C06

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySelectServiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        extractIntentData();
        setupClinicContextCard();
        setupServicesList();
        setupStickyBottomBar();
    }

    private void extractIntentData() {
        if (getIntent().hasExtra(EXTRA_CLINIC)) {
            try {
                clinicItem = (ClinicItem) getIntent().getSerializableExtra(EXTRA_CLINIC);
            } catch (Exception ignored) {
            }
        }

        // Mặc định phòng khám An Nhiên nếu chưa có
        if (clinicItem == null) {
            clinicItem = new ClinicItem(
                    "cl-01",
                    "Phòng khám Thú y An Nhiên",
                    "42 Nguyễn Huệ, Quận 1, TP.HCM",
                    4.8,
                    214,
                    "1,2 km",
                    true,
                    "Thứ 2 – CN · 08:00 – 20:00"
            );
        }
    }

    private void setupClinicContextCard() {
        // Nút Back
        binding.btnBack.setOnClickListener(v -> finish());

        // Thông tin phòng khám
        binding.tvClinicName.setText(clinicItem.getName());
        binding.tvClinicArea.setText(clinicItem.getArea());

        // Nút "Đổi" phòng khám -> Mở lại danh sách phòng khám (C04)
        View.OnClickListener changeListener = v -> {
            Intent intent = new Intent(this, ClinicListActivity.class);
            startActivity(intent);
        };
        binding.tvChangeClinic.setOnClickListener(changeListener);
        binding.cardSelectedClinic.setOnClickListener(changeListener);
    }

    private void setupServicesList() {
        services.clear();
        serviceBindings.clear();
        binding.containerServicesList.removeAllViews();

        // 6 dịch vụ chuẩn theo data.js của prototype PET CARE
        services.add(new ServiceItem("sv-01", "Khám tổng quát", "Khám lâm sàng, cân nặng, nhiệt độ và các chỉ số sinh tồn.", "30 phút", "180.000 ₫", R.drawable.ic_stethoscope));
        services.add(new ServiceItem("sv-02", "Tiêm phòng", "Vaccine thiết yếu cho chó và mèo, khám sức khỏe trước khi tiêm.", "20 phút", "250.000 ₫", R.drawable.ic_syringe));
        services.add(new ServiceItem("sv-03", "Cạo vôi & chăm sóc răng", "Cạo vôi bằng siêu âm, đánh bóng và kiểm tra khoang miệng.", "45 phút", "600.000 ₫", R.drawable.ic_sparkles));
        services.add(new ServiceItem("sv-04", "Tắm & cắt tỉa", "Tắm, sấy khô, cắt móng và vệ sinh tai.", "60 phút", "320.000 ₫", R.drawable.ic_scissors));
        services.add(new ServiceItem("sv-05", "Tư vấn da & dị ứng", "Khám chuyên khoa da liễu kèm phác đồ điều trị.", "40 phút", "420.000 ₫", R.drawable.ic_shield_check));
        services.add(new ServiceItem("sv-06", "Khám định kỳ cho bé lớn tuổi", "Xét nghiệm máu, khám tim và đánh giá vận động.", "50 phút", "780.000 ₫", R.drawable.ic_heart_pulse));

        binding.tvServiceCount.setText(services.size() + " dịch vụ đang nhận đặt lịch");

        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < services.size(); i++) {
            final int index = i;
            ServiceItem item = services.get(i);
            ItemServiceSelectableBinding itemBinding = ItemServiceSelectableBinding.inflate(inflater, binding.containerServicesList, false);

            itemBinding.ivServiceIcon.setImageResource(item.getIconResId());
            itemBinding.tvServiceName.setText(item.getName());
            itemBinding.tvServiceDesc.setText(item.getDesc());
            itemBinding.tvServiceDuration.setText(item.getDuration());
            itemBinding.tvServicePrice.setText(item.getPrice());

            itemBinding.getRoot().setOnClickListener(v -> selectService(index));

            serviceBindings.add(itemBinding);
            binding.containerServicesList.addView(itemBinding.getRoot());
        }

        // Cập nhật trạng thái hiển thị ban đầu
        updateSelectionUI();
    }

    private void selectService(int index) {
        selectedServiceIndex = index;
        updateSelectionUI();
    }

    private void updateSelectionUI() {
        for (int i = 0; i < serviceBindings.size(); i++) {
            ItemServiceSelectableBinding b = serviceBindings.get(i);
            boolean isCurrentSelected = (i == selectedServiceIndex);

            if (isCurrentSelected) {
                b.layoutServiceCard.setBackgroundResource(R.drawable.bg_service_card_selected);
                b.layoutServiceIconBox.setBackgroundResource(R.drawable.bg_service_icon_selected);
                b.ivServiceIcon.setColorFilter(ContextCompat.getColor(this, R.color.white));
                b.ivServiceCheck.setImageResource(R.drawable.bg_radio_selected);
            } else {
                b.layoutServiceCard.setBackgroundResource(R.drawable.bg_service_card_default);
                b.layoutServiceIconBox.setBackgroundResource(R.drawable.bg_service_icon);
                b.ivServiceIcon.setColorFilter(ContextCompat.getColor(this, R.color.brand_ink));
                b.ivServiceCheck.setImageResource(R.drawable.bg_radio_unselected);
            }
        }

        if (selectedServiceIndex >= 0 && selectedServiceIndex < services.size()) {
            ServiceItem selected = services.get(selectedServiceIndex);
            binding.tvSelectedServiceSummary.setText(selected.getName() + " · " + selected.getPrice());
            binding.btnContinue.setEnabled(true);
            binding.btnContinue.setAlpha(1.0f);
        } else {
            binding.tvSelectedServiceSummary.setText("Chưa chọn");
            binding.btnContinue.setEnabled(false);
            binding.btnContinue.setAlpha(0.5f);
        }
    }

    private void setupStickyBottomBar() {
        binding.btnContinue.setOnClickListener(v -> {
            if (selectedServiceIndex >= 0 && selectedServiceIndex < services.size()) {
                ServiceItem chosen = services.get(selectedServiceIndex);
                Intent intent = new Intent(this, DateTimeActivity.class);
                intent.putExtra(DateTimeActivity.EXTRA_CLINIC, clinicItem);
                intent.putExtra(DateTimeActivity.EXTRA_SELECTED_SERVICE, chosen);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Vui lòng chọn một dịch vụ để tiếp tục", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
