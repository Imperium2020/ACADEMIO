package com.imperium.academio.ui.fragment;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.imperium.academio.CustomUtil;
import com.imperium.academio.Login;
import com.imperium.academio.MaterialVideoView;
import com.imperium.academio.R;
import com.imperium.academio.databinding.FragmentMaterialBinding;
import com.imperium.academio.fireclass.MaterialHelperClass;
import com.imperium.academio.ui.adapters.MaterialItemRvAdapter;
import com.imperium.academio.ui.adapters.MaterialTopicRvAdapter;
import com.imperium.academio.ui.model.MaterialItemRvModel;
import com.imperium.academio.ui.model.MaterialTopicRvModel;

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
    StorageReference classStorage;
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
        classStorage = FirebaseStorage.getInstance().getReference(classId);
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

        int width = binding.materialType.getWidth();
        for (int i = 0; i < typeCount; i++) {
            // Inflate chip and set values
            Chip typeChip = (Chip) getLayoutInflater().inflate(R.layout.template_choice_chip, binding.materialType, false);
            typeChip.setText(materialTypes.get(i));
            typeChip.setChipIconResource(materialTypeIcons.get(i));
            typeChip.setWidth(width / (typeCount));

            // Add inflated chip to chipGroup
            binding.materialType.addView(typeChip);
        }

        // Create adapters
        topicRvAdapter = new MaterialTopicRvAdapter(activity, topicItems);
        itemRvAdapter = new MaterialItemRvAdapter(activity, items);

        prepareAdapter(binding.materialTopic, topicRvAdapter, LinearLayoutManager.HORIZONTAL);
        prepareAdapter(binding.materialItem, itemRvAdapter, LinearLayoutManager.VERTICAL);

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
                Chip chip = group.findViewById(chipId);
                selectedType = chip.getText().toString();
            }
            refresh();
        });

        // Create and attach listener to material item recyclerview
        itemRvAdapter.setOnItemClickListener(new MaterialItemRvAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                MaterialHelperClass material = getDbItem(position);
                if (material == null) return;
                switch (material.getType()) {
                    case "Links":
                    case "Alerts":
                        AlertDialog dialog = buildTextDialog(material);
                        dialog.show();
                        break;
                    case "Notes":
                        broadcastIntent(material);
                        break;
                    case "Videos":
                        if (material.link == null) return;
                        Intent videoIntent = new Intent(activity, MaterialVideoView.class);
                        videoIntent.putExtra("uriString", material.getLink());
                        videoIntent.putExtra("title", material.title);
                        videoIntent.putExtra("text", material.text);
                        startActivity(videoIntent);
                        break;

                    default:
                        break;
                }

            }

            @Override
            public void onItemLongPressed(View itemView, int position) {
                // If not teacher, do nothing
                if (!userId.equals(teacherId)) return;
                MaterialHelperClass material = getDbItem(position);

                if (material == null) return;
                AlertDialog alertDialog = buildDeleteDialog(material);
                alertDialog.show();
            }
        });

        // Add material button listener
        binding.materialAddItem.setOnClickListener(item -> {
            MaterialDialogFragment fragment = MaterialDialogFragment.newInstance(this, topicItems);
            fragment.show(activity.getSupportFragmentManager(), "addMaterial");
        });

        binding.materialBtnRefresh.setOnClickListener(btn -> refresh());

        // Add listener to swipe gesture on items
        binding.materialItemSwipe.setOnRefreshListener(this::refresh);

        handler.postDelayed(this::refresh, 600);
    }

    private AlertDialog buildDeleteDialog(MaterialHelperClass material) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle("Delete");
        builder.setMessage("Are you sure you want to delete this material ?" +
                "\nTitle: " + material.getTitle());
        builder.setPositiveButton("yes", (dialogInterface, i) -> deleteMaterial(material));

        builder.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
        return builder.create();
    }

    private AlertDialog buildTextDialog(MaterialHelperClass material) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        TextView showText = new TextView(builder.getContext());
        if (material.link != null) {
            showText.setText(material.text.concat("\n\nLink: " + material.getLink()));
            builder.setPositiveButton("Visit Link", (di, i) -> broadcastIntent(material));
        } else {
            showText.setText(material.text);
        }
        showText.setPadding(getDp(24), getDp(5), getDp(12), getDp(10));
        showText.setTextIsSelectable(true);
        builder.setView(showText);
        builder.setTitle(material.getTitle());
        builder.setCancelable(true);
        return builder.create();
    }


    @Override
    public void onSubmit(String type, String title, String topic, String text, String link) {
        createMaterial(classId, link, text, title, topic, type);
    }

    @Override
    public void onSubmit(String type, String title, String topic, String text, Uri link) {
        binding.progressbar.setVisibility(View.VISIBLE);
        // get reference link and attach link to material object link
        StorageReference mStorage = classStorage.child(CustomUtil.SHA1(title));
        try {
            InputStream fileStream = activity.getContentResolver().openInputStream(link);
            UploadTask uploadTask = mStorage.putStream(fileStream);

            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful() && task.getException() != null) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return mStorage.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    String dbLink = task.getResult().toString();
                    createMaterial(classId, dbLink, text, title, topic, type);
                } else {
                    Toast.makeText(activity, "Upload got interrupted.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (FileNotFoundException fnf) {
            Toast.makeText(activity, "File Not Found!", Toast.LENGTH_SHORT).show();
            Log.d("MaterialFragment", "onSubmit(Uri): File Not Found", fnf);
        }
    }

    private void prepareAdapter(RecyclerView recyclerView, RecyclerView.Adapter<?> holder, int orientation) {
        // set recyclerview properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity, orientation, false));
        // Attach adapter
        recyclerView.setAdapter(holder);
    }

    private void broadcastIntent(MaterialHelperClass material) {
        if (material.link == null) return;
        String chooser_title = "Select an app for viewing";
        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
        try {
            Uri uri = Uri.parse(material.getLink());
            viewIntent.setData(uri);
            Intent chooser = Intent.createChooser(viewIntent, chooser_title);
            startActivity(chooser);
        } catch (Exception e) {
            if (e instanceof ActivityNotFoundException)
                Toast.makeText(activity, "Could not find app", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(activity, "Some Error Occurred!", Toast.LENGTH_SHORT).show();
            Log.e("MaterialFragment", "onItemClick: ", e);
        }
    }

    // create material function
    private void createMaterial(String classId, String link, String text, String title, String topic, String type) {
        MaterialHelperClass materialObject = new MaterialHelperClass(
                classId, link, text, title, topic, type
        );
        String key = materialObject.generateKey();
        materials.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // add material to list of materials in db
                    materials.child(key).setValue(materialObject);
                    Toast.makeText(activity, "Creating material: " + title,
                            Toast.LENGTH_SHORT).show();
                    binding.progressbar.setVisibility(View.GONE);
                } else {
                    Toast.makeText(activity,
                            "This Material Title already exist! Please use another title",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void deleteMaterial(MaterialHelperClass material) {
        List<String> storageType = Arrays.asList("Videos", "Notes");
        String key = material.generateKey();

        materials.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    MaterialHelperClass item = snapshot.getValue(MaterialHelperClass.class);
                    if (item == null) return;
                    if (storageType.contains(item.getLink())) {
                        classStorage.child(key).delete()
                                .addOnSuccessListener(a -> materials.child(key).removeValue());
                    } else {
                        materials.child(key).removeValue();
                    }
                    refresh();
                }
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
                    Collections.sort(dbItems, (m1, m2) -> (int) (m2.timestamp - m1.timestamp));
                    binding.materialBtnRefresh.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
    }

    private MaterialHelperClass getDbItem(int position) {
        MaterialHelperClass material = null;
        MaterialItemRvModel item = items.get(position);
        String title = item.getTitle();
        for (MaterialHelperClass mat : dbItems) {
            if (title.equals(mat.title)) {
                material = mat;
                break;
            }
        }
        return material;
    }

    private int getDp(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
