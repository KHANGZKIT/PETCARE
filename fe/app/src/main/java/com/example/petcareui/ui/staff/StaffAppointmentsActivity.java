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
import com.example.petcareui.data.dto.response.AppointmentResponse;
import com.example.petcareui.data.model.StaffAppointmentItem;
import com.example.petcareui.databinding.ActivityStaffAppointmentsBinding;
import com.example.petcareui.databinding.ItemStaffApptCardBinding;
import com.example.petcareui.ui.auth.LoginActivity;
import com.example.petcareui.ui.profile.ProfileActivity;
import com.example.petcareui.utils.TokenManager;
import com.example.petcareui.utils.UiMotionUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffAppointmentsActivity extends AppCompatActivity {

    public static final String EXTRA_INITIAL_FILTER = "extra_filter";
    public static final String EXTRA_STATE = "extra_state";

    private ActivityStaffAppointmentsBinding binding;
    private TokenManager tokenManager;
    private final List<StaffAppointmentItem> allStaffAppts = new ArrayList<>();
    private final List<StaffAppointmentItem> displayedList = new ArrayList<>();

    private String currentFilter = "PENDING";
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
        UiMotionUtils.applyWindowInsets(binding.getRoot(), binding.layoutStaffAppBar, binding.bottomNavStaff);
        bindClinicHeader();
        setupAppBar();
        setupStateToggles();
        setupSearchAndFilterChips();
        setupBottomNav();
        setupEmptyAndErrorActions();

        UiMotionUtils.animateEntranceSections(
                binding.layoutStaffAppBar,
                binding.layoutSearchAndFilter
        );

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
        applyState("loading");
        ApiClient.getAppointmentApi().getClinicAppointments(null).enqueue(new Callback<List<AppointmentResponse>>() {
            @Override
            public void onResponse(Call<List<AppointmentResponse>> call, Response<List<AppointmentResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allStaffAppts.clear();
                    for (AppointmentResponse res : response.body()) {
                        allStaffAppts.add(res.toStaffAppointmentItem());
                    }
                    updateFilterCounts();
                    setupSearchAndFilterChips();
                    if (allStaffAppts.isEmpty()) {
                        applyState("empty");
                    } else {
                        applyState("default");
                    }
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
        UiMotionUtils.attachSpringTouch(binding.btnBack, binding.btnFilterSliders, binding.btnRetry, binding.btnEmptyShowAll);
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnRetry.setOnClickListener(v -> loadAppointmentsFromApi());
        binding.btnEmptyShowAll.setOnClickListener(v -> {
            binding.etStaffSearch.setText("");
            setFilter("ALL");
            applyState("default");
        });
    }

    private void setupStateToggles() {
        if (binding.chipStateDefault.getParent() instanceof View) {
            ((View) binding.chipStateDefault.getParent()).setVisibility(View.GONE);
        }
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
            chip.setTextColor(ContextCompat.getColor(this, R.color.ds_primary));
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_inactive);
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
        }
    }

    private void setupSearchAndFilterChips() {
        UiMotionUtils.attachSpringTouch(
                binding.chipFilterAll,
                binding.chipFilterPending,
                binding.chipFilterConfirmed,
                binding.chipFilterCompleted,
                binding.chipFilterNoShow
        );

        binding.chipFilterAll.setOnClickListener(v -> {
            UiMotionUtils.animateSelectionPop(v);
            setFilter("ALL");
        });
        binding.chipFilterPending.setOnClickListener(v -> {
            UiMotionUtils.animateSelectionPop(v);
            setFilter("PENDING");
        });
        binding.chipFilterConfirmed.setOnClickListener(v -> {
            UiMotionUtils.animateSelectionPop(v);
            setFilter("CONFIRMED");
        });
        binding.chipFilterCompleted.setOnClickListener(v -> {
            UiMotionUtils.animateSelectionPop(v);
            setFilter("COMPLETED");
        });
        binding.chipFilterNoShow.setOnClickListener(v -> {
            UiMotionUtils.animateSelectionPop(v);
            setFilter("NO_SHOW");
        });

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
            chip.setTextColor(ContextCompat.getColor(this, R.color.ds_primary));
            UiMotionUtils.applyFont(chip, R.font.open_sans_semibold);
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_inactive);
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
            UiMotionUtils.applyFont(chip, R.font.open_sans_regular);
        }
    }

    private void filterAndRenderList() {
        displayedList.clear();

        for (StaffAppointmentItem item : allStaffAppts) {
            boolean matchesFilter = "ALL".equalsIgnoreCase(currentFilter) || currentFilter.equalsIgnoreCase(item.getStatus());
            boolean matchesSearch = searchQuery.isEmpty()
                    || item.getPet().toLowerCase().contains(searchQuery)
                    || item.getCustomer().toLowerCase().contains(searchQuery)
                    || item.getService().toLowerCase().contains(searchQuery);

            if (matchesFilter && matchesSearch) {
                displayedList.add(item);
            }
        }

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

            cardBinding.ivStaffPetAvatar.setImageResource(item.getPetImgRes());
            cardBinding.tvStaffServiceTitle.setText(item.getService());
            cardBinding.tvStaffCustomerPet.setText(item.getCustomer() + " · " + item.getPet());
            cardBinding.tvStaffDate.setText(StaffClinicHelper.formatNaturalDateTime(item.getDate(), item.getTime()));
            cardBinding.tvStaffTime.setText(item.getTime());
            cardBinding.tvStaffDuration.setText("· " + item.getDuration());

            cardBinding.cardContainer.setBackgroundResource(R.drawable.bg_home_card_borderless);
            cardBinding.tvStaffApptStatus.setBackground(null);
            if ("PENDING".equalsIgnoreCase(item.getStatus())) {
                cardBinding.tvStaffApptStatus.setText("● Chờ xác nhận");
                cardBinding.tvStaffApptStatus.setTextColor(ContextCompat.getColor(this, R.color.ds_warning));
            } else if ("CONFIRMED".equalsIgnoreCase(item.getStatus())) {
                cardBinding.tvStaffApptStatus.setText("● Đã xác nhận");
                cardBinding.tvStaffApptStatus.setTextColor(ContextCompat.getColor(this, R.color.ds_success));
            } else if ("COMPLETED".equalsIgnoreCase(item.getStatus())) {
                cardBinding.tvStaffApptStatus.setText("● Đã hoàn thành");
                cardBinding.tvStaffApptStatus.setTextColor(ContextCompat.getColor(this, R.color.ds_primary));
            } else if ("NO_SHOW".equalsIgnoreCase(item.getStatus())) {
                cardBinding.tvStaffApptStatus.setText("● Không đến");
                cardBinding.tvStaffApptStatus.setTextColor(ContextCompat.getColor(this, R.color.ds_text_secondary));
            } else {
                cardBinding.tvStaffApptStatus.setText("● Đã hủy / Từ chối");
                cardBinding.tvStaffApptStatus.setTextColor(ContextCompat.getColor(this, R.color.ds_error));
            }

            UiMotionUtils.attachSpringTouch(cardBinding.getRoot());
            cardBinding.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(this, StaffAppointmentDetailsActivity.class);
                intent.putExtra(StaffAppointmentDetailsActivity.EXTRA_REF, item.getRef());
                intent.putExtra(StaffAppointmentDetailsActivity.EXTRA_APPT, item);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_scale_out);
            });

            binding.layoutApptList.addView(cardBinding.getRoot());
        }
        UiMotionUtils.animateStaggeredChildren(binding.layoutApptList, 40L);

        UiMotionUtils.attachSpringTouch(binding.btnLoadOlder);
        binding.btnLoadOlder.setOnClickListener(v -> {
            Toast.makeText(this, "Đang tải thêm lịch hẹn của các ngày trước...", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupEmptyAndErrorActions() {
        binding.btnEmptyShowAll.setOnClickListener(v -> {
            binding.etStaffSearch.setText("");
            setFilter("ALL");
            applyState("default");
        });

        binding.btnRetry.setOnClickListener(v -> {
            Toast.makeText(this, "Đang kết nối lại...", Toast.LENGTH_SHORT).show();
            loadAppointmentsFromApi();
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
            Intent intent = new Intent(this, StaffDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_scale_out);
            finish();
        });

        binding.navStaffAppointments.setOnClickListener(v -> {
            binding.scrollStaffAppointmentsBody.smoothScrollTo(0, 0);
        });

        binding.navStaffSlots.setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffSlotManagementActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_scale_out);
            finish();
        });

        binding.navStaffProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_scale_out);
        });
    }
}
