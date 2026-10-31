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

import com.example.petcareui.R;
import com.example.petcareui.data.api.ApiClient;
import com.example.petcareui.data.model.ClinicSlotResponse;
import com.example.petcareui.data.model.CreateSlotRequest;
import com.example.petcareui.data.model.StaffSlotItem;
import com.example.petcareui.databinding.ActivityStaffSlotManagementBinding;
import com.example.petcareui.databinding.ItemStaffSlotRowBinding;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Màn hình S04: Quản lý khung giờ (nhân viên) - Staff Slot Management.
 * Màn hình trong phân hệ nhân viên,
 * bám sát nguyên mẫu React S04SlotManagement và kết nối REST API /api/v1/staff/slots.
 * Hỗ trợ các trạng thái:
 * - Mặc định (default)
 * - Tạo khung giờ (new-slot bottom sheet)
 * - Đã tạo (created với notice & floating toast)
 * - Không có khung giờ (empty)
 */
public class StaffSlotManagementActivity extends AppCompatActivity {

    public static final String EXTRA_STATE = "extra_state";

    private ActivityStaffSlotManagementBinding binding;

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
    private final List<StaffSlotItem> slotList = new ArrayList<>();

    private String currentState = "default";
    private boolean isDailyRecurrence = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStaffSlotManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        extractIntentData();
        setupDates();
        setupAppBar();
        setupStateToggles();
        setupSheetInteractions();
        setupBottomNav();

        loadSlotsForSelectedDate();
        applyState(currentState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSlotsForSelectedDate();
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
            String isoDate = d.toString();
            dateList.add(new StaffDateItem(dayLabel, num, 0, i == 0, isoDate));
        }
        renderDateStrip();
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

        ApiClient.getStaffSlotApi().getStaffSlots(isoDate).enqueue(new Callback<List<ClinicSlotResponse>>() {
            @Override
            public void onResponse(Call<List<ClinicSlotResponse>> call, Response<List<ClinicSlotResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ClinicSlotResponse> body = response.body();
                    slotList.clear();
                    int openCount = 0;
                    for (ClinicSlotResponse res : body) {
                        StaffSlotItem item = res.toStaffSlotItem();
                        if ("open".equals(item.getState())) {
                            openCount++;
                        }
                        slotList.add(item);
                    }

                    if (selectedDate != null) {
                        selectedDate.openCount = openCount;
                    }
                    renderDateStrip();

                    if (slotList.isEmpty()) {
                        applyState("empty");
                    } else {
                        if (!"new-slot".equals(currentState) && !"created".equals(currentState)) {
                            applyState("default");
                        } else {
                            renderSlotsList("created".equals(currentState));
                        }
                    }
                } else {
                    if (slotList.isEmpty()) {
                        initMockData();
                        applyState("default");
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ClinicSlotResponse>> call, Throwable t) {
                if (slotList.isEmpty()) {
                    initMockData();
                    applyState("default");
                }
            }
        });
    }

    private void initMockData() {
        slotList.clear();
        slotList.add(new StaffSlotItem("10:00", "closed"));
        slotList.add(new StaffSlotItem("10:30", "open"));
        slotList.add(new StaffSlotItem("11:00", "booked", "Lựu", "Phạm Hà", "Khám tổng quát"));
        slotList.add(new StaffSlotItem("11:30", "open"));
        slotList.add(new StaffSlotItem("13:00", "closed"));
        slotList.add(new StaffSlotItem("13:30", "open"));
    }

