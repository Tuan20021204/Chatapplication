package com.example.chatapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import model.User;

public class UserProfileActivity extends AppCompatActivity {
    private ImageView userImage;
    private EditText textUserName, textEmail, textPhone, textPassword, textConfirmPassword, textConfirmCode;
    private TextView pConfirmCode;
    private Button editProfileButton;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private String verificationId;
    private boolean isCodeSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        // Initialize views
        initializeViews();

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Load current user data
        loadUserData();

        // Setup save button click listener
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isCodeSent) {
                    // First click - validate inputs and send verification code
                    if (validateInputs()) {
                        sendVerificationCode();
                    }
                } else {
                    // Second click - verify code and update profile
                    verifyCodeAndUpdateProfile();
                }
            }
        });
    }

    private void initializeViews() {
        userImage = findViewById(R.id.userImage);
        textUserName = findViewById(R.id.textUserName);
        textEmail = findViewById(R.id.textEmail);
        textPhone = findViewById(R.id.textPhone);
        textPassword = findViewById(R.id.textPassword);
        textConfirmPassword = findViewById(R.id.textConfirmPassword);
        textConfirmCode = findViewById(R.id.verificationId);
        pConfirmCode = findViewById(R.id.pConfirmCode);
        editProfileButton = findViewById(R.id.editProfileButton);

        // Initially hide confirmation code views
        pConfirmCode.setVisibility(View.GONE);
        textConfirmCode.setVisibility(View.GONE);
    }

    private void loadUserData() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userRef = database.getReference("users").child(currentUser.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User userData = snapshot.getValue(User.class);
                    if (userData != null) {
                        textUserName.setText(userData.getUsername());
                        textEmail.setText(userData.getEmail());
                        textPhone.setText(userData.getPhone());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UserProfileActivity.this,
                            "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean validateInputs() {
        String newPassword = textPassword.getText().toString();
        String confirmPassword = textConfirmPassword.getText().toString();

        if (!newPassword.isEmpty() && !newPassword.equals(confirmPassword)) {
            textConfirmPassword.setError("Passwords do not match");
            return false;
        }

        // Validate phone number format
        String phoneNumber = textPhone.getText().toString();
        if (!phoneNumber.matches("^[+]?[0-9]{10,13}$")) {
            textPhone.setError("Invalid phone number format");
            return false;
        }

        return true;
    }

    private void sendVerificationCode() {
        String phoneNumber = textPhone.getText().toString();

        PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        // Auto-retrieval or instant verification completed
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(UserProfileActivity.this,
                                "Verification failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        verificationId = verId;
                        isCodeSent = true;
                        pConfirmCode.setVisibility(View.VISIBLE);
                        textConfirmCode.setVisibility(View.VISIBLE);
                        Toast.makeText(UserProfileActivity.this,
                                "Verification code sent", Toast.LENGTH_SHORT).show();
                    }
                };

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyCodeAndUpdateProfile() {
        if (verificationId == null) {
            Toast.makeText(UserProfileActivity.this, "Verification ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        String code = textConfirmCode.getText().toString();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(UserProfileActivity.this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            updateUserProfile();
                        } else {
                            Toast.makeText(UserProfileActivity.this, "Invalid verification code", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void updateUserProfile() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userRef = database.getReference("users").child(currentUser.getUid());

            Map<String, Object> updates = new HashMap<>();
            updates.put("username", textUserName.getText().toString());
            updates.put("email", textEmail.getText().toString());
            updates.put("phone", textPhone.getText().toString());

            // Update password if new password is provided
            String newPassword = textPassword.getText().toString();
            if (!newPassword.isEmpty()) {
                currentUser.updatePassword(newPassword)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(UserProfileActivity.this,
                                            "Failed to update password",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

            userRef.updateChildren(updates)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(UserProfileActivity.this,
                                    "Profile updated successfully",
                                    Toast.LENGTH_SHORT).show();
                            // Reset verification state
                            isCodeSent = false;
                            pConfirmCode.setVisibility(View.GONE);
                            textConfirmCode.setVisibility(View.GONE);
                            textPassword.setText("");
                            textConfirmPassword.setText("");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UserProfileActivity.this,
                                    "Failed to update profile",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}

