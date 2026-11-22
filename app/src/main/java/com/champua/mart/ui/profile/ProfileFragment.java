package com.champua.mart.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.champua.mart.R;
import com.champua.mart.ui.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private EditText etName, etAddress, etPincode;
    private TextView tvPhoneNumber;
    private Button btnSaveProfile, btnLogout;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_profile,
                container,
                false
        );

        // Initialize views
        etName = view.findViewById(R.id.etName);
        etAddress = view.findViewById(R.id.etAddress);
        etPincode = view.findViewById(R.id.etPincode);
        tvPhoneNumber = view.findViewById(R.id.tvPhoneNumber);

        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Display logged-in phone number
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null && user.getPhoneNumber() != null) {
            tvPhoneNumber.setText(user.getPhoneNumber());
        }

        // Save profile (backend integration comes next)
        btnSaveProfile.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String pincode = etPincode.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError("Enter your name");
                return;
            }

            if (address.isEmpty()) {
                etAddress.setError("Enter your address");
                return;
            }

            if (!pincode.matches("\\d{6}")) {
                etPincode.setError("Enter a valid 6-digit PIN code");
                return;
            }

            Toast.makeText(
                    requireContext(),
                    "Profile details validated",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // Logout
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(
                    requireContext(),
                    LoginActivity.class
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
        });

        return view;
    }
}
