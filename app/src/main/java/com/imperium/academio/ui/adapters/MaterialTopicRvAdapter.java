package com.imperium.academio.ui.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.imperium.academio.R;
import com.imperium.academio.ui.adapters.helper.LoadMore;
import com.imperium.academio.ui.adapters.helper.LoadingViewHolder;
import com.imperium.academio.ui.adapters.helper.TopicRvViewHolder;
import com.imperium.academio.ui.model.MaterialTopicRvModel;

import java.util.List;


public class MaterialTopicRvAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final Activity activity;
    final List<MaterialTopicRvModel> items;
    final int visibleThreshold = 3;
    private final int VIEW_ITEM_TYPE = 0, VIEW_ITEM_LOADING = 1;
    LoadMore loadMore;
    boolean isLoading;
    int lastVisibleItem, totalItemCount;
    int topicRowIndex = -1;

    private OnTopicItemClickListener listener;

    public MaterialTopicRvAdapter(RecyclerView recyclerView, Activity activity, List<MaterialTopicRvModel> items) {
        this.activity = activity;
        this.items = items;
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (linearLayoutManager != null) {
                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                }

                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (loadMore != null)
                        loadMore.onLoadMode();
                    isLoading = true;
                }
            }
        });
    }

    public void setOnTopicItemClickListener(OnTopicItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) == null ? VIEW_ITEM_LOADING : VIEW_ITEM_TYPE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_ITEM_LOADING) {
            View view = LayoutInflater.from(activity).inflate(R.layout.template_progressbar, parent, false);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
            params.height = ConstraintLayout.LayoutParams.MATCH_PARENT;
            params.width = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            view.setLayoutParams(params);

            return new LoadingViewHolder(view);
        } else {
            View view = LayoutInflater.from(activity).inflate(R.layout.template_material_topic, parent, false);
            return new TopicRvViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TopicRvViewHolder) {
            MaterialTopicRvModel item = items.get(position);
            TopicRvViewHolder viewHolder = (TopicRvViewHolder) holder;
            viewHolder.topic.setText(item.getTopic());

            viewHolder.linearLayout.setOnClickListener(v -> {
                topicRowIndex = position;
                listener.onTopicItemClick(viewHolder.itemView, position);
                notifyDataSetChanged();
            });


            if (topicRowIndex == position) {
                viewHolder.linearLayout.setBackgroundResource(R.drawable.material_rvi_selected_bg);
            } else {
                viewHolder.linearLayout.setBackgroundResource(R.drawable.material_rvi_bg);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setLoadMore(LoadMore loadMore) {
        this.loadMore = loadMore;
    }

    public void setLoaded() {
        isLoading = false;
    }

    public interface OnTopicItemClickListener {
        void onTopicItemClick(View itemView, int position);
    }
}