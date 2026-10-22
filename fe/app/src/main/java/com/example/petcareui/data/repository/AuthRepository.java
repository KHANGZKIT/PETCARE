package com.example.petcareui.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.petcareui.data.api.ApiClient;
import com.example.petcareui.data.api.AuthApi;
import com.example.petcareui.data.model.LoginRequest;
import com.example.petcareui.data.model.LoginResponse;
import com.example.petcareui.data.model.RegisterRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private final AuthApi authApi;

    public interface AuthCallback<T> {
        void onSuccess(T data);
        void onError(String errorMessage);
    }

    public AuthRepository() {
        this.authApi = ApiClient.getAuthApi();
    }

    public void login(String email, String password, AuthCallback<LoginResponse> callback) {
        LoginRequest request = new LoginRequest(email, password);
        authApi.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Đăng nhập thất bại: Kiểm tra lại email hoặc mật khẩu (Mã " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void register(String fullName, String email, String phone, String password, AuthCallback<LoginResponse> callback) {
        RegisterRequest request = new RegisterRequest(fullName, email, phone, password);
        authApi.register(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Đăng ký thất bại: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}
