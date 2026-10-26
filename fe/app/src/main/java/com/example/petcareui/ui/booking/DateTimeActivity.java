package com.example.petcareui.ui.booking;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.petcareui.R;
import com.example.petcareui.data.model.ClinicItem;
import com.example.petcareui.data.model.DateItem;
import com.example.petcareui.data.model.ServiceItem;
import com.example.petcareui.data.model.TimeSlotItem;
import com.example.petcareui.databinding.ActivityDateTimeBinding;
import com.example.petcareui.databinding.ItemDateCardBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình C07: Chọn ngày & giờ khám (Choose Date & Time) - Bước 2/4.
 * Bám sát nguyên mẫu React C07DateTime và Design System Spec (bao gồm cả 2 trạng thái: Có khung giờ & Hết khung giờ).
 */
public class DateTimeActivity extends AppCompatActivity {

    public static final String EXTRA_CLINIC = "extra_clinic";
    public static final String EXTRA_SELECTED_SERVICE = "extra_selected_service";

    private ActivityDateTimeBinding binding;
    private ClinicItem clinicItem;
    private ServiceItem serviceItem;

    private final List<DateItem> dates = new ArrayList<>();
    private final List<ItemDateCardBinding> dateBindings = new ArrayList<>();
    private int selectedDateIndex = 2; // Mặc định chọn Thứ 2 27/10

