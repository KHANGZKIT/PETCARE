package com.example.petcareui.data.repository;

/**
 * Callback chuẩn cho tầng Repository trả về dữ liệu hoặc mã lỗi HTTP + thông báo an toàn.
 */
public interface RepositoryCallback<T> {
    void onSuccess(T data);
    void onError(int httpCode, String message);
}
