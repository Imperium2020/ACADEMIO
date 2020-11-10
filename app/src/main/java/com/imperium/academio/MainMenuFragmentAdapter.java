package com.imperium.academio;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MainMenuFragmentAdapter extends FragmentStateAdapter {
    private static int CARD_ITEM_SIZE;

    public MainMenuFragmentAdapter(@NonNull FragmentActivity fragmentActivity, int size) {
        super(fragmentActivity);
        CARD_ITEM_SIZE = size;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return Material.newInstance();
            case 1: return StudentAttendance.newInstance();
            case 2: return TeacherAttendance.newInstance();
            default: return TemplateFragment.newInstance("Whoops! Future Feature!");
        }
    }

    @Override
    public int getItemCount() {
        return CARD_ITEM_SIZE;
    }
}