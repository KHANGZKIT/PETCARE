package com.example.petcareui.ui.booking;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.petcareui.R;
import com.example.petcareui.data.api.ApiClient;
import com.example.petcareui.data.model.ClinicItem;
import com.example.petcareui.data.model.ServiceItem;
import com.example.petcareui.data.dto.response.ServiceResponse;
import com.example.petcareui.databinding.ActivitySelectServiceBinding;
import com.example.petcareui.databinding.ItemServiceSelectableBinding;
import com.example.petcareui.ui.clinic.ClinicListActivity;
import com.example.petcareui.utils.UiMotionUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Màn hình C06: Chọn dịch vụ (Select Service) - Bước 1/4.
 */
public class SelectServiceActivity extends AppCompatActivity {

    public static final String EXTRA_CLINIC = "extra_clinic";
    public static final String EXTRA_SELECTED_SERVICE = "extra_selected_service";

    private ActivitySelectServiceBinding binding;
    private ClinicItem clinicItem;

    private final List<ServiceItem> services = new ArrayList<>();
    private final List<ItemServiceSelectableBinding> serviceBindings = new ArrayList<>();
    private int selectedServiceIndex = -1;

    private static final int[] SERVICE_ICONS = new int[]{
            R.drawable.ic_stethoscope,
            R.drawable.ic_syringe,
            R.drawable.ic_sparkles,
            R.drawable.ic_scissors,
            R.drawable.ic_shield_check,
            R.drawable.ic_heart_pulse
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySelectServiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        UiMotionUtils.applyWindowInsets(binding.getRoot(), binding.layoutTopHeader, binding.layoutStickyBottom);

        extractIntentData();
        setupClinicContextCard();
        setupStickyBottomBar();
        loadServicesFromApi();
    }

    private void extractIntentData() {
        if (getIntent().hasExtra(EXTRA_CLINIC)) {
            try {
                clinicItem = (ClinicItem) getIntent().getSerializableExtra(EXTRA_CLINIC);
            } catch (Exception ignored) {
            }
        }

        if (clinicItem == null) {
            clinicItem = new ClinicItem(
                    "1",
                    "Phòng khám Thú y An Nhiên",
                    "Số 12 Chùa Hà, Cầu Giấy, Hà Nội",
                    4.8,
                    214,
                    "1,2 km",
                    true,
                    "Thứ 2 – CN · 08:00 – 20:00"
            );
        }
    }

    private void setupClinicContextCard() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.tvClinicName.setText(clinicItem.getName());
        binding.tvClinicArea.setText(clinicItem.getArea());

        View.OnClickListener changeListener = v -> {
            Intent intent = new Intent(this, ClinicListActivity.class);
            startActivity(intent);
        };
        binding.tvChangeClinic.setOnClickListener(changeListener);
        binding.cardSelectedClinic.setOnClickListener(changeListener);

