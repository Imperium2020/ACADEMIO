package com.imperium.academio;

import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.imperium.academio.databinding.ActivityMainMenuBinding;
import com.imperium.academio.fireclass.ClassHelperClass;
import com.imperium.academio.ui.adapters.MainMenuFragmentAdapter;
import com.imperium.academio.ui.fragment.AttendanceStudent;
import com.imperium.academio.ui.fragment.AttendanceTeacher;
import com.imperium.academio.ui.fragment.MaterialFragment;

import java.util.Arrays;
import java.util.List;

public class MainMenu extends AppCompatActivity {
    private final List<String> titles = Arrays.asList("Materials", "Attendance");
    DatabaseReference selectedClass;
    ActivityMainMenuBinding binding;
    Bundle args;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_menu);

        args = getIntent().getExtras();
        if (args == null) return;
        String classId = args.getString("classId");
        selectedClass = FirebaseDatabase.getInstance().getReference("class/" + classId);
        createAdapter();
    }


    private MainMenuFragmentAdapter createMenuAdapter(Bundle args) {
        return new MainMenuFragmentAdapter(MainMenu.this, titles.size()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                String userId = args.getString("userId");
                String teacherId = args.getString("teacherId");
                if (position == 0) {
                    return MaterialFragment.newInstance(args);
                } else if (position == 1 && teacherId != null && userId != null) {
                    return (userId.equals(teacherId)) ?
                            AttendanceTeacher.newInstance(args) :
                            AttendanceStudent.newInstance(args);
                } else
                    return super.createFragment(position);
            }
        };
    }

    private void createAdapter() {
        new Handler().post(() -> {
            // Check if User is a Teacher for this class
            // If yes, allow to add new materials
            selectedClass.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Get teacherId of current class
                        ClassHelperClass selectedClassObject = snapshot.getValue(ClassHelperClass.class);
                        if (selectedClassObject == null) return;

                        // continue after fetching teacher id
                        args.putString("teacherId", selectedClassObject.teacherId);
                        binding.viewPager.setAdapter(createMenuAdapter(args));
                        binding.viewPager.setUserInputEnabled(false);

                        new TabLayoutMediator(binding.tabs, binding.viewPager,
                                (tab, position) -> {
                                    if (titles.size() > position)
                                        tab.setText(titles.get(position));
                                    else
                                        tab.setText("Future Feature");
                                }).attach();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        });

    }
}