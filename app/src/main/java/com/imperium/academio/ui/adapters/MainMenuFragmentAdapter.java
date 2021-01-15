package com.imperium.academio.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.imperium.academio.ui.fragment.TemplateFragment;

public class MainMenuFragmentAdapter extends FragmentStateAdapter {
    private static int CARD_ITEM_SIZE;

    public MainMenuFragmentAdapter(@NonNull FragmentActivity fragmentActivity, int size) {
        super(fragmentActivity);
        CARD_ITEM_SIZE = size;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return TemplateFragment.newInstance("Whoops! Future Feature!");
    }

    @Override
    public int getItemCount() {
        return CARD_ITEM_SIZE;
    }
}