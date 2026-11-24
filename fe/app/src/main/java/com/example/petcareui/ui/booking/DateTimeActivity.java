package com.example.petcareui.ui.booking;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.petcareui.R;
import com.example.petcareui.data.api.ApiClient;
import com.example.petcareui.data.dto.response.ClinicSlotResponse;
import com.example.petcareui.data.model.ClinicItem;
import com.example.petcareui.data.model.DateItem;
import com.example.petcareui.data.model.PetItem;
import com.example.petcareui.data.model.ServiceItem;
import com.example.petcareui.data.model.TimeSlotItem;
import com.example.petcareui.databinding.ActivityDateTimeBinding;
import com.example.petcareui.databinding.ItemDateCardBinding;
import com.example.petcareui.utils.UiMotionUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Màn hình C07: Chọn ngày & giờ khám (Choose Date & Time) - Bước 2/4.
 * Phong cách Warm Tactile: Top bar cố định, điều hướng tháng (tối đa +3 tháng),
 * lưới khung giờ chia 3 nhóm Sáng / Chiều / Tối.
 */
public class DateTimeActivity extends AppCompatActivity {

    public static final String EXTRA_CLINIC = "extra_clinic";
    public static final String EXTRA_SELECTED_SERVICE = "extra_selected_service";
    public static final String EXTRA_SELECTED_DATE = "extra_selected_date";
    public static final String EXTRA_SELECTED_TIME = "extra_selected_time";
    public static final String EXTRA_PRESELECTED_PET = "extra_preselected_pet";
    public static final String EXTRA_EDIT_FROM_REVIEW = "extra_edit_from_review";
    private static final int MAX_MONTHS_AHEAD = 3;

    private ActivityDateTimeBinding binding;
    private ClinicItem clinicItem;
    private ServiceItem serviceItem;
    private DateItem preselectedDate;
    private TimeSlotItem preselectedTime;
    private PetItem preselectedPet;
    private boolean isEditFromReview = false;
    private boolean appliedPreselectedTime = false;

    private YearMonth minMonth;
    private YearMonth maxMonth;
    private YearMonth currentYearMonth;

    private final List<DateItem> dates = new ArrayList<>();
    private final List<ItemDateCardBinding> dateBindings = new ArrayList<>();
    private int selectedDateIndex = 0;

    private final List<TimeSlotItem> timeSlots = new ArrayList<>();
    private final Map<Integer, TextView> slotViewMap = new HashMap<>();
    private int selectedSlotIndex = -1;
    private int slotRequestSeq = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDateTimeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Cố định Top Bar (áp dụng statusBar top padding) & Sticky Bottom Bar
        UiMotionUtils.applyWindowInsets(binding.getRoot(), binding.layoutTopHeader, binding.layoutStickyBottom);
        UiMotionUtils.attachSpringTouch(binding.btnBack, binding.btnPrevMonth, binding.btnNextMonth, binding.btnContinue);

        minMonth = YearMonth.now();
        maxMonth = minMonth.plusMonths(MAX_MONTHS_AHEAD);
        currentYearMonth = minMonth;

