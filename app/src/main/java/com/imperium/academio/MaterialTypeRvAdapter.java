package com.imperium.academio;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MaterialTypeRvAdapter extends RecyclerView.Adapter<MaterialTypeRvAdapter.TypeRvViewHolder> {

    private ArrayList<MaterialTypeRvModel> items;
    int row_index = -1;

    public MaterialTypeRvAdapter(ArrayList<MaterialTypeRvModel> items) {
        this.items = items;
    }

    public void onRefreshAdapter(ArrayList<MaterialTypeRvModel> items) {
        this.items = items ;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TypeRvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.material_type_rvi, parent, false);
        return new TypeRvViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TypeRvViewHolder holder, int position) {
        MaterialTypeRvModel currentItem = items.get(position);
        holder.image.setImageResource(currentItem.getImage());
        holder.text.setText(currentItem.getText());

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                row_index = position;
                notifyDataSetChanged();
            }
        });

        if (row_index == position) {
            holder.linearLayout.setBackgroundResource(R.drawable.material_type_rvi_selected_bg);
        } else {
            holder.linearLayout.setBackgroundResource(R.drawable.material_type_rvi_bg);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class TypeRvViewHolder extends RecyclerView.ViewHolder {

        TextView text;
        ImageView image;
        LinearLayout linearLayout;

        public TypeRvViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.material_type_icon);
            text = itemView.findViewById(R.id.material_type_text);
            linearLayout = itemView.findViewById(R.id.material_type_rvl);
        }
    }
}
