package com.example.petcareui.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.petcareui.data.model.ClinicItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Quản lý vị trí người dùng dùng chung giữa Trang chủ (MainActivity) và Danh sách phòng khám (ClinicListActivity).
 * - Khi cắm máy thật tại Việt Nam (hoặc đặt mock GPS tại VN): tự động nhận GPS trực tiếp (Fused/Network/GPS)
 *   và dịch ngược tên phường/quận qua Geocoder.
 * - Khi demo trên máy ảo (GPS mặc định ở Mỹ, nằm ngoài phạm vi Việt Nam): tự động fallback về vị trí demo
 *   ĐH Công nghiệp Hà Nội (HaUI) để khoảng cách phòng khám vẫn hợp lý.
 */
public final class UserLocationManager {

    public static final double DEFAULT_LAT = 21.0537;
    public static final double DEFAULT_LNG = 105.7351;
    public static final String DEFAULT_LABEL = "ĐH Công nghiệp Hà Nội";

    private static final String PREFS_NAME = "petcare_location_prefs";
    private static final String KEY_LAT_BITS = "loc_lat_bits";
    private static final String KEY_LNG_BITS = "loc_lng_bits";
    private static final String KEY_LABEL = "loc_label";
    private static final String KEY_GPS_MODE = "loc_gps_mode";

    private static final long GPS_TIMEOUT_MS = 8000L;
    private static final ExecutorService GEOCODE_EXECUTOR = Executors.newSingleThreadExecutor();

    public static final class PresetLocation {
        public final String label;
        public final String subtitle;
        public final double latitude;
        public final double longitude;

        public PresetLocation(String label, String subtitle, double latitude, double longitude) {
            this.label = label;
            this.subtitle = subtitle;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    public static final PresetLocation[] PRESETS = new PresetLocation[]{
            new PresetLocation("ĐH Công nghiệp Hà Nội", "Số 298 Cầu Diễn · Bắc Từ Liêm", 21.0537, 105.7351),
            new PresetLocation("Cầu Giấy, Hà Nội", "Khu vực Trần Quốc Hoàn · Xuân Thủy", 21.0400, 105.7880),
            new PresetLocation("Đống Đa, Hà Nội", "Khu vực Đường Láng · Thái Hà", 21.0125, 105.8190),
            new PresetLocation("Bắc Từ Liêm, Hà Nội", "Khu vực Hồ Tùng Mậu · Phú Diễn", 21.0510, 105.7640)
    };

    public static final class LocationState {
        public final double latitude;
        public final double longitude;
        public final String label;
        public final boolean gpsMode;
        public final boolean fromLiveGps;
        public final boolean usedDemoFallback;

        public LocationState(double latitude, double longitude, String label,
                             boolean gpsMode, boolean fromLiveGps, boolean usedDemoFallback) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.label = label;
            this.gpsMode = gpsMode;
            this.fromLiveGps = fromLiveGps;
            this.usedDemoFallback = usedDemoFallback;
        }
    }

    public interface LocationCallback {
        void onLocationResolved(@NonNull LocationState state);
    }

    public interface ActiveRequest {
        void cancel();
    }

    private UserLocationManager() {
    }

