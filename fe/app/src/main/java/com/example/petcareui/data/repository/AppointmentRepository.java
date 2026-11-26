package com.example.petcareui.data.repository;

import com.example.petcareui.data.api.ApiClient;
import com.example.petcareui.data.api.ApiErrorParser;
import com.example.petcareui.data.api.AppointmentApi;
import com.example.petcareui.data.dto.response.AppointmentResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppointmentRepository {

    private final AppointmentApi appointmentApi;

    public AppointmentRepository() {
        this.appointmentApi = ApiClient.getAppointmentApi();
    }

    public void getMyAppointments(RepositoryCallback<List<AppointmentResponse>> callback) {
        appointmentApi.getMyAppointments().enqueue(new Callback<List<AppointmentResponse>>() {
            @Override
            public void onResponse(Call<List<AppointmentResponse>> call, Response<List<AppointmentResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.code(),
                            ApiErrorParser.getMessage(response, "Không thể tải danh sách lịch hẹn"));
                }
            }

            @Override
            public void onFailure(Call<List<AppointmentResponse>> call, Throwable t) {
                callback.onError(0, "Lỗi kết nối máy chủ khi tải lịch hẹn.");
            }
        });
    }

    public void getClinicAppointments(String status, RepositoryCallback<List<AppointmentResponse>> callback) {
        appointmentApi.getClinicAppointments(status).enqueue(new Callback<List<AppointmentResponse>>() {
            @Override
            public void onResponse(Call<List<AppointmentResponse>> call, Response<List<AppointmentResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.code(),
                            ApiErrorParser.getMessage(response, "Không tải được danh sách lịch hẹn phòng khám"));
                }
            }

            @Override
            public void onFailure(Call<List<AppointmentResponse>> call, Throwable t) {
                callback.onError(0, "Lỗi kết nối máy chủ khi tải lịch hẹn phòng khám");
            }
        });
    }
}
