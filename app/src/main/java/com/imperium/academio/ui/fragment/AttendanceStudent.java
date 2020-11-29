package com.imperium.academio.ui.fragment;

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
import androidx.fragment.app.Fragment;

import com.imperium.academio.R;

import java.util.Locale;

public class AttendanceStudent extends Fragment {
    public AttendanceStudent() {
        // Required empty public constructor
    }

    public static AttendanceStudent newInstance() {
        return new AttendanceStudent();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_attendance_student, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int progress = 75;
        ProgressBar _progressBar = view.findViewById(R.id.progress_bar);
        _progressBar.setProgress(progress);

        TextView _textView = view.findViewById(R.id.percent_text);
        _textView.setText(String.format(Locale.getDefault(), "%d%%", progress));

        Spinner spinner = view.findViewById(R.id.spin1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.months, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        TableLayout table = view.findViewById(R.id.table);
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