        extractIntentData();
        setupTopBar();
        setupEmptyStateActions();
        setupStickyBottomBar();
        loadMonthDates();
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
        if (getIntent().hasExtra(EXTRA_SELECTED_DATE)) {
            try {
                preselectedDate = (DateItem) getIntent().getSerializableExtra(EXTRA_SELECTED_DATE);
                if (preselectedDate != null && preselectedDate.getIsoDate() != null) {
                    YearMonth ym = YearMonth.from(LocalDate.parse(preselectedDate.getIsoDate().substring(0, 10)));
                    if (!ym.isBefore(minMonth) && !ym.isAfter(maxMonth)) {
                        currentYearMonth = ym;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        if (getIntent().hasExtra(EXTRA_SELECTED_TIME)) {
            try {
                preselectedTime = (TimeSlotItem) getIntent().getSerializableExtra(EXTRA_SELECTED_TIME);
            } catch (Exception ignored) {
            }
        }
        if (getIntent().hasExtra(EXTRA_PRESELECTED_PET)) {
            try {
                preselectedPet = (PetItem) getIntent().getSerializableExtra(EXTRA_PRESELECTED_PET);
            } catch (Exception ignored) {
            }
        }
        isEditFromReview = getIntent().getBooleanExtra(EXTRA_EDIT_FROM_REVIEW, false);
    }

    private void setupTopBar() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnPrevMonth.setOnClickListener(v -> {
            if (currentYearMonth.isAfter(minMonth)) {
                currentYearMonth = currentYearMonth.minusMonths(1);
                loadMonthDates();
            }
        });

        binding.btnNextMonth.setOnClickListener(v -> {
            if (currentYearMonth.isBefore(maxMonth)) {
                currentYearMonth = currentYearMonth.plusMonths(1);
                loadMonthDates();
            }
        });
    }

    private void updateMonthNavUI() {
        binding.tvMonthYear.setText("Tháng " + currentYearMonth.getMonthValue() + ", " + currentYearMonth.getYear());

        boolean canPrev = currentYearMonth.isAfter(minMonth);
        binding.btnPrevMonth.setEnabled(canPrev);
        binding.btnPrevMonth.setAlpha(canPrev ? 1.0f : 0.35f);

        boolean canNext = currentYearMonth.isBefore(maxMonth);
        binding.btnNextMonth.setEnabled(canNext);
        binding.btnNextMonth.setAlpha(canNext ? 1.0f : 0.35f);
    }

    private void loadMonthDates() {
        updateMonthNavUI();

        dates.clear();
        dateBindings.clear();
        binding.containerDates.removeAllViews();

        LocalDate today = LocalDate.now();
        LocalDate startDate = currentYearMonth.equals(minMonth) ? today : currentYearMonth.atDay(1);
        LocalDate endDate = currentYearMonth.atEndOfMonth();

        DateTimeFormatter fullFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter shortFmt = DateTimeFormatter.ofPattern("dd/MM");

        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            String dayLabel = getDayOfWeekLabel(d.getDayOfWeek());
            String num = String.valueOf(d.getDayOfMonth());
            String fullDate = getFullDayOfWeekLabel(d.getDayOfWeek()) + ", " + d.format(fullFmt);
            String shortDate = dayLabel + " · " + d.format(shortFmt);
            String isoDate = d.toString();

            dates.add(new DateItem(dayLabel, num, fullDate, shortDate, 0, "open", isoDate));
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

        int initialDateIdx = 0;
        if (preselectedDate != null && preselectedDate.getIsoDate() != null) {
            for (int i = 0; i < dates.size(); i++) {
                if (preselectedDate.getIsoDate().equals(dates.get(i).getIsoDate())) {
                    initialDateIdx = i;
                    break;
                }
            }
            preselectedDate = null;
        }

        if (!dates.isEmpty()) {
            selectDate(initialDateIdx);
        }
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
        selectedSlotIndex = -1;
        DateItem date = dates.get(index);

        updateDateSelectionUI();
        setContinueEnabled(false, "Đang tải khung giờ...");
        loadSlotsForDate(date);
        scrollToDateCard(index);
    }

    private void scrollToDateCard(int index) {
        if (index >= 0 && index < dateBindings.size()) {
            View targetView = dateBindings.get(index).getRoot();
            binding.scrollDatesStrip.post(() -> {
                int scrollX = targetView.getLeft() - dpToPx(16);
                binding.scrollDatesStrip.smoothScrollTo(Math.max(0, scrollX), 0);
            });
        }
    }

    private boolean isSlotInPast(String isoDate, String startTimeStr) {
        if (isoDate == null || startTimeStr == null) return false;
        try {
            LocalDate slotDate = LocalDate.parse(isoDate.substring(0, 10));
            LocalDate today = LocalDate.now();
            if (slotDate.isBefore(today)) return true;
            if (slotDate.isAfter(today)) return false;

            String hhmm = startTimeStr.trim();
            if (hhmm.length() >= 5) {
                LocalTime slotTime = LocalTime.parse(hhmm.substring(0, 5));
                return !slotTime.isAfter(LocalTime.now());
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private int extractHour(String timeStr) {
        if (timeStr == null) return 9;
        try {
            String[] parts = timeStr.trim().split(":");
            return Integer.parseInt(parts[0]);
        } catch (Exception e) {
            return 9;
        }
    }

    private void loadSlotsForDate(DateItem date) {
        Long clinicId = clinicItem != null ? clinicItem.getNumericId() : null;
        if (clinicId == null) clinicId = 1L;

        String queryDate = date.getIsoDate() != null ? date.getIsoDate() : LocalDate.now().toString();
        final int requestId = ++slotRequestSeq;

        binding.layoutSlotsAvailable.setVisibility(View.VISIBLE);
        binding.layoutNoSlotsState.setVisibility(View.GONE);
        binding.sectionMorning.setVisibility(View.GONE);
        binding.sectionAfternoon.setVisibility(View.GONE);
        binding.sectionEvening.setVisibility(View.GONE);
        binding.tvSelectedDateSubtitle.setText(date.getShortDate() + " đã chọn");
        binding.tvMorningHeader.setText("Đang tải khung giờ...");

        ApiClient.getClinicApi().getSlotsByClinicAndDate(clinicId, queryDate).enqueue(new Callback<List<ClinicSlotResponse>>() {
            @Override
            public void onResponse(Call<List<ClinicSlotResponse>> call, Response<List<ClinicSlotResponse>> response) {
                if (isFinishing() || isDestroyed() || requestId != slotRequestSeq) return;

                timeSlots.clear();
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    int availableCount = 0;
                    int firstAvailableIndex = -1;
                    int matchedPreselectedIdx = -1;
                    List<ClinicSlotResponse> body = response.body();

                    for (int i = 0; i < body.size(); i++) {
                        ClinicSlotResponse slotRes = body.get(i);
                        String formattedTime = slotRes.getFormattedStartTime();
                        boolean past = isSlotInPast(queryDate, formattedTime);
                        boolean isAvail = slotRes.isAvailable() && !past;
                        if (isAvail) {
                            availableCount++;
                            if (firstAvailableIndex == -1) {
                                firstAvailableIndex = i;
                            }
                            if (!appliedPreselectedTime && preselectedTime != null) {
                                boolean idMatch = preselectedTime.getId() != null && preselectedTime.getId().equals(slotRes.getId());
                                boolean timeMatch = preselectedTime.getTime() != null && preselectedTime.getTime().equals(formattedTime);
                                if (idMatch || timeMatch) {
                                    matchedPreselectedIdx = i;
                                }
                            }
                        }
                        timeSlots.add(new TimeSlotItem(
                                slotRes.getId(),
                                formattedTime,
                                isAvail ? "available" : "unavailable"
                        ));
                    }
                    appliedPreselectedTime = true;

                    date.setAvailable(availableCount);
                    date.setState(availableCount > 0 ? "open" : "full");

                    if (availableCount > 0) {
                        selectedSlotIndex = matchedPreselectedIdx >= 0 ? matchedPreselectedIdx : firstAvailableIndex;
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
            }

            @Override
            public void onFailure(Call<List<ClinicSlotResponse>> call, Throwable t) {
                if (isFinishing() || isDestroyed() || requestId != slotRequestSeq) return;

                timeSlots.clear();
                date.setAvailable(0);
                date.setState("full");
                selectedSlotIndex = -1;
                showNoSlotsState(date);
                binding.tvNoSlotsTitle.setText("Không thể kết nối máy chủ");
                setContinueEnabled(false, "Lỗi mạng khi tải khung giờ");
                Toast.makeText(DateTimeActivity.this, "Lỗi kết nối máy chủ khi tải khung giờ khám.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSlotsAvailableState(DateItem date, int availableCount) {
        binding.layoutSlotsAvailable.setVisibility(View.VISIBLE);
        binding.layoutNoSlotsState.setVisibility(View.GONE);

        binding.tvSelectedDateSubtitle.setText(date.getShortDate() + " đã chọn");
        binding.tvMorningHeader.setText("Khung giờ khám · " + availableCount + " chỗ trống");

        renderGroupedSlots();
    }

    private void showNoSlotsState(DateItem date) {
        binding.layoutSlotsAvailable.setVisibility(View.GONE);
        binding.layoutNoSlotsState.setVisibility(View.VISIBLE);

        binding.tvNoSlotsTitle.setText("Hết giờ khám ngày " + date.getShortDate());
        binding.tvSelectedDateSubtitle.setText(date.getShortDate() + " đã chọn");
        setContinueEnabled(false, "Chưa chọn khung giờ");
        updateJumpAvailableButton();
    }

    private void setContinueEnabled(boolean enabled, String summaryText) {
        if (summaryText != null) {
            binding.tvSelectedDateTimeSummary.setText(summaryText);
        }
        binding.btnContinue.setEnabled(enabled);
        binding.btnContinue.setAlpha(enabled ? 1.0f : 0.45f);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void renderGroupedSlots() {
        binding.layoutSlotsMorning.removeAllViews();
        binding.layoutSlotsAfternoon.removeAllViews();
        binding.layoutSlotsEvening.removeAllViews();
        slotViewMap.clear();

        List<Integer> morningIdx = new ArrayList<>();
        List<Integer> afternoonIdx = new ArrayList<>();
        List<Integer> eveningIdx = new ArrayList<>();

        for (int i = 0; i < timeSlots.size(); i++) {
            int hour = extractHour(timeSlots.get(i).getTime());
            if (hour < 12) {
                morningIdx.add(i);
            } else if (hour < 18) {
                afternoonIdx.add(i);
            } else {
                eveningIdx.add(i);
            }
        }

        renderSlotGroup(binding.sectionMorning, binding.layoutSlotsMorning, morningIdx);
        renderSlotGroup(binding.sectionAfternoon, binding.layoutSlotsAfternoon, afternoonIdx);
        renderSlotGroup(binding.sectionEvening, binding.layoutSlotsEvening, eveningIdx);

        updateSlotSelectionUI();
    }

    private void renderSlotGroup(View sectionView, LinearLayout gridContainer, List<Integer> indices) {
        if (indices.isEmpty()) {
            sectionView.setVisibility(View.GONE);
            return;
        }
        sectionView.setVisibility(View.VISIBLE);

        int columns = 4;
        int total = indices.size();
        int rows = (int) Math.ceil((double) total / columns);
        android.graphics.Typeface slotFont = ResourcesCompat.getFont(this, R.font.open_sans_semibold);

        for (int r = 0; r < rows; r++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            if (r > 0) {
                rowParams.topMargin = dpToPx(8);
            }
            rowLayout.setLayoutParams(rowParams);

            for (int c = 0; c < columns; c++) {
                int pos = r * columns + c;
                if (pos < total) {
                    final int slotIndex = indices.get(pos);
                    TimeSlotItem slot = timeSlots.get(slotIndex);

                    TextView tv = new TextView(this);
                    LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                            0,
                            dpToPx(42),
                            1.0f
                    );
                    if (c > 0) {
                        itemParams.setMarginStart(dpToPx(8));
                    }
                    tv.setLayoutParams(itemParams);
                    tv.setGravity(Gravity.CENTER);
                    tv.setText(slot.getTime());
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
                    tv.setFontFeatureSettings("tnum");
                    tv.setIncludeFontPadding(false);
                    if (slotFont != null) {
                        tv.setTypeface(slotFont);
                    }

                    if (slot.isAvailable()) {
                        tv.setOnClickListener(v -> selectTimeSlot(slotIndex));
                    } else {
                        tv.setOnClickListener(null);
                        tv.setClickable(false);
                    }

                    slotViewMap.put(slotIndex, tv);
                    rowLayout.addView(tv);
                } else {
                    View spacer = new View(this);
                    LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                            0,
                            dpToPx(42),
                            1.0f
                    );
                    if (c > 0) {
                        spacerParams.setMarginStart(dpToPx(8));
                    }
                    spacer.setLayoutParams(spacerParams);
                    spacer.setVisibility(View.INVISIBLE);
                    rowLayout.addView(spacer);
                }
            }
            gridContainer.addView(rowLayout);
        }
    }

    private void selectTimeSlot(int index) {
        if (index < 0 || index >= timeSlots.size() || !timeSlots.get(index).isAvailable()) return;
        selectedSlotIndex = index;
        updateSlotSelectionUI();
    }

    private void updateSlotSelectionUI() {
        for (Map.Entry<Integer, TextView> entry : slotViewMap.entrySet()) {
            int i = entry.getKey();
            TextView tv = entry.getValue();
            if (i < 0 || i >= timeSlots.size()) continue;

            TimeSlotItem slot = timeSlots.get(i);
            boolean isSelected = (i == selectedSlotIndex);

            if (!slot.isAvailable()) {
                tv.setBackgroundResource(R.drawable.bg_slot_unavailable);
                tv.setTextColor(ContextCompat.getColor(this, R.color.ds_text_secondary));
                tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tv.setAlpha(0.55f);
                UiMotionUtils.applyFont(tv, R.font.open_sans_medium);
            } else if (isSelected) {
                tv.setBackgroundResource(R.drawable.bg_slot_selected);
                tv.setTextColor(ContextCompat.getColor(this, R.color.white));
                tv.setPaintFlags(tv.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tv.setAlpha(1.0f);
                UiMotionUtils.applyFont(tv, R.font.open_sans_semibold);
            } else {
                tv.setBackgroundResource(R.drawable.bg_slot_available);
                tv.setTextColor(ContextCompat.getColor(this, R.color.ds_text_primary));
                tv.setPaintFlags(tv.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tv.setAlpha(1.0f);
                UiMotionUtils.applyFont(tv, R.font.open_sans_medium);
            }
        }

        if (selectedDateIndex >= 0 && selectedDateIndex < dates.size()
                && selectedSlotIndex >= 0 && selectedSlotIndex < timeSlots.size()
                && timeSlots.get(selectedSlotIndex).isAvailable()) {
            DateItem d = dates.get(selectedDateIndex);
            TimeSlotItem s = timeSlots.get(selectedSlotIndex);
            setContinueEnabled(true, d.getShortDate() + " · " + s.getTime());
        } else {
            setContinueEnabled(false, "Chưa chọn khung giờ");
        }
    }

    private void updateDateSelectionUI() {
        for (int i = 0; i < dateBindings.size(); i++) {
            ItemDateCardBinding b = dateBindings.get(i);
            boolean isSelected = (i == selectedDateIndex);

            if (isSelected) {
                b.layoutDateCard.setBackgroundResource(R.drawable.bg_date_card_selected);
                b.tvDateDay.setTextColor(ContextCompat.getColor(this, R.color.white));
                b.tvDateNum.setTextColor(ContextCompat.getColor(this, R.color.white));
            } else {
                b.layoutDateCard.setBackgroundResource(R.drawable.bg_date_card_default);
                b.tvDateDay.setTextColor(ContextCompat.getColor(this, R.color.ds_text_secondary));
                b.tvDateNum.setTextColor(ContextCompat.getColor(this, R.color.ds_text_primary));
            }
        }
    }

    private void updateJumpAvailableButton() {
        if (selectedDateIndex + 1 < dates.size()) {
            final int nextIdx = selectedDateIndex + 1;
            DateItem nextDate = dates.get(nextIdx);
            binding.btnJumpAvailableDate.setText("Xem ngày " + nextDate.getShortDate());
            binding.btnJumpAvailableDate.setOnClickListener(v -> selectDate(nextIdx));
        } else if (currentYearMonth.isBefore(maxMonth)) {
            binding.btnJumpAvailableDate.setText("Xem tháng sau");
            binding.btnJumpAvailableDate.setOnClickListener(v -> {
                currentYearMonth = currentYearMonth.plusMonths(1);
                loadMonthDates();
            });
        } else {
            binding.btnJumpAvailableDate.setText("Chọn ngày khác");
            binding.btnJumpAvailableDate.setOnClickListener(v -> {
                if (!dates.isEmpty()) selectDate(0);
            });
        }
    }

    private void setupEmptyStateActions() {
        binding.btnNotifyWhenAvailable.setOnClickListener(v -> {
            if (selectedDateIndex >= 0 && selectedDateIndex < dates.size()) {
                DateItem d = dates.get(selectedDateIndex);
                Toast.makeText(this, "Đã bật thông báo khi có khung giờ trống ngày " + d.getShortDate(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupStickyBottomBar() {
        if (isEditFromReview) {
            binding.btnContinue.setText("Lưu thay đổi");
        }
        binding.btnContinue.setOnClickListener(v -> {
            if (selectedDateIndex >= 0 && selectedDateIndex < dates.size()
                    && selectedSlotIndex >= 0 && selectedSlotIndex < timeSlots.size()
                    && timeSlots.get(selectedSlotIndex).isAvailable()) {
                DateItem d = dates.get(selectedDateIndex);
                TimeSlotItem s = timeSlots.get(selectedSlotIndex);
                if (isEditFromReview) {
                    Intent result = new Intent();
                    result.putExtra(EXTRA_SELECTED_DATE, d);
                    result.putExtra(EXTRA_SELECTED_TIME, s);
                    setResult(RESULT_OK, result);
                    finish();
                } else {
                    Intent intent = new Intent(this, com.example.petcareui.ui.pets.MyPetsActivity.class);
                    intent.putExtra(com.example.petcareui.ui.pets.MyPetsActivity.EXTRA_CLINIC, clinicItem);
                    intent.putExtra(com.example.petcareui.ui.pets.MyPetsActivity.EXTRA_SELECTED_SERVICE, serviceItem);
                    intent.putExtra(com.example.petcareui.ui.pets.MyPetsActivity.EXTRA_SELECTED_DATE, d);
                    intent.putExtra(com.example.petcareui.ui.pets.MyPetsActivity.EXTRA_SELECTED_TIME, s);
                    intent.putExtra(com.example.petcareui.ui.pets.MyPetsActivity.EXTRA_BOOKING_MODE, true);
                    if (preselectedPet != null) {
                        intent.putExtra(com.example.petcareui.ui.pets.MyPetsActivity.EXTRA_PRESELECTED_PET, preselectedPet);
                    }
                    startActivity(intent);
                }
            } else {
                Toast.makeText(this, "Vui lòng chọn khung giờ phù hợp", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
