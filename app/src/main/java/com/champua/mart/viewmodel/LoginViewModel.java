
package com.champua.mart.viewmodel;

import android.app.Activity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.champua.mart.repository.AuthRepository;
import com.google.firebase.auth.PhoneAuthCredential;

public class LoginViewModel extends ViewModel {

    private final AuthRepository repository;

    private final MutableLiveData<Boolean> loading =
            new MutableLiveData<>(false);

    private final MutableLiveData<String> verificationId =
            new MutableLiveData<>();

    private final MutableLiveData<String> error =
            new MutableLiveData<>();

    private final MutableLiveData<Boolean> codeSent =
            new MutableLiveData<>(false);

    private final MutableLiveData<Boolean> loginSuccess =
            new MutableLiveData<>(false);

    public LoginViewModel() {
        repository = new AuthRepository();
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getVerificationId() {
        return verificationId;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getCodeSent() {
        return codeSent;
    }

    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }

    public void sendOtp(Activity activity, String phoneNumber) {

        if (phoneNumber == null ||
                !phoneNumber.matches("\\+[1-9][0-9]{7,14}")) {

            error.setValue("Enter a valid phone number with country code");
            return;
        }

        loading.setValue(true);
        error.setValue(null);
        codeSent.setValue(false);

        repository.sendOtp(
                activity,
                phoneNumber,
                new AuthRepository.OtpCallback() {

                    @Override
                    public void onCodeSent(String id) {

                        loading.postValue(false);
                        verificationId.postValue(id);
                        codeSent.postValue(true);
                    }

                    @Override
                    public void onAutoVerified(
                            PhoneAuthCredential credential) {

                        repository.signIn(
                                credential,
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

                    @Override
                    public void onError(String message) {
                        loading.postValue(false);
                        error.postValue(message);
                    }

                    @Override
                    public void onTimeout(String id) {
                        verificationId.postValue(id);
                    }
                }
        );
    }
}
