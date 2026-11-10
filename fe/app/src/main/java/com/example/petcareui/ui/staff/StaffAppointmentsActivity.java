package com.example.petcareui.ui.staff;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.petcareui.R;
import com.example.petcareui.data.api.ApiClient;
import com.example.petcareui.data.model.AppointmentResponse;
import com.example.petcareui.data.model.StaffAppointmentItem;
import com.example.petcareui.databinding.ActivityStaffAppointmentsBinding;
import com.example.petcareui.databinding.ItemStaffApptCardBinding;
import com.example.petcareui.ui.auth.LoginActivity;
import com.example.petcareui.utils.TokenManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Màn hình S02: Danh sách lịch hẹn cho nhân viên (Staff Appointments List).
 * Thuộc phân hệ com.example.petcareui.ui.staff,
 * bám sát thiết kế nguyên mẫu React S02AppointmentList và kết nối API /api/v1/staff/appointments.
 * Hỗ trợ 4 trạng thái: Mặc định, Đang tải (Loading), Không khớp (Empty), Lỗi (Error).
 */
public class StaffAppointmentsActivity extends AppCompatActivity {

    public static final String EXTRA_INITIAL_FILTER = "extra_filter"; // "ALL", "PENDING", "CONFIRMED", etc.
    public static final String EXTRA_STATE = "extra_state"; // "default", "loading", "empty", "error"

    private ActivityStaffAppointmentsBinding binding;
    private TokenManager tokenManager;
    private final List<StaffAppointmentItem> allStaffAppts = new ArrayList<>();
    private final List<StaffAppointmentItem> displayedList = new ArrayList<>();

