
package com.champua.mart.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.champua.mart.repository.AuthRepository;

public class OtpViewModel extends ViewModel {

    private final AuthRepository repository;

    private final MutableLiveData<Boolean> loading =
            new MutableLiveData<>(false);

    private final MutableLiveData<Boolean> loginSuccess =
            new MutableLiveData<>(false);

    private final MutableLiveData<String> error =
            new MutableLiveData<>();

    public OtpViewModel() {
        repository = new AuthRepository();
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void verifyOtp(String verificationId, String otp) {

        if (verificationId == null || verificationId.isEmpty()) {
            error.setValue("Verification session expired");
            return;
        }

        if (otp == null || !otp.matches("[0-9]{6}")) {
            error.setValue("Enter a valid 6-digit OTP");
            return;
        }

        loading.setValue(true);
        error.setValue(null);

        repository.verifyOtp(
                verificationId,
                otp,
                new AuthRepository.LoginCallback() {

                    @Override
                    public void onSuccess(String uid) {
                        loading.postValue(false);
                        loginSuccess.postValue(true);
                    }

                    @Override
                    public void onError(String message) {
                        loading.postValue(false);
                        error.postValue(message);
                    }
                }
        );
    }
}
