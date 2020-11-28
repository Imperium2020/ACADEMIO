package com.imperium.academio.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.imperium.academio.R;

public class AttendanceTeacher extends Fragment {

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
        return inflater.inflate(R.layout.fragment_attendance_teacher, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TableLayout table = view.findViewById(R.id.table1);
        table.setStretchAllColumns(true);
        table.bringToFront();
        int n = 1;
        for (int i = 0; i < 20; i++) {
            TableRow tr = new TableRow(getActivity());
            TextView r1 = new TextView(getActivity());
            TextView r2 = new TextView(getActivity());
            r1.setText(String.valueOf(n++));
            r2.setText(getString(R.string.full_name));
            tr.addView(r1);
            tr.addView(r2);
            for (int j = 0; j < 5; j++) {

                TextView c1 = new TextView(getActivity());
                c1.setText("p");
                tr.addView(c1);
            }

            table.addView(tr);
        }

    }
}