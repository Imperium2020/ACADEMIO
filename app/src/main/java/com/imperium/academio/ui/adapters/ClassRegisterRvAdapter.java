package com.imperium.academio.ui.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.imperium.academio.R;
import com.imperium.academio.ui.adapters.helper.ClassViewHolder;
import com.imperium.academio.ui.model.ClassRegisterRvModel;

import java.util.List;


public class ClassRegisterRvAdapter extends RecyclerView.Adapter<ClassViewHolder> {
    final Activity activity;
    final List<ClassRegisterRvModel> classes;
    private OnItemClickListener listener;

    public ClassRegisterRvAdapter(Activity activity, List<ClassRegisterRvModel> classes) {
        this.activity = activity;
        this.classes = classes;
    }

    // Function to attach OnClick Listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.template_class_register_item, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClassViewHolder viewHolder, int position) {
        // Create class item from data
        ClassRegisterRvModel item = classes.get(position);
        viewHolder.name.setText(item.getName());

        // Attach click listener to items
        viewHolder.constraintLayout.setOnClickListener(v -> listener.onItemClick(v, position));
    }

    @Override
    public int getItemCount() {
        return classes.size();
    }

    // Interface to bubble up clicks
    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }
}
