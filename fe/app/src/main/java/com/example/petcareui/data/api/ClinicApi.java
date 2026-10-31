package com.example.petcareui.data.api;

import com.example.petcareui.data.model.ClinicResponse;
import com.example.petcareui.data.model.ClinicSlotResponse;
import com.example.petcareui.data.model.ServiceResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ClinicApi {

    @GET("clinics")
    Call<List<ClinicResponse>> getAllClinics(@Query("area") String area);

    @GET("clinics/{id}")
    Call<ClinicResponse> getClinicById(@Path("id") Long id);

    @GET("clinics/{id}/services")
    Call<List<ServiceResponse>> getServicesByClinicId(@Path("id") Long id);

    @GET("services/{id}")
    Call<ServiceResponse> getServiceById(@Path("id") Long id);

    @GET("clinics/{clinicId}/slots")
    Call<List<ClinicSlotResponse>> getSlotsByClinicAndDate(
            @Path("clinicId") Long clinicId,
            @Query("date") String date
    );
}
