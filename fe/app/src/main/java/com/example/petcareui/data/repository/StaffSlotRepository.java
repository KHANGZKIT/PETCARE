package com.example.petcareui.data.repository;

import com.example.petcareui.data.api.ApiClient;
import com.example.petcareui.data.api.ApiErrorParser;
import com.example.petcareui.data.api.StaffSlotApi;
import com.example.petcareui.data.dto.request.CreateSlotRequest;
import com.example.petcareui.data.dto.request.UpdateSlotStatusRequest;
import com.example.petcareui.data.dto.response.ClinicSlotResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffSlotRepository {

    private final StaffSlotApi staffSlotApi;

    public StaffSlotRepository() {
        this.staffSlotApi = ApiClient.getStaffSlotApi();
    }

    public void getStaffSlots(String isoDate, RepositoryCallback<List<ClinicSlotResponse>> callback) {
        staffSlotApi.getStaffSlots(isoDate).enqueue(new Callback<List<ClinicSlotResponse>>() {
            @Override
            public void onResponse(Call<List<ClinicSlotResponse>> call, Response<List<ClinicSlotResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.code(),
                            ApiErrorParser.getMessage(response, "Không tải được khung giờ"));
                }
            }

            @Override
            public void onFailure(Call<List<ClinicSlotResponse>> call, Throwable t) {
                callback.onError(0, "Lỗi kết nối máy chủ khi tải khung giờ");
            }
        });
    }

    public void createSlot(CreateSlotRequest request, RepositoryCallback<ClinicSlotResponse> callback) {
        staffSlotApi.createSlot(request).enqueue(new Callback<ClinicSlotResponse>() {
            @Override
            public void onResponse(Call<ClinicSlotResponse> call, Response<ClinicSlotResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.code(),
                            ApiErrorParser.getMessage(response, "Không thể tạo khung giờ. Vui lòng kiểm tra lại thời gian."));
                }
            }

            @Override
            public void onFailure(Call<ClinicSlotResponse> call, Throwable t) {
                callback.onError(0, "Lỗi kết nối máy chủ khi tạo khung giờ.");
            }
        });
    }

    public void updateSlotStatus(Long slotId, String targetStatus, RepositoryCallback<ClinicSlotResponse> callback) {
        staffSlotApi.updateSlotStatus(slotId, new UpdateSlotStatusRequest(targetStatus))
                .enqueue(new Callback<ClinicSlotResponse>() {
                    @Override
                    public void onResponse(Call<ClinicSlotResponse> call, Response<ClinicSlotResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError(response.code(),
                                    ApiErrorParser.getMessage(response, "Không thể cập nhật trạng thái khung giờ"));
                        }
                    }

                    @Override
                    public void onFailure(Call<ClinicSlotResponse> call, Throwable t) {
                        callback.onError(0, "Lỗi kết nối máy chủ khi cập nhật khung giờ");
                    }
                });
    }
}
