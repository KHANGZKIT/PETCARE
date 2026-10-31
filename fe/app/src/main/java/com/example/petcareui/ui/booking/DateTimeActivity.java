package com.example.petcareui.ui.booking;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.petcareui.R;
import com.example.petcareui.data.api.ApiClient;
import com.example.petcareui.data.model.ClinicItem;
import com.example.petcareui.data.model.ClinicSlotResponse;
import com.example.petcareui.data.model.DateItem;
import com.example.petcareui.data.model.ServiceItem;
import com.example.petcareui.data.model.TimeSlotItem;
import com.example.petcareui.databinding.ActivityDateTimeBinding;
import com.example.petcareui.databinding.ItemDateCardBinding;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private int selectedDateIndex = 0;

    private final List<TimeSlotItem> timeSlots = new ArrayList<>();
    private final List<TextView> slotViews = new ArrayList<>();
    private int selectedSlotIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDateTimeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        extractIntentData();
        setupTopBar();
        setupDateStrip();
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

        LocalDate now = LocalDate.now();
        binding.tvMonthYear.setText("Tháng " + now.getMonthValue() + ", " + now.getYear());

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

        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate d = today.plusDays(i);
            String dayLabel = getDayOfWeekLabel(d.getDayOfWeek());
            String num = String.valueOf(d.getDayOfMonth());
            String fullDate = getFullDayOfWeekLabel(d.getDayOfWeek()) + ", " + d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String shortDate = dayLabel + " · " + d.format(DateTimeFormatter.ofPattern("dd/MM"));
            String isoDate = d.toString();

            dates.add(new DateItem(dayLabel, num, fullDate, shortDate, 0, i == 0 ? "selected" : "open", isoDate));
        }

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

        selectDate(0);
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

    private String getFullDayOfWeekLabel(DayOfWeek dow) {
        switch (dow) {
            case MONDAY: return "Thứ 2";
            case TUESDAY: return "Thứ 3";
            case WEDNESDAY: return "Thứ 4";
            case THURSDAY: return "Thứ 5";
            case FRIDAY: return "Thứ 6";
            case SATURDAY: return "Thứ 7";
            case SUNDAY: return "Chủ nhật";
            default: return "";
        }
    }

    private void selectDate(int index) {
        if (index < 0 || index >= dates.size()) return;
        selectedDateIndex = index;
        DateItem date = dates.get(index);

        updateDateSelectionUI();
        loadSlotsForDate(date);
    }

    private void loadSlotsForDate(DateItem date) {
        Long clinicId = clinicItem != null ? clinicItem.getNumericId() : null;
        if (clinicId == null) {
            clinicId = 1L;
        }

        String queryDate = date.getIsoDate() != null ? date.getIsoDate() : LocalDate.now().toString();

        binding.tvMorningHeader.setText("Đang tải khung giờ...");

        ApiClient.getClinicApi().getSlotsByClinicAndDate(clinicId, queryDate).enqueue(new Callback<List<ClinicSlotResponse>>() {
            @Override
            public void onResponse(Call<List<ClinicSlotResponse>> call, Response<List<ClinicSlotResponse>> response) {
                timeSlots.clear();
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    int availableCount = 0;
                    int firstAvailableIndex = -1;
                    List<ClinicSlotResponse> body = response.body();

                    for (int i = 0; i < body.size(); i++) {
                        ClinicSlotResponse slotRes = body.get(i);
                        boolean isAvail = slotRes.isAvailable();
                        if (isAvail) {
                            availableCount++;
                            if (firstAvailableIndex == -1) {
                                firstAvailableIndex = i;
                            }
                        }
                        timeSlots.add(new TimeSlotItem(
                                slotRes.getId(),
                                slotRes.getFormattedStartTime(),
                                isAvail ? "available" : "unavailable"
                        ));
                    }

                    date.setAvailable(availableCount);
                    date.setState(availableCount > 0 ? "open" : "full");

                    if (availableCount > 0) {
                        selectedSlotIndex = firstAvailableIndex;
                        showSlotsAvailableState(date, availableCount);
                    } else {
                        selectedSlotIndex = -1;
                        showNoSlotsState(date);
                    }
                } else {
                    date.setAvailable(0);
                    date.setState("full");
                    selectedSlotIndex = -1;
                    showNoSlotsState(date);
                }
                updateDateSelectionUI();
            }

            @Override
            public void onFailure(Call<List<ClinicSlotResponse>> call, Throwable t) {
                setupFallbackTimeSlots(date);
                updateDateSelectionUI();
            }
        });
    }

    private void showSlotsAvailableState(DateItem date, int availableCount) {
        binding.layoutSlotsAvailable.setVisibility(View.VISIBLE);
        binding.layoutNoSlotsState.setVisibility(View.GONE);

        binding.tvSelectedDateSubtitle.setText(date.getShortDate() + " đã chọn");
        binding.tvMorningHeader.setText("Khung giờ khám · " + availableCount + " chỗ trống");

        renderSlotsGrid();
    }

    private void showNoSlotsState(DateItem date) {
        binding.layoutSlotsAvailable.setVisibility(View.GONE);
        binding.layoutNoSlotsState.setVisibility(View.VISIBLE);

        binding.tvNoSlotsTitle.setText("Hết giờ khám ngày " + date.getDay() + " " + date.getNum());
        binding.tvSelectedDateSubtitle.setText(date.getShortDate() + " đã chọn");
        binding.tvSelectedDateTimeSummary.setText("Chưa chọn khung giờ");

        binding.btnContinue.setEnabled(false);
        binding.btnContinue.setAlpha(0.45f);
    }

    private void setupFallbackTimeSlots(DateItem date) {
        timeSlots.clear();
        timeSlots.add(new TimeSlotItem(1L, "08:30", "available"));
        timeSlots.add(new TimeSlotItem(2L, "09:00", "unavailable"));
        timeSlots.add(new TimeSlotItem(3L, "09:30", "available"));
        timeSlots.add(new TimeSlotItem(4L, "10:00", "available"));
        timeSlots.add(new TimeSlotItem(5L, "10:30", "available"));
        timeSlots.add(new TimeSlotItem(6L, "11:00", "unavailable"));
        timeSlots.add(new TimeSlotItem(7L, "14:00", "available"));
        timeSlots.add(new TimeSlotItem(8L, "14:30", "available"));
        timeSlots.add(new TimeSlotItem(9L, "15:00", "available"));

        int count = 0;
        for (TimeSlotItem s : timeSlots) {
            if (s.isAvailable()) count++;
        }
        date.setAvailable(count);
        date.setState("open");
        selectedSlotIndex = 0;
        showSlotsAvailableState(date, count);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void renderSlotsGrid() {
        binding.layoutSlotsGrid.removeAllViews();
        slotViews.clear();

        if (timeSlots.isEmpty()) {
            return;
        }

        int total = timeSlots.size();
        int columns = 3;
        int rows = (int) Math.ceil((double) total / columns);

        for (int r = 0; r < rows; r++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            if (r > 0) {
                rowParams.topMargin = dpToPx(10);
            }
            rowLayout.setLayoutParams(rowParams);

            for (int c = 0; c < columns; c++) {
                int slotIndex = r * columns + c;
                if (slotIndex < total) {
                    final int index = slotIndex;
                    TimeSlotItem slot = timeSlots.get(index);

                    TextView tv = new TextView(this);
                    LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                            0,
                            dpToPx(44),
                            1.0f
                    );
                    if (c > 0) {
                        itemParams.setMarginStart(dpToPx(10));
                    }
                    tv.setLayoutParams(itemParams);
                    tv.setGravity(android.view.Gravity.CENTER);
                    tv.setText(slot.getTime());
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
                    tv.setTypeface(null, Typeface.BOLD);

                    if (slot.isAvailable()) {
                        tv.setOnClickListener(v -> selectTimeSlot(index));
                    } else {
                        tv.setOnClickListener(null);
                        tv.setClickable(false);
                    }

                    slotViews.add(tv);
                    rowLayout.addView(tv);
                } else {
                    View spacer = new View(this);
                    LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                            0,
                            dpToPx(44),
                            1.0f
                    );
                    if (c > 0) {
                        spacerParams.setMarginStart(dpToPx(10));
                    }
                    spacer.setLayoutParams(spacerParams);
                    spacer.setVisibility(View.INVISIBLE);
                    rowLayout.addView(spacer);
                }
            }

            binding.layoutSlotsGrid.addView(rowLayout);
        }

        updateSlotSelectionUI();
    }

    private void selectTimeSlot(int index) {
        if (index < 0 || index >= timeSlots.size() || !timeSlots.get(index).isAvailable()) return;
        selectedSlotIndex = index;
        updateSlotSelectionUI();
    }

    private void updateSlotSelectionUI() {
        for (int i = 0; i < slotViews.size(); i++) {
            TextView tv = slotViews.get(i);
            TimeSlotItem slot = timeSlots.get(i);
            boolean isSelected = (i == selectedSlotIndex);

            if (!slot.isAvailable()) {
                tv.setBackgroundResource(R.drawable.bg_slot_unavailable);
                tv.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
                tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tv.setAlpha(0.6f);
            } else if (isSelected) {
                tv.setBackgroundResource(R.drawable.bg_slot_selected);
                tv.setTextColor(ContextCompat.getColor(this, R.color.white));
                tv.setPaintFlags(tv.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tv.setAlpha(1.0f);
            } else {
                tv.setBackgroundResource(R.drawable.bg_slot_available);
                tv.setTextColor(ContextCompat.getColor(this, R.color.text_foreground));
                tv.setPaintFlags(tv.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tv.setAlpha(1.0f);
            }
        }

        if (selectedDateIndex >= 0 && selectedSlotIndex >= 0 && selectedSlotIndex < timeSlots.size()) {
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

    private void setupEmptyStateActions() {
        binding.btnJumpAvailableDate.setOnClickListener(v -> {
            for (int i = 0; i < dates.size(); i++) {
                if (i != selectedDateIndex && dates.get(i).getAvailable() > 0) {
                    selectDate(i);
                    return;
                }
            }
            if (selectedDateIndex + 1 < dates.size()) {
                selectDate(selectedDateIndex + 1);
            }
        });

        binding.btnNotifyWhenAvailable.setOnClickListener(v -> {
            if (selectedDateIndex >= 0 && selectedDateIndex < dates.size()) {
                DateItem d = dates.get(selectedDateIndex);
                Toast.makeText(this, "Đã bật thông báo khi có khung giờ trống ngày " + d.getShortDate(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupStickyBottomBar() {
        binding.btnContinue.setOnClickListener(v -> {
            if (selectedDateIndex >= 0 && selectedSlotIndex >= 0 && selectedSlotIndex < timeSlots.size()) {
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
