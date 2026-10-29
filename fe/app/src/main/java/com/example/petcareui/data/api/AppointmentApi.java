package com.example.petcareui.data.api;

import com.example.petcareui.data.model.AppointmentRequest;
import com.example.petcareui.data.model.AppointmentResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AppointmentApi {
    @POST("appointments")
    Call<AppointmentResponse> createAppointment(@Body AppointmentRequest request);

    @GET("appointments/my")
    Call<List<AppointmentResponse>> getMyAppointments();

    @GET("appointments/{id}")
    Call<AppointmentResponse> getAppointmentById(@Path("id") Long id);

    @PUT("appointments/{id}/cancel")
    Call<AppointmentResponse> cancelAppointment(@Path("id") Long id);
}
