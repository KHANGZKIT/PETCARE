package com.example.petcareui;

import android.app.Application;

import com.example.petcareui.data.api.ApiClient;

/** Initializes networking once with application-scoped context. */
public class PetCareApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ApiClient.initialize(this);
    }
}
