package com.imperium.academio.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.imperium.academio.Login;
import com.imperium.academio.R;
import com.imperium.academio.databinding.FragmentMaterialBinding;
import com.imperium.academio.fireclass.ClassHelperClass;
import com.imperium.academio.fireclass.MaterialHelperClass;
import com.imperium.academio.ui.adapters.MaterialItemRvAdapter;
import com.imperium.academio.ui.adapters.MaterialTopicRvAdapter;
import com.imperium.academio.ui.adapters.MaterialTypeRvAdapter;
import com.imperium.academio.ui.adapters.SpanningLinearLayoutManager;
import com.imperium.academio.ui.model.MaterialItemRvModel;
import com.imperium.academio.ui.model.MaterialTopicRvModel;
import com.imperium.academio.ui.model.MaterialTypeRvModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MaterialFragment extends Fragment implements MaterialDialogFragment.SubmitListener {
    FragmentMaterialBinding binding;
    FragmentActivity activity;
    FirebaseAuth firebaseAuth;
    DatabaseReference materials;
    String userId;
    String classId;
    List<MaterialTopicRvModel> topicItems;
    List<MaterialTypeRvModel> typeItems;
    List<MaterialItemRvModel> items;
    List<MaterialHelperClass> dbItems;

    MaterialTopicRvAdapter topicRvAdapter;
    MaterialTypeRvAdapter typeRvAdapter;
    MaterialItemRvAdapter itemRvAdapter;

    Handler handler;

    String selectedType = "Notes";
    String selectedTopic;

    ValueEventListener dbListener;

    public MaterialFragment() {
        // Required empty public constructor
    }

    int count = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static MaterialFragment newInstance(Bundle args) {
        MaterialFragment fragment = new MaterialFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            classId = bundle.getString("classId");
            userId = bundle.getString("userId");
        }
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_material, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = getActivity();
        handler = new Handler();
        if (activity == null) return;
        firebaseAuth = FirebaseAuth.getInstance();
        materials = FirebaseDatabase.getInstance().getReference("materials");

        if (userId == null || classId == null) {
            activity.startActivity(new Intent(activity, Login.class));
            activity.finish();
            return;
        }

        FirebaseDatabase.getInstance().getReference("class/" + classId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String teacherId = snapshot.getValue(ClassHelperClass.class).teacherId;
                    if (userId.equals(teacherId)) {
                        binding.materialAddItem.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        topicItems = (topicItems == null) ? new ArrayList<>() : topicItems;
        typeItems = (typeItems == null) ? new ArrayList<>() : typeItems;
        items = (items == null) ? new ArrayList<>() : items;
        dbItems = new ArrayList<>();
        Query query = materials.orderByChild("classId").equalTo(classId);
        query.addValueEventListener(getDbListener());
        topicItems.add(null);

        List<String> types = Arrays.asList(getResources().getStringArray(R.array.types));
        typeItems.add(new MaterialTypeRvModel(R.drawable.note_icon, types.get(0)));
        typeItems.add(new MaterialTypeRvModel(R.drawable.video_icon, types.get(1)));
        typeItems.add(new MaterialTypeRvModel(R.drawable.link_icon, types.get(2)));
        typeItems.add(new MaterialTypeRvModel(R.drawable.message_icon, types.get(3)));

        items.add(null);

        topicRvAdapter = new MaterialTopicRvAdapter(binding.materialTopic, activity, topicItems);
        typeRvAdapter = new MaterialTypeRvAdapter(typeItems);
        itemRvAdapter = new MaterialItemRvAdapter(binding.materialItem, activity, items);

        binding.materialTopic.setHasFixedSize(true);
        binding.materialType.setHasFixedSize(true);
        binding.materialItem.setHasFixedSize(true);

        binding.materialType.setLayoutManager(new SpanningLinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        binding.materialTopic.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        binding.materialItem.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));
        binding.materialTopic.setAdapter(topicRvAdapter);
        binding.materialType.setAdapter(typeRvAdapter);
        binding.materialItem.setAdapter(itemRvAdapter);


        topicRvAdapter.setOnTopicItemClickListener((itemView, position) -> {
            selectedTopic = topicItems.get(position).getTopic();
            Log.d("MaterialFragment", "initiateFirstClick: " + selectedTopic);
            refresh();
        });

        typeRvAdapter.setOnTypeItemClickListener((itemView, position) -> {
            selectedType = typeItems.get(position).getText();
            Log.d("MaterialFragment", "initiateFirstClick: " + selectedType);
            refresh();
        });

        itemRvAdapter.setOnItemClickListener((itemView, position) -> {
            String title = items.get(position).getTitle();
            query.removeEventListener(dbListener);
            Toast.makeText(activity, title + " was clicked!", Toast.LENGTH_SHORT).show();
        });

        topicRvAdapter.setLoadMore(() -> loadMore(topicRvAdapter, binding.materialTopic, topicItems));
        itemRvAdapter.setLoadMore(() -> loadMore(itemRvAdapter, binding.materialItem, items));

        binding.materialAddItem.setOnClickListener(item -> {
            MaterialDialogFragment fragment = MaterialDialogFragment.newInstance(this);
            fragment.show(activity.getSupportFragmentManager(), "addMaterial");
        });

        binding.materialType.post(() -> initiateFirstClick(binding.materialType));
        binding.materialTopic.post(() -> {
            initiateFirstClick(binding.materialTopic);
            Log.d("MaterialFragment", "onViewCreated: I REACH HERE");
        });
    }

    private void initiateFirstClick(RecyclerView recyclerView) {
        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForLayoutPosition(0);
        if (viewHolder != null && viewHolder.itemView != null) {
            viewHolder.itemView.performClick();
        } else {
            count++;
            Log.d("MaterialFragment", "initiateFirstClick: repeated " + count);
            handler.postDelayed(() -> initiateFirstClick(recyclerView), 1000);
        }
    }

    private void loadMore(RecyclerView.Adapter<RecyclerView.ViewHolder> recyclerAdapter,
                          RecyclerView recyclerView,
                          List<?> list) {
        if (!list.contains(null)) {
            list.add(null);
            recyclerView.post(() -> recyclerAdapter.notifyItemInserted(list.size() - 1));
        }
        refresh();
        if (handler != null) {
            handler.postDelayed(() -> {
                if (list.contains(null)) {
                    list.removeAll(Collections.singleton(null));
                    recyclerView.post(recyclerAdapter::notifyDataSetChanged);
                }
            }, 1000);
        }
    }


    @Override
    public void onSubmit(String type, String title, String topic, String text, String link) {
        MaterialHelperClass material = new MaterialHelperClass(classId, link, text, title, topic, type);
        Log.d("MaterialFragment", "onSubmit: " + title);
        createMaterial(material);
    }

    // create class function
    private void createMaterial(@NonNull MaterialHelperClass materialObject) {
        String key = materialObject.generateKey();
        materials.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String message;
                if (snapshot.exists() && snapshot.getValue(MaterialHelperClass.class) != null) {
                    message = "This Material already exist!";
                } else {
                    // add material to materials
                    materials.child(key).setValue(materialObject);
                    message = "Creating material: " + materialObject.getTitle();
                }
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void refresh() {
        new Handler().postDelayed(() -> {
            List<MaterialTopicRvModel> tempTopicItems = new ArrayList<>();
            List<MaterialItemRvModel> tempMaterialItems = new ArrayList<>();
            if (items.size() == 0 || (items.size() == 1 && items.get(items.size() - 1) == null)) {
                binding.materialItem.setVisibility(View.INVISIBLE);
            }
            for (MaterialHelperClass item : dbItems) {

                if (item == null) {
                    Log.d("MaterialFragment", "refresh: NULL ITEM dbItems");
                    continue;
                }
                int icon;
                switch (item.getType()) {
                    case "Videos":
                        icon = R.drawable.video_icon;
                        break;
                    case "Links":
                        icon = R.drawable.link_icon;
                        break;
                    case "Alerts":
                        icon = R.drawable.message_icon;
                        break;
                    default:
                        icon = R.drawable.note_icon;
                }

                MaterialTopicRvModel tItem = new MaterialTopicRvModel(item.getTopic());
                MaterialItemRvModel iItem = new MaterialItemRvModel(icon, item.getTitle(), item.getText(), item.generateKey());

                if (!tempTopicItems.contains(tItem)) {
                    tempTopicItems.add(tItem);
                }
                Log.d("MaterialFragment", "refresh: " + item.getType() + " :: " + selectedType);
                Log.d("MaterialFragment", "refresh: " + item.getTopic() + " :: " + selectedTopic);
                if (item.getType().equals(selectedType)
                        && !tempMaterialItems.contains(iItem)) {
                    tempMaterialItems.add(iItem);
                }
            }

            Log.d("MaterialFragment", "delayed: " + items.size() + " : " + topicItems.size());
            topicRvAdapter.notifyItemRangeRemoved(0, topicItems.size());
            topicItems.clear();
            topicItems.addAll(tempTopicItems);
            items.clear();
            items.addAll(tempMaterialItems);
            topicItems.removeAll(Collections.singleton(null));
            items.removeAll(Collections.singleton(null));
            if (items.size() > 0) {
                binding.materialItem.setVisibility(View.VISIBLE);
            }
            topicRvAdapter.notifyDataSetChanged();
            itemRvAdapter.notifyDataSetChanged();
            topicRvAdapter.setLoaded();
            itemRvAdapter.setLoaded();
            Log.d("MaterialFragment", "delayed: After load: " + items.size() + ":" + topicItems.size());
        }, 500);

    }

    private ValueEventListener getDbListener() {
        if (dbListener == null) {
            if (dbItems == null) dbItems = new ArrayList<>();
            dbListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Log.d("MaterialFragment", "onDataChange: DataBase Data Changed!");
                        for (DataSnapshot c : snapshot.getChildren()) {
                            MaterialHelperClass elt = c.getValue(MaterialHelperClass.class);
                            if (!dbItems.contains(elt)) {
                                Log.d("MaterialFragment", "onDataChange: Found Item " + elt.getTitle());
                                dbItems.add(elt);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            };
        }
        return dbListener;
    }
}










