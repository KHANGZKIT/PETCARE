package com.example.petcareui.data.api;

import com.example.petcareui.data.model.AppointmentRequest;
import com.example.petcareui.data.model.AppointmentResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AppointmentApi {

    @POST("appointments")
    Call<AppointmentResponse> createAppointment(@Body AppointmentRequest request);

    @GET("appointments/my")
    Call<List<AppointmentResponse>> getMyAppointments();

    @GET("appointments/{id}")
    Call<AppointmentResponse> getAppointmentById(@Path("id") Long id);

    @PUT("appointments/{id}/cancel")
    Call<AppointmentResponse> cancelAppointment(@Path("id") Long id);

    @PATCH("appointments/{id}/cancel")
    Call<AppointmentResponse> cancelAppointmentPatch(@Path("id") Long id);

    @GET("appointments/clinic")
    Call<List<AppointmentResponse>> getClinicAppointments(@Query("status") String status);

    @GET("staff/appointments/{id}")
    Call<AppointmentResponse> getStaffAppointmentDetail(@Path("id") Long id);

    @PUT("staff/appointments/{id}/confirm")
    Call<AppointmentResponse> confirmAppointment(@Path("id") Long id);

    @PUT("staff/appointments/{id}/reject")
    Call<AppointmentResponse> rejectAppointment(@Path("id") Long id);

    @PUT("staff/appointments/{id}/complete")
    Call<AppointmentResponse> completeAppointment(@Path("id") Long id);

    @PUT("staff/appointments/{id}/no-show")
    Call<AppointmentResponse> noShowAppointment(@Path("id") Long id);
}
