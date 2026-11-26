package com.example.petcareui.data.repository;

import com.example.petcareui.data.api.ApiClient;
import com.example.petcareui.data.api.ApiErrorParser;
import com.example.petcareui.data.api.ClinicApi;
import com.example.petcareui.data.dto.response.ClinicResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClinicRepository {

    private final ClinicApi clinicApi;

    public ClinicRepository() {
        this.clinicApi = ApiClient.getClinicApi();
    }

    public void getAllClinics(String area, RepositoryCallback<List<ClinicResponse>> callback) {
        clinicApi.getAllClinics(area).enqueue(new Callback<List<ClinicResponse>>() {
            @Override
            public void onResponse(Call<List<ClinicResponse>> call, Response<List<ClinicResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.code(),
                            ApiErrorParser.getMessage(response, "Không tải được danh sách phòng khám"));
                }
            }

            @Override
            public void onFailure(Call<List<ClinicResponse>> call, Throwable t) {
                callback.onError(0, "Không kết nối được với hệ thống. Vui lòng kiểm tra kết nối mạng.");
            }
        });
    }
}
