package com.example.petcareui.data.repository;

import com.example.petcareui.data.api.ApiClient;
import com.example.petcareui.data.api.ApiErrorParser;
import com.example.petcareui.data.api.PetApi;
import com.example.petcareui.data.dto.response.PetResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PetRepository {

    private final PetApi petApi;

    public PetRepository() {
        this.petApi = ApiClient.getPetApi();
    }

    public void getMyPets(RepositoryCallback<List<PetResponse>> callback) {
        petApi.getMyPets().enqueue(new Callback<List<PetResponse>>() {
            @Override
            public void onResponse(Call<List<PetResponse>> call, Response<List<PetResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.code(),
                            ApiErrorParser.getMessage(response, "Không tải được danh sách thú cưng"));
                }
            }

            @Override
            public void onFailure(Call<List<PetResponse>> call, Throwable t) {
                callback.onError(0, "Lỗi kết nối máy chủ khi tải danh sách thú cưng");
            }
        });
    }

    public void deletePet(Long petId, RepositoryCallback<Void> callback) {
        petApi.deletePet(petId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(response.code(),
                            ApiErrorParser.getMessage(response, "Không thể xóa bé lúc này"));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(0, "Lỗi kết nối khi xóa thú cưng");
            }
        });
    }
}
