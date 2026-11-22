package com.example.petcareui.ui.appointments;

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
import com.example.petcareui.data.model.AppointmentItem;
import com.example.petcareui.data.dto.response.AppointmentResponse;
import com.example.petcareui.databinding.ActivityMyAppointmentsBinding;
import com.example.petcareui.databinding.ItemAppointmentCardBinding;
import com.example.petcareui.ui.clinic.ClinicListActivity;
import com.example.petcareui.ui.pets.MyPetsActivity;
import com.example.petcareui.utils.UiMotionUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Màn hình C12: Lịch hẹn của tôi (My Appointments).
 * Bám sát nguyên mẫu React C12MyAppointments và kết nối REST API /api/v1/appointments/my.
 */
public class MyAppointmentsActivity extends AppCompatActivity {

    public static final String EXTRA_STATE = "extra_state";

    private ActivityMyAppointmentsBinding binding;
    private final List<AppointmentItem> upcomingList = new ArrayList<>();
    private final List<AppointmentItem> pastList = new ArrayList<>();
    private final List<AppointmentItem> allList = new ArrayList<>();
    private String currentTab = "upcoming";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyAppointmentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Chống đè Status Bar và cắt chữ Bottom Nav
        UiMotionUtils.applyWindowInsets(binding.getRoot(), binding.layoutTopHeader, binding.layoutBottomNav);

