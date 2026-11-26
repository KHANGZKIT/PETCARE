package com.example.petcareui.ui.clinic;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.example.petcareui.R;
import com.example.petcareui.data.api.ApiClient;
import com.example.petcareui.data.api.ApiErrorParser;
import com.example.petcareui.data.model.ClinicItem;
import com.example.petcareui.data.model.PetItem;
import com.example.petcareui.data.model.ServiceItem;
import com.example.petcareui.data.dto.response.ServiceResponse;
import com.example.petcareui.databinding.ActivityClinicDetailsBinding;
import com.example.petcareui.databinding.ItemServiceSelectableBinding;
import com.example.petcareui.utils.ServiceDisplayUtils;
import com.example.petcareui.utils.UiMotionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Màn hình C05: Chi tiết phòng khám (Clinic Details).
 * Bám sát nguyên mẫu React C05ClinicDetails và Design System Spec, tích hợp hiệu ứng Parallax & Spring Motion.
 */
public class ClinicDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_CLINIC = "extra_clinic";
    public static final String EXTRA_PRESELECTED_PET = "extra_preselected_pet";
    public static final String EXTRA_PREFERRED_CATEGORY = "extra_preferred_category";

    private ActivityClinicDetailsBinding binding;
    private ClinicItem clinicItem;
    private PetItem preselectedPet;
    private String preferredCategory;
    private boolean isFavorite = false;

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
        binding = ActivityClinicDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        UiMotionUtils.applyWindowInsets(binding.getRoot(), binding.layoutFloatingTopBar, binding.layoutStickyBottom);

        extractIntentData();
        setupClinicInfo();
        setupTopBarActions();
        setupQuickContactActions();
        setupBookingCTA();
        setupParallaxAndMotion();
        loadServicesFromApi();
    }

    private void setupParallaxAndMotion() {
        // Hiệu ứng cuộn Parallax cho ảnh bìa phòng khám
        binding.scrollViewContent.setOnScrollChangeListener(
                (NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    binding.ivClinicCover.setTranslationY(scrollY * 0.42f);
                }
        );

        UiMotionUtils.attachSpringTouch(
                binding.btnBack,
                binding.btnShare,
                binding.btnHeart,
                binding.btnCallClinic,
                binding.btnDirections,
                binding.btnMapDirections,
                binding.btnBookAppointment
        );
    }

    private void extractIntentData() {
        if (getIntent().hasExtra(EXTRA_CLINIC)) {
            try {
                clinicItem = (ClinicItem) getIntent().getSerializableExtra(EXTRA_CLINIC);
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

        // Dữ liệu mẫu mặc định (An Nhiên - Hà Nội)
        if (clinicItem == null) {
            clinicItem = new ClinicItem(
                    "1",
                    "Phòng khám Thú y An Nhiên",
                    "Số 12 Chùa Hà, Phường Quan Hoa, Cầu Giấy, Hà Nội",
                    4.8,
                    214,
                    "1,2 km",
                    true,
                    "Thứ 2 – CN · 08:00 – 20:00",
                    "024 3833 5566",
                    "Phòng khám chăm sóc thú cưng uy tín tại Hà Nội. Khám tổng quát, tiêm phòng, nha khoa và tiểu phẫu, có phòng xét nghiệm tại chỗ và khu thăm khám yên tĩnh.",
                    "Đến 22:00",
                    "Tiếng Việt, tiếng Anh"
            );
        }
    }

    private void setupClinicInfo() {
        binding.tvClinicName.setText(clinicItem.getName());
        binding.tvClinicAddress.setText(clinicItem.getAddress());
        binding.tvClinicRating.setText(String.valueOf(clinicItem.getRating()));
        binding.tvClinicReviews.setText("(" + clinicItem.getReviews() + " đánh giá)");
        binding.tvClinicDistance.setText(clinicItem.getDistance());
        binding.tvClinicAbout.setText(clinicItem.getAbout());

        if (clinicItem.isOpen()) {
            String hours = clinicItem.getHours() != null ? clinicItem.getHours().trim() : "";
            if (hours.contains("24/7")) {
                binding.tvCoverStatusBadge.setText("● ĐANG MỞ CỬA · 24/7");
            } else {
                String shortHours = formatShortHours(hours);
                binding.tvCoverStatusBadge.setText(shortHours.isEmpty() ? "● ĐANG MỞ CỬA" : "● ĐANG MỞ CỬA · " + shortHours);
            }
            binding.tvCoverStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.ds_success));
        } else {
            binding.tvCoverStatusBadge.setText("● ĐÃ ĐÓNG CỬA");
            binding.tvCoverStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.ds_text_secondary));
        }

        String hoursText = clinicItem.getHours() != null ? clinicItem.getHours().trim() : "";
        binding.tvInfoHours.setText(hoursText);
        if (hoursText.contains("24/7")) {
            binding.layoutEmergencyRow.setVisibility(View.GONE);
            binding.dividerEmergency.setVisibility(View.GONE);
        } else {
            binding.layoutEmergencyRow.setVisibility(View.VISIBLE);
            binding.dividerEmergency.setVisibility(View.VISIBLE);
            binding.tvInfoEmergency.setText(clinicItem.getEmergencyHours());
        }
        binding.tvInfoPhone.setText(clinicItem.getPhone());

        binding.tvMapLocationDesc.setText(clinicItem.getAddress());

        // Khung giờ trống gần nhất
        java.time.LocalDate today = java.time.LocalDate.now();
        String dayOfWeekStr = "Hôm nay";
        switch (today.getDayOfWeek()) {
            case MONDAY: dayOfWeekStr = "Thứ 2"; break;
            case TUESDAY: dayOfWeekStr = "Thứ 3"; break;
            case WEDNESDAY: dayOfWeekStr = "Thứ 4"; break;
            case THURSDAY: dayOfWeekStr = "Thứ 5"; break;
            case FRIDAY: dayOfWeekStr = "Thứ 6"; break;
            case SATURDAY: dayOfWeekStr = "Thứ 7"; break;
            case SUNDAY: dayOfWeekStr = "Chủ nhật"; break;
        }
        binding.tvFastestSlot.setText("Khung giờ trống gần nhất · " + dayOfWeekStr + " " + today.getDayOfMonth() + "/" + today.getMonthValue() + " · 08:30");
    }

    private void setupTopBarActions() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareMessage = "Xem thông tin " + clinicItem.getName() + " tại PET CARE:\n"
                    + clinicItem.getAddress() + "\nHotline: " + clinicItem.getPhone();
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, clinicItem.getName());
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ phòng khám qua"));
        });

        binding.btnHeart.setOnClickListener(v -> {
            isFavorite = !isFavorite;
            UiMotionUtils.animateSelectionPop(binding.btnHeart);
            if (isFavorite) {
                binding.btnHeart.setColorFilter(ContextCompat.getColor(this, R.color.warm_coral));
                Toast.makeText(this, "Đã lưu " + clinicItem.getName() + " vào yêu thích", Toast.LENGTH_SHORT).show();
            } else {
                binding.btnHeart.setColorFilter(ContextCompat.getColor(this, R.color.text_foreground));
                Toast.makeText(this, "Đã bỏ lưu phòng khám", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupQuickContactActions() {
        View.OnClickListener callListener = v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + clinicItem.getPhone().replace(" ", "")));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Gọi " + clinicItem.getPhone(), Toast.LENGTH_SHORT).show();
            }
        };
        binding.btnCallClinic.setOnClickListener(callListener);
        binding.tvInfoPhone.setOnClickListener(callListener);

        View.OnClickListener mapListener = v -> {
            try {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(clinicItem.getName() + ", " + clinicItem.getAddress()));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                startActivity(mapIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Chỉ đường đến " + clinicItem.getAddress(), Toast.LENGTH_SHORT).show();
            }
        };
        binding.btnDirections.setOnClickListener(mapListener);
        binding.btnMapDirections.setOnClickListener(mapListener);

        binding.tvSeeAllServices.setOnClickListener(v -> openSelectServiceScreen());
    }

    private void openSelectServiceScreen() {
        Intent intent = new Intent(this, com.example.petcareui.ui.booking.SelectServiceActivity.class);
        intent.putExtra(com.example.petcareui.ui.booking.SelectServiceActivity.EXTRA_CLINIC, clinicItem);
        if (preselectedPet != null) {
            intent.putExtra(com.example.petcareui.ui.booking.SelectServiceActivity.EXTRA_PRESELECTED_PET, preselectedPet);
        }
        if (preferredCategory != null && !preferredCategory.isEmpty()) {
            intent.putExtra(com.example.petcareui.ui.booking.SelectServiceActivity.EXTRA_PREFERRED_CATEGORY, preferredCategory);
        }
        if (selectedServiceIndex >= 0 && selectedServiceIndex < services.size()) {
            intent.putExtra(com.example.petcareui.ui.booking.SelectServiceActivity.EXTRA_SELECTED_SERVICE, services.get(selectedServiceIndex));
        }
        startActivity(intent);
    }

    private void loadServicesFromApi() {
        Long clinicId = clinicItem != null ? clinicItem.getNumericId() : null;
        if (clinicId == null) {
            clinicId = 1L;
        }

        ApiClient.getClinicApi().getServicesByClinicId(clinicId).enqueue(new Callback<List<ServiceResponse>>() {
            @Override
            public void onResponse(Call<List<ServiceResponse>> call, Response<List<ServiceResponse>> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    services.clear();
                    List<ServiceResponse> body = response.body();
                    if (body.isEmpty()) {
                        showServicesUnavailable("Phòng khám hiện chưa có dịch vụ đang mở.", false);
                        return;
                    }
                    for (ServiceResponse res : body) {
                        int icon = ServiceDisplayUtils.resolveIconByName(res.getName());
                        String durationText = res.getDuration() != null ? res.getDuration() + " phút" : "30 phút";
                        String priceText = ServiceDisplayUtils.resolvePriceByName(res.getName());
                        services.add(new ServiceItem(
                                String.valueOf(res.getId()),
                                res.getName(),
                                resolveShortServiceDesc(res.getName()),
                                durationText,
                                priceText,
                                icon
                        ));
                    }
                    renderServicesList();
                } else {
                    String errMsg = ApiErrorParser.getMessage(response, "Không thể tải danh sách dịch vụ.");
                    showServicesUnavailable(errMsg, true);
                }
            }

            @Override
            public void onFailure(Call<List<ServiceResponse>> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                showServicesUnavailable("Không thể kết nối máy chủ để tải danh sách dịch vụ.", true);
            }
        });
    }

    private void showServicesUnavailable(String message, boolean allowRetry) {
        services.clear();
        serviceBindings.clear();
        selectedServiceIndex = -1;
        binding.containerServicesList.removeAllViews();

        android.widget.LinearLayout wrapper = new android.widget.LinearLayout(this);
        wrapper.setOrientation(android.widget.LinearLayout.VERTICAL);
        int pad = Math.round(14 * getResources().getDisplayMetrics().density);
        wrapper.setPadding(pad, pad, pad, pad);

        android.widget.TextView tvMsg = new android.widget.TextView(this);
        tvMsg.setText(message);
        tvMsg.setTextColor(ContextCompat.getColor(this, R.color.ds_text_secondary));
        tvMsg.setTextSize(13f);
        wrapper.addView(tvMsg);

        if (allowRetry) {
            android.widget.TextView tvRetry = new android.widget.TextView(this);
            tvRetry.setText("Thử tải lại");
            tvRetry.setTextColor(ContextCompat.getColor(this, R.color.ds_primary));
            tvRetry.setTextSize(13.5f);
            tvRetry.setTypeface(tvRetry.getTypeface(), android.graphics.Typeface.BOLD);
            int topPad = Math.round(8 * getResources().getDisplayMetrics().density);
            tvRetry.setPadding(0, topPad, 0, 0);
            tvRetry.setOnClickListener(v -> loadServicesFromApi());
            wrapper.addView(tvRetry);
        }

        binding.containerServicesList.addView(wrapper);
    }

    private void renderServicesList() {
        serviceBindings.clear();
        binding.containerServicesList.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < services.size(); i++) {
            final int index = i;
            ServiceItem item = services.get(i);
            ItemServiceSelectableBinding itemBinding = ItemServiceSelectableBinding.inflate(inflater, binding.containerServicesList, false);

            itemBinding.ivServiceIcon.setImageResource(item.getIconResId());
            itemBinding.tvServiceName.setText(item.getName());
            if (item.getDesc() != null && !item.getDesc().trim().isEmpty()) {
                itemBinding.tvServiceDesc.setText(item.getDesc());
                itemBinding.tvServiceDesc.setVisibility(View.VISIBLE);
            } else {
                itemBinding.tvServiceDesc.setVisibility(View.GONE);
            }
            itemBinding.tvServiceDuration.setText(item.getDuration());
            itemBinding.tvServicePrice.setText(item.getPrice());
            itemBinding.dividerServiceRow.setVisibility(i == services.size() - 1 ? View.GONE : View.VISIBLE);

            UiMotionUtils.attachSpringTouch(itemBinding.getRoot());
            itemBinding.getRoot().setOnClickListener(v -> toggleServiceSelection(index));

            serviceBindings.add(itemBinding);
            binding.containerServicesList.addView(itemBinding.getRoot());
        }

        if (selectedServiceIndex == -1 && preferredCategory != null && !preferredCategory.isEmpty()) {
            for (int i = 0; i < services.size(); i++) {
                if (ClinicListActivity.matchesServiceCategory(services.get(i).getName(), preferredCategory)) {
                    toggleServiceSelection(i);
                    break;
                }
            }
        }

        UiMotionUtils.animateStaggeredChildren(binding.containerServicesList, 60);
    }

    private void toggleServiceSelection(int index) {
        if (selectedServiceIndex == index) {
            selectedServiceIndex = -1;
            binding.btnBookAppointment.setText("Đặt lịch khám");
        } else {
            selectedServiceIndex = index;
            ServiceItem selected = services.get(index);
            binding.btnBookAppointment.setText("Đặt lịch: " + selected.getName());
            if (index >= 0 && index < serviceBindings.size()) {
                UiMotionUtils.animateSelectionPop(serviceBindings.get(index).getRoot());
            }
        }

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
    }

    private String formatShortHours(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        int parenIdx = s.indexOf('(');
        if (parenIdx > 0) s = s.substring(0, parenIdx).trim();
        int ampIdx = s.indexOf('&');
        if (ampIdx > 0) s = s.substring(0, ampIdx).trim();
        return s;
    }

    private String resolveShortServiceDesc(String name) {
        if (name == null) return "";
        String lower = name.toLowerCase(java.util.Locale.ROOT);
        if (lower.contains("tổng quát") || lower.contains("khám")) {
            return "Kiểm tra lâm sàng, cân nặng và các chỉ số sinh tồn.";
        }
        if (lower.contains("tiêm") || lower.contains("vaccine")) {
            return "Khám sàng lọc trước tiêm và cấp sổ theo dõi mũi tiêm.";
        }
        if (lower.contains("cấp cứu") || lower.contains("24/7")) {
            return "Tiếp nhận xử lý khẩn cấp và theo dõi liên tục.";
        }
        if (lower.contains("răng") || lower.contains("vôi")) {
            return "Cạo vôi siêu âm, đánh bóng và kiểm tra khoang miệng.";
        }
        if (lower.contains("tắm") || lower.contains("spa") || lower.contains("cắt tỉa")) {
            return "Tắm gội, sấy khô, cắt móng và vệ sinh tai.";
        }
        if (lower.contains("siêu âm") || lower.contains("xét nghiệm") || lower.contains("máu")) {
            return "Chẩn đoán nhanh tại chỗ, trả kết quả trực tiếp.";
        }
        return "";
    }

    private void setupBookingCTA() {
        binding.btnBookAppointment.setOnClickListener(v -> openSelectServiceScreen());
    }
}
