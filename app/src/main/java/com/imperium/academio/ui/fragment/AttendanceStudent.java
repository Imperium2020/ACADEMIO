package com.imperium.academio.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.imperium.academio.R;
import com.imperium.academio.databinding.FragmentAttendanceStudentBinding;
import com.imperium.academio.databinding.TemplateAttendanceDateBinding;

import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AttendanceStudent extends Fragment {
    FragmentAttendanceStudentBinding binding;
    Calendar calInstance;

    public AttendanceStudent() {
        // Required empty public constructor
    }

    public static AttendanceStudent newInstance() {
        return new AttendanceStudent();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calInstance = Calendar.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_attendance_student, container, false
        );
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Activity activity = getActivity();
        LayoutInflater inflater = getLayoutInflater();
        if (activity == null) return;

        // Progress bar
        int progress = 75;
        binding.attendanceProgressBar.setProgress(progress);
        binding.attendancePercentText.setText(String.valueOf(progress));


        // Table creation
        TableLayout table = binding.attendanceStudentTable;
        table.setStretchAllColumns(true);
        table.bringToFront();
        String[] shortWeekDays = DateFormatSymbols.getInstance().getShortWeekdays();
        for (int i = 0; i < 7; i++) {
            // For row
            TableRow tr = new TableRow(activity);

            for (int j = 0; j < 7; j++) {
                // For Element
                TemplateAttendanceDateBinding card = DataBindingUtil.inflate(
                        inflater, R.layout.template_attendance_date, tr, true
                );
                CardView cardView = (CardView) card.getRoot();

                // setting table heading
                if (i == 0) {
                    cardView.setCardElevation(10);
                    cardView.setCardBackgroundColor(ResourcesCompat
                            .getColor(getResources(), R.color.yellow, null)
                    );
                    card.attendanceDateText.setText(shortWeekDays[j + 1]);
                }
            }
            table.addView(tr);
        }

        // Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(activity, R.array.months, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spinner = binding.attendanceSpinner;
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                // get List of absent dates from database for month
                List<Integer> absentDates = Arrays.asList(1, 8, 13, 19);
                // call function to draw table
                setTable(table, (pos == 11) ? absentDates : Collections.emptyList(), pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                List<Integer> absentDates = Collections.emptyList();
                setTable(table, absentDates, -1);
            }
        });
    }

    // fill table with data
    public void setTable(TableLayout table, List<Integer> absentDates, Integer month_pos) {
        Set<Integer> absentDateSet = new HashSet<>(absentDates);
        int currMonth = calInstance.get(Calendar.MONTH);
        int today = (month_pos == currMonth) ? calInstance.get(Calendar.DATE) : -1;

        // making custom Instance
        Calendar customInstance = Calendar.getInstance();
        customInstance.set(
                calInstance.get(Calendar.YEAR), (month_pos < 0) ? currMonth : month_pos,
                1, 0, 0, 0);

        int maxDate = customInstance.getActualMaximum(Calendar.DATE);
        int startSpace = customInstance.get(Calendar.DAY_OF_WEEK) - 1;
        int dateCounter = 1;


        // Colors
        int lightRed = ResourcesCompat.getColor(getResources(), R.color.light_red, null);
        int lightBlue = ResourcesCompat.getColor(getResources(), R.color.light_blue, null);
        int lightGreen = ResourcesCompat.getColor(getResources(), R.color.light_green, null);

        // Looping through the table
        for (int i = 1; i < table.getChildCount(); i++) {
            TableRow row = (TableRow) table.getChildAt(i);
            row.setVisibility(View.GONE);
            for (int j = 0; j < row.getChildCount(); j++) {
                String date;

                // getting textView
                CardView card = (CardView) row.getChildAt(j);
                TextView text = (TextView) card.getChildAt(0);

                // per date logic
                card.setCardBackgroundColor(lightGreen);
                if ((i == 1 && j < startSpace) || (dateCounter > maxDate)) {
                    // no date in element
                    date = " ";
                } else {
                    // there is a date in the element

                    // if first element in row, set row as visible
                    if (j == 0 || j == startSpace) row.setVisibility(View.VISIBLE);

                    // set color if the date is today
                    if (dateCounter == today) card.setCardBackgroundColor(lightBlue);
                    // absent takes precedence over today
                    if (absentDateSet.contains(dateCounter)) card.setCardBackgroundColor(lightRed);

                    date = String.valueOf(dateCounter++);
                }
                text.setText(date);

            }
        }
    }

}