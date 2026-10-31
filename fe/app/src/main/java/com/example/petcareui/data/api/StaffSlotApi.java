package com.example.petcareui.data.api;

import com.example.petcareui.data.model.ClinicSlotResponse;
import com.example.petcareui.data.model.CreateSlotRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface StaffSlotApi {

    @GET("staff/slots")
    Call<List<ClinicSlotResponse>> getStaffSlots(@Query("date") String date);

    @POST("staff/slots")
    Call<ClinicSlotResponse> createSlot(@Body CreateSlotRequest request);
}
