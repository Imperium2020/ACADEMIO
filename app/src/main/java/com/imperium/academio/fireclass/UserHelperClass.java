package com.imperium.academio.fireclass;

import com.google.firebase.database.Exclude;
import com.imperium.academio.CustomUtil;

import java.util.HashMap;
import java.util.Map;

public class UserHelperClass {
    public String email;
    public String fname;
    public String pass;
    public String uname;
    public Map<String, String> classes = new HashMap<>();


    public UserHelperClass() {
    }

    public UserHelperClass(String uname, String pass) {
        this.pass = pass;
        this.uname = uname;
    }

    public UserHelperClass(String fname, String uname, String email, String pass) {
        this(uname, pass);
        this.email = email;
        this.fname = fname;
    }

    public UserHelperClass(String fname, String uname, String email, String pass, Map<String, String> classes) {
        this(fname, uname, email, pass);
        this.classes = classes;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    @Exclude
    public String generateKey() {
        return (uname != null) ? CustomUtil.SHA1(uname) : null;
    }
}
