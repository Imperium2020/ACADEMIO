package com.imperium.academio

import android.app.Activity
import android.content.ContextWrapper
import com.google.android.material.textfield.TextInputLayout
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object CustomUtil {
    @Suppress("FunctionName")
    @JvmStatic
    fun SHA1(input: String): String {
        try {
            val md = MessageDigest.getInstance("SHA-1")
            val messageDigest = md.digest(input.toByteArray())
            val no = BigInteger(1, messageDigest)

            // Convert message digest into hex value
            val hashText = StringBuilder(no.toString(16))

            // Add preceding 0s to make it 32 bit
            while (hashText.length < 32) {
                hashText.insert(0, "0")
            }
            return hashText.toString()
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
    }

    @JvmStatic
    fun validateField(inputLayout: TextInputLayout, type: String?): String? {
        val editText = inputLayout.editText ?: return null
        val text = editText.text.toString()
        var error: String? = null
        var valid = true
        if (text.isEmpty()) {
            error = "Field cannot be empty"
        } else {
            when (type) {
                "username" -> {
                    if (text.length < 4 || text.length > 15) {
                        error = "Username should have 4 to 15 characters"
                        valid = false
                    }
                    if (valid && text.contains(" ")) {
                        error = "Username cannot contain spaces"
                        valid = false
                    }
                    if (valid && !text.matches("""^\w{4,15}$""".toRegex())) {
                        error = "Username should be alphanumeric"
                    }
                }
                "email" -> {
                    val emailPattern = """[a-zA-Z0-9._-]+@[a-z]+\.+[a-z]+""".toRegex()
                    if (!text.matches(emailPattern)) {
                        error = "Email isn't valid"
                    }
                }
                "password" -> {
                    if (text.matches("""^(.{0,7}|.{21,})$""".toRegex())) {
                        error = "Length of Password should be in range (8, 20)"
                        valid = false
                    }
                    if (valid && text.matches("""^([^A-Za-z]*)$""".toRegex())) {
                        error = "Password should contain at least one alphabet"
                        valid = false
                    }
                    if (valid && text.matches("""^([^0-9]*)$""".toRegex())) {
                        error = "Password should contain at least one digit"
                        valid = false
                    }
                    if (valid && text.matches("""^([a-zA-Z0-9]*)$""".toRegex())) {
                        error = "Password should contain at least one special character."
                        valid = false
                    }
                    if (valid && text.matches("""[^\s]*\s.*""".toRegex())) {
                        error = "Password should not contain spaces."
                    }
                }
                "year" -> if (!text.matches("""^[0-9]*$""".toRegex())) {
                    error = "Year should only contain digits."
                } else {
                    val year = text.toInt()
                    if (year < 1970 || year > 2038) {
                        error = "Year out of bounds."
                    }
                }
            }
        }
        inputLayout.error = error
        return if (error != null) { // Not valid so validator returns null
            inputLayout.postDelayed({
                var context = inputLayout.context
                var activity: Activity? = null
                while (context is ContextWrapper) {
                    if (context is Activity) {
                        activity = context
                    }
                    context = context.baseContext
                }
                activity?.runOnUiThread {
                    inputLayout.error = null
                    inputLayout.isErrorEnabled = false
                }
            }, 10000)
            null
        } else { // No error so returns text
            inputLayout.isErrorEnabled = false
            text
        }
    }
}