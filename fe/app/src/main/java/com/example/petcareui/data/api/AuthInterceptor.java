package com.example.petcareui.data.api;

import android.content.Context;

import com.example.petcareui.utils.TokenManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/** Adds JWT to protected API calls without sending a stale token to login/register. */
public final class AuthInterceptor implements Interceptor {
    private final Context applicationContext;

    public AuthInterceptor(Context context) {
        this.applicationContext = context.getApplicationContext();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String path = original.url().encodedPath();
        boolean publicAuthEndpoint = path.endsWith("/auth/login") || path.endsWith("/auth/register");
        Request.Builder builder = original.newBuilder();

        if (!publicAuthEndpoint) {
            String token = TokenManager.getInstance(applicationContext).getToken();
            if (token != null && !token.isBlank()) {
                builder.header("Authorization", "Bearer " + token);
            }
        }
        return chain.proceed(builder.build());
    }
}
