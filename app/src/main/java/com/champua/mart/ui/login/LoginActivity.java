
package com.champua.mart.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.champua.mart.MainActivity;
import com.champua.mart.databinding.ActivityLoginBinding;
import com.champua.mart.databinding.BottomSheetOtpBinding;
import com.champua.mart.viewmodel.LoginViewModel;
import com.champua.mart.viewmodel.OtpViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel loginViewModel;
    private OtpViewModel otpViewModel;

    private BottomSheetDialog otpDialog;
    private BottomSheetOtpBinding otpBinding;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.enableEdgeToEdge(getWindow());

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(
                binding.getRoot(), (view, insets) -> {

                    android.graphics.Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            ).toPlatformInsets();

                    view.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        loginViewModel =
                new ViewModelProvider(this).get(LoginViewModel.class);

        otpViewModel =
                new ViewModelProvider(this).get(OtpViewModel.class);

        setupPhoneInput();
        setupLoginObservers();
        setupOtpObservers();

        binding.btnContinue.setOnClickListener(v -> sendOtp());
    }

    private void setupPhoneInput() {

        binding.etMobile.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {

                binding.btnContinue.setEnabled(
                        s != null && s.length() == 10
                );
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void sendOtp() {

        String mobile =
                binding.etMobile.getText().toString().trim();

        if (!mobile.matches("[6-9][0-9]{9}")) {
            binding.etMobile.setError(
                    "Enter a valid 10-digit mobile number"
            );
            return;
        }

        loginViewModel.sendOtp(this, "+91" + mobile);
    }

    private void setupLoginObservers() {

        loginViewModel.getLoading().observe(this, loading -> {

            boolean isLoading = Boolean.TRUE.equals(loading);

            binding.btnContinue.setEnabled(
                    !isLoading &&
                            binding.etMobile.getText().length() == 10
            );

            binding.btnContinue.setText(
                    isLoading ? "Sending OTP..." : "Continue"
            );
        });

        loginViewModel.getVerificationId().observe(this, id -> {

            if (id != null) {
                verificationId = id;
            }
        });

        loginViewModel.getCodeSent().observe(this, sent -> {

            if (Boolean.TRUE.equals(sent)) {

                showOtpSheet(
                        "+91" +
                                binding.etMobile.getText().toString().trim()
                );
            }
        });

        loginViewModel.getError().observe(this, message -> {

            if (message != null && !message.isEmpty()) {

                Toast.makeText(
                        this,
                        message,
                        Toast.LENGTH_LONG
                ).show();
            }
        });

        loginViewModel.getLoginSuccess().observe(this, success -> {

            if (Boolean.TRUE.equals(success)) {

                FirebaseUser firebaseUser =
                        FirebaseAuth.getInstance().getCurrentUser();
                Log.d("firebaseUser", firebaseUser.toString());

                if (firebaseUser != null) {

                    firebaseUser.getIdToken(true)
                            .addOnSuccessListener(result -> {

                                String idToken = result.getToken();
                                registerUserInDatabase(idToken);

                            })
                            .addOnFailureListener(e -> {
                                Log.e("AUTH", "Token error", e);
                            });
                }

                openMainScreen();
            }
        });
    }

    private void setupOtpObservers() {

        otpViewModel.getLoading().observe(this, loading -> {

            if (otpBinding == null) return;

            boolean isLoading = Boolean.TRUE.equals(loading);

            otpBinding.progress.setVisibility(
                    isLoading ? View.VISIBLE : View.GONE
            );

            otpBinding.btnVerify.setEnabled(
                    !isLoading &&
                            otpBinding.etOtp.getText().length() == 6
            );
        });

        otpViewModel.getError().observe(this, message -> {

            if (otpBinding == null ||
                    message == null ||
                    message.isEmpty()) {
                return;
            }

            otpBinding.tvError.setText(message);
            otpBinding.tvError.setVisibility(View.VISIBLE);
        });

        otpViewModel.getLoginSuccess().observe(this, success -> {

            if (Boolean.TRUE.equals(success)) {

                if (otpDialog != null) {
                    otpDialog.dismiss();
                }

                openMainScreen();
            }
        });
    }

    private void showOtpSheet(String phoneNumber) {

        otpDialog = new BottomSheetDialog(this);

        otpBinding = BottomSheetOtpBinding.inflate(
                getLayoutInflater()
        );

        otpDialog.setContentView(otpBinding.getRoot());

        otpBinding.tvSentTo.setText(
                "Code sent to " + phoneNumber
        );

        otpBinding.etOtp.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        otpBinding.btnVerify.setEnabled(
                                s != null && s.length() == 6
                        );

                        otpBinding.tvError.setVisibility(View.GONE);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                }
        );

        otpBinding.btnVerify.setOnClickListener(v -> {

            String otp = otpBinding.etOtp
                    .getText()
                    .toString()
                    .trim();

            otpViewModel.verifyOtp(verificationId, otp);
        });

        otpBinding.tvChange.setOnClickListener(v -> {
            otpDialog.dismiss();
        });

        otpDialog.setOnDismissListener(dialog -> {
            otpBinding = null;
        });

        otpDialog.show();
    }

    private void openMainScreen() {

        Intent intent = new Intent(
                LoginActivity.this,
                MainActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    private void registerUserInDatabase(String idToken) {

        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(
                new byte[0],
                null
        );

        Request request = new Request.Builder()
                .url("https://champuamartbackendapi.onrender.com/api/users/register")
                .addHeader("Authorization", "Bearer " + idToken)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("USER_REGISTER", "Failed", e);
            }

            @Override
            public void onResponse(Call call, Response response)
                    throws IOException {

                String responseData = response.body() != null
                        ? response.body().string() : "";

                Log.d("USER_REGISTER", responseData);

                response.close();
            }
        });
    }

}
