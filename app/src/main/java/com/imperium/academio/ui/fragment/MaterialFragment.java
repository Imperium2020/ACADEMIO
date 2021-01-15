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

import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.imperium.academio.Login;
import com.imperium.academio.R;
import com.imperium.academio.databinding.FragmentMaterialBinding;
import com.imperium.academio.fireclass.MaterialHelperClass;
import com.imperium.academio.ui.adapters.MaterialItemRvAdapter;
import com.imperium.academio.ui.adapters.MaterialTopicRvAdapter;
import com.imperium.academio.ui.model.MaterialItemRvModel;
import com.imperium.academio.ui.model.MaterialTopicRvModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MaterialFragment extends Fragment implements MaterialDialogFragment.SubmitListener {
    FragmentMaterialBinding binding;
    FragmentActivity activity;
    FirebaseAuth firebaseAuth;
    DatabaseReference selectedClass;
    DatabaseReference materials;
    String userId;
    String classId;
    String teacherId;

    List<String> materialTypes;
    List<Integer> materialTypeIcons;

    List<MaterialTopicRvModel> topicItems;
    List<MaterialItemRvModel> items;
    List<MaterialHelperClass> dbItems;

    MaterialTopicRvAdapter topicRvAdapter;
    MaterialItemRvAdapter itemRvAdapter;

    Handler handler;

    String selectedType;
    String selectedTopic;

    ValueEventListener dbListener;

    public MaterialFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static MaterialFragment newInstance(Bundle args) {
        // Create instance of fragment
        MaterialFragment fragment = new MaterialFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Get data from intent
        Bundle bundle = getArguments();
        if (bundle != null) {
            classId = bundle.getString("classId");
            userId = bundle.getString("userId");
            teacherId = bundle.getString("teacherId");
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
        selectedClass = FirebaseDatabase.getInstance().getReference("class/" + classId);
        materials = FirebaseDatabase.getInstance().getReference("materials");

        // Redirect to login if user or class is not set from intent
        if (userId == null || classId == null || teacherId == null) {
            activity.startActivity(new Intent(activity, Login.class));
            activity.finish();
            return;
        }

        // Allow teacher to add material
        if (userId.equals(teacherId)) {
            binding.materialAddItem.setVisibility(View.VISIBLE);
        }

        // Data Arrays
        dbItems = new ArrayList<>();
        topicItems = new ArrayList<>();
        items = new ArrayList<>();

        // Attach listener to class
        Query query = materials.orderByChild("classId").equalTo(classId);
        dbListener = getDbListener();
        query.addValueEventListener(dbListener);


        // Populate lists for type selection
        materialTypes = Arrays.asList(getResources().getStringArray(R.array.types));
        materialTypeIcons = Arrays.asList(R.drawable.note_icon, R.drawable.video_icon,
                R.drawable.link_icon, R.drawable.message_icon);

        // Check if list sizes match and if yes, assign size to typeCount
        int typeCount;
        if (materialTypes.size() == materialTypeIcons.size())
            typeCount = materialTypes.size();
        else return;

        for (int i = 0; i < typeCount; i++) {
            // Inflate chip and set values
            Chip typeChip = (Chip) getLayoutInflater().inflate(R.layout.template_choice_chip, binding.materialType, false);
            typeChip.setText(materialTypes.get(i));
            typeChip.setChipIconResource(materialTypeIcons.get(i));

            // Add inflated chip to chipGroup
            binding.materialType.addView(typeChip);
        }

        // Create adapters
        topicRvAdapter = new MaterialTopicRvAdapter(activity, topicItems);
        itemRvAdapter = new MaterialItemRvAdapter(binding.materialItem, activity, items);

        // set recyclerview properties
        binding.materialTopic.setHasFixedSize(true);
        binding.materialItem.setHasFixedSize(true);
        binding.materialTopic.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        binding.materialItem.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));

        // Attach adapter
        binding.materialTopic.setAdapter(topicRvAdapter);
        binding.materialItem.setAdapter(itemRvAdapter);

        // Create and attach listener to topic recyclerview
        topicRvAdapter.setOnTopicItemClickListener((itemView, position) -> {
            String clickedTopic = topicItems.get(position).getTopic();
            selectedTopic =
                    (selectedTopic == null || !selectedTopic.equals(clickedTopic)) ? clickedTopic : null;
            refresh();
        });

        // Create and attach listener to type chipGroup
        binding.materialType.setOnCheckedChangeListener((group, chipId) -> {
            if (chipId == View.NO_ID) {
                selectedType = null;
            } else {
                Chip chip = binding.materialType.findViewById(chipId);
                selectedType = chip.getText().toString();
            }
            refresh();
        });

        // Create and attach listener to material item recyclerview
        itemRvAdapter.setOnItemClickListener((itemView, position) -> {
            String title = items.get(position).getTitle();
            query.removeEventListener(dbListener);
            Toast.makeText(activity, title + " was clicked!", Toast.LENGTH_SHORT).show();
        });

        // Add material button listener
        binding.materialAddItem.setOnClickListener(item -> {
            if (topicItems.size() <= 0) return;
            MaterialDialogFragment fragment = MaterialDialogFragment.newInstance(this, topicItems);
            fragment.show(activity.getSupportFragmentManager(), "addMaterial");
        });

        binding.materialBtnRefresh.setOnClickListener(btn -> refresh());

        // Add listener to swipe gesture on items
        binding.materialItemSwipe.setOnRefreshListener(this::refresh);

        handler.postDelayed(this::refresh, 600);
    }


    @Override
    public void onSubmit(String type, String title, String topic, String text, String link) {
        MaterialHelperClass material = new MaterialHelperClass(classId, link, text, title, topic, type);
        if (type.equals("Notes") || type.equals("Videos")) {
            try {
                pushFile(material);
            } catch (FileNotFoundException e) {
                Toast.makeText(activity, "Cannot Access file!", Toast.LENGTH_SHORT).show();
                Log.e("MaterialFragment", "onSubmit: File not found", e);
            }
        } else {
            createMaterial(material);
        }
    }

    // push file function
    private void pushFile(@NonNull MaterialHelperClass materialObject) throws FileNotFoundException {
        // get reference link and attach link to material object link
        StorageReference storage = FirebaseStorage.getInstance().getReference();
        String path = materialObject.link;
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        StorageReference classStorage = storage.child(materialObject.classId + "/" + fileName);

        Log.d("MaterialFragment", "pushFile: " + path);
        InputStream stream = new FileInputStream(new File(path));
        UploadTask uploadTask = classStorage.putStream(stream);
        uploadTask.addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            Toast.makeText(activity, "Upload got interrupted.", Toast.LENGTH_SHORT).show();
        }).addOnSuccessListener(taskSnapshot -> {
            // Handle successful uploads
            createMaterial(new MaterialHelperClass(
                    materialObject.classId, classStorage.getPath(), materialObject.text,
                    materialObject.title, materialObject.topic, materialObject.type
            ));
        });
    }

    // create material function
    private void createMaterial(@NonNull MaterialHelperClass materialObject) {
        String key = materialObject.generateKey();
        materials.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String message;
                if (snapshot.exists() && snapshot.getValue(MaterialHelperClass.class) != null) {
                    message = "This Material Title already exist!";
                } else {
                    // add material to list of materials in db
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

    // Function to perform when refresh is called
    private void refresh() {
        binding.materialItemSwipe.setRefreshing(true);
        binding.materialBtnRefresh.setVisibility(View.GONE);
        reloadItems();
        // remove refreshing animation after 10 seconds
        new Handler().postDelayed(() -> binding.materialItemSwipe.setRefreshing(false), 10000);
    }

    private void reloadItems() {
        new Handler().post(() -> {
            if (materialTypes == null || materialTypeIcons == null) return;

            topicItems.clear();
            items.clear();

            // For each item in database list add item according to filters
            for (MaterialHelperClass item : dbItems) {
                if (item == null) continue;

                // get icon drawable
                int icon = -1;
                if (materialTypes.contains(item.getType())) {
                    icon = materialTypeIcons.get(
                            // Index of type in materialTypes
                            materialTypes.indexOf(item.getType())
                    );
                }

                // Create and add type items
                MaterialTopicRvModel tItem = new MaterialTopicRvModel(item.getTopic());
                if (!topicItems.contains(tItem)) {
                    topicItems.add(tItem);
                }

                // Check if any filter is selected and if yes, item matches the filter
                if (selectedType != null && !selectedType.equals(item.getType())) continue;
                if (selectedTopic != null && !selectedTopic.equals(item.getTopic())) continue;

                // Add item to list
                MaterialItemRvModel iItem = new MaterialItemRvModel(icon, item.getTitle(), item.getText(), item.generateKey());
                if (!items.contains(iItem))
                    items.add(iItem);
            }

            // Remove any null element and notify the changes to adapters
            topicItems.removeAll(Collections.singleton(null));
            items.removeAll(Collections.singleton(null));
            topicRvAdapter.notifyDataSetChanged();
            itemRvAdapter.notifyDataSetChanged();

            // Remove swipe refreshing animation
            binding.materialItemSwipe.setRefreshing(false);
        });
    }

    // Create ValueEvent listener for material database
    private ValueEventListener getDbListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Clear out current background list and get from database cache
                    List<MaterialHelperClass> tempItemList = new ArrayList<>();
                    for (DataSnapshot c : snapshot.getChildren()) {
                        MaterialHelperClass elt = c.getValue(MaterialHelperClass.class);
                        if (elt != null && !tempItemList.contains(elt)) {
                            tempItemList.add(elt);
                        }
                    }
                    // Notify user to reload if new item found.
                    if (activity != null && 0 < dbItems.size() && dbItems.size() != tempItemList.size()) {
                        Toast.makeText(activity, "Stream has been updated.", Toast.LENGTH_SHORT).show();
                    }

                    dbItems.clear();
                    dbItems.addAll(tempItemList);
                    Log.d("MaterialFragment", "onDataChange");
                    Collections.sort(dbItems, (m1, m2) -> (int) (m2.timestamp - m1.timestamp));
                    binding.materialBtnRefresh.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
    }
}










