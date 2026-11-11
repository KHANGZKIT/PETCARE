package com.example.petcareui.utils;

import android.os.Build;

public class Constants {
    // Tự động chuyển 10.0.2.2 khi chạy trên Emulator để kết nối thẳng vào Spring Boot host PC
    public static String getBaseUrl() {
        if (isEmulator()) {
            return "http://10.0.2.2:8080/api/v1/";
        }
        return "http://localhost:8080/api/v1/";
    }

    public static boolean isEmulator() {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator");
    }

    public static final String BASE_URL = getBaseUrl();

    // Tên file SharedPreferences và các key lưu trạng thái đăng nhập
    public static final String PREF_NAME = "petcare_preferences";
    public static final String KEY_TOKEN = "jwt_token";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_USER_EMAIL = "user_email";
    public static final String KEY_USER_PHONE = "user_phone";
    public static final String KEY_USER_ROLE = "user_role";
    public static final String KEY_CLINIC_ID = "clinic_id";
}
