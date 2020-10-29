package com.imperium.academio;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Arrays;
import java.util.List;

public class MainMenu extends AppCompatActivity {


    TabLayout tabLayout;
    ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tabs);
        viewPager.setAdapter(createMenuAdapter());

        List<String> titles = Arrays.asList("Materials", "Attendance", "Chat");

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (titles.size() > position)
                        tab.setText(titles.get(position));
                    else
                        tab.setText("Future Feature");
                }).attach();

    }


    private MainMenuFragmentAdapter createMenuAdapter() {
        return new MainMenuFragmentAdapter(this);
    }
}