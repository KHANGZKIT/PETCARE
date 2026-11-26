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
        long previousClinicId = prefs.getLong(Constants.KEY_CLINIC_ID, -1L);
        editor.putString(Constants.KEY_TOKEN, token);
        if (userId != null) {
            editor.putLong(Constants.KEY_USER_ID, userId);
        } else {
            editor.remove(Constants.KEY_USER_ID);
        }
        editor.putString(Constants.KEY_USER_NAME, fullName);
        editor.putString(Constants.KEY_USER_EMAIL, email);
        editor.putString(Constants.KEY_USER_ROLE, role);
        if (clinicId != null) {
            editor.putLong(Constants.KEY_CLINIC_ID, clinicId);
            if (previousClinicId != clinicId) {
                editor.remove(Constants.KEY_CLINIC_NAME);
                editor.remove(Constants.KEY_CLINIC_AREA);
            }
        } else {
            editor.remove(Constants.KEY_CLINIC_ID);
            editor.remove(Constants.KEY_CLINIC_NAME);
            editor.remove(Constants.KEY_CLINIC_AREA);
        }
        editor.apply();
    }

    public void saveClinicInfo(String clinicName, String clinicArea) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.KEY_CLINIC_NAME, clinicName != null ? clinicName : "");
        editor.putString(Constants.KEY_CLINIC_AREA, clinicArea != null ? clinicArea : "");
        editor.apply();
    }

    public void saveUserPhone(String phone) {
        prefs.edit().putString(Constants.KEY_USER_PHONE, phone != null ? phone : "").apply();
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

    public String getUserPhone() {
        return prefs.getString(Constants.KEY_USER_PHONE, "");
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

    public String getClinicName() {
        return prefs.getString(Constants.KEY_CLINIC_NAME, "");
    }

    public String getClinicArea() {
        return prefs.getString(Constants.KEY_CLINIC_AREA, "");
    }

    public boolean isLoggedIn() {
        return getToken() != null && !getToken().isEmpty();
    }

    public boolean isOnboardingCompleted() {
        return prefs.getBoolean(Constants.KEY_ONBOARDING_COMPLETED, false);
    }

    public void setOnboardingCompleted(boolean completed) {
        prefs.edit().putBoolean(Constants.KEY_ONBOARDING_COMPLETED, completed).apply();
    }

    public boolean isNotificationsEnabled() {
        return prefs.getBoolean(Constants.KEY_NOTIFICATIONS_ENABLED, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(Constants.KEY_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    private String getReadNotifPrefKey() {
        Long uid = getUserId();
        String role = getUserRole();
        return "read_notifs_" + (role != null ? role : "USER") + "_" + (uid != null ? uid : 0L);
    }

    public boolean isNotificationRead(String notifId) {
        if (notifId == null || notifId.isEmpty()) return true;
        java.util.Set<String> set = prefs.getStringSet(getReadNotifPrefKey(), java.util.Collections.emptySet());
        return set != null && set.contains(notifId);
    }

    public void markNotificationRead(String notifId) {
        if (notifId == null || notifId.isEmpty()) return;
        String key = getReadNotifPrefKey();
        java.util.Set<String> existing = prefs.getStringSet(key, null);
        java.util.Set<String> copy = existing != null ? new java.util.HashSet<>(existing) : new java.util.HashSet<>();
        copy.add(notifId);
        prefs.edit().putStringSet(key, copy).apply();
    }

    public void markNotificationsRead(java.util.Collection<String> notifIds) {
        if (notifIds == null || notifIds.isEmpty()) return;
        String key = getReadNotifPrefKey();
        java.util.Set<String> existing = prefs.getStringSet(key, null);
        java.util.Set<String> copy = existing != null ? new java.util.HashSet<>(existing) : new java.util.HashSet<>();
        copy.addAll(notifIds);
        prefs.edit().putStringSet(key, copy).apply();
    }

    public void clear() {
        boolean onboardingDone = isOnboardingCompleted();
        boolean notificationsEnabled = isNotificationsEnabled();
        prefs.edit()
                .clear()
                .putBoolean(Constants.KEY_ONBOARDING_COMPLETED, onboardingDone)
                .putBoolean(Constants.KEY_NOTIFICATIONS_ENABLED, notificationsEnabled)
                .apply();
    }
}
