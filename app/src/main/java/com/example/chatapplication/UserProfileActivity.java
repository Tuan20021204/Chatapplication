package com.example.chatapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import model.User;

public class UserProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView userImage;
    private EditText textUserName, textEmail, textPhone, textPassword, textConfirmPassword, textConfirmCode;
    private TextView pConfirmCode;
    private Button editProfileButton;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private String verificationId;
    private boolean isCodeSent = false;
    private Uri imageUri;

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

        // Set up button click listener for updating profile image
        findViewById(R.id.updateImageProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        // Setup save button click listener
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri != null) {
                    uploadImageToFirebase();
                } else {
                    updateUserProfile();
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

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            userImage.setImageURI(imageUri); // Hiển thị ảnh vào ImageView
        }
    }

    private void uploadImageToFirebase() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("profile_images/" + auth.getCurrentUser().getUid() + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                updateUserProfile(uri.toString());
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveImageUrlToDatabase(String imageUrl) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userRef = database.getReference("users").child(currentUser.getUid());
            userRef.child("profileImageUrl").setValue(imageUrl)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(UserProfileActivity.this, "Profile image updated successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UserProfileActivity.this, "Failed to save image URL", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void verifyCodeAndUpdateProfile() {
        String code = textConfirmCode.getText().toString();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            updateUserProfile();
                        } else {
                            Toast.makeText(UserProfileActivity.this,
                                    "Invalid verification code",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Thay đổi hàm updateUserProfile để nhận tham số imageUrl
    private void updateUserProfile(String imageUrl) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userRef = database.getReference("users").child(currentUser.getUid());

            Map<String, Object> updates = new HashMap<>();
            updates.put("username", textUserName.getText().toString());
            updates.put("phone", textPhone.getText().toString());

            if (imageUrl != null) {
                updates.put("profileImageUrl", imageUrl); // Chỉ lưu URL nếu có ảnh mới
            }

            userRef.updateChildren(updates)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(UserProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UserProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    private void updateUserProfile() {
        updateUserProfile(null);
    }

}
