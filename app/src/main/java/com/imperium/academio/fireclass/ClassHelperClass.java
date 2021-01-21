package com.imperium.academio.fireclass;

import com.imperium.academio.CustomUtil;

import java.util.HashMap;
import java.util.Map;

public class ClassHelperClass {
    public String className;
    public String teacherId;
    public Map<String, Boolean> sessions = new HashMap<>();
    public Map<String, Boolean> students = new HashMap<>();
    public Map<String, AttendanceHelperClass> attendance = new HashMap<>();

    public ClassHelperClass() {
        // Required for firebase
    }

    public ClassHelperClass(String className, String teacherId) {
        this.className = className;
        this.teacherId = teacherId;
    }

    public ClassHelperClass(String className, String teacherId, Map<String, Boolean> sessions, Map<String, Boolean> students, Map<String, AttendanceHelperClass> attendance) {
        this.className = className;
        this.teacherId = teacherId;
        this.sessions = sessions;
        this.students = students;
        this.attendance = attendance;
    }

    public String generateKey() {
        return CustomUtil.SHA1(className + teacherId);
    }
}
