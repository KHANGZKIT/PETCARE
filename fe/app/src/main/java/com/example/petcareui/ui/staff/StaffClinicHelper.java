package com.example.petcareui.ui.staff;

import android.content.Context;

import com.example.petcareui.data.api.ApiClient;
import com.example.petcareui.data.model.ClinicResponse;
import com.example.petcareui.data.model.UserProfileResponse;
import com.example.petcareui.utils.TokenManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Helper tải và định dạng thông tin nhân viên & phòng khám đang đăng nhập từ API thật.
 */
final class StaffClinicHelper {

    private static final DateTimeFormatter DATE_DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());
    private static final DateTimeFormatter DATE_DM = DateTimeFormatter.ofPattern("dd/MM", Locale.getDefault());

    private StaffClinicHelper() {
    }

    static void syncStaffAndClinicInfo(Context context, Runnable onUpdated, Runnable onUnauthorized) {
        TokenManager tokenManager = TokenManager.getInstance(context);
        String token = tokenManager.getToken();
        if (token == null || token.trim().isEmpty()) {
            if (onUnauthorized != null) {
                onUnauthorized.run();
            }
            return;
        }

        Long cachedClinicId = tokenManager.getClinicId();
        if (cachedClinicId != null && tokenManager.getClinicName().isEmpty()) {
            fetchClinicInfo(tokenManager, cachedClinicId, onUpdated);
        }

        ApiClient.getAuthApi().getCurrentUser("Bearer " + token).enqueue(new Callback<UserProfileResponse>() {
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
                            profile.getClinicId()
                    );
                    tokenManager.saveUserPhone(profile.getPhone());

                    if (onUpdated != null) {
                        onUpdated.run();
                    }

                    if (profile.getClinicId() != null) {
                        fetchClinicInfo(tokenManager, profile.getClinicId(), onUpdated);
                    }
                } else if (response.code() == 401 || response.code() == 403) {
                    if (onUnauthorized != null) {
                        onUnauthorized.run();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                // Giữ nguyên dữ liệu đã lưu cục bộ khi mạng tạm gián đoạn
            }
        });
    }

    static void fetchClinicInfo(TokenManager tokenManager, Long clinicId, Runnable onUpdated) {
        if (clinicId == null) {
            return;
        }
        ApiClient.getClinicApi().getClinicById(clinicId).enqueue(new Callback<ClinicResponse>() {
            @Override
            public void onResponse(Call<ClinicResponse> call, Response<ClinicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ClinicResponse clinic = response.body();
                    tokenManager.saveClinicInfo(clinic.getName(), clinic.getArea());
                    if (onUpdated != null) {
                        onUpdated.run();
                    }
                }
            }

            @Override
            public void onFailure(Call<ClinicResponse> call, Throwable t) {
            }
        });
    }

    static String formatInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "NV";
        }
        String[] rawParts = fullName.trim().split("\\s+");
        int startIndex = 0;
        if (rawParts.length > 1) {
            String firstLower = rawParts[0].toLowerCase(Locale.ROOT).replace(".", "");
            if ("bs".equals(firstLower) || "ths".equals(firstLower) || "ts".equals(firstLower) || "dr".equals(firstLower)) {
                startIndex = 1;
            }
        }
        int lastIndex = rawParts.length - 1;
        StringBuilder sb = new StringBuilder();
        if (startIndex <= lastIndex && !rawParts[startIndex].isEmpty()) {
            sb.append(rawParts[startIndex].charAt(0));
        }
        if (lastIndex > startIndex && !rawParts[lastIndex].isEmpty()) {
            sb.append(rawParts[lastIndex].charAt(0));
        }
        String result = sb.toString().toUpperCase(Locale.ROOT);
        return result.isEmpty() ? "NV" : result;
    }

    static String buildClinicDisplayTitle(TokenManager tokenManager) {
        String clinicName = tokenManager.getClinicName();
        if (!clinicName.isEmpty()) {
            return clinicName;
        }
        Long clinicId = tokenManager.getClinicId();
        return clinicId != null ? "Phòng khám #" + clinicId : "Chưa gán phòng khám";
    }

    static String buildClinicSubtitleWithArea(TokenManager tokenManager) {
        String clinicName = tokenManager.getClinicName();
        String clinicArea = tokenManager.getClinicArea();
        if (!clinicName.isEmpty()) {
            return !clinicArea.isEmpty() ? clinicName + " · " + clinicArea : clinicName;
        }
        Long clinicId = tokenManager.getClinicId();
        return clinicId != null ? "Phòng khám #" + clinicId : "Chưa gán phòng khám";
    }

    static String getDayOfWeekFullLabel(DayOfWeek dow) {
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

    static String formatVietnameseFullDate(LocalDate date) {
        if (date == null) return "";
        return getDayOfWeekFullLabel(date.getDayOfWeek()) + ", " + date.format(DATE_DMY);
    }

    static String formatTodayShortLabel(LocalDate date) {
        if (date == null) return "Hôm nay";
        return "Hôm nay · " + getDayOfWeekFullLabel(date.getDayOfWeek()) + " " + date.format(DATE_DM);
    }
}
