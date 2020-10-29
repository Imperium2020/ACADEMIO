package com.imperium.academio;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MainMenuFragmentAdapter extends FragmentStateAdapter {
    private static final int CARD_ITEM_SIZE = 3;

    public MainMenuFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return Material.newInstance();
            default: return TemplateFragment.newInstance("Whoops! Future Feature!");
        }
    }

    @Override
    public int getItemCount() {
        return CARD_ITEM_SIZE;
    }
}