    private void setupAppBar() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnTopAddSlot.setOnClickListener(v -> applyState("new-slot"));
        binding.btnCreateNewSlot.setOnClickListener(v -> applyState("new-slot"));
        binding.btnEmptyCreateSlot.setOnClickListener(v -> applyState("new-slot"));
    }

    private void setupStateToggles() {
        binding.chipStateDefault.setOnClickListener(v -> applyState("default"));
        binding.chipStateNewSlot.setOnClickListener(v -> applyState("new-slot"));
        binding.chipStateCreated.setOnClickListener(v -> applyState("created"));
        binding.chipStateEmpty.setOnClickListener(v -> applyState("empty"));
    }

    private void applyState(String state) {
        currentState = state;

        updateToggleChip(binding.chipStateDefault, "default".equals(state));
        updateToggleChip(binding.chipStateNewSlot, "new-slot".equals(state));
        updateToggleChip(binding.chipStateCreated, "created".equals(state));
        updateToggleChip(binding.chipStateEmpty, "empty".equals(state));

        // Ẩn/Hiện Overlay tạo khung giờ
        if ("new-slot".equals(state)) {
            binding.layoutNewSlotSheetOverlay.setVisibility(View.VISIBLE);
        } else {
            binding.layoutNewSlotSheetOverlay.setVisibility(View.GONE);
        }

        // Cấu hình hiển thị theo từng trạng thái
        if ("empty".equals(state)) {
            binding.noticeCreated.setVisibility(View.GONE);
            binding.noticeInfo.setVisibility(View.VISIBLE);
            binding.layoutFloatingToast.setVisibility(View.GONE);
            binding.layoutSlotsList.setVisibility(View.GONE);
            binding.layoutEmptySlots.setVisibility(View.VISIBLE);
            binding.tvSlotCountHeader.setText("0 khung");
        } else if ("created".equals(state)) {
            binding.noticeInfo.setVisibility(View.GONE);
            binding.noticeCreated.setVisibility(View.VISIBLE);
            binding.layoutFloatingToast.setVisibility(View.VISIBLE);
            binding.layoutEmptySlots.setVisibility(View.GONE);
            binding.layoutSlotsList.setVisibility(View.VISIBLE);

            renderSlotsList(true);
        } else {
            // default & new-slot
            binding.noticeCreated.setVisibility(View.GONE);
            binding.noticeInfo.setVisibility(View.VISIBLE);
            binding.layoutFloatingToast.setVisibility(View.GONE);
            binding.layoutEmptySlots.setVisibility(View.GONE);
            binding.layoutSlotsList.setVisibility(View.VISIBLE);
            renderSlotsList(false);
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

            if (item.selected) {
                card.setBackgroundResource(R.drawable.bg_date_card_selected);
                tvDay.setTextColor(ContextCompat.getColor(this, R.color.white));
                tvNum.setTextColor(ContextCompat.getColor(this, R.color.white));
                tvAvailable.setTextColor(ContextCompat.getColor(this, R.color.white));
                tvAvailable.setText(item.openCount > 0 ? item.openCount + " trống" : "0 trống");
                card.setAlpha(1.0f);
            } else {
                card.setBackgroundResource(R.drawable.bg_date_card_default);
                tvDay.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
                tvNum.setTextColor(ContextCompat.getColor(this, R.color.text_foreground));
                tvAvailable.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
                tvAvailable.setText(item.openCount > 0 ? item.openCount + " trống" : "0 trống");

                if (item.openCount == 0) {
                    card.setAlpha(0.6f);
                } else {
                    card.setAlpha(1.0f);
                }
            }

            int finalIndex = i;
            card.setOnClickListener(v -> {
                for (int j = 0; j < dateList.size(); j++) {
                    dateList.get(j).selected = (j == finalIndex);
                }
                renderDateStrip();
                loadSlotsForSelectedDate();
            });

            binding.layoutDateStrip.addView(card);
        }
    }

    private void renderSlotsList(boolean highlightNewSlot) {
        binding.layoutSlotsList.removeAllViews();
        binding.tvSlotCountHeader.setText(slotList.size() + " khung");

        for (StaffSlotItem slot : slotList) {
            ItemStaffSlotRowBinding rowBinding = ItemStaffSlotRowBinding.inflate(getLayoutInflater());

            rowBinding.tvSlotTime.setText(slot.getTime());

            if ("booked".equals(slot.getState())) {
                rowBinding.tvSlotPrimaryText.setText((slot.getPet() != null ? slot.getPet() : "Thú cưng") + " · " + (slot.getService() != null ? slot.getService() : "Lịch khám"));
                rowBinding.tvSlotSecondaryText.setText(slot.getCustomer() != null ? slot.getCustomer() : "Đã có khách đặt");
                rowBinding.tvSlotBookedBadge.setVisibility(View.VISIBLE);
                rowBinding.btnToggleLock.setVisibility(View.GONE);
            } else if ("closed".equals(slot.getState())) {
                rowBinding.tvSlotPrimaryText.setText("Đã đóng");
                rowBinding.tvSlotSecondaryText.setText("Ẩn khỏi ứng dụng của khách");
                rowBinding.tvSlotBookedBadge.setVisibility(View.GONE);
                rowBinding.btnToggleLock.setVisibility(View.VISIBLE);
                rowBinding.ivLockIcon.setImageResource(R.drawable.ic_lock);
                rowBinding.ivLockIcon.setImageTintList(ContextCompat.getColorStateList(this, R.color.status_destructive));
            } else {
                // open
                rowBinding.tvSlotPrimaryText.setText("Đang mở nhận đặt lịch");
                rowBinding.tvSlotSecondaryText.setText("Khung 30 phút");
                rowBinding.tvSlotBookedBadge.setVisibility(View.GONE);
                rowBinding.btnToggleLock.setVisibility(View.VISIBLE);
                rowBinding.ivLockIcon.setImageResource(R.drawable.ic_lock);
                rowBinding.ivLockIcon.setImageTintList(ContextCompat.getColorStateList(this, R.color.text_muted));
            }

            if (highlightNewSlot && "14:00".equals(slot.getTime())) {
                rowBinding.slotRowContainer.setBackgroundResource(R.drawable.bg_card_priority);
            } else {
                rowBinding.slotRowContainer.setBackgroundResource(R.drawable.bg_card_clinic);
            }

            rowBinding.btnToggleLock.setOnClickListener(v -> {
                if ("open".equals(slot.getState())) {
                    slot.setState("closed");
                    Toast.makeText(this, "Đã đóng khung giờ " + slot.getTime(), Toast.LENGTH_SHORT).show();
                } else if ("closed".equals(slot.getState())) {
                    slot.setState("open");
                    Toast.makeText(this, "Đã mở lại khung giờ " + slot.getTime(), Toast.LENGTH_SHORT).show();
                }
                renderSlotsList(highlightNewSlot);
            });

            binding.layoutSlotsList.addView(rowBinding.getRoot());
        }
    }

    private void setupSheetInteractions() {
        binding.chipRecurrenceOnce.setOnClickListener(v -> {
            isDailyRecurrence = false;
            binding.chipRecurrenceOnce.setBackgroundResource(R.drawable.bg_service_card_default);
            binding.chipRecurrenceOnce.setTextColor(ContextCompat.getColor(this, R.color.brand_ink));
            binding.chipRecurrenceDaily.setBackground(null);
            binding.chipRecurrenceDaily.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
        });

        binding.chipRecurrenceDaily.setOnClickListener(v -> {
            isDailyRecurrence = true;
            binding.chipRecurrenceDaily.setBackgroundResource(R.drawable.bg_service_card_default);
            binding.chipRecurrenceDaily.setTextColor(ContextCompat.getColor(this, R.color.brand_ink));
            binding.chipRecurrenceOnce.setBackground(null);
            binding.chipRecurrenceOnce.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
        });

        binding.btnStartTimePicker.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                String timeStr = String.format("%02d:%02d", hourOfDay, minute);
                binding.tvNewSlotStartTime.setText(timeStr);
            }, 14, 0, true).show();
        });

        binding.btnEndTimePicker.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                String timeStr = String.format("%02d:%02d", hourOfDay, minute);
                binding.tvNewSlotEndTime.setText(timeStr);
            }, 14, 30, true).show();
        });

        // Nút Tạo khung giờ -> Gọi API Backend
        binding.btnSheetCreateSlotAction.setOnClickListener(v -> {
            String start = binding.tvNewSlotStartTime.getText().toString();
            String end = binding.tvNewSlotEndTime.getText().toString();

            StaffDateItem selectedDate = getSelectedDateItem();
            String isoDate = selectedDate != null ? selectedDate.isoDate : LocalDate.now().toString();

            CreateSlotRequest request = new CreateSlotRequest(isoDate, start, end);
            ApiClient.getStaffSlotApi().createSlot(request).enqueue(new Callback<ClinicSlotResponse>() {
                @Override
                public void onResponse(Call<ClinicSlotResponse> call, Response<ClinicSlotResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ClinicSlotResponse created = response.body();
                        binding.tvFloatingToastText.setText("Đã tạo khung giờ · " + created.getFormattedStartTime() + " – " + created.getEndTime());
                        loadSlotsForSelectedDate();
                        applyState("created");
                        Toast.makeText(StaffSlotManagementActivity.this, "Đã tạo khung giờ thành công!", Toast.LENGTH_SHORT).show();
                    } else if (response.code() == 409) {
                        Toast.makeText(StaffSlotManagementActivity.this, "Khung giờ này đã tồn tại hoặc bị trùng lặp!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(StaffSlotManagementActivity.this, "Không thể tạo khung giờ. Vui lòng kiểm tra lại thời gian.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ClinicSlotResponse> call, Throwable t) {
                    Toast.makeText(StaffSlotManagementActivity.this, "Lỗi kết nối máy chủ khi tạo khung giờ.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.btnSheetCreateSlotDismiss.setOnClickListener(v -> applyState("default"));
        binding.layoutNewSlotSheetOverlay.setOnClickListener(v -> applyState("default"));
    }

    private void setupBottomNav() {
        binding.navStaffToday.setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        binding.navStaffAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffAppointmentsActivity.class);
            startActivity(intent);
            finish();
        });

        binding.navStaffSlots.setOnClickListener(v -> {
            binding.scrollSlotsBody.smoothScrollTo(0, 0);
        });
    }
}