        UiMotionUtils.attachSpringTouch(binding.btnBack, binding.cardSelectedClinic, binding.btnContinue);
    }

    private void loadServicesFromApi() {
        Long clinicId = clinicItem != null ? clinicItem.getNumericId() : null;
        if (clinicId == null) {
            clinicId = 1L;
        }

        binding.tvServiceCount.setText("Đang tải danh sách dịch vụ...");

        ApiClient.getClinicApi().getServicesByClinicId(clinicId).enqueue(new Callback<List<ServiceResponse>>() {
            @Override
            public void onResponse(Call<List<ServiceResponse>> call, Response<List<ServiceResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    services.clear();
                    List<ServiceResponse> body = response.body();
                    String[] samplePrices = new String[]{"180.000 ₫", "250.000 ₫", "600.000 ₫", "320.000 ₫", "420.000 ₫", "780.000 ₫"};
                    for (int i = 0; i < body.size(); i++) {
                        ServiceResponse res = body.get(i);
                        int icon = resolveServiceIcon(res.getName());
                        String durationText = res.getDuration() != null ? res.getDuration() + " phút" : "30 phút";
                        String priceText = samplePrices[i % samplePrices.length];
                        services.add(new ServiceItem(
                                String.valueOf(res.getId()),
                                res.getName(),
                                "",
                                durationText,
                                priceText,
                                icon
                        ));
                    }
                    selectedServiceIndex = 0;
                    renderServices();
                } else {
                    setupDefaultServices();
                }
            }

            @Override
            public void onFailure(Call<List<ServiceResponse>> call, Throwable t) {
                setupDefaultServices();
            }
        });
    }

    private int resolveServiceIcon(String name) {
        if (name == null) return R.drawable.ic_stethoscope;
        String lower = name.toLowerCase(java.util.Locale.ROOT);
        if (lower.contains("cấp cứu")) {
            return R.drawable.ic_heart_pulse;
        }
        if (lower.contains("tiêm") || lower.contains("vắc") || lower.contains("vaccine")) {
            return R.drawable.ic_syringe;
        }
        if (lower.contains("tắm") || lower.contains("spa") || lower.contains("cắt") || lower.contains("lông")) {
            return R.drawable.ic_scissors;
        }
        if (lower.contains("khách sạn") || lower.contains("lưu chuồng")) {
            return R.drawable.ic_home;
        }
        return R.drawable.ic_stethoscope;
    }

    private void setupDefaultServices() {
        if (services.isEmpty()) {
            services.add(new ServiceItem("1", "Khám tổng quát", "Khám lâm sàng, cân nặng, nhiệt độ và các chỉ số sinh tồn.", "30 phút", "180.000 ₫", R.drawable.ic_stethoscope));
            services.add(new ServiceItem("2", "Tiêm phòng", "Vaccine thiết yếu cho chó và mèo, khám sức khỏe trước khi tiêm.", "20 phút", "250.000 ₫", R.drawable.ic_syringe));
            services.add(new ServiceItem("3", "Cạo vôi & chăm sóc răng", "Cạo vôi bằng siêu âm, đánh bóng và kiểm tra khoang miệng.", "45 phút", "600.000 ₫", R.drawable.ic_stethoscope));
            services.add(new ServiceItem("4", "Tắm & cắt tỉa", "Tắm, sấy khô, cắt móng và vệ sinh tai.", "60 phút", "320.000 ₫", R.drawable.ic_scissors));
            services.add(new ServiceItem("5", "Cấp cứu 24/7", "Tiếp nhận xử lý khẩn cấp và theo dõi liên tục.", "40 phút", "420.000 ₫", R.drawable.ic_heart_pulse));
            services.add(new ServiceItem("6", "Khách sạn & lưu chuồng", "Chăm sóc nội trú sạch sẽ, thoáng mát theo ngày.", "24 giờ", "200.000 ₫", R.drawable.ic_home));
            selectedServiceIndex = 0;
        }
        renderServices();
    }

    private void renderServices() {
        serviceBindings.clear();
        binding.containerServicesList.removeAllViews();
        binding.tvServiceCount.setText(services.size() + " dịch vụ đang nhận đặt lịch");

        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < services.size(); i++) {
            final int index = i;
            ServiceItem item = services.get(i);
            ItemServiceSelectableBinding itemBinding = ItemServiceSelectableBinding.inflate(inflater, binding.containerServicesList, false);

            itemBinding.ivServiceIcon.setImageResource(resolveServiceIcon(item.getName()));
            itemBinding.tvServiceName.setText(item.getName());

            String desc = item.getDesc() != null ? item.getDesc().trim() : "";
            if (desc.isEmpty() || desc.equalsIgnoreCase("Dịch vụ chăm sóc thú y tiêu chuẩn, tận tâm")) {
                itemBinding.tvServiceDesc.setVisibility(View.GONE);
            } else {
                itemBinding.tvServiceDesc.setText(desc);
                itemBinding.tvServiceDesc.setVisibility(View.VISIBLE);
            }

            itemBinding.tvServiceDuration.setText(item.getDuration());
            itemBinding.tvServicePrice.setText(item.getPrice());
            itemBinding.dividerServiceRow.setVisibility(i == services.size() - 1 ? View.GONE : View.VISIBLE);

            UiMotionUtils.attachSpringTouch(itemBinding.getRoot());
            itemBinding.getRoot().setOnClickListener(v -> selectService(index));

            serviceBindings.add(itemBinding);
            binding.containerServicesList.addView(itemBinding.getRoot());
        }

        updateSelectionUI();
        UiMotionUtils.animateStaggeredChildren(binding.containerServicesList, 25);
    }

    private void selectService(int index) {
        selectedServiceIndex = index;
        if (index >= 0 && index < serviceBindings.size()) {
            UiMotionUtils.animateSelectionPop(serviceBindings.get(index).getRoot());
        }
        updateSelectionUI();
    }

    private void updateSelectionUI() {
        for (int i = 0; i < serviceBindings.size(); i++) {
            ItemServiceSelectableBinding b = serviceBindings.get(i);
            boolean isCurrentSelected = (i == selectedServiceIndex);

            if (isCurrentSelected) {
                b.layoutServiceCard.setBackgroundColor(ContextCompat.getColor(this, R.color.ds_soft_peach));
                b.layoutServiceIconBox.setBackgroundResource(R.drawable.bg_service_icon_selected);
                b.ivServiceIcon.setColorFilter(ContextCompat.getColor(this, R.color.white));
                b.ivServiceCheck.setImageResource(R.drawable.bg_radio_selected);
            } else {
                b.layoutServiceCard.setBackgroundResource(R.drawable.bg_clinic_row_flat);
                b.layoutServiceIconBox.setBackgroundResource(R.drawable.bg_shortcut_icon_cream);
                b.ivServiceIcon.setColorFilter(ContextCompat.getColor(this, R.color.ds_primary));
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
