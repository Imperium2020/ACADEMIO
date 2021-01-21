package com.imperium.academio;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.imperium.academio.databinding.ActivityMainMenuBinding;
import com.imperium.academio.fireclass.ClassHelperClass;
import com.imperium.academio.ui.fragment.AttendanceStudent;
import com.imperium.academio.ui.fragment.AttendanceTeacher;
import com.imperium.academio.ui.fragment.MaterialFragment;
import com.imperium.academio.ui.fragment.TemplateFragment;

import java.util.ArrayList;
import java.util.List;

public class MainMenu extends AppCompatActivity {
    List<FragmentWithTitle> fragments;
    DatabaseReference selectedClass;
    ActivityMainMenuBinding binding;
    TabLayoutMediator mediator;
    FragmentStateAdapter adapter;
    Bundle defaultArgs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_menu);
        fragments = new ArrayList<>();

        defaultArgs = getIntent().getExtras();
        if (defaultArgs == null) return;
        String classId = defaultArgs.getString("classId");
        selectedClass = FirebaseDatabase.getInstance().getReference("class/" + classId);
        pauseForDB();
    }


    private void pauseForDB() {
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
                    defaultArgs.putString("teacherId", selectedClassObject.teacherId);
                    continueAfterDB(defaultArgs);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void continueAfterDB(Bundle args) {
        String userId = args.getString("userId");
        String teacherId = args.getString("teacherId");

        // Add Material fragment
        fragments.add(new FragmentWithTitle(MaterialFragment.newInstance(args), "Materials"));

        if (teacherId != null && userId != null) {
            // If user is teacher, add teacher fragment accordingly
            if (userId.equals(teacherId)) {
                AttendanceTeacher tFragment = AttendanceTeacher.newInstance(args);

                // Student button listener
                tFragment.setStudentViewer((sArgs, studentName) -> {
                    if (mediator == null || adapter == null) return;

                    // Detach tab mediator
                    mediator.detach();

                    // If student fragment exist, remove and notify adapter
                    if (fragments.size() == 3) {
                        fragments.remove(2);
                        adapter.notifyItemRemoved(2);
                    }

                    // Add student fragment and notify adapter
                    FragmentWithTitle ft = new FragmentWithTitle(
                            AttendanceStudent.newInstance(sArgs), studentName);
                    fragments.add(ft);
                    adapter.notifyItemInserted(2);

                    // Attach mediator after transaction
                    mediator.attach();
                });

                fragments.add(new FragmentWithTitle(tFragment, "Attendance"));
            } else {
                // Add student fragment
                fragments.add(new FragmentWithTitle(
                        AttendanceStudent.newInstance(args), "Attendance"));
            }
        }

        adapter = new FragmentStateAdapter(MainMenu.this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (fragments.size() > position) {
                    return fragments.get(position).fragment;
                } else
                    return TemplateFragment.newInstance("Whoops! Future Feature!");
            }

            @Override
            public int getItemCount() {
                return fragments.size();
            }
        };

        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setUserInputEnabled(false);
        mediator = new TabLayoutMediator(binding.tabs, binding.viewPager, (tab, position) -> {
            if (fragments.size() > position)
                tab.setText(fragments.get(position).name);
            else
                tab.setText("Future Feature");
        });
        mediator.attach();
    }

    public static class FragmentWithTitle {
        public final Fragment fragment;
        public final String name;

        public FragmentWithTitle(Fragment fragment, String name) {
            this.fragment = fragment;
            this.name = name;
        }
    }
}