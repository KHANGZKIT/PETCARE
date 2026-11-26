package com.example.petcareui.ui.staff;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.petcareui.R;
import com.example.petcareui.data.model.StaffSlotItem;
import com.example.petcareui.databinding.ActivityStaffSlotManagementBinding;
import com.example.petcareui.ui.auth.LoginActivity;
import com.example.petcareui.ui.profile.ProfileActivity;
import com.example.petcareui.utils.TokenManager;
import com.example.petcareui.utils.UiMotionUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Phân hệ Nhân viên S04: Quản lý khung giờ phòng khám — Kiến trúc MVVM + RecyclerView.
 */
public class StaffSlotManagementActivity extends AppCompatActivity {

    public static final String EXTRA_STATE = "extra_state";

    private ActivityStaffSlotManagementBinding binding;
    private StaffSlotViewModel viewModel;
    private StaffSlotAdapter slotAdapter;
    private TokenManager tokenManager;

    private static class StaffDateItem {
        String day;
        String num;
        int openCount;
        boolean selected;
        String isoDate;

        StaffDateItem(String day, String num, int openCount, boolean selected, String isoDate) {
            this.day = day;
            this.num = num;
            this.openCount = openCount;
            this.selected = selected;
            this.isoDate = isoDate;
        }
    }

