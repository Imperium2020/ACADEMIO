package com.imperium.academio;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class TeacherAttendance extends Fragment {

    public TeacherAttendance() {
        // Required empty public attendance
    }
    public static TeacherAttendance newInstance() {
        return new TeacherAttendance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.teacher_attendance, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v = getView();
        if (v == null) return;
        TableLayout table = (TableLayout) v.findViewById(R.id.table1);
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