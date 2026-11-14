package com.example.petcareui.ui.profile;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petcareui.MainActivity;
import com.example.petcareui.data.api.ApiClient;
import com.example.petcareui.data.dto.response.UserProfileResponse;
import com.example.petcareui.databinding.ActivityProfileBinding;
import com.example.petcareui.databinding.DialogLogoutConfirmationBinding;
import com.example.petcareui.ui.appointments.MyAppointmentsActivity;
import com.example.petcareui.ui.auth.LoginActivity;
import com.example.petcareui.ui.pets.MyPetsActivity;
import com.example.petcareui.ui.staff.StaffDashboardActivity;
import com.example.petcareui.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);

        if (!tokenManager.isLoggedIn()) {
            goToLogin();
            return;
        }

        displayCachedUserInfo();
        setupListeners();
        setupBottomNav();
        fetchLiveProfile();
    }

    private void displayCachedUserInfo() {
        String name = tokenManager.getUserName();
        String email = tokenManager.getUserEmail();
        String phone = tokenManager.getUserPhone();
        String role = tokenManager.getUserRole();
        Long userId = tokenManager.getUserId();

        // 1. Tên & Email
        binding.tvProfileName.setText(name != null && !name.isEmpty() ? name : "Khách hàng");
        binding.tvProfileEmail.setText(email != null && !email.isEmpty() ? email : "Chưa cập nhật email");

        // 2. Chi tiết
        binding.tvDetailFullName.setText(name != null && !name.isEmpty() ? name : "Chưa cập nhật");
        binding.tvDetailEmail.setText(email != null && !email.isEmpty() ? email : "Chưa cập nhật");
        binding.tvDetailPhone.setText(phone != null && !phone.isEmpty() ? phone : "Chưa cập nhật");
        binding.tvDetailUserId.setText(userId != null ? "#" + userId : "#--");

        // 3. Avatar initials (ví dụ: "Lê Thị Thu Hà" -> "LH")
        if (name != null && !name.trim().isEmpty()) {
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
            binding.tvAvatarInitials.setText("KH");
        }

        // 4. Vai trò (Role)
        boolean isStaff = "STAFF".equalsIgnoreCase(role);
        if (isStaff) {
            binding.tvProfileRole.setText("Nhân viên phòng khám");
            binding.rowStaffPortal.setVisibility(View.VISIBLE);
        } else {
            binding.tvProfileRole.setText("Khách hàng");
            binding.rowStaffPortal.setVisibility(View.GONE);
        }
    }

    private void fetchLiveProfile() {
        String token = tokenManager.getToken();
        if (token == null) return;

        ApiClient.getAuthApi().getCurrentUser("Bearer " + token)
                .enqueue(new Callback<UserProfileResponse>() {
                    @Override
                    public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            UserProfileResponse profile = response.body();
                            tokenManager.saveAuthData(
                                    token,
                                    profile.getId(),
                                    profile.getFullName(),
                                    profile.getEmail(),
                                    profile.getRole(),
                                    tokenManager.getClinicId()
                            );
                            tokenManager.saveUserPhone(profile.getPhone());
                            displayCachedUserInfo();
                        } else if (response.code() == 401 || response.code() == 403) {
                            Toast.makeText(ProfileActivity.this, "Phiên đăng nhập hết hạn", Toast.LENGTH_SHORT).show();
                            tokenManager.clear();
                            goToLogin();
                        }
                    }

                    @Override
                    public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                        // Vẫn dùng thông tin lưu trong cache
                    }
                });
    }

    private void setupListeners() {
        // Nút Quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Tiện ích: Thú cưng của tôi
        binding.rowMyPets.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyPetsActivity.class);
            intent.putExtra(MyPetsActivity.EXTRA_BOOKING_MODE, false);
            startActivity(intent);
        });

        // Tiện ích: Lịch hẹn của tôi
        binding.rowMyAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyAppointmentsActivity.class);
            startActivity(intent);
        });

        // Tiện ích: Bảng nhân viên (nếu có role STAFF)
        binding.rowStaffPortal.setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffDashboardActivity.class);
            startActivity(intent);
        });

        // Cài đặt: Menu xổ xuống (Collapsible Accordion)
        binding.headerSettingsGroup.setOnClickListener(v -> toggleSettingsSection());

        // Cài đặt: Thông báo
        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String msg = isChecked ? "Đã bật thông báo nhắc lịch khám" : "Đã tắt thông báo";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        // Hỗ trợ CSKH
        binding.rowHelpCenter.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Hỗ trợ khách hàng")
                    .setMessage("Mọi thắc mắc hoặc yêu cầu hỗ trợ khẩn cấp, vui lòng liên hệ tổng đài PET CARE:\n\nHotline: 1900 1234 (08:00 - 21:00 hàng ngày)\nEmail: support@petcare.com")
                    .setPositiveButton("Đã hiểu", null)
                    .show();
        });

        // Nút Đăng xuất -> Mở Custom Dialog xác nhận
        binding.btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    private void setupBottomNav() {
        // Tab 1: Trang chủ -> Mở MainActivity
        binding.navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Tab 2: Lịch hẹn -> Mở MyAppointmentsActivity
        binding.navAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyAppointmentsActivity.class);
            startActivity(intent);
            finish();
        });

        // Tab 3: Thú cưng -> Mở MyPetsActivity
        binding.navPets.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyPetsActivity.class);
            intent.putExtra(MyPetsActivity.EXTRA_BOOKING_MODE, false);
            startActivity(intent);
            finish();
        });

        // Tab 4: Tài khoản -> Đang ở màn hình Profile, cuộn mượt lên đầu trang
        binding.navProfile.setOnClickListener(v -> {
            binding.scrollProfile.smoothScrollTo(0, 0);
        });
    }

    private void toggleSettingsSection() {
        boolean isCurrentlyVisible = binding.layoutSettingsContent.getVisibility() == View.VISIBLE;
        if (isCurrentlyVisible) {
            binding.layoutSettingsContent.setVisibility(View.GONE);
            binding.ivSettingsChevron.animate().rotation(0f).setDuration(200).start();
        } else {
            binding.layoutSettingsContent.setVisibility(View.VISIBLE);
            binding.ivSettingsChevron.animate().rotation(180f).setDuration(200).start();
        }
    }

    private void showLogoutConfirmationDialog() {
        Dialog dialog = new Dialog(this);
        DialogLogoutConfirmationBinding dialogBinding = DialogLogoutConfirmationBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.88);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        dialogBinding.btnCancelLogout.setOnClickListener(v -> dialog.dismiss());
        dialogBinding.btnConfirmLogout.setOnClickListener(v -> {
            dialog.dismiss();
            tokenManager.clear();
            Toast.makeText(this, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();
            goToLogin();
        });

        dialog.show();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

