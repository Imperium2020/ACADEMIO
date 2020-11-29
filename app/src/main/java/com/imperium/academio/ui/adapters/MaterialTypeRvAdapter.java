package com.imperium.academio.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.imperium.academio.ui.model.MaterialTypeRvModel;
import com.imperium.academio.R;

import java.util.List;

public class MaterialTypeRvAdapter extends RecyclerView.Adapter<MaterialTypeRvAdapter.TypeRvViewHolder> {
    private final List<MaterialTypeRvModel> items;
    int row_index = -1;
    private OnTypeItemClickListener listener;

    public MaterialTypeRvAdapter(List<MaterialTypeRvModel> items) {
        this.items = items;
    }

    public void setOnTypeItemClickListener(OnTypeItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TypeRvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.template_material_type, parent, false);
        return new TypeRvViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TypeRvViewHolder holder, int position) {
        MaterialTypeRvModel currentItem = items.get(position);
        holder.image.setImageResource(currentItem.getImage());
        holder.text.setText(currentItem.getText());

        holder.linearLayout.setOnClickListener(v -> {
            row_index = position;
            listener.OnTypeItemClick(holder.itemView, position);
            notifyDataSetChanged();
        });

        if (row_index == position) {
            holder.linearLayout.setBackgroundResource(R.drawable.material_rvi_selected_bg);
        } else {
            holder.linearLayout.setBackgroundResource(R.drawable.material_rvi_bg);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnTypeItemClickListener {
        void OnTypeItemClick(View itemView, int position);
    }

    public static class TypeRvViewHolder extends RecyclerView.ViewHolder {

        final TextView text;
        final ImageView image;
        final LinearLayout linearLayout;

        public TypeRvViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.material_type_icon);
            text = itemView.findViewById(R.id.material_type_text);
            linearLayout = itemView.findViewById(R.id.material_type_rvl);
        }
    }
}
