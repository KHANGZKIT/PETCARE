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
import com.example.petcareui.data.api.ApiClient;
import com.example.petcareui.data.model.AppointmentResponse;
import com.example.petcareui.data.model.StaffAppointmentItem;
import com.example.petcareui.databinding.ActivityStaffDashboardBinding;
import com.example.petcareui.databinding.ItemStaffPendingCardBinding;
import com.example.petcareui.databinding.ItemStaffScheduleRowBinding;
import com.example.petcareui.ui.notifications.NotificationsActivity;
import com.example.petcareui.utils.TokenManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Màn hình S01: Bảng làm việc nhân viên (Staff Dashboard).
 * Thuộc phân hệ riêng biệt com.example.petcareui.ui.staff,
 * bám sát nguyên mẫu React S01Dashboard và kết nối API thật /api/v1/staff/appointments.
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
        setupTopBar();
        setupStateToggles();
        setupShortcuts();
        setupBottomNav();

        loadDashboardData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void extractIntentData() {
        if (getIntent().hasExtra(EXTRA_STATE)) {
            currentState = getIntent().getStringExtra(EXTRA_STATE);
        }
    }

    private void loadDashboardData() {
        ApiClient.getAppointmentApi().getClinicAppointments(null).enqueue(new Callback<List<AppointmentResponse>>() {
            @Override
            public void onResponse(Call<List<AppointmentResponse>> call, Response<List<AppointmentResponse>> response) {
                pendingList.clear();
                todayScheduleList.clear();

                if (response.isSuccessful() && response.body() != null) {
                    for (AppointmentResponse res : response.body()) {
                        StaffAppointmentItem item = res.toStaffAppointmentItem();
                        if ("PENDING".equalsIgnoreCase(item.getStatus())) {
                            pendingList.add(item);
                        } else if ("CONFIRMED".equalsIgnoreCase(item.getStatus())) {
                            todayScheduleList.add(item);
                        }
                    }
                    if (pendingList.isEmpty() && todayScheduleList.isEmpty()) {
                        applyState("empty");
                    } else {
                        applyState("default");
                    }
                } else {
                    applyState("empty");
                }
            }

            @Override
            public void onFailure(Call<List<AppointmentResponse>> call, Throwable t) {
                pendingList.clear();
                todayScheduleList.clear();
                applyState("empty");
                Toast.makeText(StaffDashboardActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTopBar() {
        TokenManager tokenManager = TokenManager.getInstance(this);
        Long clinicId = tokenManager.getClinicId();
        String clinicName = (clinicId != null && clinicId == 1) ? "Phòng khám An Nhiên"
                : (clinicId != null && clinicId == 2) ? "Bệnh viện Thú y HaUI"
                : (clinicId != null && clinicId == 3) ? "Bệnh viện PetCare Đống Đa"
                : "Phòng khám #" + (clinicId != null ? clinicId : "");
        String clinicArea = (clinicId != null && clinicId == 1) ? "Cầu Giấy · Hà Nội"
                : (clinicId != null && clinicId == 2) ? "Bắc Từ Liêm · Hà Nội"
                : (clinicId != null && clinicId == 3) ? "Đống Đa · Hà Nội"
                : "Hà Nội";

        binding.tvStaffClinicName.setText(clinicName);
        binding.tvStaffSubtitle.setText("Khu vực nhân viên · " + clinicArea);

        // Mở Thông báo nhân viên
        binding.btnStaffNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificationsActivity.class);
            startActivity(intent);
        });

        // Hộp thoại thông tin nhân viên & Chuyển đổi vai trò Khách hàng / Nhân viên
        binding.btnStaffAvatar.setOnClickListener(v -> showStaffAccountDialog());
    }

    private void setupStateToggles() {
        // Ẩn thanh toggle trạng thái giả lập khi dùng ứng dụng thật
        if (binding.chipStateDefault.getParent() instanceof View) {
            ((View) binding.chipStateDefault.getParent()).setVisibility(View.GONE);
        }
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
                intent.putExtra(StaffAppointmentDetailsActivity.EXTRA_APPT, item);
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
                intent.putExtra(StaffAppointmentDetailsActivity.EXTRA_APPT, item);
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
        TokenManager tokenManager = TokenManager.getInstance(this);
        String name = tokenManager.getUserName();
        String email = tokenManager.getUserEmail();
        Long clinicId = tokenManager.getClinicId();
        String clinicName = (clinicId != null && clinicId == 1) ? "Phòng khám Thú y An Nhiên (Cầu Giấy, Hà Nội)"
                : (clinicId != null && clinicId == 2) ? "Bệnh viện Thú y HaUI (Bắc Từ Liêm, Hà Nội)"
                : (clinicId != null && clinicId == 3) ? "Bệnh viện Thú y PetCare (Đống Đa, Hà Nội)"
                : "Phòng khám cơ sở #" + (clinicId != null ? clinicId : "--");

        new AlertDialog.Builder(this)
                .setTitle("Tài khoản Nhân viên")
                .setMessage("Nhân viên: " + (name != null && !name.isEmpty() ? name : "Bác sĩ phụ trách")
                        + "\nEmail: " + (email != null && !email.isEmpty() ? email : "Chưa cập nhật")
                        + "\nCơ sở: " + clinicName
                        + "\nVai trò: Quản trị viên phòng khám")
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
