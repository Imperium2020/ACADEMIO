package com.imperium.academio.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.imperium.academio.R;
import com.imperium.academio.databinding.FragmentAttendanceTeacherBinding;
import com.imperium.academio.databinding.TemplateAttendanceTeacherRowBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class AttendanceTeacher extends Fragment {
    FragmentAttendanceTeacherBinding binding;
    int date, month, year;
    boolean isDateselected = false;

    public AttendanceTeacher() {
        // Required empty public attendance
    }

    public static AttendanceTeacher newInstance() {
        return new AttendanceTeacher();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        LayoutInflater inflater = getLayoutInflater();
        if (activity == null) return;

        // Temporary list of fullNames
        // TODO: get names from firebase
        List<String> users = Arrays.asList(
                "Murtaza Rosas", "Sultan Armstrong", "Harriett Pennington", "Mindy Lynch",
                "Mariya Gibbs", "Arif Bradford", "Tarik Fuentes", "Nicole Regan",
                "Saffron Garner", "Keanu Patterson", "Henna Solis", "Katherine Cooley",
                "Wesley Gilliam", "Umaiza Driscoll", "Chris Barker", "Emilio Cairns",
                "Ariah Melton", "Ellen Odling", "Rhonda Hodge", "Adil Monroe",
                "Pharrell Ford", "Kanye Donaldson", "Nawal Mccormick", "Dominick Donovan",
                "Lucas Pickett", "Asher Fox", "Ellice Oneil", "Reuben Dillard",
                "Freddy Acosta", "Anwen Howells", "Jonathan Alexander", "Lilianna Espinoza",
                "Roksana Povey", "Jane Macdonald", "Amanda Curtis", "Manraj Morin",
                "Lynn Magana", "Kajol Rojas", "Meg Pham", "Izabella Ruiz");
        Collections.sort(users);

        // Pick date
        binding.btnDatePick.setOnClickListener(view1 -> {
            DatePickerFragment newFragment = new DatePickerFragment();
            newFragment.setListener(
                    (datePicker, y, m, d) -> {
                        isDateselected = true;
                        date = d;
                        month = m;
                        year = y;
                        String str = "Selected Date: " + date + "/" + (month + 1) + "/" + year;
                        binding.txtSelectedDate.setText(str);
                    }
            );
            newFragment.show(activity.getSupportFragmentManager(), "datePicker");
        });

        // Table binding
        TableLayout table = binding.table;
        table.setStretchAllColumns(true);
        table.bringToFront();

        // inflating the rows adding users
        for (int i = 1; i <= users.size(); i++) {
            TemplateAttendanceTeacherRowBinding row = DataBindingUtil.inflate(
                    inflater, R.layout.template_attendance_teacher_row, table, true
            );
            row.roll.setText(String.valueOf(i));
            row.name.setText(users.get(i - 1));
        }


        // Setting up select all button
        TableRow header = (TableRow) table.getChildAt(0);
        CheckBox selectAll = (CheckBox) header.getChildAt(2);
        CompoundButton.OnCheckedChangeListener listener = (compoundButton, b) -> {
            for (int i = 1; i < table.getChildCount(); i++) {
                // getting the checkbox which is inside linear layout, again inside the table row
                TableRow tableRow = (TableRow) table.getChildAt(i);
                LinearLayout viewStub = (LinearLayout) tableRow.getChildAt(2);
                CheckBox cb = (CheckBox) viewStub.getChildAt(0);
                cb.setChecked(b);
            }
        };
        selectAll.setOnCheckedChangeListener(listener);

        // Setting up submit button
        binding.btnSubmit.setOnClickListener(v -> {
            List<String> absent = new ArrayList<>();
            Calendar cal = Calendar.getInstance();
            if (!isDateselected) {
                Toast.makeText(activity, "Select Date!", Toast.LENGTH_SHORT).show();
                return;
            }
            cal.set(year, month, date, 0, 0, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long timestamp = cal.getTimeInMillis();

            for (int i = 1; i < table.getChildCount(); i++) {
                // getting the checkbox which is inside linear layout, again inside the table row
                TableRow tableRow = (TableRow) table.getChildAt(i);
                LinearLayout viewStub = (LinearLayout) tableRow.getChildAt(2);
                CheckBox cb = (CheckBox) viewStub.getChildAt(0);

                if (!cb.isChecked()) {
                    absent.add(users.get(i - 1));
                }
            }


            // for (String student : absent) {
            // Get userId from users list
            // create object to upload to db from
            // absent, timestamp to database
            // }
            Log.d("Absent count:", absent.size() + " on timestamp:" + timestamp);

        });
    }
}