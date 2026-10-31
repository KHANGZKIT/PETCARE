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
import com.example.petcareui.data.model.AppointmentResponse;
import com.example.petcareui.databinding.ActivityMyAppointmentsBinding;
import com.example.petcareui.databinding.ItemAppointmentCardBinding;
import com.example.petcareui.ui.clinic.ClinicListActivity;
import com.example.petcareui.ui.pets.MyPetsActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Màn hình C12: Lịch hẹn của tôi (My Appointments).
 * Bám sát nguyên mẫu React C12MyAppointments và kết nối REST API /api/v1/appointments/my.
 * Hỗ trợ chuyển đổi tab Sắp tới, Đã qua, Tất cả và Trạng thái rỗng (Empty State).
 */
public class MyAppointmentsActivity extends AppCompatActivity {

    public static final String EXTRA_STATE = "extra_state"; // "upcoming", "past", "empty"

    private ActivityMyAppointmentsBinding binding;
    private final List<AppointmentItem> upcomingList = new ArrayList<>();
    private final List<AppointmentItem> pastList = new ArrayList<>();
    private final List<AppointmentItem> allList = new ArrayList<>();
    private String currentTab = "upcoming"; // "upcoming", "past", "all", "empty"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyAppointmentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
                if (response.isSuccessful() && response.body() != null) {
                    List<AppointmentResponse> body = response.body();
                    upcomingList.clear();
                    pastList.clear();
                    allList.clear();

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
                    if (allList.isEmpty()) {
                        initMockData();
                        renderTab(currentTab);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<AppointmentResponse>> call, Throwable t) {
                if (allList.isEmpty()) {
                    initMockData();
                    renderTab(currentTab);
                }
            }
        });
    }

    private void initMockData() {
        upcomingList.clear();
        pastList.clear();
        allList.clear();

        upcomingList.add(new AppointmentItem(
                1L, "PC-48213", "Phòng khám Thú y An Nhiên", "Quận 1",
                "Mochi", R.drawable.img_pet_mochi, "Tiêm phòng",
                "Thứ 2, 27/10/2026", "10:00", "PENDING",
                "250.000 ₫", "Gửi cách đây 2 giờ", "Mochi hơi mẩn nhẹ sau mũi tiêm lần trước."
        ));

        upcomingList.add(new AppointmentItem(
                2L, "PC-47980", "Bệnh viện Thú y Gia Định", "Quận 2",
                "Bông", R.drawable.img_pet_bong, "Cạo vôi & chăm sóc răng",
                "Thứ 4, 29/10/2026", "14:00", "CONFIRMED",
                "600.000 ₫", "Phòng khám đã xác nhận", null
        ));

        pastList.add(new AppointmentItem(
                3L, "PC-46902", "Spa & Phòng khám Thú cưng Mimi", "Quận 7",
                "Mochi", R.drawable.img_pet_mochi, "Tắm & cắt tỉa",
                "Thứ 7, 11/10/2026", "11:00", "COMPLETED",
                "320.000 ₫", "Đã hoàn thành lượt khám", null
        ));

        pastList.add(new AppointmentItem(
                4L, "PC-45877", "Bệnh viện Thú y Gia Định", "Quận 2",
                "Lựu", R.drawable.img_pet_luu, "Tư vấn da & dị ứng",
                "Thứ 3, 09/09/2026", "15:00", "CANCELLED",
                "420.000 ₫", "Bạn đã hủy lịch hẹn", null
        ));

        allList.addAll(upcomingList);
        allList.addAll(pastList);

        binding.tvHeaderSubtitle.setText(upcomingList.size() + " sắp tới · " + pastList.size() + " đã qua");
        binding.chipUpcoming.setText("Sắp tới · " + upcomingList.size());
        binding.chipPast.setText("Đã qua · " + pastList.size());
    }

    private void setupTopBar() {
        binding.tvHeaderSubtitle.setText(upcomingList.size() + " sắp tới · " + pastList.size() + " đã qua");

        // Nút + Đặt lịch -> C04 Danh sách phòng khám
        binding.btnNewBooking.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClinicListActivity.class);
            startActivity(intent);
        });

        // Nút Xem tất cả trong phần Đã qua thu gọn
        binding.tvSeeAllPastAppts.setOnClickListener(v -> renderTab("past"));

        // Nút Tìm phòng khám khi ở trạng thái Rỗng
        binding.btnEmptyFindClinic.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClinicListActivity.class);
            startActivity(intent);
        });
    }

    private void setupFilterChips() {
        binding.chipUpcoming.setText("Sắp tới · " + upcomingList.size());
        binding.chipPast.setText("Đã qua · " + pastList.size());
        binding.chipAll.setText("Tất cả");

        binding.chipUpcoming.setOnClickListener(v -> renderTab("upcoming"));
        binding.chipPast.setOnClickListener(v -> renderTab("past"));
        binding.chipAll.setOnClickListener(v -> renderTab("all"));

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
    }

    private void updateChipStyle(TextView chip, boolean active) {
        if (active) {
            chip.setBackgroundResource(R.drawable.bg_chip_active);
            chip.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_inactive);
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
        }
    }

    private View createAppointmentCardView(AppointmentItem item) {
        ItemAppointmentCardBinding cardBinding = ItemAppointmentCardBinding.inflate(getLayoutInflater());

        cardBinding.tvApptRef.setText(item.getRef());
        applyBadgeStatus(cardBinding.tvApptStatus, item.getStatus());
        cardBinding.ivPetImage.setImageResource(item.getPetImgRes());
        cardBinding.tvServiceTitle.setText(item.getService());
        cardBinding.tvPetAndClinic.setText(item.getPet() + " · " + item.getClinic());
        cardBinding.tvApptDate.setText(item.getDate());
        cardBinding.tvApptTime.setText(item.getTime());

        cardBinding.getRoot().setOnClickListener(v -> {
            Intent intent = new Intent(this, AppointmentDetailsActivity.class);
            intent.putExtra(AppointmentDetailsActivity.EXTRA_APPT, item);
            intent.putExtra(AppointmentDetailsActivity.EXTRA_REF, item.getRef());
            startActivity(intent);
        });

        return cardBinding.getRoot();
    }

    private void applyBadgeStatus(TextView tvBadge, String status) {
        if ("CONFIRMED".equalsIgnoreCase(status)) {
            tvBadge.setText("• Đã xác nhận");
            tvBadge.setBackgroundResource(R.drawable.bg_badge_confirmed);
            tvBadge.setTextColor(ContextCompat.getColor(this, R.color.status_success_ink));
        } else if ("COMPLETED".equalsIgnoreCase(status)) {
            tvBadge.setText("• Đã hoàn thành");
            tvBadge.setBackgroundResource(R.drawable.bg_badge_confirmed);
            tvBadge.setTextColor(ContextCompat.getColor(this, R.color.status_success_ink));
        } else if ("NO_SHOW".equalsIgnoreCase(status)) {
            tvBadge.setText("• Không đến");
            tvBadge.setBackgroundResource(R.drawable.bg_badge_no_show);
            tvBadge.setTextColor(ContextCompat.getColor(this, R.color.status_info_ink));
        } else if ("CANCELLED".equalsIgnoreCase(status)) {
            tvBadge.setText("• Đã hủy");
            tvBadge.setBackgroundResource(R.drawable.bg_badge_cancelled);
            tvBadge.setTextColor(ContextCompat.getColor(this, R.color.status_neutral_ink));
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            tvBadge.setText("• Đã từ chối");
            tvBadge.setBackgroundResource(R.drawable.bg_badge_rejected);
            tvBadge.setTextColor(ContextCompat.getColor(this, R.color.status_destructive_ink));
        } else {
            tvBadge.setText("• Chờ xác nhận");
            tvBadge.setBackgroundResource(R.drawable.bg_badge_pending);
            tvBadge.setTextColor(ContextCompat.getColor(this, R.color.status_warning_ink));
        }
    }

    private void setupBottomNav() {
        binding.navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        binding.navAppointments.setOnClickListener(v -> {
            binding.scrollViewContent.smoothScrollTo(0, 0);
        });

        binding.navPets.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyPetsActivity.class);
            intent.putExtra(MyPetsActivity.EXTRA_BOOKING_MODE, false);
            startActivity(intent);
        });

        binding.navProfile.setOnClickListener(v -> showAccountDialog());
    }

    private void showAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Tài khoản Pet Care")
                .setMessage("Khách hàng: Nguyễn Lan\nEmail: lan.nguyen@example.com\nSố điện thoại: +84 90 123 4567\nThành viên từ tháng 3/2026")
                .setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
