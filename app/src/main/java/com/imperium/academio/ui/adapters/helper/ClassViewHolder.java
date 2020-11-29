package com.imperium.academio.ui.adapters.helper;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.imperium.academio.R;

public class ClassViewHolder extends RecyclerView.ViewHolder {
    public final TextView name;
    public final ConstraintLayout constraintLayout;

    public ClassViewHolder(@NonNull View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.class_item_name);
        constraintLayout = itemView.findViewById(R.id.class_register_rvl);
    }
}
