package com.imperium.academio;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.imperium.academio.databinding.ActivityLoginBinding;
import com.imperium.academio.fireclass.UserHelperClass;

public class Login extends AppCompatActivity {

    private ActivityLoginBinding loginBinding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        loginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        loginBinding.btnLoginSignIn.setOnClickListener(view -> {
            String username = CustomUtil.validateField(loginBinding.loginUsername, "username");
            String password = CustomUtil.validateField(loginBinding.loginPassword, "password");
            if (username == null || password == null) {
                return;
            }
            loginBinding.loginProgressBar.setVisibility(View.VISIBLE);
            loginBinding.btnLoginSignIn.setVisibility(View.GONE);
            loginBinding.btnLoginGotoRegister.setVisibility(View.GONE);
            checkUser(new UserHelperClass(username, CustomUtil.SHA1(password)));
        });

        loginBinding.btnLoginGotoRegister.setOnClickListener(view -> {
            startActivity(new Intent(Login.this, Register.class));
            finish();
        });
    }

    private void loginUser(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    loginBinding.loginProgressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && user != null) {
                        startActivity(new Intent(Login.this, ClassRegister.class));
                        finish();
                    } else {
                        loginBinding.btnLoginSignIn.setVisibility(View.VISIBLE);

                        // If sign in fails, display a message to the user.
                        Log.w("LOGIN_ERROR", "signInWithEmail:failure", task.getException());
                        Toast.makeText(Login.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUser(UserHelperClass uObj) {
        DatabaseReference users = FirebaseDatabase.getInstance().getReference("users");
        String key = uObj.generateKey();
        users.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserHelperClass user = snapshot.getValue(UserHelperClass.class);
                if (snapshot.exists() && user != null) {
                    Toast.makeText(Login.this, "Found user! Logging in...", Toast.LENGTH_SHORT).show();
                    loginUser(user.getEmail(), uObj.getPass());
                } else {
                    loginBinding.btnLoginGotoRegister.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Login.this, "Error while trying to access user.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}


/* To sign out, use:
FirebaseAuth.getInstance().signOut();
 */