    /**
     * Nhận diện máy ảo Android Emulator để luôn gắn vị trí demo tại ĐH Công nghiệp Hà Nội
     * thay vì lấy GPS giả (thường cắm ở Mỹ hoặc lưu lại từ lần test trước).
     * Khi cắm điện thoại thật sẽ trả về false và lấy GPS trực tiếp từ thiết bị.
     */
    public static boolean isEmulator() {
        String fingerprint = Build.FINGERPRINT != null ? Build.FINGERPRINT : "";
        String model = Build.MODEL != null ? Build.MODEL : "";
        String manufacturer = Build.MANUFACTURER != null ? Build.MANUFACTURER : "";
        String brand = Build.BRAND != null ? Build.BRAND : "";
        String device = Build.DEVICE != null ? Build.DEVICE : "";
        String product = Build.PRODUCT != null ? Build.PRODUCT : "";
        String hardware = Build.HARDWARE != null ? Build.HARDWARE : "";

        return fingerprint.startsWith("generic")
                || fingerprint.startsWith("unknown")
                || model.contains("google_sdk")
                || model.contains("Emulator")
                || model.contains("Android SDK built for x86")
                || manufacturer.contains("Genymotion")
                || hardware.contains("goldfish")
                || hardware.contains("ranchu")
                || product.contains("sdk_gphone")
                || product.contains("google_sdk")
                || product.contains("sdk_x86")
                || product.contains("vbox86p")
                || product.contains("emulator")
                || product.contains("simulator")
                || (brand.startsWith("generic") && device.startsWith("generic"));
    }

    public static boolean hasLocationPermission(@NonNull Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isInVietnam(double lat, double lng) {
        return lat >= 8.0 && lat <= 24.0 && lng >= 102.0 && lng <= 110.0;
    }

    public static boolean isGpsMode(@NonNull Context context) {
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_GPS_MODE, true);
    }

    @NonNull
    public static LocationState getCurrent(@NonNull Context context) {
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean gpsMode = prefs.getBoolean(KEY_GPS_MODE, true);
        if (gpsMode && isEmulator()) {
            return new LocationState(DEFAULT_LAT, DEFAULT_LNG, DEFAULT_LABEL, true, false, true);
        }
        double lat = prefs.contains(KEY_LAT_BITS)
                ? Double.longBitsToDouble(prefs.getLong(KEY_LAT_BITS, Double.doubleToRawLongBits(DEFAULT_LAT)))
                : DEFAULT_LAT;
        double lng = prefs.contains(KEY_LNG_BITS)
                ? Double.longBitsToDouble(prefs.getLong(KEY_LNG_BITS, Double.doubleToRawLongBits(DEFAULT_LNG)))
                : DEFAULT_LNG;
        String label = prefs.getString(KEY_LABEL, DEFAULT_LABEL);
        if (label == null || label.trim().isEmpty()) {
            label = DEFAULT_LABEL;
        }
        if (!isInVietnam(lat, lng)) {
            lat = DEFAULT_LAT;
            lng = DEFAULT_LNG;
            label = DEFAULT_LABEL;
        }
        return new LocationState(lat, lng, label, gpsMode, false, false);
    }

    public static LocationState saveManualLocation(@NonNull Context context, double lat, double lng, @NonNull String label) {
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putLong(KEY_LAT_BITS, Double.doubleToRawLongBits(lat))
                .putLong(KEY_LNG_BITS, Double.doubleToRawLongBits(lng))
                .putString(KEY_LABEL, label)
                .putBoolean(KEY_GPS_MODE, false)
                .apply();
        return new LocationState(lat, lng, label, false, false, false);
    }

    public static void setGpsMode(@NonNull Context context, boolean enabled) {
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_GPS_MODE, enabled).apply();
    }

    private static void saveGpsResolved(@NonNull Context context, double lat, double lng, @NonNull String label) {
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putLong(KEY_LAT_BITS, Double.doubleToRawLongBits(lat))
                .putLong(KEY_LNG_BITS, Double.doubleToRawLongBits(lng))
                .putString(KEY_LABEL, label)
                .putBoolean(KEY_GPS_MODE, true)
                .apply();
    }

    public static String[] buildPickerItems() {
        String[] items = new String[PRESETS.length + 1];
        items[0] = "Dùng vị trí GPS hiện tại";
        for (int i = 0; i < PRESETS.length; i++) {
            items[i + 1] = PRESETS[i].label;
        }
        return items;
    }

