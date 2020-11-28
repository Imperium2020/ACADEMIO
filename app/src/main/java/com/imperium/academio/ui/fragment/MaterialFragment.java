package com.imperium.academio.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.imperium.academio.ui.adapters.MaterialItemRvAdapter;
import com.imperium.academio.ui.model.MaterialItemRvModel;
import com.imperium.academio.ui.adapters.MaterialTopicRvAdapter;
import com.imperium.academio.ui.model.MaterialTopicRvModel;
import com.imperium.academio.ui.adapters.MaterialTypeRvAdapter;
import com.imperium.academio.ui.model.MaterialTypeRvModel;
import com.imperium.academio.R;
import com.imperium.academio.ui.adapters.SpanningLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class MaterialFragment extends Fragment {
    public MaterialFragment() {
        // Required empty public constructor
    }

    public static MaterialFragment newInstance() {
        return new MaterialFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_material, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView topicRv, typeRv, itemRv;

        MaterialTopicRvAdapter topicRvAdapter;
        MaterialTypeRvAdapter typeRvAdapter;
        MaterialItemRvAdapter itemRvAdapter;

        List<MaterialTopicRvModel> topicItems = new ArrayList<>();
        List<MaterialTypeRvModel> typeItems = new ArrayList<>();
        List<MaterialItemRvModel> items = new ArrayList<>();

        typeItems.add(new MaterialTypeRvModel(R.drawable.note_icon, "Notes"));
        typeItems.add(new MaterialTypeRvModel(R.drawable.video_icon, "Videos"));
        typeItems.add(new MaterialTypeRvModel(R.drawable.link_icon, "Links"));
        typeItems.add(new MaterialTypeRvModel(R.drawable.message_icon, "Alerts"));

        topicItems.add(new MaterialTopicRvModel("Module 1"));
        topicItems.add(new MaterialTopicRvModel("Module 2"));
        topicItems.add(new MaterialTopicRvModel("Module 3"));
        topicItems.add(new MaterialTopicRvModel("Module 4"));

        items.add(new MaterialItemRvModel(R.drawable.note_icon, "Topic 1 - Subtopic 1", "Description"));
        items.add(new MaterialItemRvModel(R.drawable.note_icon, "Topic 1 - Subtopic 2", "Description"));
        items.add(new MaterialItemRvModel(R.drawable.note_icon, "Topic 2 - Subtopic 1", "Description"));
        items.add(new MaterialItemRvModel(R.drawable.note_icon, "Topic 2 - Subtopic 2", "Description"));
        items.add(new MaterialItemRvModel(R.drawable.note_icon, "Topic 3 - Subtopic 1", "Description"));


        topicRv = view.findViewById(R.id.material_topic);
        typeRv = view.findViewById(R.id.material_type);
        itemRv = view.findViewById(R.id.material_item);


        topicRvAdapter = new MaterialTopicRvAdapter(topicRv, getActivity(), topicItems);
        topicRv.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        topicRv.setAdapter(topicRvAdapter);

        typeRvAdapter = new MaterialTypeRvAdapter(typeItems);
        typeRv.setLayoutManager(new SpanningLinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        typeRv.setAdapter(typeRvAdapter);

        itemRvAdapter = new MaterialItemRvAdapter(itemRv, getActivity(), items);
        itemRv.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        itemRv.setAdapter(itemRvAdapter);


        topicRvAdapter.setOnTopicItemClickListener((itemView, position) -> {
            String topic = topicItems.get(position).getTopic();
            Toast.makeText(getActivity(), topic + " was clicked!", Toast.LENGTH_SHORT).show();
        });

        typeRvAdapter.setOnTypeItemClickListener((itemView, position) -> {
            String text = typeItems.get(position).getText();
            Toast.makeText(getActivity(), text + " was clicked!", Toast.LENGTH_SHORT).show();
        });

        itemRvAdapter.setOnItemClickListener((itemView, position) -> {
            String title = items.get(position).getTitle();
            Toast.makeText(getActivity(), title + " was clicked!", Toast.LENGTH_SHORT).show();
        });

        topicRvAdapter.setLoadMore(() -> {
            if (topicItems.size() < 100) {
                topicItems.add(null);
                topicRv.post(() -> topicRvAdapter.notifyItemInserted(topicItems.size() - 1));
                new Handler().postDelayed(() -> {
                    topicItems.remove(null);
                    topicItems.add(new MaterialTopicRvModel("Module x"));
                    topicItems.add(new MaterialTopicRvModel("Module x"));
                    topicItems.add(new MaterialTopicRvModel("Module x"));
                    topicRvAdapter.notifyDataSetChanged();
                    topicRvAdapter.setLoaded();
                }, 4000);
            }
        });

        itemRvAdapter.setLoadMore(() -> {
            if (items.size() < 100) {
                items.add(null);
                itemRv.post(() -> itemRvAdapter.notifyItemInserted(items.size() - 1));
                new Handler().postDelayed(() -> {
                    items.remove(null);
                    items.add(new MaterialItemRvModel(R.drawable.note_icon, "Topic x - Subtopic x", "Description"));
                    items.add(new MaterialItemRvModel(R.drawable.note_icon, "Topic x - Subtopic x", "Description"));
                    items.add(new MaterialItemRvModel(R.drawable.note_icon, "Topic x - Subtopic x", "Description"));
                    itemRvAdapter.notifyDataSetChanged();
                    itemRvAdapter.setLoaded();
                }, 4000);

            }
        });


    }
}










