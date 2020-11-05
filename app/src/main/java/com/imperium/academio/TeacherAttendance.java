package com.imperium.academio;

import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TeacherAttendance extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_attendance);

        TableLayout table = (TableLayout) findViewById(R.id.table1);
        table.setStretchAllColumns(true);
        table.bringToFront();
        int n = 1;
        for (int i = 0; i < 20; i++) {
            TableRow tr = new TableRow(this);
            TextView r1 = new TextView(this);
            TextView r2 = new TextView(this);
            r1.setText(String.valueOf(n++));
            r2.setText("name");
            tr.addView(r1);
            tr.addView(r2);
            for (int j = 0; j < 5; j++) {

                TextView c1 = new TextView(this);
                c1.setText("p");
                tr.addView(c1);
            }

            table.addView(tr);
        }

    }
}