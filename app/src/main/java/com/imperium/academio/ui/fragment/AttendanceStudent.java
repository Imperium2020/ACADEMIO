package com.imperium.academio.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.imperium.academio.CustomUtil;
import com.imperium.academio.R;
import com.imperium.academio.databinding.FragmentAttendanceStudentBinding;
import com.imperium.academio.databinding.TemplateAttendanceDateBinding;
import com.imperium.academio.fireclass.AttendanceHelperClass;
import com.imperium.academio.fireclass.ClassHelperClass;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AttendanceStudent extends Fragment {
    FragmentAttendanceStudentBinding binding;
    DatabaseReference selectedClass;
    String classId;
    String userId;
    List<Long> absentList;

    public AttendanceStudent() {
        // Required empty public constructor
    }

    public static AttendanceStudent newInstance(Bundle args) {
        AttendanceStudent fragement = new AttendanceStudent();
        fragement.setArguments(args);
        return fragement;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            classId = bundle.getString("classId");
            userId = bundle.getString("userId");
        }
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
        if (activity == null || classId == null || userId == null) return;

        selectedClass = FirebaseDatabase.getInstance().getReference("class/" + classId);
        absentList = new ArrayList<>();
        fetchAttendanceSheet();

        // Temporarily fill progress bar
        setProgress(100);

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
                    cardView.setCardBackgroundColor(getColor(R.color.yellow));
                    card.attendanceDateText.setText(shortWeekDays[j + 1]);
                }
            }
            table.addView(tr);
        }

        // Default year as current year
        Calendar cal = Calendar.getInstance();
        binding.inpAttendanceYear.setText(String.valueOf(cal.get(Calendar.YEAR)));

        // Autocomplete text
        List<String> months = Arrays.asList(getResources().getStringArray(R.array.months));
        binding.inpAttendanceMonth.setAdapter(new ArrayAdapter<>(
                activity, android.R.layout.simple_list_item_1, months));
        binding.inpAttendanceMonth.setText(months.get(cal.get(Calendar.MONTH)));

        binding.btnAttendanceGet.setOnClickListener(view1 -> {
            String y = CustomUtil.validateField(binding.attendanceYear, "year");
            String m = CustomUtil.validateField(binding.attendanceMonth, "text");
            List<String> mArray = Arrays.asList(getResources().getStringArray(R.array.months));

            if (y == null || m == null || !mArray.contains(m)) return;
            int year = Integer.parseInt(y);
            int month = mArray.indexOf(m);

            // get List of absent dates from database for month
            List<Integer> absentDates = getAbsentDates(year, month);

            // call function to draw table
            setTable(table, absentDates, month, year);
        });
    }

    // fill table with data
    public void setTable(TableLayout table, List<Integer> absentDates, Integer month, Integer year) {
        Set<Integer> absentDateSet = new HashSet<>(absentDates);
        Calendar calInstance = Calendar.getInstance();
        int currYear = calInstance.get(Calendar.YEAR);
        int currMonth = calInstance.get(Calendar.MONTH);

        // Check today is present in the selected view
        int today = (year == currYear && month == currMonth) ? calInstance.get(Calendar.DATE) : -1;

        // making custom Instance
        Calendar customInstance = Calendar.getInstance();
        customInstance.set(calInstance.get(Calendar.YEAR), (month < 0) ? currMonth : month,
                1, 0, 0, 0);
        customInstance.set(Calendar.MILLISECOND, 0);

        int maxDate = customInstance.getActualMaximum(Calendar.DATE);
        int startSpace = customInstance.get(Calendar.DAY_OF_WEEK) - 1;
        int dateCounter = 1;


        // Colors
        int lightRed = getColor(R.color.light_red);
        int lightBlue = getColor(R.color.light_blue);
        int lightGreen = getColor(R.color.light_green);

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

    private void fetchAttendanceSheet() {
        selectedClass.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                ClassHelperClass currentClass = snapshot.getValue(ClassHelperClass.class);
                if (currentClass == null) return;
                if (absentList == null || !absentList.isEmpty())
                    absentList = new ArrayList<>();

                for (Map.Entry<String, Boolean> session : currentClass.sessions.entrySet()) {
                    String date = session.getKey();
                    String attendanceKey = CustomUtil.SHA1(userId + date);
                    AttendanceHelperClass record = currentClass.attendance.get(attendanceKey);
                    if (record != null) {
                        absentList.add(record.absent_date);
                    }
                }

                // Set progress bar
                int sessionCount = currentClass.sessions.size();
                if (sessionCount == 0) return;
                int percent = (sessionCount - absentList.size()) * (100 / sessionCount);
                setProgress(percent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private List<Integer> getAbsentDates(int year, int month) {
        if (absentList.isEmpty()) {
            fetchAttendanceSheet();
            return Collections.emptyList();
        }

        // Set an instance to given year and month
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // Get both bounds for filtering the absent list
        long minTime = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, 1);
        long maxTime = cal.getTimeInMillis();

        // Filter absentList to get dates of selected range
        List<Integer> absentDatesInMonth = new ArrayList<>();
        for (long date : absentList) {
            if (minTime <= date && date < maxTime) {
                cal.setTimeInMillis(date);
                absentDatesInMonth.add(cal.get(Calendar.DATE));
            }
        }
        return absentDatesInMonth;
    }

    private void setProgress(int progress) {
        binding.attendanceProgressBar.setProgress(progress);
        binding.attendancePercentText.setText(String.format("%s%%", progress));
    }

    private int getColor(int resColor) {
        return ResourcesCompat.getColor(getResources(), resColor, null);
    }
}