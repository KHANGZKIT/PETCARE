package com.example.petcareui.ui.staff;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.petcareui.MainActivity;
import com.example.petcareui.R;
import com.example.petcareui.data.model.StaffAppointmentItem;
import com.example.petcareui.databinding.ActivityStaffDashboardBinding;
import com.example.petcareui.databinding.ItemStaffPendingCardBinding;
import com.example.petcareui.databinding.ItemStaffScheduleRowBinding;
import com.example.petcareui.ui.notifications.NotificationsActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình S01: Bảng làm việc nhân viên (Staff Dashboard).
 * Thuộc phân hệ riêng biệt com.example.petcareui.ui.staff,
 * bám sát nguyên mẫu React S01Dashboard và Design System Spec.
 * Hỗ trợ 3 trạng thái: Mặc định, Đang tải (Loading), Không có lịch hẹn (Empty).
 */
public class StaffDashboardActivity extends AppCompatActivity {

    public static final String EXTRA_STATE = "extra_state"; // "default", "loading", "empty"

    private ActivityStaffDashboardBinding binding;
    private final List<StaffAppointmentItem> pendingList = new ArrayList<>();
    private final List<StaffAppointmentItem> todayScheduleList = new ArrayList<>();
    private String currentState = "default";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStaffDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        extractIntentData();
        initMockData();
        setupTopBar();
        setupStateToggles();
        setupShortcuts();
        setupBottomNav();
        applyState(currentState);
    }

    private void extractIntentData() {
        if (getIntent().hasExtra(EXTRA_STATE)) {
            currentState = getIntent().getStringExtra(EXTRA_STATE);
        }
    }

    private void initMockData() {
        // Dữ liệu chuẩn theo data.js (STAFF_APPTS)
        pendingList.clear();
        pendingList.add(new StaffAppointmentItem(
                "PC-48213", "Mochi", R.drawable.img_pet_mochi,
                "Pomeranian · 2 tuổi · Cái · 3,4 kg",
                "Nguyễn Lan", "+84 90 123 4567", "Khách hàng từ tháng 3/2026",
                "Tiêm phòng", "20 phút", "Thứ 2, 27/10/2026", "10:00",
                "PENDING", "250.000 ₫", "Gửi cách đây 2 giờ",
                "Mochi hơi mẩn nhẹ sau mũi tiêm lần trước."
        ));

        pendingList.add(new StaffAppointmentItem(
                "PC-48230", "Lựu", R.drawable.img_pet_luu,
                "Corgi · 6 tháng · Đực · 7,2 kg",
                "Phạm Hà", "+84 91 884 2310", "Khách hàng từ tháng 6/2026",
                "Khám tổng quát", "30 phút", "Thứ 2, 27/10/2026", "11:30",
                "PENDING", "180.000 ₫", "Gửi cách đây 5 giờ", null
        ));

        todayScheduleList.clear();
        todayScheduleList.add(new StaffAppointmentItem(
                "PC-47980", "Bông", R.drawable.img_pet_bong,
                "Anh lông ngắn · 4 tuổi · Đực · 5,1 kg",
                "Trần Minh", "+84 93 220 7781", "Khách hàng từ tháng 1/2025",
                "Cạo vôi & chăm sóc răng", "45 phút", "Thứ 2, 27/10/2026", "14:00",
                "CONFIRMED", "600.000 ₫", "Phòng khám đã xác nhận",
                "Nhờ bác sĩ xem lại răng hàm dưới bên trái."
        ));

        todayScheduleList.add(new StaffAppointmentItem(
                "PC-48112", "Miu", R.drawable.img_pet_bong,
                "Mèo ta · 3 tuổi · Cái · 4,2 kg",
                "Đỗ Quỳnh", "+84 96 771 5522", "Khách hàng từ tháng 4/2026",
                "Tắm & cắt tỉa", "60 phút", "Thứ 2, 27/10/2026", "15:30",
                "CONFIRMED", "320.000 ₫", "Phòng khám đã xác nhận", null
        ));
    }

    private void setupTopBar() {
        // Mở Thông báo nhân viên
        binding.btnStaffNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificationsActivity.class);
            startActivity(intent);
        });

        // Hộp thoại thông tin nhân viên & Chuyển đổi vai trò Khách hàng / Nhân viên
        binding.btnStaffAvatar.setOnClickListener(v -> showStaffAccountDialog());
    }

    private void setupStateToggles() {
        binding.chipStateDefault.setOnClickListener(v -> applyState("default"));
        binding.chipStateLoading.setOnClickListener(v -> applyState("loading"));
        binding.chipStateEmpty.setOnClickListener(v -> applyState("empty"));
    }

    private void applyState(String state) {
        currentState = state;

        updateToggleChip(binding.chipStateDefault, "default".equals(state));
        updateToggleChip(binding.chipStateLoading, "loading".equals(state));
        updateToggleChip(binding.chipStateEmpty, "empty".equals(state));

        if ("loading".equals(state)) {
            binding.layoutDefaultContent.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.layoutLoadingState.setVisibility(View.VISIBLE);
        } else if ("empty".equals(state)) {
            binding.layoutDefaultContent.setVisibility(View.GONE);
            binding.layoutLoadingState.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            binding.layoutLoadingState.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.layoutDefaultContent.setVisibility(View.VISIBLE);
            renderDefaultContent();
        }
    }

    private void updateToggleChip(TextView chip, boolean active) {
        if (active) {
            chip.setBackgroundResource(R.drawable.bg_chip_active);
            chip.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_inactive);
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
        }
    }

    private void renderDefaultContent() {
        // 1. Render danh sách chờ duyệt (Pending cards)
        binding.layoutPendingList.removeAllViews();
        binding.tvPendingNoticeTitle.setText(pendingList.size() + " yêu cầu đang chờ duyệt");

        for (StaffAppointmentItem item : pendingList) {
            ItemStaffPendingCardBinding cardBinding = ItemStaffPendingCardBinding.inflate(getLayoutInflater());
            cardBinding.tvStaffApptRef.setText(item.getRef());
            cardBinding.ivStaffPetAvatar.setImageResource(item.getPetImgRes());
            cardBinding.tvStaffServiceTitle.setText(item.getService());
            cardBinding.tvStaffCustomerPet.setText(item.getCustomer() + " · " + item.getPet());
            cardBinding.tvStaffDate.setText(item.getDate());
            cardBinding.tvStaffTime.setText(item.getTime());
            cardBinding.tvStaffDuration.setText(item.getDuration());

            cardBinding.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(this, StaffAppointmentDetailsActivity.class);
                intent.putExtra(StaffAppointmentDetailsActivity.EXTRA_REF, item.getRef());
                startActivity(intent);
            });

            binding.layoutPendingList.addView(cardBinding.getRoot());
        }

        // 2. Render danh sách hôm nay (Schedule rows)
        binding.layoutTodaySchedule.removeAllViews();
        for (StaffAppointmentItem item : todayScheduleList) {
            ItemStaffScheduleRowBinding rowBinding = ItemStaffScheduleRowBinding.inflate(getLayoutInflater());
            rowBinding.tvScheduleTime.setText(item.getTime());
            rowBinding.tvSchedulePetService.setText(item.getPet() + " · " + item.getService());
            rowBinding.tvScheduleCustomer.setText(item.getCustomer());

            rowBinding.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(this, StaffAppointmentDetailsActivity.class);
                intent.putExtra(StaffAppointmentDetailsActivity.EXTRA_REF, item.getRef());
                startActivity(intent);
            });

            binding.layoutTodaySchedule.addView(rowBinding.getRoot());
        }
    }

    private void setupShortcuts() {
        binding.shortcutNewSlot.setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffSlotManagementActivity.class);
            intent.putExtra(StaffSlotManagementActivity.EXTRA_STATE, "new-slot");
            startActivity(intent);
        });

        binding.shortcutManageSlots.setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffSlotManagementActivity.class);
            startActivity(intent);
        });

        binding.shortcutClinicProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Mở hồ sơ phòng khám & nhân viên", Toast.LENGTH_SHORT).show();
        });

        binding.tvSeeAllPending.setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffAppointmentsActivity.class);
            intent.putExtra(StaffAppointmentsActivity.EXTRA_INITIAL_FILTER, "PENDING");
            startActivity(intent);
        });

        binding.tvSeeAllToday.setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffAppointmentsActivity.class);
            intent.putExtra(StaffAppointmentsActivity.EXTRA_INITIAL_FILTER, "ALL");
            startActivity(intent);
        });

        binding.btnEmptyManageSlots.setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffSlotManagementActivity.class);
            startActivity(intent);
        });
    }

    private void setupBottomNav() {
        // Tab 1: Hôm nay (Đang ở màn này)
        binding.navStaffToday.setOnClickListener(v -> {
            binding.scrollStaffBody.smoothScrollTo(0, 0);
        });

        // Tab 2: Lịch hẹn (S02)
        binding.navStaffAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffAppointmentsActivity.class);
            startActivity(intent);
        });

        // Tab 3: Khung giờ (S04)
        binding.navStaffSlots.setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffSlotManagementActivity.class);
            startActivity(intent);
        });
    }

    private void showStaffAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Tài khoản Nhân viên")
                .setMessage("Nhân viên: Trần Minh\nPhòng khám: Thú y An Nhiên (Quận 1)\nVai trò: Quản trị viên phòng khám")
                .setPositiveButton("Chuyển sang Khách hàng", (dialog, which) -> {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Đóng", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
