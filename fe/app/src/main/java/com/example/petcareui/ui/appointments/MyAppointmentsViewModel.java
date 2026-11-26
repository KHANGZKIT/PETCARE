package com.example.petcareui.ui.appointments;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.petcareui.data.dto.response.AppointmentResponse;
import com.example.petcareui.data.model.AppointmentItem;
import com.example.petcareui.data.repository.AppointmentRepository;
import com.example.petcareui.data.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyAppointmentsViewModel extends ViewModel {

    public static class AppointmentsUiState {
        public final String activeTab;
        public final List<AppointmentItem> primaryItems;
        public final List<AppointmentItem> pastPreviewItems;
        public final int upcomingCount;
        public final int pastCount;
        public final boolean isEmpty;

        public AppointmentsUiState(String activeTab,
                                   List<AppointmentItem> primaryItems,
                                   List<AppointmentItem> pastPreviewItems,
                                   int upcomingCount,
                                   int pastCount,
                                   boolean isEmpty) {
            this.activeTab = activeTab;
            this.primaryItems = primaryItems;
            this.pastPreviewItems = pastPreviewItems;
            this.upcomingCount = upcomingCount;
            this.pastCount = pastCount;
            this.isEmpty = isEmpty;
        }
    }

    private final AppointmentRepository repository = new AppointmentRepository();

    private final List<AppointmentItem> upcomingList = new ArrayList<>();
    private final List<AppointmentItem> pastList = new ArrayList<>();
    private final List<AppointmentItem> allList = new ArrayList<>();

    private final MutableLiveData<AppointmentsUiState> uiState = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private String currentTab = "upcoming";

    public LiveData<AppointmentsUiState> getUiState() {
        return uiState;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void setCurrentTab(String tab) {
        if (tab != null && !tab.isEmpty()) {
            this.currentTab = tab;
        }
        publishState();
    }

    public void clearError() {
        errorMessage.setValue(null);
    }

    public void loadAppointments() {
        repository.getMyAppointments(new RepositoryCallback<List<AppointmentResponse>>() {
            @Override
            public void onSuccess(List<AppointmentResponse> data) {
                upcomingList.clear();
                pastList.clear();
                allList.clear();

                for (AppointmentResponse res : data) {
                    AppointmentItem item = res.toAppointmentItem();
                    allList.add(item);
                    if (item.isUpcoming()) {
                        upcomingList.add(item);
                    } else {
                        pastList.add(item);
                    }
                }
                publishState();
            }

            @Override
            public void onError(int httpCode, String message) {
                upcomingList.clear();
                pastList.clear();
                allList.clear();
                publishState();
                errorMessage.setValue(message);
            }
        });
    }

    private void publishState() {
        if (allList.isEmpty()) {
            uiState.setValue(new AppointmentsUiState(
                    "empty",
                    Collections.emptyList(),
                    Collections.emptyList(),
                    0,
                    0,
                    true
            ));
            return;
        }

        List<AppointmentItem> primary;
        List<AppointmentItem> pastPreview = Collections.emptyList();

        if ("past".equals(currentTab)) {
            primary = new ArrayList<>(pastList);
        } else if ("all".equals(currentTab)) {
            primary = new ArrayList<>(allList);
        } else {
            primary = new ArrayList<>(upcomingList);
            if (!pastList.isEmpty()) {
                pastPreview = new ArrayList<>(pastList.subList(0, Math.min(2, pastList.size())));
            }
        }

        uiState.setValue(new AppointmentsUiState(
                currentTab,
                primary,
                pastPreview,
                upcomingList.size(),
                pastList.size(),
                false
        ));
    }
}
