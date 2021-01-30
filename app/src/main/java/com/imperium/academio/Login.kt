package com.imperium.academio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.imperium.academio.databinding.ActivityLoginBinding
import com.imperium.academio.fireclass.UserHelperClass

class Login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var users: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance()
        users = FirebaseDatabase.getInstance().getReference("users")

        // Set layout
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.apply {
            // Login button
            btnLoginSignIn.setOnClickListener {
                // Get username and password
                val username = CustomUtil.validateField(binding.loginUsername, "username")
                val password = CustomUtil.validateField(binding.loginPassword, "password")
                if (username == null || password == null) {
                    return@setOnClickListener
                }

                // Hide buttons and show progress bar
                loginProgressBar.visibility = View.VISIBLE
                btnLoginSignIn.visibility = View.GONE
                btnLoginGotoRegister.visibility = View.GONE
                checkUser(username, CustomUtil.SHA1(password))
            }

            // Goto Register button
            btnLoginGotoRegister.setOnClickListener {
                startActivity(Intent(this@Login, Register::class.java))
                finish()
            }
        }
    }

    private fun checkUser(username: String, password: String) {
        // User trying to login
        val guest = UserHelperClass(username, password)
        val key = guest.generateKey()

        users.child(key).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                // User matching username from database
                val user = snapshot.getValue(UserHelperClass::class.java)
                if (snapshot.exists() && user != null) {
                    toast("Trying to authenticate user...")
                    loginUser(user.email, guest.pass)
                } else {
                    binding.apply {
                        loginProgressBar.visibility = View.GONE
                        btnLoginGotoRegister.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.apply {
                    loginProgressBar.visibility = View.GONE
                    btnLoginSignIn.visibility = View.VISIBLE
                }
                toast("Error while trying to access user.")
            }
        })
    }

    private fun loginUser(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this)
        { task: Task<AuthResult?> ->
            if (task.isSuccessful && mAuth.currentUser != null) {
                startActivity(Intent(this@Login, ClassRegister::class.java))
                finish()
            } else {
                binding.apply {
                    loginProgressBar.visibility = View.GONE
                    btnLoginSignIn.visibility = View.VISIBLE
                }
                // If sign in fails, display a message to the user.
                toast("Authentication failed.")
            }
        }
    }

    fun Context.toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}