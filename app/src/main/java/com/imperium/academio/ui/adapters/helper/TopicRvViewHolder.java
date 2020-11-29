package com.imperium.academio.ui.adapters.helper;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.imperium.academio.R;

public class TopicRvViewHolder extends RecyclerView.ViewHolder {
    final public TextView topic;
    final public LinearLayout linearLayout;

    public TopicRvViewHolder(@NonNull View itemView) {
        super(itemView);
        topic = itemView.findViewById(R.id.material_topic_text);
        linearLayout = itemView.findViewById(R.id.material_topic_rvl);
    }
}