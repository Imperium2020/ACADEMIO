package com.imperium.academio;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class StudentAttendance extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_attendance);
        ProgressBar _progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        _progressBar.setProgress(75);

        Spinner spinner = (Spinner) findViewById(R.id.spin1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.months, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        TableLayout table = (TableLayout) findViewById(R.id.table);
        table.setStretchAllColumns(true);
        table.bringToFront();
        int count = 1;
        for (int i = 0; i < 5; i++) {
            TableRow tr = new TableRow(this);
            for (int j = 0; j < 7; j++) {
                TextView c1 = new TextView(this);
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