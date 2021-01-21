package com.imperium.academio.fireclass;

import com.imperium.academio.CustomUtil;

public class AttendanceHelperClass {
    public String student;
    public long absent_date;

    public AttendanceHelperClass() {
        // Required constructor for firebase
    }

    public AttendanceHelperClass(String student, long absent_date) {
        this.student = student;
        this.absent_date = absent_date;
    }

    public String getStudent() {
        return student;
    }

    public void setStudent(String student) {
        this.student = student;
    }

    public long getAbsent_date() {
        return absent_date;
    }

    public void setAbsent_date(long absent_date) {
        this.absent_date = absent_date;
    }

    public String generateKey() {
        return CustomUtil.SHA1(student + absent_date);
    }
}
