package com.example.petcareui.ui.clinic;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.petcareui.MainActivity;
import com.example.petcareui.R;
import com.example.petcareui.data.model.ClinicItem;
import com.example.petcareui.data.model.PetItem;
import com.example.petcareui.databinding.ActivityClinicListBinding;
import com.example.petcareui.ui.appointments.MyAppointmentsActivity;
import com.example.petcareui.ui.pets.MyPetsActivity;
import com.example.petcareui.utils.UiMotionUtils;
import com.example.petcareui.utils.UserLocationManager;

import java.util.List;
import java.util.Locale;

public class ClinicListActivity extends AppCompatActivity {

    public static final String EXTRA_TAB_MODE = "extra_tab_mode";
    public static final String EXTRA_INITIAL_FILTER = "extra_initial_filter";
    public static final String EXTRA_PRESELECTED_PET = "extra_preselected_pet";
    private static final int REQ_LOCATION = 1001;

    private ActivityClinicListBinding binding;
    private ClinicListViewModel viewModel;
    private ClinicListAdapter clinicAdapter;
    private PetItem preselectedPet;

    private UserLocationManager.ActiveRequest activeLocationRequest;
    private boolean pendingUserTriggeredGps = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClinicListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ClinicListViewModel.class);

        boolean isTabMode = getIntent().getBooleanExtra(EXTRA_TAB_MODE, false);
        if (isTabMode) {
            binding.btnBack.setVisibility(View.GONE);
            binding.layoutBottomNavContainer.setVisibility(View.VISIBLE);
            UiMotionUtils.applyWindowInsets(binding.getRoot(), binding.layoutTopBar, binding.bottomNavigation);
            setupBottomNav();
        } else {
            binding.btnBack.setVisibility(View.VISIBLE);
            binding.layoutBottomNavContainer.setVisibility(View.GONE);
            UiMotionUtils.applyWindowInsets(binding.getRoot(), binding.layoutTopBar, binding.layoutResultsBody);
        }

        if (getIntent().hasExtra(EXTRA_PRESELECTED_PET)) {
            try {
                preselectedPet = (PetItem) getIntent().getSerializableExtra(EXTRA_PRESELECTED_PET);
            } catch (Exception ignored) {
            }
        }

        setupRecyclerView();
        setupListeners();
        observeViewModel();

        if (savedInstanceState == null) {
            String initialFilter = getIntent().getStringExtra(EXTRA_INITIAL_FILTER);
            if ("exam".equalsIgnoreCase(initialFilter)
                    || "vaccine".equalsIgnoreCase(initialFilter)
                    || "spa".equalsIgnoreCase(initialFilter)
                    || "emergency".equalsIgnoreCase(initialFilter)) {
                viewModel.setFilterMode(initialFilter.toLowerCase(Locale.ROOT));
            } else {
                viewModel.setFilterMode("all");
            }
            viewModel.loadClinics();
        }

        syncLocationFromStore();
        if (UserLocationManager.isGpsMode(this)) {
            if (UserLocationManager.isEmulator() || UserLocationManager.hasLocationPermission(this)) {
                updateUserLocation(false);
            } else {
                requestLocationPermission(false);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncLocationFromStore();
    }

    private void syncLocationFromStore() {
        UserLocationManager.LocationState current = UserLocationManager.getCurrent(this);
        viewModel.updateLocation(current.latitude, current.longitude, current.label);
    }

    private void setupRecyclerView() {
        clinicAdapter = new ClinicListAdapter(this::openClinicDetails);
        binding.rvClinicsList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvClinicsList.setAdapter(clinicAdapter);
    }

    private void openClinicDetails(ClinicItem clinic) {
        Intent intent = new Intent(this, ClinicDetailsActivity.class);
        intent.putExtra(ClinicDetailsActivity.EXTRA_CLINIC, clinic);
        if (preselectedPet != null) {
            intent.putExtra(ClinicDetailsActivity.EXTRA_PRESELECTED_PET, preselectedPet);
        }
        String mode = viewModel.getCurrentFilterMode();
        if ("exam".equals(mode) || "vaccine".equals(mode)
                || "spa".equals(mode) || "emergency".equals(mode)) {
            intent.putExtra(ClinicDetailsActivity.EXTRA_PREFERRED_CATEGORY, mode);
        }
        startActivity(intent);
    }

    private void observeViewModel() {
        viewModel.getLocationLabel().observe(this, label -> refreshLocationSubtitle(label));

        viewModel.getFilterMode().observe(this, this::syncActiveFilterChip);

        viewModel.getSortMode().observe(this, sortMode -> {
            if ("RATING".equals(sortMode)) {
                binding.tvSortOption.setText("Sắp xếp · Đánh giá cao");
            } else {
                binding.tvSortOption.setText("Sắp xếp · Gần nhất");
            }
        });

        viewModel.getDisplayedClinics().observe(this, list -> {
            int count = list != null ? list.size() : 0;
            binding.tvClinicCount.setText(count + " phòng khám");
            clinicAdapter.submitList(list);
        });

        viewModel.getUiState().observe(this, state -> {
            if (state == null) return;
            switch (state) {
                case LOADING:
                    showLoading();
                    break;
                case RESULTS:
                    showResultsState();
                    break;
                case EMPTY:
                    showEmpty();
                    break;
                case ERROR:
                    showError();
                    break;
            }
        });
    }

    private void setupBottomNav() {
        binding.navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        binding.navClinics.setOnClickListener(v ->
                binding.layoutResultsState.smoothScrollTo(0, 0));

        binding.navAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyAppointmentsActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        binding.navPets.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyPetsActivity.class);
            intent.putExtra(MyPetsActivity.EXTRA_BOOKING_MODE, false);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });
    }

    private void requestLocationPermission(boolean userTriggered) {
        pendingUserTriggeredGps = userTriggered;
        if (!UserLocationManager.hasLocationPermission(this)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQ_LOCATION);
        } else {
            updateUserLocation(userTriggered);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION) {
            boolean granted = false;
            for (int r : grantResults) {
                if (r == PackageManager.PERMISSION_GRANTED) {
                    granted = true;
                    break;
                }
            }
            if (granted) {
                updateUserLocation(pendingUserTriggeredGps);
            } else if (pendingUserTriggeredGps) {
                Toast.makeText(this, "Chưa cấp quyền vị trí — sử dụng mặc định " + UserLocationManager.DEFAULT_LABEL, Toast.LENGTH_SHORT).show();
            }
            pendingUserTriggeredGps = false;
        }
    }

    private void clearLocationListener() {
        if (activeLocationRequest != null) {
            activeLocationRequest.cancel();
            activeLocationRequest = null;
        }
    }

    @Override
    protected void onDestroy() {
        clearLocationListener();
        super.onDestroy();
    }

    private void updateUserLocation(boolean userTriggered) {
        clearLocationListener();
        if (userTriggered && !UserLocationManager.isEmulator()) {
            Toast.makeText(this, "Đang lấy vị trí GPS...", Toast.LENGTH_SHORT).show();
        }
        activeLocationRequest = UserLocationManager.fetchLiveLocation(this, userTriggered, state -> {
            if (isFinishing() || isDestroyed()) return;
            viewModel.updateLocation(state.latitude, state.longitude, state.label);
            if (userTriggered) {
                Toast.makeText(this, "Đã cập nhật vị trí: " + state.label, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.etSearchLocation.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s != null ? s.toString() : "");
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.btnFilter.setOnClickListener(v ->
                Toast.makeText(this, "Bộ lọc: Đang hiển thị các cơ sở tại Hà Nội", Toast.LENGTH_SHORT).show());

        binding.tvSortOption.setOnClickListener(v -> {
            viewModel.toggleSortMode();
            String msg = "RATING".equals(viewModel.getCurrentSortMode())
                    ? "Đã sắp xếp theo đánh giá cao nhất"
                    : "Đã sắp xếp theo khoảng cách gần nhất";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        binding.btnRetry.setOnClickListener(v -> viewModel.loadClinics());

        binding.btnClearFilter.setOnClickListener(v -> {
            binding.etSearchLocation.setText("");
            viewModel.resetFilters();
        });

        binding.tvChangeLocation.setOnClickListener(v -> showChangeLocationDialog());
        binding.tvLocationSubtitle.setOnClickListener(v -> showChangeLocationDialog());

        UiMotionUtils.attachSpringTouch(
                binding.btnBack,
                binding.btnFilter,
                binding.btnClearFilter,
                binding.btnRetry,
                binding.chipAll,
                binding.chipOpen,
                binding.chipNearby,
                binding.chipExam,
                binding.chipVaccine,
                binding.chipSpa,
                binding.chipEmergency
        );

        setupFilterChips();
    }

    private void refreshLocationSubtitle(String label) {
        String safeLabel = label != null ? label : UserLocationManager.DEFAULT_LABEL;
        if (preselectedPet != null && preselectedPet.getName() != null && !preselectedPet.getName().trim().isEmpty()) {
            binding.tvLocationSubtitle.setText("Đặt lịch cho " + preselectedPet.getName().trim() + " · " + safeLabel);
        } else {
            binding.tvLocationSubtitle.setText("Vị trí: " + safeLabel + " ▾");
        }
    }

    private void showChangeLocationDialog() {
        LocationPickerBottomSheet.show(this, new LocationPickerBottomSheet.OnLocationSelectedListener() {
            @Override
            public void onSelectGps() {
                UserLocationManager.setGpsMode(ClinicListActivity.this, true);
                if (UserLocationManager.isEmulator()) {
                    updateUserLocation(true);
                } else {
                    requestLocationPermission(true);
                }
            }

            @Override
            public void onSelectPreset(@NonNull UserLocationManager.PresetLocation preset) {
                UserLocationManager.saveManualLocation(ClinicListActivity.this, preset.latitude, preset.longitude, preset.label);
                viewModel.updateLocation(preset.latitude, preset.longitude, preset.label);
            }
        });
    }

    private void setupFilterChips() {
        binding.chipAll.setOnClickListener(v -> viewModel.setFilterMode("all"));
        binding.chipOpen.setOnClickListener(v -> viewModel.setFilterMode("open"));
        binding.chipNearby.setOnClickListener(v -> viewModel.setFilterMode("nearby"));
        binding.chipExam.setOnClickListener(v -> viewModel.setFilterMode("exam"));
        binding.chipVaccine.setOnClickListener(v -> viewModel.setFilterMode("vaccine"));
        binding.chipSpa.setOnClickListener(v -> viewModel.setFilterMode("spa"));
        binding.chipEmergency.setOnClickListener(v -> viewModel.setFilterMode("emergency"));
    }

    private void syncActiveFilterChip(String mode) {
        if ("open".equals(mode)) {
            setActiveChip(binding.chipOpen);
        } else if ("nearby".equals(mode)) {
            setActiveChip(binding.chipNearby);
        } else if ("exam".equals(mode)) {
            setActiveChip(binding.chipExam);
        } else if ("vaccine".equals(mode)) {
            setActiveChip(binding.chipVaccine);
        } else if ("spa".equals(mode)) {
            setActiveChip(binding.chipSpa);
        } else if ("emergency".equals(mode)) {
            setActiveChip(binding.chipEmergency);
        } else {
            setActiveChip(binding.chipAll);
        }
    }

    private void setActiveChip(View activeView) {
        TextView[] textChips = new TextView[]{
                binding.chipAll,
                binding.chipOpen,
                binding.chipExam,
                binding.chipVaccine,
                binding.chipSpa,
                binding.chipEmergency
        };
        int activeColor = ContextCompat.getColor(this, R.color.ds_primary);
        int inactiveColor = ContextCompat.getColor(this, R.color.ds_text_secondary);

        for (TextView chip : textChips) {
            boolean isActive = (chip == activeView);
            chip.setBackgroundResource(isActive ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
            chip.setTextColor(isActive ? activeColor : inactiveColor);
            UiMotionUtils.applyFont(chip, isActive ? R.font.open_sans_semibold : R.font.open_sans_medium);
        }

        boolean nearbyActive = (activeView == binding.chipNearby);
        binding.chipNearby.setBackgroundResource(nearbyActive ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
        binding.tvChipNearby.setTextColor(nearbyActive ? activeColor : inactiveColor);
        binding.ivChipNearby.setColorFilter(nearbyActive ? activeColor : inactiveColor);
        UiMotionUtils.applyFont(binding.tvChipNearby, nearbyActive ? R.font.open_sans_semibold : R.font.open_sans_medium);
    }

    public static boolean matchesServiceCategory(String serviceName, String category) {
        if (serviceName == null || category == null) return false;
        String lower = serviceName.toLowerCase(Locale.ROOT);
        switch (category) {
            case "exam":
                return lower.contains("khám") || lower.contains("siêu âm") || lower.contains("xét nghiệm");
            case "vaccine":
                return lower.contains("tiêm") || lower.contains("vắc") || lower.contains("vaccine");
            case "spa":
                return lower.contains("tắm") || lower.contains("spa") || lower.contains("cắt tỉa")
                        || lower.contains("cắt móng") || lower.contains("chăm sóc lông") || lower.contains("dưỡng ẩm lông");
            case "emergency":
                return lower.contains("cấp cứu") || lower.contains("24/7") || lower.contains("hồi sức");
            default:
                return true;
        }
    }

    private void showResultsState() {
        binding.layoutResultsState.setVisibility(View.VISIBLE);
        binding.layoutLoadingState.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.GONE);
        binding.layoutErrorState.setVisibility(View.GONE);
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
