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
import com.imperium.academio.ui.adapters.helper.ClassViewHolder;
import com.imperium.academio.ui.adapters.helper.LoadMore;
import com.imperium.academio.ui.adapters.helper.LoadingViewHolder;
import com.imperium.academio.ui.model.ClassRegisterRvModel;

import java.util.List;


public class ClassRegisterRvAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final Activity activity;
    final List<ClassRegisterRvModel> classes;
    final int visibleThreshold = 4;
    private final int VIEW_ITEM_TYPE = 0, VIEW_ITEM_LOADING = 1;
    LoadMore loadMore;
    boolean isLoading;
    int lastVisibleItem, totalItemCount;

    private OnItemClickListener listener;

    public ClassRegisterRvAdapter(RecyclerView recyclerView, Activity activity, List<ClassRegisterRvModel> classes) {
        this.activity = activity;
        this.classes = classes;
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

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return classes.get(position) == null ? VIEW_ITEM_LOADING : VIEW_ITEM_TYPE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_ITEM_LOADING) {
            View view = LayoutInflater.from(activity).inflate(R.layout.template_progressbar, parent, false);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
            params.width = ConstraintLayout.LayoutParams.MATCH_PARENT;
            params.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            view.setLayoutParams(params);

            return new LoadingViewHolder(view);
        } else {
            View view = LayoutInflater.from(activity).inflate(R.layout.template_class_register_item, parent, false);
            return new ClassViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ClassViewHolder) {
            ClassRegisterRvModel item = classes.get(position);
            ClassViewHolder viewHolder = (ClassViewHolder) holder;
            viewHolder.name.setText(item.getName());

            viewHolder.constraintLayout.setOnClickListener(v -> {
                listener.onItemClick(v, position);
                notifyDataSetChanged();
            });
        }
    }

    @Override
    public int getItemCount() {
        return classes.size();
    }

    public void setLoadMore(LoadMore loadMore) {
        this.loadMore = loadMore;
    }

    public void setLoaded() {
        isLoading = false;
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }
}
