package com.example.petcareui.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.petcareui.data.dto.response.LoginResponse;
import com.example.petcareui.data.repository.AuthRepository;

public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;

    private final MutableLiveData<LoginResponse> loginResult = new MutableLiveData<>();
    private final MutableLiveData<LoginResponse> registerResult = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public AuthViewModel() {
        this.authRepository = new AuthRepository();
    }

    public LiveData<LoginResponse> getLoginResult() {
        return loginResult;
    }

    public LiveData<LoginResponse> getRegisterResult() {
        return registerResult;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void login(String email, String password) {
        String cleanEmail = email != null ? email.trim() : "";
        String cleanPassword = password != null ? password.trim() : "";

        if (cleanEmail.isEmpty()) {
            errorMessage.setValue("Vui lòng nhập email.");
            return;
        }
        if (cleanPassword.isEmpty()) {
            errorMessage.setValue("Vui lòng nhập mật khẩu.");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);
        authRepository.login(cleanEmail, cleanPassword, new AuthRepository.AuthCallback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse data) {
                isLoading.setValue(false);
                loginResult.setValue(data);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void register(String fullName, String email, String phone, String password) {
        String cleanName = fullName != null ? fullName.trim() : "";
        String cleanEmail = email != null ? email.trim() : "";
        String cleanPhone = phone != null ? phone.trim() : "";
        String cleanPass = password != null ? password.trim() : "";

        if (cleanName.isEmpty() || cleanEmail.isEmpty() || cleanPhone.isEmpty() || cleanPass.isEmpty()) {
            errorMessage.setValue("Vui lòng điền đầy đủ các thông tin bắt buộc.");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);
        authRepository.register(cleanName, cleanEmail, cleanPhone, cleanPass, new AuthRepository.AuthCallback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse data) {
                isLoading.setValue(false);
                registerResult.setValue(data);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }
}
