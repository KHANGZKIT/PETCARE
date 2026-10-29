package com.example.petcareui.data.api;

import com.example.petcareui.data.model.PetRequest;
import com.example.petcareui.data.model.PetResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface PetApi {
    @GET("pets")
    Call<List<PetResponse>> getMyPets();

    @POST("pets")
    Call<PetResponse> addPet(@Body PetRequest request);
}
