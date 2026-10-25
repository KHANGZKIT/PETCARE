package com.example.petcareui.ui.booking;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petcareui.R;
import com.example.petcareui.data.model.ClinicItem;
import com.example.petcareui.data.model.DateItem;
import com.example.petcareui.data.model.PetItem;
import com.example.petcareui.data.model.ServiceItem;
import com.example.petcareui.data.model.TimeSlotItem;
import com.example.petcareui.databinding.ActivityBookingReviewBinding;
import com.example.petcareui.ui.clinic.ClinicDetailsActivity;
import com.example.petcareui.ui.pets.MyPetsActivity;

/**
 * Màn hình C10: Xác nhận thông tin (Booking Review) - Bước 4/4 của luồng đặt lịch.
 * Bám sát nguyên mẫu React C10BookingReview và Design System Spec (hỗ trợ cả 3 trạng thái: Mặc định, Đang gửi, Khung giờ bị chiếm).
 */
public class BookingReviewActivity extends AppCompatActivity {

    public static final String EXTRA_CLINIC = "extra_clinic";
    public static final String EXTRA_SELECTED_SERVICE = "extra_selected_service";
    public static final String EXTRA_SELECTED_DATE = "extra_selected_date";
    public static final String EXTRA_SELECTED_TIME = "extra_selected_time";
    public static final String EXTRA_SELECTED_PET = "extra_selected_pet";
    public static final String EXTRA_STATE = "extra_state"; // "default", "loading", "conflict"

    private ActivityBookingReviewBinding binding;

    private ClinicItem clinicItem;
    private ServiceItem serviceItem;
    private DateItem dateItem;
    private TimeSlotItem timeSlotItem;
    private PetItem petItem;
    private String currentState = "default";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookingReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        extractIntentData();
        setupTopBar();
        populateData();
        setupEditActions();
        setupSubmissionActions();
        applyState(currentState);
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
        if (getIntent().hasExtra(EXTRA_STATE)) {
            currentState = getIntent().getStringExtra(EXTRA_STATE);
        }

        // Dữ liệu mẫu chuẩn theo data.js (STAFF_CLINIC, SERVICES[1], PETS[0], SELECTED) nếu mở độc lập
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

    private void setupTopBar() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void populateData() {
        // Hàng 1: Phòng khám
        binding.tvClinicName.setText(clinicItem.getName());
        binding.tvClinicArea.setText(clinicItem.getArea());

        // Hàng 2: Dịch vụ
        binding.tvServiceName.setText(serviceItem.getName());
        binding.tvServiceDurationPrice.setText(serviceItem.getDuration() + " · " + serviceItem.getPrice());
        binding.ivServiceIcon.setImageResource(serviceItem.getIconResId());

        // Hàng 3: Ngày & giờ khám
        binding.tvDateTime.setText(dateItem.getFullDate() + " · " + timeSlotItem.getTime());

        // Hàng 4: Thú cưng
        binding.tvPetNameBreed.setText(petItem.getName() + " · " + petItem.getBreed());
        binding.tvPetAgeSex.setText(petItem.getAge() + " · " + petItem.getSex());

        // Tổng ước tính
        binding.tvEstimatedTotal.setText(serviceItem.getPrice());

        // Ghi chú ban đầu
        if ("Mochi".equals(petItem.getName())) {
            binding.etBookingNote.setText("Mochi hơi mẩn nhẹ sau mũi tiêm lần trước.");
        }
    }