    private final List<StaffDateItem> dateList = new ArrayList<>();
    private String currentState = "default";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStaffSlotManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);
        if (!tokenManager.isLoggedIn()) {
            goToLogin();
            return;
        }

        viewModel = new ViewModelProvider(this).get(StaffSlotViewModel.class);

        extractIntentData();
        UiMotionUtils.applyWindowInsets(binding.getRoot(), binding.layoutAppBar, binding.bottomNavStaff);
        bindClinicHeader();
        setupRecyclerView();
        setupDates();
        setupAppBar();
        setupSheetInteractions();
        setupBottomNav();
        observeViewModel();

        UiMotionUtils.animateEntranceSections(
                binding.layoutAppBar,
                binding.layoutDateStripContainer,
                binding.noticeInfo
        );

        applyState(currentState);
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
        loadSlotsForSelectedDate();
    }

    private void setupRecyclerView() {
        slotAdapter = new StaffSlotAdapter((slot, actionView) -> {
            actionView.setEnabled(false);
            actionView.setAlpha(0.5f);
            viewModel.toggleSlotStatus(slot, () -> {
                if (!isFinishing() && !isDestroyed()) {
                    actionView.setEnabled(true);
                    actionView.setAlpha(1.0f);
                }
            });
        });
        binding.rvSlotsList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSlotsList.setAdapter(slotAdapter);
    }

    private void observeViewModel() {
        viewModel.getOpenSlotCount().observe(this, openCount -> {
            StaffDateItem selectedDate = getSelectedDateItem();
            if (selectedDate != null) {
                selectedDate.openCount = openCount != null ? openCount : 0;
                renderDateStrip();
            }
        });

        viewModel.getSlots().observe(this, slots -> {
            int count = slots != null ? slots.size() : 0;
            binding.tvSlotCountHeader.setText(count + " khung");
            slotAdapter.submitList(slots);

            if (count == 0) {
                if ("new-slot".equals(currentState)) {
                    binding.noticeCreated.setVisibility(View.GONE);
                    binding.noticeInfo.setVisibility(View.VISIBLE);
                    binding.layoutFloatingToast.setVisibility(View.GONE);
                    binding.rvSlotsList.setVisibility(View.GONE);
                    binding.layoutEmptySlots.setVisibility(View.VISIBLE);
                } else {
                    applyState("empty");
                }
            } else if (!"new-slot".equals(currentState) && !"created".equals(currentState)) {
                applyState("default");
            } else {
                binding.layoutEmptySlots.setVisibility(View.GONE);
                binding.rvSlotsList.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getSlotCreatedEvent().observe(this, created -> {
            if (created == null) return;
            binding.tvFloatingToastText.setText("Đã tạo khung giờ · " + created.formattedStart + " – " + created.formattedEnd);
            binding.tvNoticeCreatedBody.setText(created.formattedStart + " – " + created.formattedEnd + " đã mở để nhận đặt lịch.");
            applyState("created");
            viewModel.clearSlotCreatedEvent();
        });

        viewModel.getToastMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                viewModel.clearToast();
            }
        });

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

    private void extractIntentData() {
        if (getIntent().hasExtra(EXTRA_STATE)) {
            currentState = getIntent().getStringExtra(EXTRA_STATE);
        }
    }

    private void setupDates() {
        dateList.clear();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate d = today.plusDays(i);
            String dayLabel = getDayOfWeekLabel(d.getDayOfWeek());
            String num = String.valueOf(d.getDayOfMonth());
            dateList.add(new StaffDateItem(dayLabel, num, 0, i == 0, d.toString()));
        }
        renderDateStrip();
        updateSelectedDateNotices();
    }

    private void updateSelectedDateNotices() {
        StaffDateItem selectedDate = getSelectedDateItem();
        LocalDate parsedDate = selectedDate != null ? LocalDate.parse(selectedDate.isoDate) : LocalDate.now();
        String formattedDate = StaffClinicHelper.formatVietnameseFullDate(parsedDate);
        binding.tvNoticeDateTitle.setText(formattedDate);
        binding.tvSheetCreateSlotSubtitle.setText("Thêm khung giờ trống cho " + formattedDate + ". Khách có thể đặt ngay khi bạn lưu.");
    }

    private String getDayOfWeekLabel(DayOfWeek dow) {
        switch (dow) {
            case MONDAY: return "T2";
            case TUESDAY: return "T3";
            case WEDNESDAY: return "T4";
            case THURSDAY: return "T5";
            case FRIDAY: return "T6";
            case SATURDAY: return "T7";
            case SUNDAY: return "CN";
            default: return "";
        }
    }

    private StaffDateItem getSelectedDateItem() {
        for (StaffDateItem item : dateList) {
            if (item.selected) return item;
        }
        return dateList.isEmpty() ? null : dateList.get(0);
    }

    private void loadSlotsForSelectedDate() {
        StaffDateItem selectedDate = getSelectedDateItem();
        String isoDate = selectedDate != null ? selectedDate.isoDate : LocalDate.now().toString();
        updateSelectedDateNotices();
        viewModel.loadSlots(isoDate);
    }

    private void setupAppBar() {
        UiMotionUtils.attachSpringTouch(
                binding.btnBack,
                binding.btnTopAddSlot,
                binding.btnCreateNewSlot,
                binding.btnEmptyCreateSlot,
                binding.btnStartTimePicker,
                binding.btnEndTimePicker,
                binding.btnSheetCreateSlotAction
        );
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnTopAddSlot.setOnClickListener(v -> applyState("new-slot"));
        binding.btnCreateNewSlot.setOnClickListener(v -> applyState("new-slot"));
        binding.btnEmptyCreateSlot.setOnClickListener(v -> applyState("new-slot"));
    }

    private void applyState(String state) {
        currentState = state;

        binding.layoutNewSlotSheetOverlay.setVisibility("new-slot".equals(state) ? View.VISIBLE : View.GONE);

        if ("empty".equals(state)) {
            binding.noticeCreated.setVisibility(View.GONE);
            binding.noticeInfo.setVisibility(View.VISIBLE);
            binding.layoutFloatingToast.setVisibility(View.GONE);
            binding.rvSlotsList.setVisibility(View.GONE);
            binding.layoutEmptySlots.setVisibility(View.VISIBLE);
            binding.tvSlotCountHeader.setText("0 khung");
        } else if ("created".equals(state)) {
            binding.noticeInfo.setVisibility(View.GONE);
            binding.noticeCreated.setVisibility(View.VISIBLE);
            binding.layoutFloatingToast.setVisibility(View.VISIBLE);
            binding.layoutEmptySlots.setVisibility(View.GONE);
            binding.rvSlotsList.setVisibility(View.VISIBLE);
        } else {
            binding.noticeCreated.setVisibility(View.GONE);
            binding.noticeInfo.setVisibility(View.VISIBLE);
            binding.layoutFloatingToast.setVisibility(View.GONE);
            binding.layoutEmptySlots.setVisibility(View.GONE);
            binding.rvSlotsList.setVisibility(View.VISIBLE);
        }
    }

    private void renderDateStrip() {
        binding.layoutDateStrip.removeAllViews();

        for (int i = 0; i < dateList.size(); i++) {
            StaffDateItem item = dateList.get(i);
            View card = LayoutInflater.from(this).inflate(R.layout.item_date_card, binding.layoutDateStrip, false);

            TextView tvDay = card.findViewById(R.id.tvDateDay);
            TextView tvNum = card.findViewById(R.id.tvDateNum);
            TextView tvAvailable = card.findViewById(R.id.tvDateSlotCount);

            tvDay.setText(item.day);
            tvNum.setText(item.num);
            tvAvailable.setText(item.openCount > 0 ? item.openCount + " trống" : "0 trống");

            if (item.selected) {
                card.setBackgroundResource(R.drawable.bg_date_card_selected);
                tvDay.setTextColor(ContextCompat.getColor(this, R.color.white));
                tvNum.setTextColor(ContextCompat.getColor(this, R.color.white));
                tvAvailable.setTextColor(ContextCompat.getColor(this, R.color.white));
                card.setAlpha(1.0f);
            } else {
                card.setBackgroundResource(R.drawable.bg_date_card_default);
                tvDay.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
                tvNum.setTextColor(ContextCompat.getColor(this, R.color.text_foreground));
                tvAvailable.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
                card.setAlpha(item.openCount == 0 ? 0.6f : 1.0f);
            }

            int finalIndex = i;
            UiMotionUtils.attachSpringTouch(card);
            card.setOnClickListener(v -> {
                UiMotionUtils.animateSelectionPop(v);
                for (int j = 0; j < dateList.size(); j++) {
                    dateList.get(j).selected = (j == finalIndex);
                }
                renderDateStrip();
                loadSlotsForSelectedDate();
            });

            binding.layoutDateStrip.addView(card);
        }
    }

    private void setupSheetInteractions() {
        binding.btnStartTimePicker.setOnClickListener(v ->
                new TimePickerDialog(this, (view, hourOfDay, minute) ->
                        binding.tvNewSlotStartTime.setText(String.format(Locale.US, "%02d:%02d", hourOfDay, minute)),
                        14, 0, true).show());

        binding.btnEndTimePicker.setOnClickListener(v ->
                new TimePickerDialog(this, (view, hourOfDay, minute) ->
                        binding.tvNewSlotEndTime.setText(String.format(Locale.US, "%02d:%02d", hourOfDay, minute)),
                        14, 30, true).show());

        binding.btnSheetCreateSlotAction.setOnClickListener(v -> {
            String start = binding.tvNewSlotStartTime.getText().toString();
            String end = binding.tvNewSlotEndTime.getText().toString();
            StaffDateItem selectedDate = getSelectedDateItem();
            String isoDate = selectedDate != null ? selectedDate.isoDate : LocalDate.now().toString();
            viewModel.createSlot(isoDate, start, end);
        });

        binding.btnSheetCreateSlotDismiss.setOnClickListener(v -> applyState("default"));
        binding.layoutNewSlotSheetOverlay.setOnClickListener(v -> applyState("default"));
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
            Intent intent = new Intent(this, StaffAppointmentsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_scale_out);
            finish();
        });

        binding.navStaffSlots.setOnClickListener(v ->
                binding.scrollSlotsBody.smoothScrollTo(0, 0));

        binding.navStaffProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_scale_out);
        });
    }
}
