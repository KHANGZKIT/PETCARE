package com.example.petcareui.utils;

public class Constants {
    // Sử dụng localhost kết hợp adb reverse để không lộ IP
    public static final String BASE_URL = "http://localhost:8080/api/v1/";

    // Tên file SharedPreferences và các key lưu trạng thái đăng nhập
    public static final String PREF_NAME = "petcare_preferences";
    public static final String KEY_TOKEN = "jwt_token";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_USER_EMAIL = "user_email";
    public static final String KEY_USER_PHONE = "user_phone";
    public static final String KEY_USER_ROLE = "user_role";
    public static final String KEY_CLINIC_ID = "clinic_id";
    public static final String KEY_CLINIC_NAME = "clinic_name";
    public static final String KEY_CLINIC_AREA = "clinic_area";
}