    private void setupEditActions() {
        // Sửa Phòng khám -> Quay lại C05
        binding.btnEditClinic.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClinicDetailsActivity.class);
            intent.putExtra(ClinicDetailsActivity.EXTRA_CLINIC, clinicItem);
            startActivity(intent);
        });

        // Sửa Dịch vụ -> Quay lại C06
        binding.btnEditService.setOnClickListener(v -> {
            Intent intent = new Intent(this, SelectServiceActivity.class);
            intent.putExtra(SelectServiceActivity.EXTRA_CLINIC, clinicItem);
            startActivity(intent);
        });

        // Sửa Ngày & giờ -> Quay lại C07
        binding.btnEditDateTime.setOnClickListener(v -> {
            Intent intent = new Intent(this, DateTimeActivity.class);
            intent.putExtra(DateTimeActivity.EXTRA_CLINIC, clinicItem);
            intent.putExtra(DateTimeActivity.EXTRA_SELECTED_SERVICE, serviceItem);
            startActivity(intent);
        });

        // Sửa Thú cưng -> Quay lại C08
        binding.btnEditPet.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyPetsActivity.class);
            intent.putExtra(MyPetsActivity.EXTRA_CLINIC, clinicItem);
            intent.putExtra(MyPetsActivity.EXTRA_SELECTED_SERVICE, serviceItem);
            intent.putExtra(MyPetsActivity.EXTRA_SELECTED_DATE, dateItem);
            intent.putExtra(MyPetsActivity.EXTRA_SELECTED_TIME, timeSlotItem);
            startActivity(intent);
        });
    }

    private void setupSubmissionActions() {
        // Nút Xác nhận đặt lịch (Mặc định)
        binding.btnConfirmBooking.setOnClickListener(v -> {
            applyState("loading");

            // Mô phỏng gửi yêu cầu mạng 1.2s rồi chuyển sang màn C11 Đặt lịch thành công
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(this, BookingSuccessActivity.class);
                intent.putExtra(BookingSuccessActivity.EXTRA_CLINIC, clinicItem);
                intent.putExtra(BookingSuccessActivity.EXTRA_SELECTED_SERVICE, serviceItem);
                intent.putExtra(BookingSuccessActivity.EXTRA_SELECTED_DATE, dateItem);
                intent.putExtra(BookingSuccessActivity.EXTRA_SELECTED_TIME, timeSlotItem);
                intent.putExtra(BookingSuccessActivity.EXTRA_SELECTED_PET, petItem);
                intent.putExtra(BookingSuccessActivity.EXTRA_BOOKING_REF, "PC-48213");
                startActivity(intent);
                finish();
            }, 1200);
        });

        // Nút "Chọn giờ khác" khi bị xung đột (Conflict State)
        binding.btnPickOtherSlot.setOnClickListener(v -> {
            Intent intent = new Intent(this, DateTimeActivity.class);
            intent.putExtra(DateTimeActivity.EXTRA_CLINIC, clinicItem);
            intent.putExtra(DateTimeActivity.EXTRA_SELECTED_SERVICE, serviceItem);
            startActivity(intent);
            finish();
        });

        // Nút "Xem lại sau" khi bị xung đột
        binding.btnReviewLater.setOnClickListener(v -> finish());
    }

    /**
     * Chuyển đổi linh hoạt giữa 3 trạng thái theo Spec: "default", "loading", "conflict"
     */
    public void applyState(String state) {
        currentState = state;

        if ("conflict".equals(state)) {
            // Trạng thái 3: Khung giờ bị chiếm
            binding.layoutNoticeWarning.setVisibility(View.GONE);
            binding.layoutNoticeConflict.setVisibility(View.VISIBLE);
            binding.layoutSubmittingToast.setVisibility(View.GONE);
            binding.layoutConflictStatusBar.setVisibility(View.VISIBLE);
            binding.layoutDefaultButtons.setVisibility(View.GONE);
            binding.layoutConflictButtons.setVisibility(View.VISIBLE);
        } else if ("loading".equals(state)) {
            // Trạng thái 2: Đang gửi yêu cầu
            binding.layoutNoticeWarning.setVisibility(View.VISIBLE);
            binding.layoutNoticeConflict.setVisibility(View.GONE);
            binding.layoutSubmittingToast.setVisibility(View.VISIBLE);
            binding.layoutConflictStatusBar.setVisibility(View.GONE);
            binding.layoutDefaultButtons.setVisibility(View.VISIBLE);
            binding.layoutConflictButtons.setVisibility(View.GONE);

            binding.btnConfirmBooking.setText("Đang gửi yêu cầu…");
            binding.btnConfirmBooking.setEnabled(false);
        } else {
            // Trạng thái 1: Mặc định
            binding.layoutNoticeWarning.setVisibility(View.VISIBLE);
            binding.layoutNoticeConflict.setVisibility(View.GONE);
            binding.layoutSubmittingToast.setVisibility(View.GONE);
            binding.layoutConflictStatusBar.setVisibility(View.GONE);
            binding.layoutDefaultButtons.setVisibility(View.VISIBLE);
            binding.layoutConflictButtons.setVisibility(View.GONE);

            binding.btnConfirmBooking.setText("Xác nhận đặt lịch");
            binding.btnConfirmBooking.setEnabled(true);
        }
    }
}
