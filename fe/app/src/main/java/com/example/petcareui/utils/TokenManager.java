package com.example.petcareui.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static TokenManager instance;
    private final SharedPreferences prefs;

    private TokenManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context);
        }
        return instance;
    }

    public void saveAuthData(String token, Long userId, String fullName, String email, String role, Long clinicId) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.KEY_TOKEN, token);
        if (userId != null) editor.putLong(Constants.KEY_USER_ID, userId);
        editor.putString(Constants.KEY_USER_NAME, fullName);
        editor.putString(Constants.KEY_USER_EMAIL, email);
        editor.putString(Constants.KEY_USER_ROLE, role);
        if (clinicId != null) editor.putLong(Constants.KEY_CLINIC_ID, clinicId);
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(Constants.KEY_TOKEN, null);
    }

    public String getUserName() {
        return prefs.getString(Constants.KEY_USER_NAME, "");
    }

    public String getUserEmail() {
        return prefs.getString(Constants.KEY_USER_EMAIL, "");
    }

    public String getUserRole() {
        return prefs.getString(Constants.KEY_USER_ROLE, "");
    }

    public Long getUserId() {
        long id = prefs.getLong(Constants.KEY_USER_ID, -1);
        return id != -1 ? id : null;
    }

    public Long getClinicId() {
        long id = prefs.getLong(Constants.KEY_CLINIC_ID, -1);
        return id != -1 ? id : null;
    }

    public boolean isLoggedIn() {
        return getToken() != null && !getToken().isEmpty();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
