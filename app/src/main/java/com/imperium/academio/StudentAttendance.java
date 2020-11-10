package com.imperium.academio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class StudentAttendance extends Fragment {
    public StudentAttendance() {
        // Required empty public constructor
    }
    public static StudentAttendance newInstance() {
        return new StudentAttendance();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.student_attendance, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v = getView();
        if (v == null) return;
        ProgressBar _progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        _progressBar.setProgress(75);

        Spinner spinner = (Spinner) v.findViewById(R.id.spin1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.months, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        TableLayout table = (TableLayout) v.findViewById(R.id.table);
        table.setStretchAllColumns(true);
        table.bringToFront();
        int count = 1;
        for (int i = 0; i < 5; i++) {
            TableRow tr = new TableRow(getActivity());
            for (int j = 0; j < 7; j++) {
                TextView c1 = new TextView(getActivity());
                if (count <= 31) {

                    c1.setText(String.valueOf(count++));
                } else {
                    c1.setText(" ");
                }
                tr.addView(c1);
            }
            table.addView(tr);
        }
    }
}