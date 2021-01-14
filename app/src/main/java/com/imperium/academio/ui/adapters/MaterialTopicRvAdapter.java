package com.imperium.academio.ui.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.imperium.academio.R;
import com.imperium.academio.ui.adapters.helper.TopicRvViewHolder;
import com.imperium.academio.ui.model.MaterialTopicRvModel;

import java.util.List;


public class MaterialTopicRvAdapter extends RecyclerView.Adapter<TopicRvViewHolder> {

    final Activity activity;
    final List<MaterialTopicRvModel> items;
    private OnTopicItemClickListener listener;
    int selectedTopicIndex = -1;

    public MaterialTopicRvAdapter(Activity activity, List<MaterialTopicRvModel> items) {
        this.activity = activity;
        this.items = items;
    }

    public void setOnTopicItemClickListener(OnTopicItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TopicRvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType ) {
        View view = LayoutInflater.from(activity).inflate(R.layout.template_material_topic, parent, false);
        return new TopicRvViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicRvViewHolder viewHolder, int position) {
        MaterialTopicRvModel item = items.get(position);
        viewHolder.topic.setText(item.getTopic());

        viewHolder.linearLayout.setOnClickListener(v -> {
            selectedTopicIndex = selectedTopicIndex == position ? -1 : position;
            listener.onTopicItemClick(viewHolder.itemView, position);
        });

        viewHolder.linearLayout.setBackgroundResource(
                selectedTopicIndex == position ?
                        R.drawable.material_rvi_selected_bg :
                        R.drawable.material_rvi_bg
        );
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnTopicItemClickListener {
        void onTopicItemClick(View itemView, int position);
    }
}