package com.imperium.academio;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.imperium.academio.databinding.ActivityRegisterBinding;
import com.imperium.academio.fireclass.UserHelperClass;

public class Register extends AppCompatActivity {
    private ActivityRegisterBinding registerBinding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        registerBinding = DataBindingUtil.setContentView(this, R.layout.activity_register);

        // Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        registerBinding.btnGotoLogin.setOnClickListener(view -> {
            Intent intent = new Intent(Register.this, Login.class);
            startActivity(intent);
            finish();
        });

        registerBinding.btnUserRegister.setOnClickListener(view -> {
            String fullname = CustomUtil.validateField(registerBinding.registerFullName, "fullname");
            String username = CustomUtil.validateField(registerBinding.registerUserName, "username");
            String email = CustomUtil.validateField(registerBinding.registerEmail, "email");
            String password = CustomUtil.validateField(registerBinding.registerPassword, "password");
            if (fullname == null || username == null || email == null || password == null) {
                return;
            }
            UserHelperClass userData = new UserHelperClass(fullname, username, email, CustomUtil.SHA1(password));
            addUser(userData);
        });

    }

    private void registerUser(UserHelperClass uObj) {
        mAuth.createUserWithEmailAndPassword(uObj.getEmail(), uObj.getPass())
                .addOnCompleteListener(this, task -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (task.isSuccessful() && user != null) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest
                                .Builder()
                                .setDisplayName(uObj.getUname())
                                .build();

                        user.updateProfile(profileUpdates);

                        Toast.makeText(
                                this,
                                "New user registered: " + uObj.getUname(),
                                Toast.LENGTH_SHORT
                        ).show();
                        startActivity(new Intent(Register.this, ClassRegister.class));
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("REGISTER_ERROR", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(Register.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addUser(UserHelperClass uObj) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference users = db.getReference("users");
        String key = uObj.generateKey();
        users.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(Register.this, "username already taken", Toast.LENGTH_SHORT).show();
                } else {
                    users.child(key).setValue(uObj);
                    registerUser(uObj);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}