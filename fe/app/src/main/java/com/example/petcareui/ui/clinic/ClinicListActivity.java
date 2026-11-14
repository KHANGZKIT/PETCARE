package com.example.petcareui.ui.clinic;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.petcareui.R;
import com.example.petcareui.data.api.ApiClient;
import com.example.petcareui.data.model.ClinicItem;
import com.example.petcareui.data.dto.response.ClinicResponse;
import com.example.petcareui.databinding.ActivityClinicListBinding;
import com.example.petcareui.databinding.ItemClinicRowBinding;
import com.example.petcareui.utils.UiMotionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClinicListActivity extends AppCompatActivity {

    private static final int REQ_LOCATION = 1001;
    // Tọa độ gốc mặc định: Đại học Công nghiệp Hà Nội (HaUI - Bắc Từ Liêm)
    private static final double DEFAULT_LAT = 21.0537;
    private static final double DEFAULT_LNG = 105.7351;

    private ActivityClinicListBinding binding;
    private final List<ClinicItem> allClinics = new ArrayList<>();
    private String currentFilterMode = "all"; // "all", "open", "emergency", "nearby"
    private String currentSortMode = "NEAREST"; // "NEAREST", "RATING"
    private String currentSearchQuery = "";

    private double userLatitude = DEFAULT_LAT;
    private double userLongitude = DEFAULT_LNG;
    private String locationLabel = "ĐH Công nghiệp Hà Nội (HaUI)";

    private static final int[] CLINIC_THUMBNAILS = new int[]{
            R.drawable.img_clinic_cover,
            R.drawable.ic_clinic_illustration_hospital,
            R.drawable.ic_clinic_illustration_spa
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClinicListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        UiMotionUtils.applyWindowInsets(binding.getRoot(), binding.layoutTopBar, binding.layoutResultsBody);
        requestLocationPermission();
        updateUserLocation();
        setupListeners();
        loadClinicsFromApi();
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQ_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            updateUserLocation();
            calculateDistances(allClinics);
            filterClinics();
        }
    }

    private void updateUserLocation() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (locationManager != null) {
                    // 1. Thử lấy vị trí cache gần nhất để hiển thị tức thì
                    Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (loc == null) {
                        loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                    if (loc != null) {
                        applyValidLocation(loc);
                    }

                    // 2. Đồng thời đăng ký nhận GPS thực tế mới nhất (rất quan trọng khi cắm máy thật)
                    android.location.LocationListener singleUpdateListener = new android.location.LocationListener() {
                        @Override
                        public void onLocationChanged(@NonNull Location newLoc) {
                            applyValidLocation(newLoc);
                            try {
                                locationManager.removeUpdates(this);
                            } catch (Exception ignored) {}
                        }
                        @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
                        @Override public void onProviderEnabled(@NonNull String provider) {}
                        @Override public void onProviderDisabled(@NonNull String provider) {}
                    };

                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 5f, singleUpdateListener, android.os.Looper.getMainLooper());
                    } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 5f, singleUpdateListener, android.os.Looper.getMainLooper());
                    }
                }
            }
        } catch (Exception ignored) {
        }
        binding.tvLocationSubtitle.setText("Vị trí: " + locationLabel);
    }

    private void applyValidLocation(Location loc) {
        if (loc == null) return;
        double lat = loc.getLatitude();
        double lng = loc.getLongitude();
        // Kiểm tra nếu vị trí hợp lệ trong lãnh thổ Việt Nam (tránh vị trí mặc định ở Mỹ của máy ảo)
        if (lat >= 8.0 && lat <= 24.0 && lng >= 102.0 && lng <= 110.0) {
            userLatitude = lat;
            userLongitude = lng;
            locationLabel = "Vị trí GPS thực tế";
            binding.tvLocationSubtitle.setText("Vị trí: " + locationLabel);
            if (!allClinics.isEmpty()) {
                calculateDistances(allClinics);
                filterClinics();
            }
        }
    }

    private void calculateDistances(List<ClinicItem> clinics) {
        float[] result = new float[1];
        for (ClinicItem c : clinics) {
            if (c.getLatitude() != null && c.getLongitude() != null) {
                Location.distanceBetween(userLatitude, userLongitude, c.getLatitude(), c.getLongitude(), result);
                float distKm = result[0] / 1000f;
                c.setDistanceKm(distKm);
                if (distKm < 0.2f) {
                    c.setDistance("Gần đây · < 200m");
                } else if (distKm < 1.0f) {
                    c.setDistance(String.format(Locale.US, "%.0f m", result[0]));
                } else {
                    c.setDistance(String.format(Locale.US, "%.1f km", distKm));
                }
            } else {
                c.setDistanceKm(999f);
                c.setDistance("-- km");
            }
        }
    }

    private void loadClinicsFromApi() {
        showLoading();
        ApiClient.getClinicApi().getAllClinics(null).enqueue(new Callback<List<ClinicResponse>>() {
            @Override
            public void onResponse(Call<List<ClinicResponse>> call, Response<List<ClinicResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allClinics.clear();
                    for (ClinicResponse res : response.body()) {
                        allClinics.add(res.toClinicItem());
                    }
                    calculateDistances(allClinics);
                    filterClinics();
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(Call<List<ClinicResponse>> call, Throwable t) {
                showError();
            }
        });
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        // Lắng nghe nhập tìm kiếm phòng khám / khu vực
        binding.etSearchLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s != null ? s.toString() : "";
                filterClinics();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Nút lọc nâng cao
        binding.btnFilter.setOnClickListener(v -> {
            Toast.makeText(this, "Bộ lọc: Đang hiển thị các cơ sở tại Hà Nội", Toast.LENGTH_SHORT).show();
        });

        binding.tvSortOption.setOnClickListener(v -> {
            if ("NEAREST".equals(currentSortMode)) {
                currentSortMode = "RATING";
                binding.tvSortOption.setText("Sắp xếp · Đánh giá cao");
                Toast.makeText(this, "Đã sắp xếp theo đánh giá cao nhất", Toast.LENGTH_SHORT).show();
            } else {
                currentSortMode = "NEAREST";
                binding.tvSortOption.setText("Sắp xếp · Gần nhất");
                Toast.makeText(this, "Đã sắp xếp theo khoảng cách gần nhất", Toast.LENGTH_SHORT).show();
            }
            filterClinics();
        });

        binding.btnLoadMore.setOnClickListener(v -> {
            Toast.makeText(this, "Đã hiển thị toàn bộ hệ thống cơ sở", Toast.LENGTH_SHORT).show();
        });

        // Thử lại trong Error State
        binding.btnRetry.setOnClickListener(v -> loadClinicsFromApi());

        binding.btnClearFilter.setOnClickListener(v -> {
            binding.etSearchLocation.setText("");
            currentSearchQuery = "";
            currentFilterMode = "all";
            resetFilterChips();
            filterClinics();
        });

        // Đổi vị trí tham chiếu
        binding.tvChangeLocation.setOnClickListener(v -> showChangeLocationDialog());

        UiMotionUtils.attachSpringTouch(
                binding.btnBack,
                binding.btnFilter,
                binding.btnLoadMore,
                binding.btnClearFilter,
                binding.btnRetry,
                binding.chipAll,
                binding.chipOpen,
                binding.chipNearby,
                binding.chipEmergency
        );

        setupFilterChips();
    }

    private void showChangeLocationDialog() {
        String[] locations = new String[]{
                "ĐH Công nghiệp Hà Nội (HaUI - Bắc Từ Liêm)",
                "Cầu Giấy (12 Chùa Hà)",
                "Đống Đa (88 Thái Hà)",
                "Định vị lại theo GPS"
        };
        new AlertDialog.Builder(this)
                .setTitle("Chọn vị trí định vị của bạn")
                .setItems(locations, (dialog, which) -> {
                    if (which == 0) {
                        userLatitude = 21.0537;
                        userLongitude = 105.7351;
                        locationLabel = "ĐH Công nghiệp Hà Nội (HaUI)";
                    } else if (which == 1) {
                        userLatitude = 21.0360;
                        userLongitude = 105.7950;
                        locationLabel = "Cầu Giấy, Hà Nội";
                    } else if (which == 2) {
                        userLatitude = 21.0125;
                        userLongitude = 105.8190;
                        locationLabel = "Đống Đa, Hà Nội";
                    } else {
                        updateUserLocation();
                    }
                    binding.tvLocationSubtitle.setText("Vị trí: " + locationLabel);
                    calculateDistances(allClinics);
                    filterClinics();
                })
                .show();
    }

    private void setupFilterChips() {
        binding.chipAll.setOnClickListener(v -> {
            setActiveChip(binding.chipAll);
            currentFilterMode = "all";
            filterClinics();
        });

        binding.chipOpen.setOnClickListener(v -> {
            setActiveChip(binding.chipOpen);
            currentFilterMode = "open";
            filterClinics();
        });

        binding.chipNearby.setOnClickListener(v -> {
            setActiveChip(binding.chipNearby);
            currentFilterMode = "nearby";
            currentSortMode = "NEAREST";
            binding.tvSortOption.setText("Sắp xếp · Gần nhất");
            filterClinics();
        });

        binding.chipEmergency.setOnClickListener(v -> {
            setActiveChip(binding.chipEmergency);
            currentFilterMode = "emergency";
            filterClinics();
        });
    }

    private String removeAccents(String text) {
        if (text == null) return "";
        String normalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("đ", "d")
                .replaceAll("Đ", "D")
                .toLowerCase();
    }

    private void filterClinics() {
        List<ClinicItem> filtered = new ArrayList<>();
        String rawQuery = currentSearchQuery.trim().toLowerCase();
        String unaccentedQuery = removeAccents(rawQuery);

        for (ClinicItem c : allClinics) {
            String name = c.getName() != null ? c.getName() : "";
            String addr = c.getAddress() != null ? c.getAddress() : "";
            String area = c.getArea() != null ? c.getArea() : "";
            String dist = c.getDistance() != null ? c.getDistance() : "";

            boolean matchesSearch = rawQuery.isEmpty()
                    || name.toLowerCase().contains(rawQuery) || removeAccents(name).contains(unaccentedQuery)
                    || addr.toLowerCase().contains(rawQuery) || removeAccents(addr).contains(unaccentedQuery)
                    || area.toLowerCase().contains(rawQuery) || removeAccents(area).contains(unaccentedQuery)
                    || dist.toLowerCase().contains(rawQuery);

            boolean matchesChip = true;
            if ("open".equals(currentFilterMode)) {
                matchesChip = c.isOpen();
            } else if ("emergency".equals(currentFilterMode)) {
                String hours = c.getHours() != null ? c.getHours().toLowerCase() : "";
                matchesChip = hours.contains("24/7") || hours.contains("cấp cứu") || name.toLowerCase().contains("cấp cứu");
            }

            if (matchesSearch && matchesChip) {
                filtered.add(c);
            }
        }

        // Sắp xếp danh sách
        if ("RATING".equals(currentSortMode)) {
            Collections.sort(filtered, (a, b) -> Double.compare(b.getRating(), a.getRating()));
        } else {
            // Mặc định sắp xếp khoảng cách gần nhất
            Collections.sort(filtered, (a, b) -> Float.compare(
                    a.getDistanceKm() != null ? a.getDistanceKm() : 999f,
                    b.getDistanceKm() != null ? b.getDistanceKm() : 999f
            ));
        }

        if (filtered.isEmpty()) {
            showEmpty();
        } else {
            showResults(filtered);
        }
    }

    private void setActiveChip(View activeView) {
        UiMotionUtils.animateSelectionPop(activeView);
        View[] chipContainers = new View[]{
                binding.chipAll,
                binding.chipOpen,
                binding.chipNearby,
                binding.chipEmergency
        };

        for (View c : chipContainers) {
            boolean isActive = (c == activeView);
            c.setBackgroundResource(isActive ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);

            if (c instanceof TextView) {
                ((TextView) c).setTextColor(ContextCompat.getColor(this, isActive ? R.color.white : R.color.text_muted));
            } else if (c == binding.chipNearby) {
                binding.tvChipNearby.setTextColor(ContextCompat.getColor(this, isActive ? R.color.white : R.color.text_muted));
            }
        }
    }

    private void resetFilterChips() {
        setActiveChip(binding.chipAll);
    }

    private void showResults(List<ClinicItem> list) {
        binding.layoutResultsState.setVisibility(View.VISIBLE);
        binding.layoutLoadingState.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.GONE);
        binding.layoutErrorState.setVisibility(View.GONE);

        binding.tvClinicCount.setText(list.size() + " phòng khám");

        binding.containerClinicsList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < list.size(); i++) {
            ClinicItem clinic = list.get(i);
            ItemClinicRowBinding rowBinding = ItemClinicRowBinding.inflate(inflater, binding.containerClinicsList, false);

            // Gán ảnh đại diện phòng khám đa dạng (Ảnh thật + Minh họa bệnh viện/spa)
            int thumbRes = CLINIC_THUMBNAILS[i % CLINIC_THUMBNAILS.length];
            rowBinding.ivClinicImage.setImageResource(thumbRes);

            rowBinding.tvClinicName.setText(clinic.getName());
            rowBinding.tvClinicAddress.setText(clinic.getAddress());
            rowBinding.tvClinicRating.setText(String.valueOf(clinic.getRating()));
            rowBinding.tvClinicMeta.setText(" · " + clinic.getReviews() + " đánh giá · " + clinic.getDistance());
            rowBinding.ivClinicImage.setImageResource(clinic.getImageResId());
            rowBinding.ivClinicImage.setColorFilter(null);

            if (clinic.isOpen()) {
                rowBinding.tvClinicStatusBadge.setText("● ĐANG MỞ CỬA");
                rowBinding.tvClinicStatusBadge.setBackgroundResource(R.drawable.bg_badge_open);
                rowBinding.tvClinicStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_success_ink));
            } else {
                rowBinding.tvClinicStatusBadge.setText("ĐÃ ĐÓNG CỬA");
                rowBinding.tvClinicStatusBadge.setBackgroundResource(R.drawable.bg_badge_closed);
                rowBinding.tvClinicStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
            }

            rowBinding.tvClinicHours.setText(clinic.getHours());

            UiMotionUtils.attachSpringTouch(rowBinding.getRoot());

            rowBinding.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(this, ClinicDetailsActivity.class);
                intent.putExtra(ClinicDetailsActivity.EXTRA_CLINIC, clinic);
                startActivity(intent);
            });

            binding.containerClinicsList.addView(rowBinding.getRoot());
        }

        // Hiệu ứng đổ danh sách tuần tự (Staggered Animation)
        UiMotionUtils.animateStaggeredChildren(binding.containerClinicsList, 30);
    }

    public void showLoading() {
        binding.layoutResultsState.setVisibility(View.GONE);
        binding.layoutLoadingState.setVisibility(View.VISIBLE);
        binding.layoutEmptyState.setVisibility(View.GONE);
        binding.layoutErrorState.setVisibility(View.GONE);
    }

    public void showEmpty() {
        binding.layoutResultsState.setVisibility(View.GONE);
        binding.layoutLoadingState.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.VISIBLE);
        binding.layoutErrorState.setVisibility(View.GONE);
    }

    public void showError() {
        binding.layoutResultsState.setVisibility(View.GONE);
        binding.layoutLoadingState.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.GONE);
        binding.layoutErrorState.setVisibility(View.VISIBLE);
    }
}
