package com.imperium.academio.ui.adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.imperium.academio.ui.fragment.AttendanceStudent;
import com.imperium.academio.ui.fragment.AttendanceTeacher;
import com.imperium.academio.ui.fragment.MaterialFragment;
import com.imperium.academio.ui.fragment.TemplateFragment;

public class MainMenuFragmentAdapter extends FragmentStateAdapter {
    private static int CARD_ITEM_SIZE;
    private static Bundle args;

    public MainMenuFragmentAdapter(@NonNull FragmentActivity fragmentActivity, Bundle arguments, int size) {
        super(fragmentActivity);
        CARD_ITEM_SIZE = size;
        args = arguments;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return MaterialFragment.newInstance(args);
            case 1:
                return AttendanceStudent.newInstance();
            case 2:
                return AttendanceTeacher.newInstance();
            default:
                return TemplateFragment.newInstance("Whoops! Future Feature!");
        }
    }

    @Override
    public int getItemCount() {
        return CARD_ITEM_SIZE;
    }
}