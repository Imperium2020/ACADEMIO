package com.imperium.academio;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

class LoadingItemViewHolder extends RecyclerView.ViewHolder {

    public ProgressBar progressBar;

    public LoadingItemViewHolder(@NonNull View itemView) {
        super(itemView);
        progressBar = itemView.findViewById(R.id.material_progress_bar);
    }
}

class ItemViewHolder extends RecyclerView.ViewHolder {
    public ImageView icon;
    public TextView title;
    public TextView text;
    public ConstraintLayout constraintLayout;

    public ItemViewHolder(@NonNull View itemView) {
        super(itemView);
        icon = itemView.findViewById(R.id.material_item_icon);
        title = itemView.findViewById(R.id.material_item_title);
        text = itemView.findViewById(R.id.material_item_text);
        constraintLayout = itemView.findViewById(R.id.material_item_rvl);
    }
}


public class MaterialItemRvAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_ITEM_TYPE = 0, VIEW_ITEM_LOADING = 1;
    LoadMore loadMore;
    boolean isLoading;
    Activity activity;
    List<MaterialItemRvModel> items;
    int visibleThreshold = 4;
    int lastVisibleItem, totalItemCount;
    int topicRowIndex = -1;

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public MaterialItemRvAdapter(RecyclerView recyclerView, Activity activity, List<MaterialItemRvModel> items) {
        this.activity = activity;
        this.items = items;
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (loadMore != null)
                        loadMore.onLoadMode();
                    isLoading = true;
                }
            }
        });
    }


    @Override
    public int getItemViewType(int position) {
        return items.get(position) == null ? VIEW_ITEM_LOADING : VIEW_ITEM_TYPE;
    }

    public void setLoadMore(LoadMore loadMore) {
        this.loadMore = loadMore;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_ITEM_LOADING) {
            View view = LayoutInflater.from(activity).inflate(R.layout.material_progress, parent, false);
            return new LoadingItemViewHolder(view);
        } else {
            // viewType == VIEW_ITEM_TYPE
            View view = LayoutInflater.from(activity).inflate(R.layout.material_item_rvi, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            MaterialItemRvModel item = items.get(position);
            ItemViewHolder viewHolder = (ItemViewHolder) holder;
            viewHolder.icon.setImageResource(item.getIcon());
            viewHolder.title.setText((items.get(position)).getTitle());
            viewHolder.text.setText(item.getSubtitle());

            viewHolder.constraintLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    topicRowIndex = position;
                    listener.onItemClick(v, position);
                    notifyDataSetChanged();
                }
            });


            if (topicRowIndex == position) {
                viewHolder.constraintLayout.setBackgroundResource(R.drawable.material_topic_rvi_selected_bg);

            } else {
                viewHolder.constraintLayout.setBackgroundResource(R.drawable.material_topic_rvi_bg);
            }

        } else if (holder instanceof LoadingItemViewHolder) {
            LoadingItemViewHolder loadingItemViewHolder = (LoadingItemViewHolder) holder;

        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setLoaded() {
        isLoading = false;
    }
}
