package com.example.petcareui.data.repository;

import com.example.petcareui.data.api.ApiClient;
import com.example.petcareui.data.api.ApiErrorParser;
import com.example.petcareui.data.api.AuthApi;
import com.example.petcareui.data.dto.request.LoginRequest;
import com.example.petcareui.data.dto.request.RegisterRequest;
import com.example.petcareui.data.dto.response.LoginResponse;

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
                } else if (response.code() == 401 || response.code() == 400) {
                    callback.onError(ApiErrorParser.getMessage(response, "Thông tin đăng nhập chưa chính xác"));
                } else {
                    callback.onError(ApiErrorParser.getMessage(response, "Đăng nhập không thành công"));
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                callback.onError("Không thể kết nối máy chủ. Vui lòng kiểm tra lại kết nối mạng và thử lại.");
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
                } else if (response.code() == 409) {
                    callback.onError(ApiErrorParser.getMessage(response, "Email đã được sử dụng. Vui lòng đăng nhập hoặc dùng email khác."));
                } else {
                    callback.onError(ApiErrorParser.getMessage(response, "Đăng ký không thành công"));
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                callback.onError("Không thể kết nối máy chủ. Vui lòng kiểm tra lại kết nối mạng và thử lại.");
            }
        });
    }
}
