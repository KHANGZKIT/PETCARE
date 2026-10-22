package com.example.petcareui.data.api;

import com.example.petcareui.data.model.LoginRequest;
import com.example.petcareui.data.model.LoginResponse;
import com.example.petcareui.data.model.RegisterRequest;
import com.example.petcareui.data.model.UserProfileResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<LoginResponse> register(@Body RegisterRequest request);

    @GET("auth/me")
    Call<UserProfileResponse> getCurrentUser(@Header("Authorization") String bearerToken);
}
