package com.example.petcareui.ui.clinic;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.petcareui.data.dto.response.ClinicResponse;
import com.example.petcareui.data.model.ClinicItem;
import com.example.petcareui.data.repository.ClinicRepository;
import com.example.petcareui.data.repository.RepositoryCallback;
import com.example.petcareui.utils.UserLocationManager;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ClinicListViewModel extends ViewModel {

    public enum UiState {
        LOADING, RESULTS, EMPTY, ERROR
    }

    public static final double DEFAULT_LAT = UserLocationManager.DEFAULT_LAT;
    public static final double DEFAULT_LNG = UserLocationManager.DEFAULT_LNG;

    private final ClinicRepository clinicRepository = new ClinicRepository();
    private final List<ClinicItem> allClinics = new ArrayList<>();

    private final MutableLiveData<UiState> uiState = new MutableLiveData<>(UiState.LOADING);
    private final MutableLiveData<List<ClinicItem>> displayedClinics = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<String> filterMode = new MutableLiveData<>("all");
    private final MutableLiveData<String> sortMode = new MutableLiveData<>("NEAREST");
    private final MutableLiveData<String> locationLabel = new MutableLiveData<>(UserLocationManager.DEFAULT_LABEL);

    private String searchQuery = "";
    private double userLatitude = DEFAULT_LAT;
    private double userLongitude = DEFAULT_LNG;

    public LiveData<UiState> getUiState() {
        return uiState;
    }

    public LiveData<List<ClinicItem>> getDisplayedClinics() {
        return displayedClinics;
    }

    public LiveData<String> getFilterMode() {
        return filterMode;
    }

    public LiveData<String> getSortMode() {
        return sortMode;
    }

    public LiveData<String> getLocationLabel() {
        return locationLabel;
    }

    public String getCurrentFilterMode() {
        String mode = filterMode.getValue();
        return mode != null ? mode : "all";
    }

    public String getCurrentSortMode() {
        String mode = sortMode.getValue();
        return mode != null ? mode : "NEAREST";
    }

    public void loadClinics() {
        uiState.setValue(UiState.LOADING);
        clinicRepository.getAllClinics(null, new RepositoryCallback<List<ClinicResponse>>() {
            @Override
            public void onSuccess(List<ClinicResponse> data) {
                allClinics.clear();
                for (ClinicResponse res : data) {
                    allClinics.add(res.toClinicItem());
                }
                calculateDistances();
                applyFilterAndSort();
            }

            @Override
            public void onError(int httpCode, String message) {
                uiState.setValue(UiState.ERROR);
            }
        });
    }

    public void setFilterMode(String mode) {
        String safeMode = mode != null ? mode : "all";
        filterMode.setValue(safeMode);
        if ("nearby".equals(safeMode)) {
            sortMode.setValue("NEAREST");
        }
        if (!allClinics.isEmpty()) {
            applyFilterAndSort();
        }
    }

    public void toggleSortMode() {
        String next = "NEAREST".equals(getCurrentSortMode()) ? "RATING" : "NEAREST";
        sortMode.setValue(next);
        if (!allClinics.isEmpty()) {
            applyFilterAndSort();
        }
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query != null ? query : "";
        if (!allClinics.isEmpty()) {
            applyFilterAndSort();
        }
    }

    public void resetFilters() {
        this.searchQuery = "";
        filterMode.setValue("all");
        if (!allClinics.isEmpty()) {
            applyFilterAndSort();
        }
    }

    public void updateLocation(double lat, double lng, String label) {
        this.userLatitude = lat;
        this.userLongitude = lng;
        if (label != null) {
            this.locationLabel.setValue(label);
        }
        if (!allClinics.isEmpty()) {
            calculateDistances();
            applyFilterAndSort();
        }
    }

    private void calculateDistances() {
        for (ClinicItem c : allClinics) {
            UserLocationManager.applyDistance(c, userLatitude, userLongitude);
        }
    }

    private void applyFilterAndSort() {
        List<ClinicItem> filtered = new ArrayList<>();
        String cleanQuery = removeAccents(searchQuery.trim());
        String mode = getCurrentFilterMode();

        for (ClinicItem clinic : allClinics) {
            boolean matchesSearch = true;
            if (!cleanQuery.isEmpty()) {
                String name = removeAccents(clinic.getName());
                String area = removeAccents(clinic.getArea());
                String address = removeAccents(clinic.getAddress());
                String hours = removeAccents(clinic.getHours());
                boolean serviceMatch = false;
                if (clinic.getServiceNames() != null) {
                    for (String sn : clinic.getServiceNames()) {
                        if (removeAccents(sn).contains(cleanQuery)) {
                            serviceMatch = true;
                            break;
                        }
                    }
                }
                matchesSearch = name.contains(cleanQuery)
                        || area.contains(cleanQuery)
                        || address.contains(cleanQuery)
                        || hours.contains(cleanQuery)
                        || serviceMatch;
            }

            if (!matchesSearch) continue;

            boolean matchesChip = true;
            if ("open".equals(mode)) {
                matchesChip = clinic.isOpen();
            } else if ("nearby".equals(mode)) {
                matchesChip = clinic.getDistanceKm() <= 8.0f;
            } else if ("exam".equals(mode) || "vaccine".equals(mode)
                    || "spa".equals(mode) || "emergency".equals(mode)) {
                matchesChip = clinicSupportsCategory(clinic, mode);
            }

            if (matchesChip) {
                filtered.add(clinic);
            }
        }

        if ("RATING".equals(getCurrentSortMode())) {
            Collections.sort(filtered, (a, b) -> Double.compare(b.getRating(), a.getRating()));
        } else {
            Collections.sort(filtered, (a, b) -> Float.compare(a.getDistanceKm(), b.getDistanceKm()));
        }

        displayedClinics.setValue(filtered);
        uiState.setValue(filtered.isEmpty() ? UiState.EMPTY : UiState.RESULTS);
    }

    private boolean clinicSupportsCategory(ClinicItem clinic, String category) {
        if ("emergency".equals(category)) {
            String hours = clinic.getHours() != null ? clinic.getHours().toLowerCase(Locale.ROOT) : "";
            if (hours.contains("24/7") || hours.contains("cấp cứu")) {
                return true;
            }
        }
        if (clinic.getServiceNames() != null) {
            for (String sn : clinic.getServiceNames()) {
                if (ClinicListActivity.matchesServiceCategory(sn, category)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String removeAccents(String text) {
        if (text == null) return "";
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("đ", "d")
                .replaceAll("Đ", "D")
                .toLowerCase(Locale.ROOT);
    }
}
