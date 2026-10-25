package com.example.petcareui.ui.clinic;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.petcareui.R;
import com.example.petcareui.data.model.ClinicItem;
import com.example.petcareui.data.model.ServiceItem;
import com.example.petcareui.databinding.ActivityClinicDetailsBinding;
import com.example.petcareui.databinding.ItemServiceSelectableBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình C05: Chi tiết phòng khám (Clinic Details).
 * Bám sát nguyên mẫu React C05ClinicDetails và Design System Spec.
 */
public class ClinicDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_CLINIC = "extra_clinic";

    private ActivityClinicDetailsBinding binding;
    private ClinicItem clinicItem;
    private boolean isFavorite = false;

    private final List<ServiceItem> services = new ArrayList<>();
    private final List<ItemServiceSelectableBinding> serviceBindings = new ArrayList<>();
    private int selectedServiceIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClinicDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        extractIntentData();
        setupClinicInfo();
        setupTopBarActions();
        setupQuickContactActions();
        setupServicesList();
        setupBookingCTA();
    }

    private void extractIntentData() {
        if (getIntent().hasExtra(EXTRA_CLINIC)) {
            try {
                clinicItem = (ClinicItem) getIntent().getSerializableExtra(EXTRA_CLINIC);
            } catch (Exception ignored) {
            }
        }

        // Dữ liệu mẫu mặc định (An Nhiên - chuẩn theo prototype CLINICS[0])
        if (clinicItem == null) {
            clinicItem = new ClinicItem(
                    "cl-01",
                    "Phòng khám Thú y An Nhiên",
                    "42 Nguyễn Huệ, Quận 1, TP.HCM",
                    4.8,
                    214,
                    "1,2 km",
                    true,
                    "Thứ 2 – CN · 08:00 – 20:00",
                    "+84 28 3822 1140",
                    "Phòng khám nhỏ cho chó và mèo. Khám tổng quát, tiêm phòng, nha khoa và tiểu phẫu, có phòng xét nghiệm tại chỗ và khu thăm khám yên tĩnh, ít gây căng thẳng cho thú cưng.",
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

        // Badge trên ảnh bìa
        if (clinicItem.isOpen()) {
            binding.tvCoverStatusBadge.setText("ĐANG MỞ CỬA · ĐẾN 20:00");
            binding.tvCoverStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_success_ink));
        } else {
            binding.tvCoverStatusBadge.setText("ĐÃ ĐÓNG CỬA");
            binding.tvCoverStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
        }

        // Thẻ thông tin phòng khám
        binding.tvInfoHours.setText(clinicItem.getHours());
        binding.tvInfoEmergency.setText(clinicItem.getEmergencyHours());
        binding.tvInfoLanguages.setText(clinicItem.getLanguages());
        binding.tvInfoPhone.setText(clinicItem.getPhone());

        // Mô tả vị trí dưới bản đồ
        binding.tvMapLocationDesc.setText(clinicItem.getAddress() + " · đi ô tô 6 phút");
    }

    private void setupTopBarActions() {
        // Nút quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút chia sẻ phòng khám (System Share Sheet)
        binding.btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareMessage = "Xem thông tin " + clinicItem.getName() + " tại PET CARE:\n"
                    + clinicItem.getAddress() + "\nHotline: " + clinicItem.getPhone();
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, clinicItem.getName());
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ phòng khám qua"));
        });

        // Nút yêu thích
        binding.btnHeart.setOnClickListener(v -> {
            isFavorite = !isFavorite;
            if (isFavorite) {
                binding.btnHeart.setColorFilter(ContextCompat.getColor(this, R.color.status_destructive));
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

        binding.tvSeeAllServices.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.petcareui.ui.booking.SelectServiceActivity.class);
            intent.putExtra(com.example.petcareui.ui.booking.SelectServiceActivity.EXTRA_CLINIC, clinicItem);
            startActivity(intent);
        });
    }

    private void setupServicesList() {
        services.clear();
        serviceBindings.clear();
        binding.containerServicesList.removeAllViews();

        services.add(new ServiceItem("sv-01", "Khám tổng quát", "Khám lâm sàng, cân nặng, nhiệt độ và các chỉ số sinh tồn.", "30 phút", "180.000 ₫", R.drawable.ic_stethoscope));
        services.add(new ServiceItem("sv-02", "Tiêm phòng", "Vaccine thiết yếu cho chó và mèo, khám sức khỏe trước khi tiêm.", "20 phút", "250.000 ₫", R.drawable.ic_syringe));
        services.add(new ServiceItem("sv-03", "Cạo vôi & chăm sóc răng", "Cạo vôi bằng siêu âm, đánh bóng và kiểm tra khoang miệng.", "45 phút", "600.000 ₫", R.drawable.ic_sparkles));
        services.add(new ServiceItem("sv-04", "Tắm & cắt tỉa", "Tắm, sấy khô, cắt móng và vệ sinh tai.", "60 phút", "320.000 ₫", R.drawable.ic_scissors));

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

            itemBinding.getRoot().setOnClickListener(v -> toggleServiceSelection(index));

            serviceBindings.add(itemBinding);
            binding.containerServicesList.addView(itemBinding.getRoot());
        }
    }

    private void toggleServiceSelection(int index) {
        if (selectedServiceIndex == index) {
            // Hủy chọn
            selectedServiceIndex = -1;
            binding.btnBookAppointment.setText("Đặt lịch khám");
        } else {
            selectedServiceIndex = index;
            ServiceItem selected = services.get(index);
            binding.btnBookAppointment.setText("Đặt lịch: " + selected.getName());
        }

        // Cập nhật giao diện từng thẻ
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
    }

    private void setupBookingCTA() {
        binding.btnBookAppointment.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.petcareui.ui.booking.SelectServiceActivity.class);
            intent.putExtra(com.example.petcareui.ui.booking.SelectServiceActivity.EXTRA_CLINIC, clinicItem);
            startActivity(intent);
        });
    }
}
