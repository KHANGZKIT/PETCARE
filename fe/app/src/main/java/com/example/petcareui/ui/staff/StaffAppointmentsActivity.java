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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.petcareui.R;
import com.example.petcareui.data.model.StaffAppointmentItem;
import com.example.petcareui.databinding.ActivityStaffAppointmentsBinding;
import com.example.petcareui.ui.auth.LoginActivity;
import com.example.petcareui.ui.profile.ProfileActivity;
import com.example.petcareui.utils.TokenManager;
import com.example.petcareui.utils.UiMotionUtils;

import java.util.List;

/**
 * Phân hệ Nhân viên S02: Danh sách lịch hẹn phòng khám — Kiến trúc MVVM + RecyclerView.
 */
public class StaffAppointmentsActivity extends AppCompatActivity {

    public static final String EXTRA_INITIAL_FILTER = "extra_filter";
    public static final String EXTRA_STATE = "extra_state";

    private ActivityStaffAppointmentsBinding binding;
    private StaffAppointmentsViewModel viewModel;
    private StaffAppointmentAdapter adapter;
    private TokenManager tokenManager;

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

        viewModel = new ViewModelProvider(this).get(StaffAppointmentsViewModel.class);

        if (savedInstanceState == null && getIntent().hasExtra(EXTRA_INITIAL_FILTER)) {
            viewModel.setInitialFilter(getIntent().getStringExtra(EXTRA_INITIAL_FILTER));
        }

        UiMotionUtils.applyWindowInsets(binding.getRoot(), binding.layoutStaffAppBar, binding.bottomNavStaff);
        bindClinicHeader();
        setupRecyclerView();
        setupAppBar();
        setupSearchAndFilterChips();
        setupBottomNav();
        setupEmptyAndErrorActions();
        observeViewModel();

        UiMotionUtils.animateEntranceSections(
                binding.layoutStaffAppBar,
                binding.layoutSearchAndFilter
        );
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
        viewModel.loadClinicAppointments();
    }

    private void setupRecyclerView() {
        adapter = new StaffAppointmentAdapter(this::openStaffAppointmentDetail);
        binding.rvApptList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvApptList.setAdapter(adapter);
    }

    private void openStaffAppointmentDetail(StaffAppointmentItem item) {
        Intent intent = new Intent(this, StaffAppointmentDetailsActivity.class);
        intent.putExtra(StaffAppointmentDetailsActivity.EXTRA_REF, item.getRef());
        intent.putExtra(StaffAppointmentDetailsActivity.EXTRA_APPT, item);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_scale_out);
    }

    private void observeViewModel() {
        viewModel.getFilterCounts().observe(this, counts -> {
            if (counts == null) return;
            binding.chipFilterAll.setText("Tất cả · " + counts.all);
            binding.chipFilterPending.setText("Chờ xác nhận · " + counts.pending);
            binding.chipFilterConfirmed.setText("Đã xác nhận · " + counts.confirmed);
            binding.chipFilterCompleted.setText("Đã hoàn thành · " + counts.completed);
            binding.chipFilterNoShow.setText("Không đến · " + counts.noShow);
        });

        viewModel.getCurrentFilter().observe(this, this::updateFilterChipStyles);

        viewModel.getDisplayedAppointments().observe(this, list -> {
            adapter.submitList(list);
            int size = list != null ? list.size() : 0;
            int pendingInList = 0;
            if (list != null) {
                for (StaffAppointmentItem item : list) {
                    if ("PENDING".equalsIgnoreCase(item.getStatus())) pendingInList++;
                }
            }
            if (pendingInList > 0) {
                binding.tvSectionHeader.setText("Danh sách · " + pendingInList + " yêu cầu chờ duyệt");
            } else {
                binding.tvSectionHeader.setText("Danh sách · " + size + " lịch hẹn");
            }
        });

        viewModel.getUiState().observe(this, this::applyState);

        viewModel.getUnauthorizedEvent().observe(this, unauth -> {
            if (Boolean.TRUE.equals(unauth)) {
                handleUnauthorized();
            }
        });
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

    private void setupAppBar() {
        UiMotionUtils.attachSpringTouch(binding.btnBack, binding.btnFilterSliders, binding.btnRetry, binding.btnEmptyShowAll);
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnRetry.setOnClickListener(v -> viewModel.loadClinicAppointments());
        binding.btnEmptyShowAll.setOnClickListener(v -> {
            binding.etStaffSearch.setText("");
            viewModel.resetSearchAndFilter();
        });
    }

    private void applyState(String state) {
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
            viewModel.setFilter("ALL");
        });
        binding.chipFilterPending.setOnClickListener(v -> {
            UiMotionUtils.animateSelectionPop(v);
            viewModel.setFilter("PENDING");
        });
        binding.chipFilterConfirmed.setOnClickListener(v -> {
            UiMotionUtils.animateSelectionPop(v);
            viewModel.setFilter("CONFIRMED");
        });
        binding.chipFilterCompleted.setOnClickListener(v -> {
            UiMotionUtils.animateSelectionPop(v);
            viewModel.setFilter("COMPLETED");
        });
        binding.chipFilterNoShow.setOnClickListener(v -> {
            UiMotionUtils.animateSelectionPop(v);
            viewModel.setFilter("NO_SHOW");
        });

        binding.etStaffSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s != null ? s.toString() : "");
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.btnFilterSliders.setOnClickListener(v ->
                Toast.makeText(this, "Bộ lọc nâng cao theo ngày & bác sĩ", Toast.LENGTH_SHORT).show());
    }

    private void updateFilterChipStyles(String activeFilter) {
        updateFilterChip(binding.chipFilterAll, "ALL".equalsIgnoreCase(activeFilter));
        updateFilterChip(binding.chipFilterPending, "PENDING".equalsIgnoreCase(activeFilter));
        updateFilterChip(binding.chipFilterConfirmed, "CONFIRMED".equalsIgnoreCase(activeFilter));
        updateFilterChip(binding.chipFilterCompleted, "COMPLETED".equalsIgnoreCase(activeFilter));
        updateFilterChip(binding.chipFilterNoShow, "NO_SHOW".equalsIgnoreCase(activeFilter));
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

    private void updateEmptyStateCopy() {
        if (viewModel.isAllListEmpty()) {
            binding.tvEmptyTitle.setText("Phòng khám chưa có lịch hẹn");
            binding.tvEmptySubtitle.setText("Lịch hẹn mới từ khách hàng sẽ hiển thị tại đây ngay khi được đặt.");
            binding.btnEmptyShowAll.setVisibility(View.GONE);
        } else {
            binding.tvEmptyTitle.setText("Không có lịch hẹn phù hợp");
            binding.tvEmptySubtitle.setText("Thử đổi bộ lọc trạng thái hoặc xóa tìm kiếm để xem tất cả lịch hẹn.");
            binding.btnEmptyShowAll.setVisibility(View.VISIBLE);
        }
    }

    private void setupEmptyAndErrorActions() {
        binding.btnEmptyShowAll.setOnClickListener(v -> {
            binding.etStaffSearch.setText("");
            viewModel.resetSearchAndFilter();
        });

        binding.btnRetry.setOnClickListener(v -> {
            Toast.makeText(this, "Đang kết nối lại...", Toast.LENGTH_SHORT).show();
            viewModel.loadClinicAppointments();
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

        binding.navStaffAppointments.setOnClickListener(v ->
                binding.scrollStaffAppointmentsBody.smoothScrollTo(0, 0));

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
