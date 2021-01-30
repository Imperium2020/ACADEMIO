package com.imperium.academio

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.imperium.academio.databinding.ActivitySplashBinding

class Splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize firebase
        FirebaseApp.initializeApp(applicationContext)

        // Set layout and prepare animation
        val binding: ActivitySplashBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.logo.animation = AnimationUtils
                .loadAnimation(this, R.anim.top_animation)

        Thread {
            try {
                // Move to next activity after duration
                Thread.sleep(SPLASH_SCREEN_DURATION)

                var intent = Intent(this@Splash, Login::class.java)

                // If user is already logged in, skip to courses
                if (FirebaseAuth.getInstance().currentUser != null) {
                    intent = Intent(this@Splash, ClassRegister::class.java)
                }

                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Log.e("Splash", "onCreate: ", e)
            }
        }.start()
    }

    companion object {
        // duration of splash
        private const val SPLASH_SCREEN_DURATION: Long = 1000
    }
}