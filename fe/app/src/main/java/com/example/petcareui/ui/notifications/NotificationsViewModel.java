package com.example.petcareui.ui.notifications;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.petcareui.data.dto.response.AppointmentResponse;
import com.example.petcareui.data.repository.AppointmentRepository;
import com.example.petcareui.data.repository.RepositoryCallback;

import java.util.Collections;
import java.util.List;

public class NotificationsViewModel extends ViewModel {

    private final AppointmentRepository appointmentRepository = new AppointmentRepository();

    private final MutableLiveData<List<AppointmentResponse>> appointmentSnapshots = new MutableLiveData<>();

    public LiveData<List<AppointmentResponse>> getAppointmentSnapshots() {
        return appointmentSnapshots;
    }

    public void loadNotifications(boolean isStaff) {
        RepositoryCallback<List<AppointmentResponse>> callback = new RepositoryCallback<List<AppointmentResponse>>() {
            @Override
            public void onSuccess(List<AppointmentResponse> data) {
                appointmentSnapshots.setValue(data != null ? data : Collections.emptyList());
            }

            @Override
            public void onError(int httpCode, String message) {
                appointmentSnapshots.setValue(Collections.emptyList());
            }
        };

        if (isStaff) {
            appointmentRepository.getClinicAppointments(null, callback);
        } else {
            appointmentRepository.getMyAppointments(callback);
        }
    }
}
