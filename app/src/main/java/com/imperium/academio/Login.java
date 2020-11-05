package com.imperium.academio;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {

    Button callSignUp, callMaterial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        callSignUp = findViewById(R.id.btn_material);
        callSignUp.setOnClickListener(view -> {
            Intent intent = new Intent(Login.this, StudentAttendance.class);
            startActivity(intent);
        });
        callMaterial = findViewById(R.id.signup_screen);
        callMaterial.setOnClickListener(view -> {
            Intent intent = new Intent(Login.this, MainMenu.class);
            startActivity(intent);
        });
    }
}