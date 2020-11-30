package com.imperium.academio;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class CustomUtil {
    public static String SHA1(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            StringBuilder hashText = new StringBuilder(no.toString(16));

            // Add preceding 0s to make it 32 bit
            while (hashText.length() < 32) {
                hashText.insert(0, "0");
            }
            return hashText.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String validateField(TextInputLayout inputLayout, String type) {
        EditText editText = inputLayout.getEditText();
        if (editText == null)
            return null;
        String text = editText.getText().toString();
        String error = null;
        boolean valid = true;
        if (text.isEmpty()) {
            error = "Field cannot be empty";
        } else {
            switch (type) {
                case "username":
                    if (text.length() < 4 || text.length() > 15) {
                        error = "Username should have 4 to 15 characters";
                        valid = false;
                    }
                    if (valid && text.contains(" ")) {
                        error = "Username cannot contain spaces";
                        valid = false;
                    }
                    if (valid && !text.matches("^\\w{4,15}$")) {
                        error = "Username should be alphanumeric";
                    }
                    break;
                case "email":
                    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
                    if (!text.matches(emailPattern)) {
                        error = "Email isn't valid";
                    }
                    break;
                case "password":
                    if (text.matches("^(.{0,7}|.{21,})$")) {
                        error = "Length of Password should be in range (8, 20)";
                        valid = false;
                    }
                    if (valid && text.matches("^([^A-Za-z]*)$")) {
                        error = "Password should contain at least one alphabet";
                        valid = false;
                    }
                    if (valid && text.matches("^([^0-9]*)$")) {
                        error = "Password should contain at least one digit";
                        valid = false;
                    }
                    if (valid && text.matches("^([a-zA-Z0-9]*)$")) {
                        error = "Password should contain at least one special character.";
                        valid = false;
                    }
                    if (valid && text.matches("[^\\s]*\\s.*")) {
                        error = "Password should not contain spaces.";
                    }
                    break;
            }
        }
        inputLayout.setError(error);
        if (error != null) { // Not valid so validator returns null
            new Handler().postDelayed(() -> {
                Context context = inputLayout.getContext();
                Activity activity = null;
                while (context instanceof ContextWrapper) {
                    if (context instanceof Activity) {
                        activity = (Activity) context;
                    }
                    context = ((ContextWrapper) context).getBaseContext();
                }

                if (activity != null)
                    activity.runOnUiThread(() -> {
                        inputLayout.setError(null);
                        inputLayout.setErrorEnabled(false);
                        Log.d("UIHERE", "validated");
                    });
            }, 10000);
            return null;
        } else { // No error so returns text
            inputLayout.setErrorEnabled(false);
            return text;
        }
    }
}