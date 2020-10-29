package com.imperium.academio;

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

import java.util.ArrayList;
import java.util.List;

public class Material extends Fragment {

    private MaterialTopicRvAdapter topicRvAdapter;
    private MaterialItemRvAdapter itemRvAdapter;

    List<MaterialItemRvModel> items = new ArrayList<>();
    List<MaterialTopicRvModel> topicItems = new ArrayList<>();

    public Material() {
        // Required empty public constructor
    }

    public static Material newInstance(){
        return new Material();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_material, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        RecyclerView topicRv, typeRv, itemRv;
        MaterialTypeRvAdapter typeRvAdapter;
        View v = getView();

        if (v == null) return;

        topicItems.add(new MaterialTopicRvModel("Module 1"));
        topicItems.add(new MaterialTopicRvModel("Module 2"));
        topicItems.add(new MaterialTopicRvModel("Module 3"));
        topicItems.add(new MaterialTopicRvModel("Module 4"));

        topicRv = v.findViewById(R.id.material_topic);
        topicRvAdapter = new MaterialTopicRvAdapter(topicRv, getActivity(), topicItems);
        topicRv.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        topicRv.setAdapter(topicRvAdapter);

        topicRvAdapter.setLoadMore(() -> new Handler().postDelayed(() -> {
            if (topicItems.size() < 7) {
                topicItems.add(new MaterialTopicRvModel("Module x"));
                topicItems.add(new MaterialTopicRvModel("Module x"));
                topicItems.add(new MaterialTopicRvModel("Module x"));
                topicRvAdapter.notifyDataSetChanged();
                topicRvAdapter.setLoaded();
            }
        }, 1500));

        topicRvAdapter.setOnTopicItemClickListener((itemView, position) -> {
            String topic = topicItems.get(position).getTopic();
            Toast.makeText(getActivity(), topic + " was clicked!", Toast.LENGTH_SHORT).show();
        });

        ArrayList<MaterialTypeRvModel> typeItems = new ArrayList<>();
        typeItems.add(new MaterialTypeRvModel(R.drawable.note_icon, "Notes"));
        typeItems.add(new MaterialTypeRvModel(R.drawable.video_icon, "Videos"));
        typeItems.add(new MaterialTypeRvModel(R.drawable.link_icon, "Links"));
        typeItems.add(new MaterialTypeRvModel(R.drawable.message_icon, "Alerts"));
        typeRv = v.findViewById(R.id.material_type);
        typeRvAdapter = new MaterialTypeRvAdapter(typeItems);
        typeRv.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        typeRv.setAdapter(typeRvAdapter);

        items.add(new MaterialItemRvModel(R.drawable.note_icon, "Topic 1 - Subtopic 1", "Description"));
        items.add(new MaterialItemRvModel(R.drawable.note_icon, "Topic 1 - Subtopic 2", "Description"));
        items.add(new MaterialItemRvModel(R.drawable.note_icon, "Topic 2 - Subtopic 1", "Description"));
        items.add(new MaterialItemRvModel(R.drawable.note_icon, "Topic 2 - Subtopic 2", "Description"));
        items.add(new MaterialItemRvModel(R.drawable.note_icon, "Topic 3 - Subtopic 1", "Description"));


        itemRv = v.findViewById(R.id.material_item);
        itemRvAdapter = new MaterialItemRvAdapter(itemRv, getActivity(), items);
        itemRv.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        itemRv.setAdapter(itemRvAdapter);


        itemRvAdapter.setLoadMore(() -> new Handler().postDelayed(() -> {
            if (items.size() < 8) {
                items.add(new MaterialItemRvModel(R.drawable.note_icon, "Topic x - Subtopic x", "Description"));
                items.add(new MaterialItemRvModel(R.drawable.note_icon, "Topic x - Subtopic x", "Description"));
                items.add(new MaterialItemRvModel(R.drawable.note_icon, "Topic x - Subtopic x", "Description"));
                itemRvAdapter.notifyDataSetChanged();
                itemRvAdapter.setLoaded();
            }
        }, 1500));

        itemRvAdapter.setOnItemClickListener((itemView, position) -> {
            String title = items.get(position).getTitle();
            Toast.makeText(getActivity(), title + " was clicked!", Toast.LENGTH_SHORT).show();
        });
    }
}










