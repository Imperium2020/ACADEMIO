package com.imperium.academio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private final static int SPLASH_SCREEN = 2500;

    //Variables
    Animation topAnim;
    TextView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        //Animations
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);

        logo = findViewById(R.id.text);

        logo.setAnimation(topAnim);
        new Thread(() -> {
            try {
                Thread.sleep(SPLASH_SCREEN);
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
                finish();
            }catch (Exception e){
                Log.e("MYAPP", "exception", e);}
        }


        ).start();


    }
}