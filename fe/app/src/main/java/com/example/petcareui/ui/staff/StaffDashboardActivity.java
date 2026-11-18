package com.example.petcareui.ui.staff;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.petcareui.R;
import com.example.petcareui.data.api.ApiClient;
import com.example.petcareui.data.dto.response.AppointmentResponse;
import com.example.petcareui.data.model.StaffAppointmentItem;
import com.example.petcareui.databinding.ActivityStaffDashboardBinding;
import com.example.petcareui.databinding.ItemStaffPendingCardBinding;
import com.example.petcareui.databinding.ItemStaffScheduleRowBinding;
import com.example.petcareui.ui.auth.LoginActivity;
import com.example.petcareui.ui.notifications.NotificationsActivity;
import com.example.petcareui.ui.profile.ProfileActivity;
import com.example.petcareui.utils.TokenManager;
import com.example.petcareui.utils.UiMotionUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffDashboardActivity extends AppCompatActivity {

    public static final String EXTRA_STATE = "extra_state";

    private ActivityStaffDashboardBinding binding;
    private TokenManager tokenManager;
    private final List<StaffAppointmentItem> pendingList = new ArrayList<>();
    private final List<StaffAppointmentItem> todayScheduleList = new ArrayList<>();
    private String currentState = "default";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStaffDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);
        if (!tokenManager.isLoggedIn()) {
            goToLogin();
            return;
        }

        extractIntentData();
        UiMotionUtils.applyWindowInsets(binding.getRoot(), binding.layoutStaffAppBar, binding.layoutStaffBottomNav);
        bindStaffHeader();
        setupTopBar();
        setupStateToggles();
        setupShortcuts();
        setupBottomNav();

        UiMotionUtils.animateEntranceSections(
                binding.layoutStaffAppBar,
                binding.cardStaffHero,
                binding.cardClinicShortcuts
        );

        StaffClinicHelper.syncStaffAndClinicInfo(this, this::bindStaffHeader, this::handleUnauthorized);
        loadDashboardData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!tokenManager.isLoggedIn()) {
            goToLogin();
            return;
        }
        bindStaffHeader();
        StaffClinicHelper.syncStaffAndClinicInfo(this, this::bindStaffHeader, this::handleUnauthorized);
        loadDashboardData();
    }

    private void extractIntentData() {
        if (getIntent().hasExtra(EXTRA_STATE)) {
            currentState = getIntent().getStringExtra(EXTRA_STATE);
        }
    }

    private void bindStaffHeader() {
        binding.tvStaffClinicName.setText(StaffClinicHelper.buildClinicDisplayTitle(tokenManager));
        String area = tokenManager.getClinicArea();
        binding.tvStaffSubtitle.setText(!area.isEmpty() ? "Khu vực nhân viên · " + area : "Khu vực nhân viên");
        binding.tvStaffAvatarInitials.setText(StaffClinicHelper.formatInitials(tokenManager.getUserName()));
        binding.tvTodayScheduleTitle.setText(StaffClinicHelper.formatTodayShortLabel(LocalDate.now()));
    }

    private void handleUnauthorized() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        Toast.makeText(this, "Phiên đăng nhập không hợp lệ hoặc đã hết hạn, vui lòng đăng nhập lại", Toast.LENGTH_LONG).show();
        tokenManager.clear();
        goToLogin();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadDashboardData() {
        ApiClient.getAppointmentApi().getClinicAppointments(null).enqueue(new Callback<List<AppointmentResponse>>() {
            @Override
            public void onResponse(Call<List<AppointmentResponse>> call, Response<List<AppointmentResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    pendingList.clear();
                    todayScheduleList.clear();
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
                    } else if ("loading".equals(currentState) || "empty".equals(currentState)) {
                        applyState("default");
                    } else {
                        applyState(currentState);
                    }
                } else if (response.code() == 401 || response.code() == 403) {
                    handleUnauthorized();
                } else {
                    pendingList.clear();
                    todayScheduleList.clear();
                    applyState("empty");
                    Toast.makeText(StaffDashboardActivity.this,
                            "Không tải được dữ liệu lịch hẹn",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AppointmentResponse>> call, Throwable t) {
                pendingList.clear();
                todayScheduleList.clear();
                applyState("empty");
                Toast.makeText(StaffDashboardActivity.this,
                        "Lỗi kết nối máy chủ khi tải lịch hẹn",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTopBar() {
        UiMotionUtils.attachSpringTouch(binding.btnStaffNotifications, binding.btnStaffAvatar);

        binding.btnStaffNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificationsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_scale_out);
        });

        binding.btnStaffAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_scale_out);
        });
    }

    private void setupStateToggles() {
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
        java.time.LocalDate today = java.time.LocalDate.now();
        String dayOfWeekStr = "Thứ 2";
        switch (today.getDayOfWeek()) {
            case MONDAY: dayOfWeekStr = "Thứ 2"; break;
            case TUESDAY: dayOfWeekStr = "Thứ 3"; break;
            case WEDNESDAY: dayOfWeekStr = "Thứ 4"; break;
            case THURSDAY: dayOfWeekStr = "Thứ 5"; break;
            case FRIDAY: dayOfWeekStr = "Thứ 6"; break;
            case SATURDAY: dayOfWeekStr = "Thứ 7"; break;
            case SUNDAY: dayOfWeekStr = "Chủ nhật"; break;
        }
        binding.tvTodayScheduleTitle.setText("Hôm nay · " + dayOfWeekStr + " " + today.getDayOfMonth() + "/" + today.getMonthValue());

        binding.layoutPendingList.removeAllViews();
        binding.tvPendingNoticeTitle.setText(pendingList.size() + " yêu cầu đang chờ duyệt");

        for (StaffAppointmentItem item : pendingList) {
            ItemStaffPendingCardBinding cardBinding = ItemStaffPendingCardBinding.inflate(getLayoutInflater());
            cardBinding.ivStaffPetAvatar.setImageResource(item.getPetImgRes());
            cardBinding.tvStaffServiceTitle.setText(item.getService());
            cardBinding.tvStaffCustomerPet.setText(item.getCustomer() + " · " + item.getPet());
            cardBinding.tvStaffDate.setText(item.getDate());
            cardBinding.tvStaffTime.setText(item.getTime());
            cardBinding.tvStaffDuration.setText(item.getDuration());

            UiMotionUtils.attachSpringTouch(cardBinding.getRoot());
            cardBinding.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(this, StaffAppointmentDetailsActivity.class);
                intent.putExtra(StaffAppointmentDetailsActivity.EXTRA_REF, item.getRef());
                intent.putExtra(StaffAppointmentDetailsActivity.EXTRA_APPT, item);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_scale_out);
            });

            binding.layoutPendingList.addView(cardBinding.getRoot());
        }
        UiMotionUtils.animateStaggeredChildren(binding.layoutPendingList, 50L);

        binding.layoutTodaySchedule.removeAllViews();
        for (StaffAppointmentItem item : todayScheduleList) {
            ItemStaffScheduleRowBinding rowBinding = ItemStaffScheduleRowBinding.inflate(getLayoutInflater());
            rowBinding.tvScheduleTime.setText(item.getTime());
            rowBinding.tvSchedulePetService.setText(item.getPet() + " · " + item.getService());
            rowBinding.tvScheduleCustomer.setText(item.getCustomer());

            UiMotionUtils.attachSpringTouch(rowBinding.getRoot());
            rowBinding.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(this, StaffAppointmentDetailsActivity.class);
                intent.putExtra(StaffAppointmentDetailsActivity.EXTRA_REF, item.getRef());
                intent.putExtra(StaffAppointmentDetailsActivity.EXTRA_APPT, item);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_scale_out);
            });

            binding.layoutTodaySchedule.addView(rowBinding.getRoot());
        }
        UiMotionUtils.animateStaggeredChildren(binding.layoutTodaySchedule, 120L);
    }

    private void setupShortcuts() {
        UiMotionUtils.attachSpringTouch(
                binding.shortcutNewSlot,
                binding.shortcutManageSlots,
                binding.shortcutClinicProfile,
                binding.tvSeeAllPending,
                binding.tvSeeAllToday,
                binding.btnEmptyManageSlots
        );

        binding.shortcutNewSlot.setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffSlotManagementActivity.class);
            intent.putExtra(StaffSlotManagementActivity.EXTRA_STATE, "new-slot");
            startActivity(intent);
            overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_scale_out);
        });

        binding.shortcutManageSlots.setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffSlotManagementActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_scale_out);
        });

        binding.shortcutClinicProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_scale_out);
        });

        binding.tvSeeAllPending.setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffAppointmentsActivity.class);
            intent.putExtra(StaffAppointmentsActivity.EXTRA_INITIAL_FILTER, "PENDING");
            startActivity(intent);
            overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_scale_out);
        });

        binding.tvSeeAllToday.setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffAppointmentsActivity.class);
            intent.putExtra(StaffAppointmentsActivity.EXTRA_INITIAL_FILTER, "ALL");
            startActivity(intent);
            overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_scale_out);
        });

        binding.btnEmptyManageSlots.setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffSlotManagementActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_scale_out);
        });
    }

    private void setupBottomNav() {
        UiMotionUtils.attachSpringTouch(
                binding.navStaffToday,
                binding.navStaffAppointments,
                binding.navStaffSlots,
                binding.navStaffProfile
        );

        binding.navStaffToday.setOnClickListener(v -> {
            binding.scrollStaffBody.smoothScrollTo(0, 0);
        });

        binding.navStaffAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffAppointmentsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_scale_out);
        });

        binding.navStaffSlots.setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffSlotManagementActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_scale_out);
        });

        binding.navStaffProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_scale_out);
        });
    }

    private void showStaffAccountDialog() {
        String staffName = !tokenManager.getUserName().isEmpty() ? tokenManager.getUserName() : "Nhân viên phòng khám";
        String email = !tokenManager.getUserEmail().isEmpty() ? tokenManager.getUserEmail() : "Chưa cập nhật";
        String phone = !tokenManager.getUserPhone().isEmpty() ? tokenManager.getUserPhone() : "Chưa cập nhật";

        String clinicName = tokenManager.getClinicName();
        String clinicArea = tokenManager.getClinicArea();
        String clinicDisplay;
        if (!clinicName.isEmpty()) {
            clinicDisplay = !clinicArea.isEmpty() ? clinicName + " (" + clinicArea + ")" : clinicName;
        } else if (tokenManager.getClinicId() != null) {
            clinicDisplay = "Phòng khám thú y";
        } else {
            clinicDisplay = "Chưa gắn phòng khám";
        }

        new AlertDialog.Builder(this)
                .setTitle("Tài khoản Nhân viên")
                .setMessage("Nhân viên: " + staffName
                        + "\nEmail: " + email
                        + "\nSố điện thoại: " + phone
                        + "\nPhòng khám: " + clinicDisplay
                        + "\nVai trò: Nhân viên phòng khám")
                .setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss())
                .setNeutralButton("Chuyển sang Khách hàng", (dialog, which) -> {
                    Intent intent = new Intent(this, com.example.petcareui.MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Đăng xuất", (dialog, which) -> {
                    tokenManager.clear();
                    goToLogin();
                })
                .show();
    }
}
