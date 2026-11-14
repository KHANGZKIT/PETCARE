package com.example.petcareui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.petcareui.data.api.ApiClient;
import com.example.petcareui.data.model.UserProfileResponse;
import com.example.petcareui.databinding.ActivityMainBinding;
import com.example.petcareui.ui.auth.LoginActivity;
import com.example.petcareui.utils.TokenManager;
import com.example.petcareui.utils.UiMotionUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Xử lý chống tràn Status Bar & Navigation Bar
        UiMotionUtils.applyWindowInsets(binding.main, binding.layoutHomeBody, binding.bottomNavigation);

        tokenManager = TokenManager.getInstance(this);

        // Kiểm tra phiên đăng nhập
        if (!tokenManager.isLoggedIn()) {
            goToLogin();
            return;
        }

        displayUserInfo();
        refreshUserProfile(false);
        setupListeners();
        setupMotionEffects();
    }

    private void setupMotionEffects() {
        UiMotionUtils.animateEntranceSections(
                binding.sectionHeader,
                binding.sectionSearch,
                binding.bannerBooking,
                binding.sectionClinics,
                binding.sectionServices
        );

        UiMotionUtils.attachSpringTouch(
                binding.btnNotification,
                binding.btnAvatar,
                binding.btnSearchBar,
                binding.btnFilter,
                binding.bannerBooking,
                binding.btnBookNow,
                binding.cardClinicAnNhien,
                binding.cardClinicGiaDinh,
                binding.rowServiceCheckup,
                binding.rowServiceVaccine,
                binding.rowServiceDental,
                binding.chipAll,
                binding.chipNearby,
                binding.chipOpen,
                binding.chipEmergency,
                binding.chipSpa
        );
    }

    private void refreshUserProfile(boolean showDialogAfterLoad) {
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            goToLogin();
            return;
        }

        ApiClient.getAuthApi().getCurrentUser("Bearer " + token)
                .enqueue(new Callback<UserProfileResponse>() {
                    @Override
                    public void onResponse(Call<UserProfileResponse> call,
                                           Response<UserProfileResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            UserProfileResponse profile = response.body();
                            tokenManager.saveAuthData(
                                    token,
                                    profile.getId(),
                                    profile.getFullName(),
                                    profile.getEmail(),
                                    profile.getRole(),
                                    profile.getClinicId()
                            );
                            tokenManager.saveUserPhone(profile.getPhone());
                            displayUserInfo();
                            if (showDialogAfterLoad) {
                                showAccountDialog();
                            }
                        } else if (response.code() == 401 || response.code() == 403) {
                            Toast.makeText(MainActivity.this,
                                    "Phiên đăng nhập không hợp lệ hoặc đã hết hạn, vui lòng đăng nhập lại",
                                    Toast.LENGTH_LONG).show();
                            tokenManager.clear();
                            goToLogin();
                        } else if (response.code() == 404) {
                            if (showDialogAfterLoad) {
                                showProfileErrorDialog("Không thể tìm thấy thông tin tài khoản", false);
                            }
                        } else {
                            if (showDialogAfterLoad) {
                                showProfileErrorDialog("Không thể tải thông tin tài khoản. Vui lòng thực hiện lại thao tác.", true);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                        if (showDialogAfterLoad) {
                            showProfileErrorDialog("Lỗi kết nối mạng. Vui lòng khôi phục kết nối và tải lại thông tin tài khoản.", true);
                        }
                    }
                });
    }

    private void showProfileErrorDialog(String message, boolean allowRetry) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Thông báo")
                .setMessage(message)
                .setNegativeButton("Đóng", null);
        if (allowRetry) {
            builder.setPositiveButton("Tải lại", (dialog, which) -> refreshUserProfile(true));
        }
        builder.show();
    }

    private void displayUserInfo() {
        String name = tokenManager.getUserName();

        if (name != null && !name.isEmpty()) {
            binding.tvWelcomeName.setText(name);

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
            binding.tvWelcomeName.setText("Khách hàng");
            binding.tvAvatarInitials.setText("KH");
        }
    }

    private void setupListeners() {
        // 1. Thông báo & Avatar
        binding.btnNotification.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, com.example.petcareui.ui.notifications.NotificationsActivity.class);
            startActivity(intent);
        });

        binding.btnAvatar.setOnClickListener(v -> refreshUserProfile(true));

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
            binding.scrollContent.smoothScrollTo(0, 0);
        });

        binding.navAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, com.example.petcareui.ui.appointments.MyAppointmentsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_scale_out);
        });

        binding.navPets.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, com.example.petcareui.ui.pets.MyPetsActivity.class);
            intent.putExtra(com.example.petcareui.ui.pets.MyPetsActivity.EXTRA_BOOKING_MODE, false);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_scale_in, R.anim.fade_scale_out);
        });

        binding.navProfile.setOnClickListener(v -> refreshUserProfile(true));
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
                UiMotionUtils.animateSelectionPop(chip);
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
        String phone = tokenManager.getUserPhone();
        String phoneDisplay = (phone != null && !phone.isEmpty()) ? phone : "Chưa cập nhật";
        String role = tokenManager.getUserRole();

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Thông tin tài khoản")
                .setMessage("Họ và tên: " + tokenManager.getUserName()
                        + "\nEmail: " + tokenManager.getUserEmail()
                        + "\nSố điện thoại: " + phoneDisplay
                        + "\nVai trò: " + role)
                .setPositiveButton("Đóng", null)
                .setNegativeButton("Đăng xuất", (dialog, which) -> {
                    tokenManager.clear();
                    goToLogin();
                });

        if ("STAFF".equalsIgnoreCase(role)) {
            builder.setNeutralButton("Bảng nhân viên (S01)", (dialog, which) -> {
                Intent intent = new Intent(this, com.example.petcareui.ui.staff.StaffDashboardActivity.class);
                startActivity(intent);
            });
        }

        builder.show();
    }

    private void goToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
