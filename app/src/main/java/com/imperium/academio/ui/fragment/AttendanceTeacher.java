package com.imperium.academio.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.imperium.academio.R;
import com.imperium.academio.databinding.FragmentAttendanceTeacherBinding;
import com.imperium.academio.databinding.TemplateAttendanceTeacherRowBinding;
import com.imperium.academio.fireclass.AttendanceHelperClass;
import com.imperium.academio.fireclass.UserHelperClass;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendanceTeacher extends Fragment {
    FragmentAttendanceTeacherBinding binding;
    public StudentViewer studentViewer;
    DatabaseReference usersReference;
    LayoutInflater inflater;
    List<UserHelperClass> userList;
    int date, month, year;
    DatabaseReference selectedClass;
    boolean isDateSelected = false;
    String classId;
    String teacherId;

    public AttendanceTeacher() {
        // Required empty public attendance
    }

    public static AttendanceTeacher newInstance(Bundle args) {
        AttendanceTeacher fragment = new AttendanceTeacher();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            // Get id from intent
            classId = args.getString("classId");
            teacherId = args.getString("teacherId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_attendance_teacher, container, false
        );
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentActivity activity = getActivity();
        inflater = getLayoutInflater();
        if (activity == null || classId == null || teacherId == null) return;

        selectedClass = FirebaseDatabase.getInstance().getReference("class/" + classId);
        usersReference = FirebaseDatabase.getInstance().getReference("users");

        // Pick date
        binding.btnDatePick.setOnClickListener(view1 -> {
            DatePickerFragment newFragment = new DatePickerFragment();
            newFragment.setListener(
                    (datePicker, y, m, d) -> {
                        isDateSelected = true;
                        date = d;
                        month = m;
                        year = y;
                        String selectedDate = "Selected Date: " + date + "/" + (month + 1) + "/" + year;
                        binding.txtSelectedDate.setText(selectedDate);
                    }
            );
            newFragment.show(activity.getSupportFragmentManager(), "datePicker");
        });

        // Table binding
        binding.table.setStretchAllColumns(true);
        binding.table.bringToFront();

        // Setting up select all button
        binding.checkbox.setOnCheckedChangeListener((compoundButton, b) -> {
            for (int i = 1; i < binding.table.getChildCount(); i++) {
                // getting the checkbox which is inside linear layout, again inside the table row
                TableRow tableRow = (TableRow) binding.table.getChildAt(i);
                LinearLayout viewStub = (LinearLayout) tableRow.getChildAt(2);
                CheckBox cb = (CheckBox) viewStub.getChildAt(0);
                cb.setChecked(b);
            }
        });

        // List of students in class
        userList = new ArrayList<>();
        populateStudentList();

        // Setting up submit button
        binding.btnSubmit.setOnClickListener(v -> {
            Map<String, Object> absent = new HashMap<>();
            Calendar cal = Calendar.getInstance();
            if (!isDateSelected) {
                Toast.makeText(activity, "Select Date!", Toast.LENGTH_SHORT).show();
                return;
            }
            cal.set(year, month, date, 0, 0, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long timestamp = cal.getTimeInMillis();

            for (int i = 1; i < binding.table.getChildCount(); i++) {
                // getting the checkbox which is inside linear layout, again inside the table row
                TableRow tableRow = (TableRow) binding.table.getChildAt(i);
                LinearLayout viewStub = (LinearLayout) tableRow.getChildAt(2);
                CheckBox cb = (CheckBox) viewStub.getChildAt(0);

                // update absent record of the selected date
                String userId = userList.get(i - 1).generateKey();
                AttendanceHelperClass tempAtt = new AttendanceHelperClass(userId, timestamp);
                absent.put(tempAtt.generateKey(), cb.isChecked() ? null : tempAtt);
            }

            selectedClass.child("attendance").updateChildren(absent);
            selectedClass.child("sessions/" + timestamp).setValue(true);
            Toast.makeText(activity, "Submitted attendance record", Toast.LENGTH_SHORT).show();
        });
    }


    private void populateStudentList() {
        // Get list of student ids from class
        List<String> idList = new ArrayList<>();
        selectedClass.child("students").orderByValue().addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) return;
                        for (DataSnapshot user : snapshot.getChildren())
                            idList.add(user.getKey());

                        // Get username of students in list
                        getUserNames(idList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                }
        );
    }

    private void getUserNames(List<String> idList) {
        // Get username of students in list
        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                for (String id : idList) {
                    UserHelperClass u = snapshot.child(id).getValue(UserHelperClass.class);
                    if (u == null) continue;
                    userList.add(u);

                }
                // Sort users according to name
                Collections.sort(userList, (u1, u2) -> u1.fname.compareTo(u2.fname));

                // Inflate the rows of table, adding users
                for (UserHelperClass u : userList) {
                    int rowId = binding.table.getChildCount();
                    TemplateAttendanceTeacherRowBinding row = DataBindingUtil.inflate(
                            inflater, R.layout.template_attendance_teacher_row, binding.table, true
                    );
                    row.roll.setText(String.valueOf(rowId));
                    row.name.setText(u.getFname());
                    row.name.setOnClickListener(btn -> {
                        Bundle args = new Bundle();
                        args.putString("userId", u.generateKey());
                        args.putString("classId", classId);

                        // Bubble up request to student attendance viewer
                        if (studentViewer != null) studentViewer.createViewer(args, u.getFname());
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void setStudentViewer(StudentViewer viewer) {
        this.studentViewer = viewer;
    }

    public interface StudentViewer {
        void createViewer(Bundle studentArgs, String studentName);
    }
}