    private String currentFilter = "PENDING"; // Mặc định trong screenshot S02 là "Chờ xác nhận · 2"
    private String currentState = "default";
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStaffAppointmentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);
        if (!tokenManager.isLoggedIn()) {
            goToLogin();
            return;
        }

        extractIntentData();
        bindClinicHeader();
        setupAppBar();
        setupStateToggles();
        setupSearchAndFilterChips();
        setupBottomNav();
        setupEmptyAndErrorActions();

        StaffClinicHelper.syncStaffAndClinicInfo(this, this::bindClinicHeader, this::handleUnauthorized);
        loadAppointmentsFromApi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!tokenManager.isLoggedIn()) {
            goToLogin();
            return;
        }
        bindClinicHeader();
        StaffClinicHelper.syncStaffAndClinicInfo(this, this::bindClinicHeader, this::handleUnauthorized);
        loadAppointmentsFromApi();
    }

    private void extractIntentData() {
        if (getIntent().hasExtra(EXTRA_STATE)) {
            currentState = getIntent().getStringExtra(EXTRA_STATE);
        }
        if (getIntent().hasExtra(EXTRA_INITIAL_FILTER)) {
            currentFilter = getIntent().getStringExtra(EXTRA_INITIAL_FILTER);
        }
    }

    private void bindClinicHeader() {
        binding.tvStaffClinicSubtitle.setText(StaffClinicHelper.buildClinicSubtitleWithArea(tokenManager));
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

    private void loadAppointmentsFromApi() {
        ApiClient.getAppointmentApi().getClinicAppointments(null).enqueue(new Callback<List<AppointmentResponse>>() {
            @Override
            public void onResponse(Call<List<AppointmentResponse>> call, Response<List<AppointmentResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allStaffAppts.clear();
                    for (AppointmentResponse res : response.body()) {
                        allStaffAppts.add(res.toStaffAppointmentItem());
                    }
                    updateFilterCounts();
                    applyState("default");
                } else if (response.code() == 401 || response.code() == 403) {
                    handleUnauthorized();
                } else {
                    allStaffAppts.clear();
                    updateFilterCounts();
                    applyState("error");
                }
            }

            @Override
            public void onFailure(Call<List<AppointmentResponse>> call, Throwable t) {
                allStaffAppts.clear();
                updateFilterCounts();
                applyState("error");
            }
        });
    }

    private void setupAppBar() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupStateToggles() {
        binding.chipStateDefault.setOnClickListener(v -> applyState("default"));
        binding.chipStateLoading.setOnClickListener(v -> applyState("loading"));
        binding.chipStateEmpty.setOnClickListener(v -> applyState("empty"));
        binding.chipStateError.setOnClickListener(v -> applyState("error"));
    }

    private void applyState(String state) {
        currentState = state;

        updateStateChip(binding.chipStateDefault, "default".equals(state));
        updateStateChip(binding.chipStateLoading, "loading".equals(state));
        updateStateChip(binding.chipStateEmpty, "empty".equals(state));
        updateStateChip(binding.chipStateError, "error".equals(state));

        if ("loading".equals(state)) {
            binding.layoutDefaultContent.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.layoutErrorState.setVisibility(View.GONE);
            binding.layoutLoadingState.setVisibility(View.VISIBLE);
        } else if ("empty".equals(state)) {
            binding.layoutDefaultContent.setVisibility(View.GONE);
            binding.layoutLoadingState.setVisibility(View.GONE);
            binding.layoutErrorState.setVisibility(View.GONE);
            updateEmptyStateCopy();
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
        } else if ("error".equals(state)) {
            binding.layoutDefaultContent.setVisibility(View.GONE);
            binding.layoutLoadingState.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.layoutErrorState.setVisibility(View.VISIBLE);
        } else {
            binding.layoutLoadingState.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.layoutErrorState.setVisibility(View.GONE);
            binding.layoutDefaultContent.setVisibility(View.VISIBLE);
            filterAndRenderList();
        }
    }

    private void updateStateChip(TextView chip, boolean active) {
        if (active) {
            chip.setBackgroundResource(R.drawable.bg_chip_active);
            chip.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_inactive);
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
        }
    }

    private void setupSearchAndFilterChips() {
        // Bắt sự kiện chọn chip
        binding.chipFilterAll.setOnClickListener(v -> setFilter("ALL"));
        binding.chipFilterPending.setOnClickListener(v -> setFilter("PENDING"));
        binding.chipFilterConfirmed.setOnClickListener(v -> setFilter("CONFIRMED"));
        binding.chipFilterCompleted.setOnClickListener(v -> setFilter("COMPLETED"));
        binding.chipFilterNoShow.setOnClickListener(v -> setFilter("NO_SHOW"));

        // Lắng nghe tìm kiếm (chỉ đăng ký 1 lần trong onCreate)
        binding.etStaffSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim().toLowerCase();
                if ("default".equals(currentState)) {
                    filterAndRenderList();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnFilterSliders.setOnClickListener(v -> {
            Toast.makeText(this, "Bộ lọc nâng cao theo ngày & bác sĩ", Toast.LENGTH_SHORT).show();
        });

        updateFilterCounts();
    }

    private void updateFilterCounts() {
        int pendingCount = 0;
        int confirmedCount = 0;
        int completedCount = 0;
        int noShowCount = 0;

        for (StaffAppointmentItem item : allStaffAppts) {
            if ("PENDING".equalsIgnoreCase(item.getStatus())) pendingCount++;
            else if ("CONFIRMED".equalsIgnoreCase(item.getStatus())) confirmedCount++;
            else if ("COMPLETED".equalsIgnoreCase(item.getStatus())) completedCount++;
            else if ("NO_SHOW".equalsIgnoreCase(item.getStatus())) noShowCount++;
        }

        binding.chipFilterAll.setText("Tất cả · " + allStaffAppts.size());
        binding.chipFilterPending.setText("Chờ xác nhận · " + pendingCount);
        binding.chipFilterConfirmed.setText("Đã xác nhận · " + confirmedCount);
        binding.chipFilterCompleted.setText("Đã hoàn thành · " + completedCount);
        binding.chipFilterNoShow.setText("Không đến · " + noShowCount);

        updateFilterChipStyles();
    }

    private void setFilter(String filter) {
        currentFilter = filter;
        updateFilterChipStyles();
        if ("default".equals(currentState)) {
            filterAndRenderList();
        }
    }

    private void updateFilterChipStyles() {
        updateFilterChip(binding.chipFilterAll, "ALL".equalsIgnoreCase(currentFilter));
        updateFilterChip(binding.chipFilterPending, "PENDING".equalsIgnoreCase(currentFilter));
        updateFilterChip(binding.chipFilterConfirmed, "CONFIRMED".equalsIgnoreCase(currentFilter));
        updateFilterChip(binding.chipFilterCompleted, "COMPLETED".equalsIgnoreCase(currentFilter));
        updateFilterChip(binding.chipFilterNoShow, "NO_SHOW".equalsIgnoreCase(currentFilter));
    }

    private void updateFilterChip(TextView chip, boolean active) {
        if (active) {
            chip.setBackgroundResource(R.drawable.bg_chip_active);
            chip.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_inactive);
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
        }
    }

    private void filterAndRenderList() {
        displayedList.clear();

        for (StaffAppointmentItem item : allStaffAppts) {
            boolean matchesFilter = "ALL".equalsIgnoreCase(currentFilter) || currentFilter.equalsIgnoreCase(item.getStatus());
            boolean matchesSearch = searchQuery.isEmpty()
                    || item.getPet().toLowerCase().contains(searchQuery)
                    || item.getCustomer().toLowerCase().contains(searchQuery)
                    || item.getRef().toLowerCase().contains(searchQuery)
                    || item.getService().toLowerCase().contains(searchQuery);

            if (matchesFilter && matchesSearch) {
                displayedList.add(item);
            }
        }

        // Đếm số lượng chờ duyệt hiện tại
        int pendingInList = 0;
        for (StaffAppointmentItem item : displayedList) {
            if ("PENDING".equalsIgnoreCase(item.getStatus())) pendingInList++;
        }

        if (displayedList.isEmpty()) {
            binding.layoutDefaultContent.setVisibility(View.GONE);
            updateEmptyStateCopy();
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.layoutDefaultContent.setVisibility(View.VISIBLE);

            if (pendingInList > 0) {
                binding.tvSectionHeader.setText("Danh sách · " + pendingInList + " yêu cầu chờ duyệt");
            } else {
                binding.tvSectionHeader.setText("Danh sách · " + displayedList.size() + " lịch hẹn");
            }

            renderAppointmentCards();
        }
    }

    private void updateEmptyStateCopy() {
        if (allStaffAppts.isEmpty()) {
            binding.tvEmptyTitle.setText("Phòng khám chưa có lịch hẹn");
            binding.tvEmptySubtitle.setText("Lịch hẹn mới từ khách hàng sẽ hiển thị tại đây ngay khi được đặt.");
            binding.btnEmptyShowAll.setVisibility(View.GONE);
        } else {
            binding.tvEmptyTitle.setText("Không có lịch hẹn phù hợp");
            binding.tvEmptySubtitle.setText("Thử đổi bộ lọc trạng thái hoặc xóa tìm kiếm để xem tất cả lịch hẹn.");
            binding.btnEmptyShowAll.setVisibility(View.VISIBLE);
        }
    }

    private void renderAppointmentCards() {
        binding.layoutApptList.removeAllViews();

        for (StaffAppointmentItem item : displayedList) {
            ItemStaffApptCardBinding cardBinding = ItemStaffApptCardBinding.inflate(getLayoutInflater());

            // 1. Dữ liệu cơ bản
            cardBinding.tvStaffApptRef.setText(item.getRef());
            cardBinding.ivStaffPetAvatar.setImageResource(item.getPetImgRes());
            cardBinding.tvStaffServiceTitle.setText(item.getService());
            cardBinding.tvStaffCustomerPet.setText(item.getCustomer() + " · " + item.getPet());
            cardBinding.tvStaffDate.setText(item.getDate());
            cardBinding.tvStaffTime.setText(item.getTime());
            cardBinding.tvStaffDuration.setText(item.getDuration());

            // 2. Định dạng theo trạng thái & thanh ưu tiên cam
            boolean isPending = "PENDING".equalsIgnoreCase(item.getStatus());
            if (isPending) {
                cardBinding.viewPriorityBar.setVisibility(View.VISIBLE);
                cardBinding.cardContainer.setBackgroundResource(R.drawable.bg_card_priority);
                cardBinding.tvStaffApptStatus.setText("• Chờ xác nhận");
                cardBinding.tvStaffApptStatus.setBackgroundResource(R.drawable.bg_badge_pending);
                cardBinding.tvStaffApptStatus.setTextColor(ContextCompat.getColor(this, R.color.status_warning_ink));
            } else if ("CONFIRMED".equalsIgnoreCase(item.getStatus())) {
                cardBinding.viewPriorityBar.setVisibility(View.GONE);
                cardBinding.cardContainer.setBackgroundResource(R.drawable.bg_card_clinic);
                cardBinding.tvStaffApptStatus.setText("• Đã xác nhận");
                cardBinding.tvStaffApptStatus.setBackgroundResource(R.drawable.bg_badge_confirmed);
                cardBinding.tvStaffApptStatus.setTextColor(ContextCompat.getColor(this, R.color.status_success_ink));
            } else if ("COMPLETED".equalsIgnoreCase(item.getStatus())) {
                cardBinding.viewPriorityBar.setVisibility(View.GONE);
                cardBinding.cardContainer.setBackgroundResource(R.drawable.bg_card_clinic);
                cardBinding.tvStaffApptStatus.setText("• Đã hoàn thành");
                cardBinding.tvStaffApptStatus.setBackgroundResource(R.drawable.bg_badge_completed);
                cardBinding.tvStaffApptStatus.setTextColor(ContextCompat.getColor(this, R.color.brand_ink));
            } else if ("NO_SHOW".equalsIgnoreCase(item.getStatus())) {
                cardBinding.viewPriorityBar.setVisibility(View.GONE);
                cardBinding.cardContainer.setBackgroundResource(R.drawable.bg_card_clinic);
                cardBinding.tvStaffApptStatus.setText("• Không đến");
                cardBinding.tvStaffApptStatus.setBackgroundResource(R.drawable.bg_badge_no_show);
                cardBinding.tvStaffApptStatus.setTextColor(ContextCompat.getColor(this, R.color.status_info_ink));
            } else {
                cardBinding.viewPriorityBar.setVisibility(View.GONE);
                cardBinding.cardContainer.setBackgroundResource(R.drawable.bg_card_clinic);
                cardBinding.tvStaffApptStatus.setText("• Đã hủy / Từ chối");
                cardBinding.tvStaffApptStatus.setBackgroundResource(R.drawable.bg_badge_cancelled);
                cardBinding.tvStaffApptStatus.setTextColor(ContextCompat.getColor(this, R.color.status_destructive_ink));
            }

            // 3. Click xem chi tiết (S03)
            cardBinding.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(this, StaffAppointmentDetailsActivity.class);
                intent.putExtra(StaffAppointmentDetailsActivity.EXTRA_REF, item.getRef());
                intent.putExtra(StaffAppointmentDetailsActivity.EXTRA_APPT, item);
                startActivity(intent);
            });

            binding.layoutApptList.addView(cardBinding.getRoot());
        }

        binding.btnLoadOlder.setOnClickListener(v -> {
            Toast.makeText(this, "Đang tải thêm lịch hẹn của các ngày trước...", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupEmptyAndErrorActions() {
        // Nút "Xem tất cả trạng thái" khi không có kết quả tìm kiếm
        binding.btnEmptyShowAll.setOnClickListener(v -> {
            binding.etStaffSearch.setText("");
            setFilter("ALL");
            applyState("default");
        });

        // Nút "Thử lại" khi gặp lỗi kết nối
        binding.btnRetry.setOnClickListener(v -> {
            Toast.makeText(this, "Đang kết nối lại...", Toast.LENGTH_SHORT).show();
            loadAppointmentsFromApi();
        });
    }

    private void setupBottomNav() {
        // Tab 1: Hôm nay -> Mở S01
        binding.navStaffToday.setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Tab 2: Lịch hẹn -> Đang ở màn này, cuộn lên đầu trang
        binding.navStaffAppointments.setOnClickListener(v -> {
            binding.scrollStaffAppointmentsBody.smoothScrollTo(0, 0);
        });

        // Tab 3: Khung giờ -> Mở S04
        binding.navStaffSlots.setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffSlotManagementActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
