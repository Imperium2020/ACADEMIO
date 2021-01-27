package com.imperium.academio.ui.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.imperium.academio.R;
import com.imperium.academio.ui.adapters.helper.ItemViewHolder;
import com.imperium.academio.ui.model.MaterialItemRvModel;

import java.util.List;


public class MaterialItemRvAdapter extends RecyclerView.Adapter<ItemViewHolder> {
    final Activity activity;
    final List<MaterialItemRvModel> items;

    private OnItemClickListener listener;

    public MaterialItemRvAdapter(Activity activity, List<MaterialItemRvModel> items) {
        this.activity = activity;
        this.items = items;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater.from(activity)
                .inflate(R.layout.template_material_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder viewHolder, int position) {
        MaterialItemRvModel item = items.get(position);
        viewHolder.icon.setImageResource(item.getIcon());
        viewHolder.title.setText(item.getTitle());
        String sub = item.getSubtitle();
        if (sub != null && sub.length() > 0)
            viewHolder.text.setText((sub.length() < 25) ? sub : sub.substring(0, 20) + "...");
        else
            viewHolder.text.setText("");
        viewHolder.constraintLayout.setOnClickListener(v -> listener.onItemClick(v, position));

        viewHolder.constraintLayout.setOnLongClickListener(v -> {
            listener.onItemLongPressed(v, position);
            notifyDataSetChanged();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);

        void onItemLongPressed(View itemView, int position);
    }
}
