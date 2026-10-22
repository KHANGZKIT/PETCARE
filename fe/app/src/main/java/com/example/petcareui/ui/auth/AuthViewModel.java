package com.example.petcareui.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.petcareui.data.model.LoginResponse;
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
        if (email.trim().isEmpty() || password.trim().isEmpty()) {
            errorMessage.setValue("Vui lòng nhập đầy đủ email và mật khẩu");
            return;
        }

        isLoading.setValue(true);
        authRepository.login(email.trim(), password.trim(), new AuthRepository.AuthCallback<LoginResponse>() {
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
        if (fullName.trim().isEmpty() || email.trim().isEmpty() || phone.trim().isEmpty() || password.trim().isEmpty()) {
            errorMessage.setValue("Vui lòng điền đầy đủ tất cả các trường");
            return;
        }

        isLoading.setValue(true);
        authRepository.register(fullName.trim(), email.trim(), phone.trim(), password.trim(), new AuthRepository.AuthCallback<LoginResponse>() {
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
