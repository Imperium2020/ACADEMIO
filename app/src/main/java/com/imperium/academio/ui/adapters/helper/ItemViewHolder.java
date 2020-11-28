package com.imperium.academio.ui.adapters.helper;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.imperium.academio.R;

public class ItemViewHolder extends RecyclerView.ViewHolder {
    public final ImageView icon;
    public final TextView title;
    public final TextView text;
    public final ConstraintLayout constraintLayout;

    public ItemViewHolder(@NonNull View itemView) {
        super(itemView);
        icon = itemView.findViewById(R.id.material_item_icon);
        title = itemView.findViewById(R.id.material_item_title);
        text = itemView.findViewById(R.id.material_item_text);
        constraintLayout = itemView.findViewById(R.id.material_item_rvl);
    }
}
