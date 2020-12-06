package com.imperium.academio.fireclass;

import com.imperium.academio.CustomUtil;

import java.util.HashMap;
import java.util.Map;

public class ClassHelperClass {
    public String className;
    public String teacherId;
    public Integer sessionCount;
    public Map<String, Boolean> students = new HashMap<>();
    public Map<String, Boolean> attendance = new HashMap<>();

    public ClassHelperClass() {
        // Required for firebase
    }

    public ClassHelperClass(String className, String teacherId) {
        this.className = className;
        this.teacherId = teacherId;
    }

    public ClassHelperClass(String className, String teacherId, Integer sessionCount, Map<String, Boolean> students, Map<String, Boolean> attendance) {
        this.className = className;
        this.teacherId = teacherId;
        this.sessionCount = sessionCount;
        this.students = students;
        this.attendance = attendance;
    }

    public String generateKey() {
        return CustomUtil.SHA1(className + teacherId);
    }
}
