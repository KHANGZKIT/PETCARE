package com.example.petcareui.ui.booking;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petcareui.MainActivity;
import com.example.petcareui.R;
import com.example.petcareui.data.model.ClinicItem;
import com.example.petcareui.data.model.DateItem;
import com.example.petcareui.data.model.PetItem;
import com.example.petcareui.data.model.ServiceItem;
import com.example.petcareui.data.model.TimeSlotItem;
import com.example.petcareui.databinding.ActivityBookingSuccessBinding;

/**
 * Màn hình C11: Đặt lịch thành công (Booking Success).
 * Bám sát nguyên mẫu React C11BookingSuccess và Design System Spec.
 * Hiển thị xác nhận thành công, mã lịch hẹn, huy hiệu "Chờ xác nhận",
 * tóm tắt lịch hẹn, lộ trình ba bước và 2 hành động điều hướng.
 */
public class BookingSuccessActivity extends AppCompatActivity {

    public static final String EXTRA_CLINIC = "extra_clinic";
    public static final String EXTRA_SELECTED_SERVICE = "extra_selected_service";
    public static final String EXTRA_SELECTED_DATE = "extra_selected_date";
    public static final String EXTRA_SELECTED_TIME = "extra_selected_time";
    public static final String EXTRA_SELECTED_PET = "extra_selected_pet";
    public static final String EXTRA_BOOKING_REF = "extra_booking_ref";

    private ActivityBookingSuccessBinding binding;

    private ClinicItem clinicItem;
    private ServiceItem serviceItem;
    private DateItem dateItem;
    private TimeSlotItem timeSlotItem;
    private PetItem petItem;
    private String bookingRef = "PC-48213";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookingSuccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        extractIntentData();
        populateData();
        setupActions();
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
                serviceItem = (ServiceItem) getIntent().getSerializableExtra(EXTRA_SELECTED_SERVICE);
            } catch (Exception ignored) {
            }
        }
        if (getIntent().hasExtra(EXTRA_SELECTED_DATE)) {
            try {
                dateItem = (DateItem) getIntent().getSerializableExtra(EXTRA_SELECTED_DATE);
            } catch (Exception ignored) {
            }
        }
        if (getIntent().hasExtra(EXTRA_SELECTED_TIME)) {
            try {
                timeSlotItem = (TimeSlotItem) getIntent().getSerializableExtra(EXTRA_SELECTED_TIME);
            } catch (Exception ignored) {
            }
        }
        if (getIntent().hasExtra(EXTRA_SELECTED_PET)) {
            try {
                petItem = (PetItem) getIntent().getSerializableExtra(EXTRA_SELECTED_PET);
            } catch (Exception ignored) {
            }
        }
        if (getIntent().hasExtra(EXTRA_BOOKING_REF)) {
            bookingRef = getIntent().getStringExtra(EXTRA_BOOKING_REF);
        }

        // Dữ liệu fallback chuẩn theo data.js (STAFF_CLINIC, SERVICES[1], PETS[0], SELECTED) nếu mở độc lập
        if (clinicItem == null) {
            clinicItem = new ClinicItem(
                    "cl-01", "Phòng khám Thú y An Nhiên", "42 Nguyễn Huệ, Quận 1, TP.HCM", 4.8, 214, "1,2 km", true, "Thứ 2 – CN · 08:00 – 20:00"
            );
        }
        if (serviceItem == null) {
            serviceItem = new ServiceItem("sv-02", "Tiêm phòng", "Vaccine thiết yếu cho chó và mèo, khám sức khỏe trước khi tiêm.", "20 phút", "250.000 ₫", R.drawable.ic_syringe);
        }
        if (dateItem == null) {
            dateItem = new DateItem("T2", "27", "Thứ 2, 27/10/2026", "Thứ 2 · 27/10", 6, "selected");
        }
        if (timeSlotItem == null) {
            timeSlotItem = new TimeSlotItem("10:00", "selected");
        }
        if (petItem == null) {
            petItem = new PetItem("pet-01", "Mochi", "Chó", "Pomeranian", "2 tuổi", "12/03/2024", "Cái", "3,4 kg", R.drawable.img_pet_mochi);
        }
    }

    private void populateData() {
        // Tên ngắn phòng khám cho phụ đề
        String clinicShort = "An Nhiên";
        if (clinicItem != null && clinicItem.getName() != null) {
            if (clinicItem.getName().contains("An Nhiên")) {
                clinicShort = "An Nhiên";
            } else {
                clinicShort = clinicItem.getName();
            }
        }
        binding.tvSuccessSubtitle.setText(clinicShort + " đã nhận yêu cầu của bạn. Lịch hẹn đang chờ phòng khám xác nhận.");

        // Mã lịch hẹn
        binding.tvBookingRef.setText("Mã lịch hẹn · " + bookingRef);

        // Thẻ tóm tắt: Dịch vụ
        binding.tvServiceName.setText(serviceItem.getName());
        binding.tvServiceDurationPrice.setText(serviceItem.getDuration() + " · " + serviceItem.getPrice());
        binding.ivServiceIcon.setImageResource(serviceItem.getIconResId());

        // Thẻ tóm tắt: Phòng khám
        binding.tvClinicName.setText(clinicItem.getName());
        binding.tvClinicArea.setText(clinicItem.getArea());

        // Thẻ tóm tắt: Ngày giờ
        binding.tvDateTime.setText(dateItem.getFullDate() + " · " + timeSlotItem.getTime());

        // Thẻ tóm tắt: Thú cưng
        binding.tvPetNameBreed.setText(petItem.getName() + " · " + petItem.getBreed());
        binding.tvPetAgeSex.setText(petItem.getAge() + " · " + petItem.getSex());

        // Banner thông báo (định dạng HTML in đậm "chờ xác nhận")
        String warningHtml = "Lịch hẹn đang <b>chờ xác nhận</b>. Chúng tôi sẽ thông báo ngay khi phòng khám phản hồi.";
        binding.tvNoticeWarning.setText(Html.fromHtml(warningHtml, Html.FROM_HTML_MODE_LEGACY));
    }

    private void setupActions() {
        // Nút chính: Xem chi tiết lịch hẹn -> Mở Chi tiết lịch hẹn (C13)
        binding.btnViewAppointmentDetails.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.petcareui.ui.appointments.AppointmentDetailsActivity.class);
            intent.putExtra(com.example.petcareui.ui.appointments.AppointmentDetailsActivity.EXTRA_REF, bookingRef);
            intent.putExtra(com.example.petcareui.ui.appointments.AppointmentDetailsActivity.EXTRA_STATE, "pending");
            startActivity(intent);
            finish();
        });

        // Nút phụ: Về trang chủ
        binding.btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Tránh quay lại màn hình xác nhận đặt lịch khi bấm nút Back vật lý
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
