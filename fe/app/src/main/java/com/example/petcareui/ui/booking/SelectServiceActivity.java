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
import com.example.petcareui.data.api.ApiErrorParser;
import com.example.petcareui.data.model.ClinicItem;
import com.example.petcareui.data.model.PetItem;
import com.example.petcareui.data.model.ServiceItem;
import com.example.petcareui.data.dto.response.ServiceResponse;
import com.example.petcareui.databinding.ActivitySelectServiceBinding;
import com.example.petcareui.databinding.ItemServiceSelectableBinding;
import com.example.petcareui.ui.clinic.ClinicListActivity;
import com.example.petcareui.utils.ServiceDisplayUtils;
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
    public static final String EXTRA_PRESELECTED_PET = "extra_preselected_pet";
    public static final String EXTRA_PREFERRED_CATEGORY = "extra_preferred_category";
    public static final String EXTRA_EDIT_FROM_REVIEW = "extra_edit_from_review";

    private ActivitySelectServiceBinding binding;
    private ClinicItem clinicItem;
    private ServiceItem preselectedService;
    private PetItem preselectedPet;
    private String preferredCategory;
    private boolean isEditFromReview = false;

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
        if (getIntent().hasExtra(EXTRA_SELECTED_SERVICE)) {
            try {
                preselectedService = (ServiceItem) getIntent().getSerializableExtra(EXTRA_SELECTED_SERVICE);
            } catch (Exception ignored) {
            }
        }
        if (getIntent().hasExtra(EXTRA_PRESELECTED_PET)) {
            try {
                preselectedPet = (PetItem) getIntent().getSerializableExtra(EXTRA_PRESELECTED_PET);
            } catch (Exception ignored) {
            }
        }
        preferredCategory = getIntent().getStringExtra(EXTRA_PREFERRED_CATEGORY);
        isEditFromReview = getIntent().getBooleanExtra(EXTRA_EDIT_FROM_REVIEW, false);

        if (clinicItem == null) {
            clinicItem = new ClinicItem(
                    "1",
                    "Bệnh viện Thú y Tropicpet Cầu Giấy",
                    "Số 8 Trần Quốc Hoàn, Cầu Giấy, Hà Nội",
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

        binding.ivClinicThumb.setImageResource(clinicItem.getImageResId());
        binding.tvClinicName.setText(clinicItem.getName());
        binding.tvClinicArea.setText(clinicItem.getArea());

        if (isEditFromReview) {
            binding.tvChangeClinic.setVisibility(View.GONE);
            binding.cardSelectedClinic.setClickable(false);
            binding.btnContinue.setText("Lưu thay đổi");
        } else {
            View.OnClickListener changeListener = v -> {
                Intent intent = new Intent(this, ClinicListActivity.class);
                intent.putExtra(ClinicListActivity.EXTRA_TAB_MODE, false);
                if (preselectedPet != null) {
                    intent.putExtra(ClinicListActivity.EXTRA_PRESELECTED_PET, preselectedPet);
                }
                startActivity(intent);
                finish();
            };
            binding.tvChangeClinic.setOnClickListener(changeListener);
            binding.cardSelectedClinic.setOnClickListener(changeListener);
        }

        UiMotionUtils.attachSpringTouch(binding.btnBack, binding.cardSelectedClinic, binding.btnContinue);
    }

    private int resolveInitialServiceIndex() {
        if (services.isEmpty()) return -1;
        if (preselectedService != null) {
            for (int i = 0; i < services.size(); i++) {
                ServiceItem item = services.get(i);
                if (preselectedService.getId() != null && preselectedService.getId().equals(item.getId())) {
                    return i;
                }
                if (preselectedService.getName() != null
                        && preselectedService.getName().equalsIgnoreCase(item.getName())) {
                    return i;
                }
            }
        }
        if (preferredCategory != null && !preferredCategory.isEmpty()) {
            for (int i = 0; i < services.size(); i++) {
                if (ClinicListActivity.matchesServiceCategory(services.get(i).getName(), preferredCategory)) {
                    return i;
                }
            }
        }
        return 0;
    }

    private void loadServicesFromApi() {
        Long clinicId = clinicItem != null ? clinicItem.getNumericId() : null;
        if (clinicId == null) {
            clinicId = 1L;
        }

        binding.tvServiceCount.setText("Đang tải danh sách dịch vụ...");
        binding.btnContinue.setEnabled(false);
        binding.btnContinue.setAlpha(0.5f);

        ApiClient.getClinicApi().getServicesByClinicId(clinicId).enqueue(new Callback<List<ServiceResponse>>() {
            @Override
            public void onResponse(Call<List<ServiceResponse>> call, Response<List<ServiceResponse>> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    services.clear();
                    List<ServiceResponse> body = response.body();
                    if (body.isEmpty()) {
                        showServicesMessage("Phòng khám hiện chưa có dịch vụ đang nhận đặt lịch.", false);
                        return;
                    }
                    for (ServiceResponse res : body) {
                        int icon = ServiceDisplayUtils.resolveIconByName(res.getName());
                        String durationText = res.getDuration() != null ? res.getDuration() + " phút" : "30 phút";
                        String priceText = ServiceDisplayUtils.resolvePriceByName(res.getName());
                        services.add(new ServiceItem(
                                String.valueOf(res.getId()),
                                res.getName(),
                                "",
                                durationText,
                                priceText,
                                icon
                        ));
                    }
                    selectedServiceIndex = resolveInitialServiceIndex();
                    renderServices();
                } else {
                    String errMsg = ApiErrorParser.getMessage(response, "Không thể tải danh sách dịch vụ.");
                    showServicesMessage(errMsg, true);
                }
            }

            @Override
            public void onFailure(Call<List<ServiceResponse>> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                showServicesMessage("Lỗi kết nối máy chủ khi tải danh sách dịch vụ.", true);
            }
        });
    }

    private int resolveServiceIcon(String name) {
        return ServiceDisplayUtils.resolveIconByName(name);
    }

    private void showServicesMessage(String message, boolean allowRetry) {
        services.clear();
        serviceBindings.clear();
        selectedServiceIndex = -1;
        binding.containerServicesList.removeAllViews();
        binding.tvServiceCount.setText(allowRetry ? "Không tải được dịch vụ" : "0 dịch vụ đang nhận đặt lịch");

        android.widget.LinearLayout wrapper = new android.widget.LinearLayout(this);
        wrapper.setOrientation(android.widget.LinearLayout.VERTICAL);
        int pad = Math.round(16 * getResources().getDisplayMetrics().density);
        wrapper.setPadding(pad, pad, pad, pad);

        android.widget.TextView tvMsg = new android.widget.TextView(this);
        tvMsg.setText(message);
        tvMsg.setTextColor(ContextCompat.getColor(this, R.color.ds_text_secondary));
        tvMsg.setTextSize(13.5f);
        wrapper.addView(tvMsg);

        if (allowRetry) {
            android.widget.TextView tvRetry = new android.widget.TextView(this);
            tvRetry.setText("Thử tải lại");
            tvRetry.setTextColor(ContextCompat.getColor(this, R.color.ds_primary));
            tvRetry.setTextSize(14f);
            tvRetry.setTypeface(tvRetry.getTypeface(), android.graphics.Typeface.BOLD);
            int topPad = Math.round(10 * getResources().getDisplayMetrics().density);
            tvRetry.setPadding(0, topPad, 0, 0);
            tvRetry.setOnClickListener(v -> loadServicesFromApi());
            wrapper.addView(tvRetry);
        }

        binding.containerServicesList.addView(wrapper);
        updateSelectionUI();
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
                if (isEditFromReview) {
                    Intent result = new Intent();
                    result.putExtra(EXTRA_SELECTED_SERVICE, chosen);
                    setResult(RESULT_OK, result);
                    finish();
                } else {
                    Intent intent = new Intent(this, DateTimeActivity.class);
                    intent.putExtra(DateTimeActivity.EXTRA_CLINIC, clinicItem);
                    intent.putExtra(DateTimeActivity.EXTRA_SELECTED_SERVICE, chosen);
                    if (preselectedPet != null) {
                        intent.putExtra(DateTimeActivity.EXTRA_PRESELECTED_PET, preselectedPet);
                    }
                    startActivity(intent);
                }
            } else {
                Toast.makeText(this, "Vui lòng chọn một dịch vụ để tiếp tục", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
