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
import com.example.petcareui.data.model.AppointmentItem;
import com.example.petcareui.databinding.ActivityMyAppointmentsBinding;
import com.example.petcareui.databinding.ItemAppointmentCardBinding;
import com.example.petcareui.ui.clinic.ClinicListActivity;
import com.example.petcareui.ui.pets.MyPetsActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình C12: Lịch hẹn của tôi (My Appointments).
 * Bám sát nguyên mẫu React C12MyAppointments và Design System Spec.
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

        initMockData();
        extractIntentData();
        setupTopBar();
        setupFilterChips();
        setupBottomNav();
        renderTab(currentTab);
    }

    private void extractIntentData() {
        if (getIntent().hasExtra(EXTRA_STATE)) {
            currentTab = getIntent().getStringExtra(EXTRA_STATE);
        }
    }

    private void initMockData() {
        // Dữ liệu chuẩn theo data.js (APPTS)
        // 1. Sắp tới (Upcoming)
        upcomingList.add(new AppointmentItem(
                "PC-48213", "Phòng khám Thú y An Nhiên", "Quận 1",
                "Mochi", R.drawable.img_pet_mochi, "Tiêm phòng",
                "Thứ 2, 27/10/2026", "10:00", "PENDING",
                "250.000 ₫", "Gửi cách đây 2 giờ", "Mochi hơi mẩn nhẹ sau mũi tiêm lần trước."
        ));

        upcomingList.add(new AppointmentItem(
                "PC-47980", "Bệnh viện Thú y Gia Định", "Quận 2",
                "Bông", R.drawable.img_pet_bong, "Cạo vôi & chăm sóc răng",
                "Thứ 4, 29/10/2026", "14:00", "CONFIRMED",
                "600.000 ₫", "Phòng khám đã xác nhận", null
        ));

        upcomingList.add(new AppointmentItem(
                "PC-47155", "Phòng khám Thú y An Nhiên", "Quận 1",
                "Lựu", R.drawable.img_pet_luu, "Khám tổng quát",
                "Thứ 6, 31/10/2026", "16:30", "PENDING",
                "180.000 ₫", "Gửi hôm qua", null
        ));

        // 2. Đã qua (Past)
        pastList.add(new AppointmentItem(
                "PC-46902", "Spa & Phòng khám Thú cưng Mimi", "Quận 7",
                "Mochi", R.drawable.img_pet_mochi, "Tắm & cắt tỉa",
                "Thứ 7, 11/10/2026", "11:00", "COMPLETED",
                "320.000 ₫", "Đã hoàn thành lượt khám", null
        ));

        pastList.add(new AppointmentItem(
                "PC-46410", "Phòng khám Thú cưng Bình Thạnh", "Bình Thạnh",
                "Bông", R.drawable.img_pet_bong, "Khám định kỳ cho bé lớn tuổi",
                "Thứ 5, 25/09/2026", "09:30", "NO_SHOW",
                "780.000 ₫", "Bé không đến hẹn", null
        ));

        pastList.add(new AppointmentItem(
                "PC-45877", "Bệnh viện Thú y Gia Định", "Quận 2",
                "Lựu", R.drawable.img_pet_luu, "Tư vấn da & dị ứng",
                "Thứ 3, 09/09/2026", "15:00", "CANCELLED",
                "420.000 ₫", "Bạn đã hủy lịch hẹn", null
        ));

        pastList.add(new AppointmentItem(
                "PC-45220", "Spa & Phòng khám Thú cưng Mimi", "Quận 7",
                "Mochi", R.drawable.img_pet_mochi, "Tiêm phòng",
                "Thứ 2, 18/08/2026", "10:30", "REJECTED",
                "250.000 ₫", "Hết chỗ vào khung giờ này", null
        ));

        allList.addAll(upcomingList);
        allList.addAll(pastList);
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

        // Nhấn giữ nút Tất cả để thử nghiệm trạng thái rỗng (Empty State) theo thiết kế
        binding.chipAll.setOnLongClickListener(v -> {
            renderTab("empty");
            Toast.makeText(this, "Xem trước Trạng thái rỗng (Empty State)", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void renderTab(String tab) {
        currentTab = tab;

        // Cập nhật trạng thái hiển thị của các Chip
        updateChipStyle(binding.chipUpcoming, "upcoming".equals(tab));
        updateChipStyle(binding.chipPast, "past".equals(tab));
        updateChipStyle(binding.chipAll, "all".equals(tab));

        binding.layoutApptList.removeAllViews();
        binding.layoutPastPreviewList.removeAllViews();

        if ("empty".equals(tab)) {
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
            // Danh sách lịch hẹn sắp tới
            for (AppointmentItem appt : upcomingList) {
                View cardView = createAppointmentCardView(appt);
                binding.layoutApptList.addView(cardView);
            }

            // Hiển thị phần "Lịch khám đã qua" (tối đa 2 thẻ thu gọn)
            binding.layoutPastSectionInUpcoming.setVisibility(View.VISIBLE);
            int previewCount = Math.min(2, pastList.size());
            for (int i = 0; i < previewCount; i++) {
                View cardView = createAppointmentCardView(pastList.get(i));
                binding.layoutPastPreviewList.addView(cardView);
            }
        } else if ("past".equals(tab)) {
            // Toàn bộ danh sách lịch khám đã qua
            binding.layoutPastSectionInUpcoming.setVisibility(View.GONE);
            for (AppointmentItem appt : pastList) {
                View cardView = createAppointmentCardView(appt);
                binding.layoutApptList.addView(cardView);
            }
        } else {
            // Tất cả lịch hẹn
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

        // Mã lịch hẹn
        cardBinding.tvApptRef.setText(item.getRef());

        // Huy hiệu trạng thái
        applyBadgeStatus(cardBinding.tvApptStatus, item.getStatus());

        // Ảnh thú cưng
        cardBinding.ivPetImage.setImageResource(item.getPetImgRes());

        // Dịch vụ & Phòng khám
        cardBinding.tvServiceTitle.setText(item.getService());
        cardBinding.tvPetAndClinic.setText(item.getPet() + " · " + item.getClinic());

        // Thời gian hẹn
        cardBinding.tvApptDate.setText(item.getDate());
        cardBinding.tvApptTime.setText(item.getTime());

        // Click thẻ -> Mở Chi tiết lịch hẹn (C13)
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
            // Mặc định PENDING
            tvBadge.setText("• Chờ xác nhận");
            tvBadge.setBackgroundResource(R.drawable.bg_badge_pending);
            tvBadge.setTextColor(ContextCompat.getColor(this, R.color.status_warning_ink));
        }
    }

    private void setupBottomNav() {
        // Tab 1: Trang chủ
        binding.navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Tab 2: Lịch hẹn (đang ở màn này)
        binding.navAppointments.setOnClickListener(v -> {
            binding.scrollViewContent.smoothScrollTo(0, 0);
        });

        // Tab 3: Thú cưng
        binding.navPets.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyPetsActivity.class);
            intent.putExtra(MyPetsActivity.EXTRA_BOOKING_MODE, false);
            startActivity(intent);
        });

        // Tab 4: Tài khoản
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