    private final List<TimeSlotItem> timeSlots = new ArrayList<>();
    private final List<TextView> slotViews = new ArrayList<>();
    private int selectedSlotIndex = 2; // Mặc định chọn 10:00

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDateTimeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        extractIntentData();
        setupTopBar();
        setupDateStrip();
        setupTimeSlots();
        setupEmptyStateActions();
        setupStickyBottomBar();
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
    }

    private void setupTopBar() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnPrevMonth.setOnClickListener(v -> {
            Toast.makeText(this, "Xem tháng trước", Toast.LENGTH_SHORT).show();
        });

        binding.btnNextMonth.setOnClickListener(v -> {
            Toast.makeText(this, "Xem tháng sau", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupDateStrip() {
        dates.clear();
        dateBindings.clear();
        binding.containerDates.removeAllViews();

        // Danh sách ngày mẫu theo data.js (25/10 đến 31/10)
        dates.add(new DateItem("T7", "25", "Thứ 7, 25/10/2026", "Thứ 7 · 25/10", 0, "past"));
        dates.add(new DateItem("CN", "26", "Chủ nhật, 26/10/2026", "CN · 26/10", 0, "past"));
        dates.add(new DateItem("T2", "27", "Thứ 2, 27/10/2026", "Thứ 2 · 27/10", 6, "selected"));
        dates.add(new DateItem("T3", "28", "Thứ 3, 28/10/2026", "Thứ 3 · 28/10", 8, "open"));
        dates.add(new DateItem("T4", "29", "Thứ 4, 29/10/2026", "Thứ 4 · 29/10", 4, "open"));
        dates.add(new DateItem("T5", "30", "Thứ 5, 30/10/2026", "Thứ 5 · 30/10", 9, "open"));
        dates.add(new DateItem("T6", "31", "Thứ 6, 31/10/2026", "Thứ 6 · 31/10", 0, "full"));

        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < dates.size(); i++) {
            final int index = i;
            DateItem item = dates.get(i);
            ItemDateCardBinding itemBinding = ItemDateCardBinding.inflate(inflater, binding.containerDates, false);

            itemBinding.tvDateDay.setText(item.getDay());
            itemBinding.tvDateNum.setText(item.getNum());

            itemBinding.getRoot().setOnClickListener(v -> selectDate(index));

            dateBindings.add(itemBinding);
            binding.containerDates.addView(itemBinding.getRoot());
        }

        updateDateSelectionUI();
    }

    private void selectDate(int index) {
        selectedDateIndex = index;
        DateItem date = dates.get(index);

        // Kiểm tra xem ngày này có khung giờ hay hết khung giờ
        if ("full".equals(date.getState()) || date.getAvailable() == 0) {
            // Chuyển sang Trạng thái 2: HẾT KHUNG GIỜ (No Slots Empty State)
            binding.layoutSlotsAvailable.setVisibility(View.GONE);
            binding.layoutNoSlotsState.setVisibility(View.VISIBLE);

            binding.tvNoSlotsTitle.setText("Hết giờ khám ngày " + date.getDay() + " " + date.getNum() + "/10");
            binding.tvSelectedDateSubtitle.setText(date.getShortDate() + " đã chọn");
            binding.tvSelectedDateTimeSummary.setText("Chưa chọn khung giờ");

            binding.btnContinue.setEnabled(false);
            binding.btnContinue.setAlpha(0.45f);
        } else {
            // Chuyển sang Trạng thái 1: CÓ KHUNG GIỜ
            binding.layoutSlotsAvailable.setVisibility(View.VISIBLE);
            binding.layoutNoSlotsState.setVisibility(View.GONE);

            binding.tvSelectedDateSubtitle.setText(date.getShortDate() + " đã chọn");
            binding.tvMorningHeader.setText("Buổi sáng · " + date.getAvailable() + " chỗ trống");

            // Khôi phục khung giờ chọn
            if (selectedSlotIndex < 0) {
                selectedSlotIndex = 2; // Mặc định 10:00
            }
            updateSlotSelectionUI();
        }

        updateDateSelectionUI();
    }

    private void updateDateSelectionUI() {
        for (int i = 0; i < dateBindings.size(); i++) {
            ItemDateCardBinding b = dateBindings.get(i);
            DateItem d = dates.get(i);
            boolean isSelected = (i == selectedDateIndex);

            if (isSelected) {
                b.layoutDateCard.setBackgroundResource(R.drawable.bg_date_card_selected);
                b.tvDateDay.setTextColor(ContextCompat.getColor(this, R.color.white));
                b.tvDateNum.setTextColor(ContextCompat.getColor(this, R.color.white));
                b.tvDateSlotCount.setTextColor(ContextCompat.getColor(this, R.color.white));
                b.tvDateSlotCount.setText(d.getAvailable() > 0 ? d.getAvailable() + " chỗ" : "Hết chỗ");
                b.layoutDateCard.setAlpha(1.0f);
            } else {
                b.layoutDateCard.setBackgroundResource(R.drawable.bg_date_card_default);
                b.tvDateDay.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
                b.tvDateNum.setTextColor(ContextCompat.getColor(this, R.color.text_foreground));
                b.tvDateSlotCount.setTextColor(ContextCompat.getColor(this, R.color.text_muted));

                if ("past".equals(d.getState()) || "full".equals(d.getState())) {
                    b.tvDateSlotCount.setText("Hết chỗ");
                    b.layoutDateCard.setAlpha(0.45f);
                } else {
                    b.tvDateSlotCount.setText(d.getAvailable() + " trống");
                    b.layoutDateCard.setAlpha(1.0f);
                }
            }
        }
    }

    private void setupTimeSlots() {
        timeSlots.clear();
        slotViews.clear();

        // 9 Khung giờ theo data.js
        timeSlots.add(new TimeSlotItem("09:00", "unavailable"));
        timeSlots.add(new TimeSlotItem("09:30", "available"));
        timeSlots.add(new TimeSlotItem("10:00", "selected"));
        timeSlots.add(new TimeSlotItem("10:30", "available"));
        timeSlots.add(new TimeSlotItem("11:00", "available"));
        timeSlots.add(new TimeSlotItem("11:30", "unavailable"));
        timeSlots.add(new TimeSlotItem("13:30", "available"));
        timeSlots.add(new TimeSlotItem("14:00", "available"));
        timeSlots.add(new TimeSlotItem("14:30", "available"));

        slotViews.add(binding.slot0);
        slotViews.add(binding.slot1);
        slotViews.add(binding.slot2);
        slotViews.add(binding.slot3);
        slotViews.add(binding.slot4);
        slotViews.add(binding.slot5);
        slotViews.add(binding.slot6);
        slotViews.add(binding.slot7);
        slotViews.add(binding.slot8);

        for (int i = 0; i < slotViews.size(); i++) {
            final int index = i;
            TextView tv = slotViews.get(i);
            TimeSlotItem slot = timeSlots.get(i);

            tv.setText(slot.getTime());

            if (slot.isAvailable()) {
                tv.setOnClickListener(v -> selectTimeSlot(index));
            } else {
                tv.setOnClickListener(null);
                tv.setClickable(false);
            }
        }

        updateSlotSelectionUI();
    }

    private void selectTimeSlot(int index) {
        if (!timeSlots.get(index).isAvailable()) return;
        selectedSlotIndex = index;
        updateSlotSelectionUI();
    }

    private void updateSlotSelectionUI() {
        for (int i = 0; i < slotViews.size(); i++) {
            TextView tv = slotViews.get(i);
            TimeSlotItem slot = timeSlots.get(i);
            boolean isSelected = (i == selectedSlotIndex);

            if (!slot.isAvailable()) {
                // Đã đặt: Nền xám nhạt, gạch ngang chữ
                tv.setBackgroundResource(R.drawable.bg_slot_unavailable);
                tv.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
                tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tv.setAlpha(0.6f);
            } else if (isSelected) {
                // Đang chọn: Nền Teal thương hiệu, chữ trắng
                tv.setBackgroundResource(R.drawable.bg_slot_selected);
                tv.setTextColor(ContextCompat.getColor(this, R.color.white));
                tv.setPaintFlags(tv.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tv.setAlpha(1.0f);
            } else {
                // Còn chỗ: Nền trắng, viền mảnh
                tv.setBackgroundResource(R.drawable.bg_slot_available);
                tv.setTextColor(ContextCompat.getColor(this, R.color.text_foreground));
                tv.setPaintFlags(tv.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tv.setAlpha(1.0f);
            }
        }

        // Cập nhật dòng tóm tắt phía dưới
        if (selectedDateIndex >= 0 && selectedSlotIndex >= 0) {
            DateItem d = dates.get(selectedDateIndex);
            TimeSlotItem s = timeSlots.get(selectedSlotIndex);
            binding.tvSelectedDateTimeSummary.setText(d.getShortDate() + " · " + s.getTime());
            binding.btnContinue.setEnabled(true);
            binding.btnContinue.setAlpha(1.0f);
        } else {
            binding.tvSelectedDateTimeSummary.setText("Chưa chọn khung giờ");
            binding.btnContinue.setEnabled(false);
            binding.btnContinue.setAlpha(0.45f);
        }
    }

    private void setupEmptyStateActions() {
        // Nút bấm "Xem Thứ 3 28/10" tự động nhảy sang ngày còn chỗ gần nhất
        binding.btnJumpAvailableDate.setOnClickListener(v -> {
            selectDate(3); // Thứ 3 28/10
        });

        // Nút nhận thông báo khi có chỗ hủy
        binding.btnNotifyWhenAvailable.setOnClickListener(v -> {
            DateItem d = dates.get(selectedDateIndex);
            Toast.makeText(this, "Đã bật thông báo khi có khung giờ trống ngày " + d.getShortDate(), Toast.LENGTH_SHORT).show();
        });
    }

    private void setupStickyBottomBar() {
        binding.btnContinue.setOnClickListener(v -> {
            if (selectedDateIndex >= 0 && selectedSlotIndex >= 0) {
                DateItem d = dates.get(selectedDateIndex);
                TimeSlotItem s = timeSlots.get(selectedSlotIndex);
                Intent intent = new Intent(this, com.example.petcareui.ui.pets.MyPetsActivity.class);
                intent.putExtra(com.example.petcareui.ui.pets.MyPetsActivity.EXTRA_CLINIC, clinicItem);
                intent.putExtra(com.example.petcareui.ui.pets.MyPetsActivity.EXTRA_SELECTED_SERVICE, serviceItem);
                intent.putExtra(com.example.petcareui.ui.pets.MyPetsActivity.EXTRA_SELECTED_DATE, d);
                intent.putExtra(com.example.petcareui.ui.pets.MyPetsActivity.EXTRA_SELECTED_TIME, s);
                intent.putExtra(com.example.petcareui.ui.pets.MyPetsActivity.EXTRA_BOOKING_MODE, true);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Vui lòng chọn khung giờ phù hợp", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
