package com.imperium.academio;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

class LoadingTopicViewHolder extends RecyclerView.ViewHolder {

    public ProgressBar progressBar;

    public LoadingTopicViewHolder(@NonNull View itemView) {
        super(itemView);
        progressBar = itemView.findViewById(R.id.material_progress_bar);
    }
}

class TopicRvViewHolder extends RecyclerView.ViewHolder {
    public TextView topic;
    public LinearLayout linearLayout;

    public TopicRvViewHolder(@NonNull View itemView) {
        super(itemView);
        topic = itemView.findViewById(R.id.material_topic_text);
        linearLayout = itemView.findViewById(R.id.material_topic_rvl);
    }
}

public class MaterialTopicRvAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_ITEM_TYPE = 0, VIEW_ITEM_LOADING = 1;
    LoadMore loadMore;
    boolean isLoading;
    Activity activity;
    List<MaterialTopicRvModel> items;
    int visibleThreshold = 3;
    int lastVisibleItem, totalItemCount;
    int topicRowIndex = -1;

    private OnTopicItemClickListener listener;

    public interface OnTopicItemClickListener {
        void onTopicItemClick(View itemView, int position);
    }

    public void setOnTopicItemClickListener(OnTopicItemClickListener listener) {
        this.listener = listener;
    }

    public MaterialTopicRvAdapter(RecyclerView recyclerView, Activity activity, List<MaterialTopicRvModel> items) {
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
            View view = LayoutInflater.from(activity).inflate(R.layout.material_topic_rvi, parent, false);

            return new TopicRvViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TopicRvViewHolder) {
            MaterialTopicRvModel item = items.get(position);
            TopicRvViewHolder viewHolder = (TopicRvViewHolder) holder;
            viewHolder.topic.setText(item.getTopic());

            viewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    topicRowIndex = position;
                    listener.onTopicItemClick(viewHolder.itemView, position);
                    notifyDataSetChanged();
                }
            });


            if (topicRowIndex == position) {
                viewHolder.linearLayout.setBackgroundResource(R.drawable.material_topic_rvi_selected_bg);
            } else {
                viewHolder.linearLayout.setBackgroundResource(R.drawable.material_topic_rvi_bg);
            }
        } else if (holder instanceof LoadingTopicViewHolder) {
            LoadingTopicViewHolder loadingTopicViewHolder = (LoadingTopicViewHolder) holder;
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