        extractIntentData();
        setupTopBar();
        setupFilterChips();
        setupBottomNav();
        loadAppointmentsFromApi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAppointmentsFromApi();
    }

    private void extractIntentData() {
        if (getIntent().hasExtra(EXTRA_STATE)) {
            currentTab = getIntent().getStringExtra(EXTRA_STATE);
        }
    }

    private void loadAppointmentsFromApi() {
        ApiClient.getAppointmentApi().getMyAppointments().enqueue(new Callback<List<AppointmentResponse>>() {
            @Override
            public void onResponse(Call<List<AppointmentResponse>> call, Response<List<AppointmentResponse>> response) {
                upcomingList.clear();
                pastList.clear();
                allList.clear();

                if (response.isSuccessful() && response.body() != null) {
                    List<AppointmentResponse> body = response.body();
                    for (AppointmentResponse res : body) {
                        AppointmentItem item = res.toAppointmentItem();
                        allList.add(item);
                        if (item.isUpcoming()) {
                            upcomingList.add(item);
                        } else {
                            pastList.add(item);
                        }
                    }

                    binding.tvHeaderSubtitle.setText(upcomingList.size() + " sắp tới · " + pastList.size() + " đã qua");
                    binding.chipUpcoming.setText("Sắp tới · " + upcomingList.size());
                    binding.chipPast.setText("Đã qua · " + pastList.size());

                    if (allList.isEmpty()) {
                        renderTab("empty");
                    } else {
                        renderTab(currentTab);
                    }
                } else {
                    renderTab("empty");
                    Toast.makeText(MyAppointmentsActivity.this, "Không thể tải danh sách lịch hẹn", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AppointmentResponse>> call, Throwable t) {
                upcomingList.clear();
                pastList.clear();
                allList.clear();
                renderTab("empty");
                Toast.makeText(MyAppointmentsActivity.this, "Lỗi kết nối máy chủ khi tải lịch hẹn.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTopBar() {
        binding.tvHeaderSubtitle.setText(upcomingList.size() + " sắp tới · " + pastList.size() + " đã qua");

        binding.btnNewBooking.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClinicListActivity.class);
            startActivity(intent);
        });

        binding.tvSeeAllPastAppts.setOnClickListener(v -> renderTab("past"));

        binding.btnEmptyFindClinic.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClinicListActivity.class);
            startActivity(intent);
        });

        UiMotionUtils.attachSpringTouch(
                binding.btnNewBooking,
                binding.btnEmptyFindClinic,
                binding.chipUpcoming,
                binding.chipPast,
                binding.chipAll
        );
    }

    private void setupFilterChips() {
        binding.chipUpcoming.setText("Sắp tới · " + upcomingList.size());
        binding.chipPast.setText("Đã qua · " + pastList.size());
        binding.chipAll.setText("Tất cả");

        binding.chipUpcoming.setOnClickListener(v -> {
            UiMotionUtils.animateSelectionPop(binding.chipUpcoming);
            renderTab("upcoming");
        });
        binding.chipPast.setOnClickListener(v -> {
            UiMotionUtils.animateSelectionPop(binding.chipPast);
            renderTab("past");
        });
        binding.chipAll.setOnClickListener(v -> {
            UiMotionUtils.animateSelectionPop(binding.chipAll);
            renderTab("all");
        });

        binding.chipAll.setOnLongClickListener(v -> {
            renderTab("empty");
            Toast.makeText(this, "Xem trước Trạng thái rỗng (Empty State)", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void renderTab(String tab) {
        currentTab = tab;

        updateChipStyle(binding.chipUpcoming, "upcoming".equals(tab));
        updateChipStyle(binding.chipPast, "past".equals(tab));
        updateChipStyle(binding.chipAll, "all".equals(tab));

        binding.layoutApptList.removeAllViews();
        binding.layoutPastPreviewList.removeAllViews();

        if ("empty".equals(tab) || allList.isEmpty()) {
            binding.tvHeaderSubtitle.setVisibility(View.GONE);
            binding.layoutApptList.setVisibility(View.GONE);
            binding.layoutPastSectionInUpcoming.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
            return;
        }

        binding.tvHeaderSubtitle.setVisibility(View.VISIBLE);
        binding.layoutEmptyState.setVisibility(View.GONE);
        binding.layoutApptList.setVisibility(View.VISIBLE);

        if ("upcoming".equals(tab)) {
            for (AppointmentItem appt : upcomingList) {
                View cardView = createAppointmentCardView(appt);
                binding.layoutApptList.addView(cardView);
            }

            if (!pastList.isEmpty()) {
                binding.layoutPastSectionInUpcoming.setVisibility(View.VISIBLE);
                int previewCount = Math.min(2, pastList.size());
                for (int i = 0; i < previewCount; i++) {
                    View cardView = createAppointmentCardView(pastList.get(i));
                    binding.layoutPastPreviewList.addView(cardView);
                }
                UiMotionUtils.animateStaggeredChildren(binding.layoutPastPreviewList, 120);
            } else {
                binding.layoutPastSectionInUpcoming.setVisibility(View.GONE);
            }
        } else if ("past".equals(tab)) {
            binding.layoutPastSectionInUpcoming.setVisibility(View.GONE);
            for (AppointmentItem appt : pastList) {
                View cardView = createAppointmentCardView(appt);
                binding.layoutApptList.addView(cardView);
            }
        } else {
            binding.layoutPastSectionInUpcoming.setVisibility(View.GONE);
            for (AppointmentItem appt : allList) {
                View cardView = createAppointmentCardView(appt);
                binding.layoutApptList.addView(cardView);
            }
        }

        UiMotionUtils.animateStaggeredChildren(binding.layoutApptList, 20);
    }

    private void updateChipStyle(TextView chip, boolean active) {
        if (active) {
            chip.setBackgroundResource(R.drawable.bg_chip_active);
            chip.setTextColor(ContextCompat.getColor(this, R.color.ds_primary));
            UiMotionUtils.applyFont(chip, R.font.open_sans_semibold);
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_inactive);
            chip.setTextColor(ContextCompat.getColor(this, R.color.ds_text_secondary));
            UiMotionUtils.applyFont(chip, R.font.open_sans_medium);
        }
    }

    private View createAppointmentCardView(AppointmentItem item) {
        ItemAppointmentCardBinding cardBinding = ItemAppointmentCardBinding.inflate(getLayoutInflater());

        applyBadgeStatus(cardBinding.tvApptStatus, cardBinding.viewStatusAccent, item.getStatus());

        int avatarRes = item.getPetImgRes();
        cardBinding.ivPetImage.setImageResource(avatarRes);
        if (avatarRes == R.drawable.img_pet_bong) {
            cardBinding.ivPetImage.setPadding(0, 0, 0, 0);
            cardBinding.ivPetImage.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        } else {
            int pad = Math.round(4 * getResources().getDisplayMetrics().density);
            cardBinding.ivPetImage.setPadding(pad, pad, pad, pad);
            cardBinding.ivPetImage.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
        }

        cardBinding.tvServiceTitle.setText(item.getService());
        cardBinding.tvPetAndClinic.setText(item.getPet() + " · " + formatShortClinicName(item.getClinic()));
        cardBinding.tvApptDate.setText(formatNaturalDateTime(item.getDate(), item.getTime()));

        UiMotionUtils.attachSpringTouch(cardBinding.getRoot());

        cardBinding.getRoot().setOnClickListener(v -> {
            Intent intent = new Intent(this, AppointmentDetailsActivity.class);
            intent.putExtra(AppointmentDetailsActivity.EXTRA_APPT, item);
            intent.putExtra(AppointmentDetailsActivity.EXTRA_REF, item.getRef());
            startActivity(intent);
        });

        return cardBinding.getRoot();
    }

    private String formatShortClinicName(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "Phòng khám thú y";
        String s = raw.trim();
        if (s.toLowerCase(java.util.Locale.ROOT).startsWith("phòng khám thú y ")) {
            return "Thú y " + s.substring("Phòng khám Thú y ".length()).trim();
        }
        return s;
    }

    private String formatNaturalDateTime(String rawDate, String rawTime) {
        String datePart = rawDate != null ? rawDate.trim() : "";
        try {
            if (datePart.length() >= 10 && datePart.charAt(4) == '-' && datePart.charAt(7) == '-') {
                java.time.LocalDate parsed = java.time.LocalDate.parse(datePart.substring(0, 10));
                String dow;
                switch (parsed.getDayOfWeek()) {
                    case MONDAY: dow = "T2"; break;
                    case TUESDAY: dow = "T3"; break;
                    case WEDNESDAY: dow = "T4"; break;
                    case THURSDAY: dow = "T5"; break;
                    case FRIDAY: dow = "T6"; break;
                    case SATURDAY: dow = "T7"; break;
                    default: dow = "CN"; break;
                }
                datePart = String.format(java.util.Locale.US, "%s, %02d/%02d",
                        dow, parsed.getDayOfMonth(), parsed.getMonthValue());
            }
        } catch (Exception ignored) {
        }

        String timePart = rawTime != null ? rawTime.trim() : "";
        if (timePart.length() > 5 && timePart.charAt(2) == ':') {
            timePart = timePart.substring(0, 5);
        }

        if (!datePart.isEmpty() && !timePart.isEmpty()) {
            return datePart + " · " + timePart;
        }
        return !datePart.isEmpty() ? datePart : timePart;
    }

    private void applyBadgeStatus(TextView tvBadge, View viewAccent, String status) {
        if ("CONFIRMED".equalsIgnoreCase(status)) {
            tvBadge.setText("● Đã xác nhận");
            tvBadge.setTextColor(ContextCompat.getColor(this, R.color.ds_success));
        } else if ("COMPLETED".equalsIgnoreCase(status)) {
            tvBadge.setText("● Đã hoàn thành");
            tvBadge.setTextColor(ContextCompat.getColor(this, R.color.ds_success));
        } else if ("NO_SHOW".equalsIgnoreCase(status)) {
            tvBadge.setText("● Không đến");
            tvBadge.setTextColor(ContextCompat.getColor(this, R.color.status_info));
        } else if ("CANCELLED".equalsIgnoreCase(status)) {
            tvBadge.setText("● Đã hủy");
            tvBadge.setTextColor(ContextCompat.getColor(this, R.color.ds_text_secondary));
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            tvBadge.setText("● Đã từ chối");
            tvBadge.setTextColor(ContextCompat.getColor(this, R.color.ds_error));
        } else {
            tvBadge.setText("● Chờ xác nhận");
            tvBadge.setTextColor(ContextCompat.getColor(this, R.color.ds_warning));
        }
    }

    private void setupBottomNav() {
        binding.navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        binding.navClinics.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClinicListActivity.class);
            intent.putExtra(ClinicListActivity.EXTRA_TAB_MODE, true);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        binding.navAppointments.setOnClickListener(v ->
                binding.scrollViewContent.smoothScrollTo(0, 0));

        binding.navPets.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyPetsActivity.class);
            intent.putExtra(MyPetsActivity.EXTRA_BOOKING_MODE, false);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });
    }
}
