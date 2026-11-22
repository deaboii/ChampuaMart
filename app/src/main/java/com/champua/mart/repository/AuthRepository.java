
package com.champua.mart.repository;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class AuthRepository {

    private final FirebaseAuth auth;

    public AuthRepository() {
        auth = FirebaseAuth.getInstance();
    }

    // Receives OTP sending events
    public interface OtpCallback {
        void onCodeSent(String verificationId);
        void onAutoVerified(PhoneAuthCredential credential);
        void onError(String message);
        void onTimeout(String verificationId);
    }

    // Receives login results
    public interface LoginCallback {
        void onSuccess(String uid);
        void onError(String message);
    }

    // Send OTP
    public void sendOtp(Activity activity, String phoneNumber, OtpCallback callback)
    {

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(activity)
                        .setCallbacks(
                                new PhoneAuthProvider
                                        .OnVerificationStateChangedCallbacks() {

                                    @Override
                                    public void onVerificationCompleted(
                                            @NonNull PhoneAuthCredential credential) {

                                        callback.onAutoVerified(credential);
                                    }

                                    @Override
                                    public void onVerificationFailed(
                                            @NonNull com.google.firebase.FirebaseException e) {

                                        callback.onError(
                                                e.getMessage() != null
                                                        ? e.getMessage()
                                                        : "Unable to send OTP"
                                        );
                                    }

                                    @Override
                                    public void onCodeSent(
                                            @NonNull String verificationId,
                                            @NonNull PhoneAuthProvider
                                                    .ForceResendingToken token) {

                                        callback.onCodeSent(verificationId);
                                    }

                                    @Override
                                    public void onCodeAutoRetrievalTimeOut(
                                            @NonNull String verificationId) {

                                        callback.onTimeout(verificationId);
                                    }
                                })
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // Verify OTP
    public void verifyOtp(
            String verificationId,
            String otp,
            LoginCallback callback) {

        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(
                        verificationId,
                        otp
                );

        signIn(credential, callback);
    }

    // Sign in using Firebase credential
    public void signIn(
            PhoneAuthCredential credential,
            LoginCallback callback) {

        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()
                            && task.getResult().getUser() != null) {

                        callback.onSuccess(
                                task.getResult().getUser().getUid()
                        );

                    } else {

                        String error = task.getException() != null
                                ? task.getException().getMessage()
                                : "Authentication failed";

                        callback.onError(error);
                    }
                });
    }

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public String getCurrentUserId() {
        return auth.getCurrentUser() != null
                ? auth.getCurrentUser().getUid()
                : null;
    }

    public void logout() {
        auth.signOut();
    }
}