    public static void applyDistance(@NonNull ClinicItem clinic, double userLat, double userLng) {
        if (clinic.getLatitude() != null && clinic.getLongitude() != null) {
            float[] result = new float[1];
            Location.distanceBetween(userLat, userLng, clinic.getLatitude(), clinic.getLongitude(), result);
            float distKm = result[0] / 1000f;
            clinic.setDistanceKm(distKm);
            if (distKm < 0.2f) {
                clinic.setDistance("< 200m");
            } else if (distKm < 1.0f) {
                clinic.setDistance(String.format(Locale.US, "%.0f m", result[0]));
            } else {
                clinic.setDistance(String.format(Locale.US, "%.1f km", distKm));
            }
        } else {
            clinic.setDistanceKm(999f);
            clinic.setDistance("-- km");
        }
    }

    /**
     * Lấy vị trí GPS thực tế từ Fused / Network / GPS provider.
     * Nếu tọa độ nằm trong Việt Nam -> trả về tọa độ thật kèm tên địa danh dịch ngược từ Geocoder.
     * Nếu thiết bị (máy ảo) trả về tọa độ ngoài Việt Nam (ví dụ Mỹ) -> tự động fallback về ĐH Công nghiệp Hà Nội.
     */
    @SuppressLint("MissingPermission")
    @NonNull
    public static ActiveRequest fetchLiveLocation(@NonNull Context context,
                                                  boolean forceFresh,
                                                  @NonNull LocationCallback callback) {
        final Context appContext = context.getApplicationContext();
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        final AtomicBoolean cancelled = new AtomicBoolean(false);
        final AtomicBoolean delivered = new AtomicBoolean(false);

        // Chế độ demo trên máy ảo: luôn gắn mặc định ĐH Công nghiệp Hà Nội
        if (isEmulator()) {
            LocationState demo = fallbackToDemo(appContext);
            mainHandler.post(() -> {
                if (!cancelled.get()) {
                    callback.onLocationResolved(demo);
                }
            });
            return () -> cancelled.set(true);
        }

        if (!hasLocationPermission(appContext)) {
            LocationState fallback = fallbackToDemo(appContext);
            mainHandler.post(() -> {
                if (!cancelled.get()) {
                    callback.onLocationResolved(fallback);
                }
            });
            return () -> cancelled.set(true);
        }

        final LocationManager lm = (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
        if (lm == null) {
            LocationState fallback = fallbackToDemo(appContext);
            mainHandler.post(() -> {
                if (!cancelled.get()) {
                    callback.onLocationResolved(fallback);
                }
            });
            return () -> cancelled.set(true);
        }

        // 1. Kiểm tra lastKnownLocation trên tất cả các provider (ưu tiên vị trí mới nhất nằm trong Việt Nam)
        Location bestInVietnam = null;
        Location bestOutsideVietnam = null;
        for (String provider : getCandidateProviders()) {
            try {
                Location loc = lm.getLastKnownLocation(provider);
                if (loc == null) continue;
                if (isInVietnam(loc.getLatitude(), loc.getLongitude())) {
                    if (bestInVietnam == null || loc.getTime() > bestInVietnam.getTime()) {
                        bestInVietnam = loc;
                    }
                } else {
                    if (bestOutsideVietnam == null || loc.getTime() > bestOutsideVietnam.getTime()) {
                        bestOutsideVietnam = loc;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        final Location cachedVietnam = bestInVietnam;
        if (!forceFresh && cachedVietnam != null) {
            delivered.set(true);
            resolveVietnamLocation(appContext, cachedVietnam.getLatitude(), cachedVietnam.getLongitude(),
                    cancelled, mainHandler, callback);
            return () -> cancelled.set(true);
        }

        // 2. Đăng ký lắng nghe cập nhật mới từ mọi provider đang bật (hỗ trợ cả trong nhà bằng Network/Fused)
        final List<LocationListener> registeredListeners = new ArrayList<>();
        final Runnable cleanup = () -> {
            for (LocationListener listener : registeredListeners) {
                try {
                    lm.removeUpdates(listener);
                } catch (Exception ignored) {
                }
            }
            registeredListeners.clear();
        };

        final boolean[] sawOutsideVietnam = new boolean[]{bestOutsideVietnam != null};

        final Runnable timeoutRunnable = () -> {
            cleanup.run();
            if (cancelled.get() || !delivered.compareAndSet(false, true)) return;
            if (!sawOutsideVietnam[0] && cachedVietnam != null) {
                resolveVietnamLocation(appContext, cachedVietnam.getLatitude(), cachedVietnam.getLongitude(),
                        cancelled, mainHandler, callback);
            } else {
                LocationState fallback = fallbackToDemo(appContext);
                callback.onLocationResolved(fallback);
            }
        };

        boolean requestedAny = false;
        for (String provider : getActiveRequestProviders()) {
            try {
                if (lm.isProviderEnabled(provider)) {
                    // Phải tạo LocationListener riêng cho từng provider vì Android LocationManager
                    // dùng chính object listener làm key (dùng chung 1 object sẽ ghi đè provider trước đó).
                    LocationListener providerListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(@NonNull Location loc) {
                            if (cancelled.get()) {
                                cleanup.run();
                                return;
                            }
                            if (isInVietnam(loc.getLatitude(), loc.getLongitude())) {
                                if (!delivered.compareAndSet(false, true)) return;
                                mainHandler.removeCallbacks(timeoutRunnable);
                                cleanup.run();
                                resolveVietnamLocation(appContext, loc.getLatitude(), loc.getLongitude(),
                                        cancelled, mainHandler, callback);
                            } else {
                                // Không hủy sớm ngay vì trên máy ảo Fused/Network có thể trả tọa độ Mỹ cũ
                                // trước khi GPS_PROVIDER kịp phát tọa độ thật tại Việt Nam.
                                sawOutsideVietnam[0] = true;
                            }
                        }

                        @Override public void onStatusChanged(String p, int status, Bundle extras) {}
                        @Override public void onProviderEnabled(@NonNull String p) {}
                        @Override public void onProviderDisabled(@NonNull String p) {}
                    };
                    lm.requestLocationUpdates(provider, 500L, 0f, providerListener, Looper.getMainLooper());
                    registeredListeners.add(providerListener);
                    requestedAny = true;
                }
            } catch (Exception ignored) {
            }
        }

        if (!requestedAny) {
            if (cachedVietnam != null) {
                if (delivered.compareAndSet(false, true)) {
                    resolveVietnamLocation(appContext, cachedVietnam.getLatitude(), cachedVietnam.getLongitude(),
                            cancelled, mainHandler, callback);
                }
            } else {
                LocationState fallback = fallbackToDemo(appContext);
                mainHandler.post(() -> {
                    if (!cancelled.get() && delivered.compareAndSet(false, true)) {
                        callback.onLocationResolved(fallback);
                    }
                });
            }
        } else {
            // Luôn đợi đủ GPS_TIMEOUT_MS (8s) vì cold-start GPS mất ~4-6s mới phát fix thật
            mainHandler.postDelayed(timeoutRunnable, GPS_TIMEOUT_MS);
        }

        return () -> {
            cancelled.set(true);
            mainHandler.removeCallbacks(timeoutRunnable);
            cleanup.run();
        };
    }

    private static List<String> getCandidateProviders() {
        List<String> providers = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            providers.add(LocationManager.FUSED_PROVIDER);
        }
        providers.add(LocationManager.GPS_PROVIDER);
        providers.add(LocationManager.NETWORK_PROVIDER);
        providers.add(LocationManager.PASSIVE_PROVIDER);
        return providers;
    }

    private static List<String> getActiveRequestProviders() {
        List<String> providers = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            providers.add(LocationManager.FUSED_PROVIDER);
        }
        providers.add(LocationManager.NETWORK_PROVIDER);
        providers.add(LocationManager.GPS_PROVIDER);
        return providers;
    }

    private static LocationState fallbackToDemo(@NonNull Context appContext) {
        saveGpsResolved(appContext, DEFAULT_LAT, DEFAULT_LNG, DEFAULT_LABEL);
        return new LocationState(DEFAULT_LAT, DEFAULT_LNG, DEFAULT_LABEL, true, false, true);
    }

    private static void resolveVietnamLocation(@NonNull Context appContext,
                                               double lat,
                                               double lng,
                                               @NonNull AtomicBoolean cancelled,
                                               @NonNull Handler mainHandler,
                                               @NonNull LocationCallback callback) {
        final String immediateLabel = "Vị trí GPS thực tế";
        saveGpsResolved(appContext, lat, lng, immediateLabel);

        // Nếu không có Geocoder thì trả ngay tọa độ thật
        if (!Geocoder.isPresent()) {
            mainHandler.post(() -> {
                if (!cancelled.get()) {
                    callback.onLocationResolved(new LocationState(lat, lng, immediateLabel, true, true, false));
                }
            });
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                Geocoder geocoder = new Geocoder(appContext, new Locale("vi", "VN"));
                geocoder.getFromLocation(lat, lng, 1, new Geocoder.GeocodeListener() {
                    @Override
                    public void onGeocode(@NonNull List<Address> addresses) {
                        String resolved = extractVietnameseLabel(addresses, immediateLabel);
                        saveGpsResolved(appContext, lat, lng, resolved);
                        mainHandler.post(() -> {
                            if (!cancelled.get()) {
                                callback.onLocationResolved(new LocationState(lat, lng, resolved, true, true, false));
                            }
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        mainHandler.post(() -> {
                            if (!cancelled.get()) {
                                callback.onLocationResolved(new LocationState(lat, lng, immediateLabel, true, true, false));
                            }
                        });
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (!cancelled.get()) {
                        callback.onLocationResolved(new LocationState(lat, lng, immediateLabel, true, true, false));
                    }
                });
            }
        } else {
            GEOCODE_EXECUTOR.execute(() -> {
                String resolved = immediateLabel;
                try {
                    Geocoder geocoder = new Geocoder(appContext, new Locale("vi", "VN"));
                    @SuppressWarnings("deprecation")
                    List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                    resolved = extractVietnameseLabel(addresses, immediateLabel);
                } catch (Exception ignored) {
                }
                final String finalLabel = resolved;
                saveGpsResolved(appContext, lat, lng, finalLabel);
                mainHandler.post(() -> {
                    if (!cancelled.get()) {
                        callback.onLocationResolved(new LocationState(lat, lng, finalLabel, true, true, false));
                    }
                });
            });
        }
    }

    private static String extractVietnameseLabel(List<Address> addresses, String fallback) {
        if (addresses == null || addresses.isEmpty()) return fallback;
        Address addr = addresses.get(0);
        if (addr == null) return fallback;

        String ward = cleanText(addr.getSubLocality());
        String district = cleanText(addr.getSubAdminArea());
        String city = cleanText(addr.getAdminArea());
        String locality = cleanText(addr.getLocality());

        if (!ward.isEmpty() && !district.isEmpty() && !ward.equalsIgnoreCase(district)) {
            return ward + ", " + district;
        }
        if (!district.isEmpty() && !city.isEmpty() && !district.equalsIgnoreCase(city)) {
            return district + ", " + city;
        }
        if (!locality.isEmpty() && !city.isEmpty() && !locality.equalsIgnoreCase(city)) {
            return locality + ", " + city;
        }
        if (!district.isEmpty()) return district;
        if (!locality.isEmpty()) return locality;
        if (!city.isEmpty()) return city;
        return fallback;
    }

    private static String cleanText(String raw) {
        return raw != null ? raw.trim() : "";
    }
}
