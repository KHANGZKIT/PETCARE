package com.example.petcareui.data.api;

import org.json.JSONObject;

import java.util.Iterator;

import retrofit2.Response;

/** Parses Spring ErrorResponse without exposing raw response payloads to users. */
public final class ApiErrorParser {
    private ApiErrorParser() { }

    public static String getMessage(Response<?> response, String fallback) {
        if (response != null && response.errorBody() != null) {
            try {
                JSONObject json = new JSONObject(response.errorBody().string());
                String message = json.optString("message", "");
                if (!message.isBlank()) return message;

                JSONObject errors = json.optJSONObject("errors");
                if (errors != null) {
                    StringBuilder builder = new StringBuilder();
                    Iterator<String> keys = errors.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        if (builder.length() > 0) builder.append("; ");
                        builder.append(key).append(": ").append(errors.optString(key));
                    }
                    if (builder.length() > 0) return builder.toString();
                }
            } catch (Exception ignored) {
                // Keep a user-safe fallback if the response body is absent or malformed.
            }
        }
        return fallback + (response == null ? "" : " (HTTP " + response.code() + ")");
    }
}
