package com.imperium.academio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;

public class SignUp extends AppCompatActivity {

    Button callLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sign_up);

        callLogin  = findViewById(R.id.login_screen);
        callLogin.setOnClickListener(view -> {
            Intent intent = new Intent(SignUp.this,Login.class);
            startActivity(intent);

        });


    }
}