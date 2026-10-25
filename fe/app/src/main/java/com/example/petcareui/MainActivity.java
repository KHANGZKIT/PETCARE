package com.example.petcareui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.petcareui.databinding.ActivityMainBinding;
import com.example.petcareui.ui.auth.LoginActivity;
import com.example.petcareui.utils.TokenManager;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);

        // Kiểm tra phiên đăng nhập
        if (!tokenManager.isLoggedIn()) {
            goToLogin();
            return;
        }

        displayUserInfo();
        setupListeners();
    }

    private void displayUserInfo() {
        String name = tokenManager.getUserName();

        if (name != null && !name.isEmpty()) {
            binding.tvWelcomeName.setText(name);

            // Tạo chữ viết tắt cho Avatar (ví dụ "Nguyễn Lan" -> "NL")
            String[] parts = name.trim().split("\\s+");
            StringBuilder initials = new StringBuilder();
            if (parts.length > 0 && !parts[0].isEmpty()) {
                initials.append(parts[0].charAt(0));
            }
            if (parts.length > 1 && !parts[parts.length - 1].isEmpty()) {
                initials.append(parts[parts.length - 1].charAt(0));
            }
            binding.tvAvatarInitials.setText(initials.toString().toUpperCase());
        } else {
            binding.tvWelcomeName.setText("Nguyễn Lan");
            binding.tvAvatarInitials.setText("NL");
        }
    }

    private void setupListeners() {
        // 1. Thông báo & Avatar
        binding.btnNotification.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, com.example.petcareui.ui.notifications.NotificationsActivity.class);
            startActivity(intent);
        });

        binding.btnAvatar.setOnClickListener(v -> showAccountDialog());

        // 2. Tìm kiếm & Lọc -> Mở C04 Danh sách phòng khám
        binding.btnSearchBar.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, com.example.petcareui.ui.clinic.ClinicListActivity.class));
        });

        binding.btnFilter.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, com.example.petcareui.ui.clinic.ClinicListActivity.class));
        });

        // 3. Chip bộ lọc nhanh
        setupFilterChips();

        // 4. Banner Đặt lịch khám -> Mở C04
        View.OnClickListener bookingListener = v -> {
            startActivity(new Intent(MainActivity.this, com.example.petcareui.ui.clinic.ClinicListActivity.class));
        };
        binding.bannerBooking.setOnClickListener(bookingListener);
        binding.btnBookNow.setOnClickListener(bookingListener);

        // 5. Phòng khám gần bạn
        binding.tvSeeAllClinics.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, com.example.petcareui.ui.clinic.ClinicListActivity.class));
        });

        binding.cardClinicAnNhien.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, com.example.petcareui.ui.clinic.ClinicDetailsActivity.class);
            startActivity(intent);
        });

        binding.cardClinicGiaDinh.setOnClickListener(v -> {
            com.example.petcareui.data.model.ClinicItem giaDinh = new com.example.petcareui.data.model.ClinicItem(
                    "cl-02", "Bệnh viện Thú y Gia Định", "128 Trần Não, Quận 2", 4.6, 158, "3,4 km", true, "Thứ 2 – CN · 07:30 – 21:00",
                    "+84 28 3744 2210",
                    "Bệnh viện mở giờ kéo dài, có tiếp nhận cấp cứu, chẩn đoán hình ảnh và phòng xét nghiệm tại chỗ.",
                    "24/7",
                    "Tiếng Việt, tiếng Anh"
            );
            Intent intent = new Intent(MainActivity.this, com.example.petcareui.ui.clinic.ClinicDetailsActivity.class);
            intent.putExtra(com.example.petcareui.ui.clinic.ClinicDetailsActivity.EXTRA_CLINIC, giaDinh);
            startActivity(intent);
        });

        // 6. Dịch vụ phổ biến -> Mở C06 Chọn dịch vụ
        View.OnClickListener openServiceListener = v -> {
            Intent intent = new Intent(MainActivity.this, com.example.petcareui.ui.booking.SelectServiceActivity.class);
            startActivity(intent);
        };
        binding.tvSeeAllServices.setOnClickListener(openServiceListener);
        binding.rowServiceCheckup.setOnClickListener(openServiceListener);
        binding.rowServiceVaccine.setOnClickListener(openServiceListener);
        binding.rowServiceDental.setOnClickListener(openServiceListener);

        // 7. Bottom Navigation Tabs
        binding.navHome.setOnClickListener(v -> {
            // Đang ở Trang chủ
        });

        binding.navAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, com.example.petcareui.ui.appointments.MyAppointmentsActivity.class);
            startActivity(intent);
        });

        binding.navPets.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, com.example.petcareui.ui.pets.MyPetsActivity.class);
            intent.putExtra(com.example.petcareui.ui.pets.MyPetsActivity.EXTRA_BOOKING_MODE, false);
            startActivity(intent);
        });

        binding.navProfile.setOnClickListener(v -> showAccountDialog());
    }

    private void setupFilterChips() {
        TextView[] chips = new TextView[]{
                binding.chipAll,
                binding.chipNearby,
                binding.chipOpen,
                binding.chipEmergency,
                binding.chipSpa
        };

        for (TextView chip : chips) {
            chip.setOnClickListener(v -> {
                for (TextView c : chips) {
                    if (c == chip) {
                        c.setBackgroundResource(R.drawable.bg_chip_active);
                        c.setTextColor(ContextCompat.getColor(this, R.color.white));
                    } else {
                        c.setBackgroundResource(R.drawable.bg_chip_inactive);
                        c.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
                    }
                }
            });
        }
    }

    private void showAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Tài khoản của bạn")
                .setMessage("Tên: " + tokenManager.getUserName() + "\nEmail: " + tokenManager.getUserEmail() + "\nVai trò: " + tokenManager.getUserRole())
                .setNeutralButton("Bảng nhân viên (S01)", (dialog, which) -> {
                    Intent intent = new Intent(this, com.example.petcareui.ui.staff.StaffDashboardActivity.class);
                    startActivity(intent);
                })
                .setPositiveButton("Đóng", null)
                .setNegativeButton("Đăng xuất", (dialog, which) -> {
                    tokenManager.clear();
                    goToLogin();
                })
                .show();
    }

    private void goToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}