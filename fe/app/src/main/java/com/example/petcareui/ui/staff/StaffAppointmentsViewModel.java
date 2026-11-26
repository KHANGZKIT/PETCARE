package com.example.petcareui.ui.staff;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.petcareui.data.dto.response.AppointmentResponse;
import com.example.petcareui.data.model.StaffAppointmentItem;
import com.example.petcareui.data.repository.AppointmentRepository;
import com.example.petcareui.data.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class StaffAppointmentsViewModel extends ViewModel {

    public static class FilterCounts {
        public final int all;
        public final int pending;
        public final int confirmed;
        public final int completed;
        public final int noShow;

        public FilterCounts(int all, int pending, int confirmed, int completed, int noShow) {
            this.all = all;
            this.pending = pending;
            this.confirmed = confirmed;
            this.completed = completed;
            this.noShow = noShow;
        }
    }

    private final AppointmentRepository repository = new AppointmentRepository();
    private final List<StaffAppointmentItem> allAppointments = new ArrayList<>();

    private final MutableLiveData<String> uiState = new MutableLiveData<>("loading");
    private final MutableLiveData<List<StaffAppointmentItem>> displayedAppointments =
            new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<FilterCounts> filterCounts =
            new MutableLiveData<>(new FilterCounts(0, 0, 0, 0, 0));
    private final MutableLiveData<String> currentFilter = new MutableLiveData<>("PENDING");
    private final MutableLiveData<Boolean> unauthorizedEvent = new MutableLiveData<>(false);

    private String searchQuery = "";

    public LiveData<String> getUiState() {
        return uiState;
    }

    public LiveData<List<StaffAppointmentItem>> getDisplayedAppointments() {
        return displayedAppointments;
    }

    public LiveData<FilterCounts> getFilterCounts() {
        return filterCounts;
    }

    public LiveData<String> getCurrentFilter() {
        return currentFilter;
    }

    public LiveData<Boolean> getUnauthorizedEvent() {
        return unauthorizedEvent;
    }

    public boolean isAllListEmpty() {
        return allAppointments.isEmpty();
    }

    public void setInitialFilter(String filter) {
        if (filter != null && !filter.isEmpty()) {
            currentFilter.setValue(filter);
        }
    }

    public void setFilter(String filter) {
        currentFilter.setValue(filter != null ? filter : "ALL");
        if (!"loading".equals(uiState.getValue()) && !"error".equals(uiState.getValue())) {
            applyFilter();
        }
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query != null ? query.trim().toLowerCase(Locale.ROOT) : "";
        if (!"loading".equals(uiState.getValue()) && !"error".equals(uiState.getValue())) {
            applyFilter();
        }
    }

    public void resetSearchAndFilter() {
        this.searchQuery = "";
        currentFilter.setValue("ALL");
        applyFilter();
    }

    public void loadClinicAppointments() {
        uiState.setValue("loading");
        repository.getClinicAppointments(null, new RepositoryCallback<List<AppointmentResponse>>() {
            @Override
            public void onSuccess(List<AppointmentResponse> data) {
                allAppointments.clear();
                for (AppointmentResponse res : data) {
                    allAppointments.add(res.toStaffAppointmentItem());
                }
                publishCounts();
                if (allAppointments.isEmpty()) {
                    displayedAppointments.setValue(Collections.emptyList());
                    uiState.setValue("empty");
                } else {
                    applyFilter();
                }
            }

            @Override
            public void onError(int httpCode, String message) {
                if (httpCode == 401 || httpCode == 403) {
                    unauthorizedEvent.setValue(true);
                    return;
                }
                allAppointments.clear();
                publishCounts();
                displayedAppointments.setValue(Collections.emptyList());
                uiState.setValue("error");
            }
        });
    }

    private void publishCounts() {
        int pending = 0;
        int confirmed = 0;
        int completed = 0;
        int noShow = 0;
        for (StaffAppointmentItem item : allAppointments) {
            if ("PENDING".equalsIgnoreCase(item.getStatus())) pending++;
            else if ("CONFIRMED".equalsIgnoreCase(item.getStatus())) confirmed++;
            else if ("COMPLETED".equalsIgnoreCase(item.getStatus())) completed++;
            else if ("NO_SHOW".equalsIgnoreCase(item.getStatus())) noShow++;
        }
        filterCounts.setValue(new FilterCounts(allAppointments.size(), pending, confirmed, completed, noShow));
    }

    private void applyFilter() {
        String activeFilter = currentFilter.getValue() != null ? currentFilter.getValue() : "ALL";
        List<StaffAppointmentItem> result = new ArrayList<>();
        for (StaffAppointmentItem item : allAppointments) {
            boolean matchesFilter = "ALL".equalsIgnoreCase(activeFilter)
                    || activeFilter.equalsIgnoreCase(item.getStatus());
            boolean matchesSearch = searchQuery.isEmpty()
                    || (item.getPet() != null && item.getPet().toLowerCase(Locale.ROOT).contains(searchQuery))
                    || (item.getCustomer() != null && item.getCustomer().toLowerCase(Locale.ROOT).contains(searchQuery))
                    || (item.getService() != null && item.getService().toLowerCase(Locale.ROOT).contains(searchQuery));
            if (matchesFilter && matchesSearch) {
                result.add(item);
            }
        }
        displayedAppointments.setValue(result);
        uiState.setValue(result.isEmpty() ? "empty" : "default");
    }
}
