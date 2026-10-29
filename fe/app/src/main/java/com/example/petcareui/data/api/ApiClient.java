package com.example.petcareui.data.api;

import android.content.Context;

import com.example.petcareui.utils.Constants;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class ApiClient {

    private static Retrofit retrofit = null;
    private static Context applicationContext;

    public static synchronized void initialize(Context context) {
        applicationContext = context.getApplicationContext();
    }

    public static synchronized Retrofit getClient() {
        if (retrofit == null) {
            if (applicationContext == null) {
                throw new IllegalStateException("ApiClient chưa được khởi tạo từ Application");
            }

            // BASIC logging avoids writing request bodies such as passwords to Logcat.
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.redactHeader("Authorization");
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(applicationContext))
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static AuthApi getAuthApi() {
        return getClient().create(AuthApi.class);
    }

    public static ClinicApi getClinicApi() {
        return getClient().create(ClinicApi.class);
    }

    public static PetApi getPetApi() {
        return getClient().create(PetApi.class);
    }

    public static AppointmentApi getAppointmentApi() {
        return getClient().create(AppointmentApi.class);
    }
}
