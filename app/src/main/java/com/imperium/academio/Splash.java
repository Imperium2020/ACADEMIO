package com.imperium.academio;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {

    // duration of splash
    private final static int SPLASH_SCREEN_DURATION = 1000;

    //Variables
    Animation topAnim;
    TextView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int FLAG_FULLSCREEN = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        //Animations
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);

        logo = findViewById(R.id.text);

        logo.setAnimation(topAnim);
        new Thread(() -> {
            try {
                Thread.sleep(SPLASH_SCREEN_DURATION);
                Intent intent = new Intent(Splash.this, Login.class);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                Log.e(getString(R.string.app_name), "exception", e);
            }
        }


        ).start();


    }
}