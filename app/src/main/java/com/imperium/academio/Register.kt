package com.imperium.academio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.*
import com.imperium.academio.databinding.ActivityRegisterBinding
import com.imperium.academio.fireclass.UserHelperClass

class Register : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var users: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Firebase get instance and references
        mAuth = FirebaseAuth.getInstance()
        users = FirebaseDatabase.getInstance().getReference("users")

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            // Goto Login button
            btnGotoLogin.setOnClickListener {
                startActivity(Intent(this@Register, Login::class.java))
                finish()
            }

            // Register button
            btnUserRegister.setOnClickListener {
                val fullname = CustomUtil.validateField(binding.registerFullName, "fullname")
                val username = CustomUtil.validateField(binding.registerUserName, "username")
                val email = CustomUtil.validateField(binding.registerEmail, "email")
                val password = CustomUtil.validateField(binding.registerPassword, "password")
                if (fullname != null && username != null && email != null && password != null) {
                    addUser(fullname, username, email, CustomUtil.SHA1(password))
                }
            }

        }
    }

    private fun addUser(fullName: String, username: String, email: String, pass: String) {
        val guest = UserHelperClass(fullName, username, email, pass)
        val key = guest.generateKey()

        users.child(key).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    users.child(key).setValue(guest)
                    registerUser(guest)
                } else {
                    toast("username already taken!")
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun registerUser(guest: UserHelperClass) {
        mAuth.createUserWithEmailAndPassword(guest.email, guest.pass).addOnCompleteListener(this)
        { task: Task<AuthResult?> ->
            val user = mAuth.currentUser
            if (task.isSuccessful && user != null) {
                user.updateProfile(UserProfileChangeRequest.Builder()
                        .setDisplayName(guest.uname).build())

                toast("New user registered: ${guest.uname}")
                startActivity(Intent(this@Register, ClassRegister::class.java))
                finish()
            } else {
                // If sign in fails, display a message to the user.
                toast("Authentication failed.")
                users.child(guest.generateKey()).removeValue()
            }
        }
    }

    fun Context.toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}