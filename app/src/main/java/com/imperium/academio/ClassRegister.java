package com.imperium.academio;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.imperium.academio.databinding.ActivityClassRegisterBinding;
import com.imperium.academio.fireclass.ClassHelperClass;
import com.imperium.academio.ui.adapters.ClassRegisterRvAdapter;
import com.imperium.academio.ui.fragment.ClassDialogFragment;
import com.imperium.academio.ui.model.ClassRegisterRvModel;

import java.util.ArrayList;
import java.util.List;

public class ClassRegister extends AppCompatActivity implements ClassDialogFragment.SubmitListener {
    ActivityClassRegisterBinding binding;
    private FirebaseAuth firebaseAuth;
    DatabaseReference classes;
    String userId;
    List<ClassRegisterRvModel> cList;
    ClassRegisterRvAdapter registerRvAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_class_register);
        firebaseAuth = FirebaseAuth.getInstance();
        classes = FirebaseDatabase.getInstance().getReference("class");

        // Get user from FirebaseAuth
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null || user.getDisplayName() == null) {
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }
        userId = CustomUtil.SHA1(user.getDisplayName());
        cList = (cList == null) ? new ArrayList<>() : cList;

        // Create recycler adapter for class list
        registerRvAdapter = new ClassRegisterRvAdapter(ClassRegister.this, cList);
        binding.classRegister.setHasFixedSize(true);
        binding.classRegister.setLayoutManager(new LinearLayoutManager(
                ClassRegister.this, LinearLayoutManager.VERTICAL, false
        ));

        // Attach OnClick listener to all classes in class list
        registerRvAdapter.setOnItemClickListener((itemView, position) -> {
            Toast.makeText(
                    ClassRegister.this,
                    "Opening class: " + cList.get(position).getName(),
                    Toast.LENGTH_SHORT
            ).show();
            // Open the class and finish the activity
            Intent intent = new Intent(ClassRegister.this, MainMenu.class);
            intent.putExtra("classId", cList.get(position).getKey());
            intent.putExtra("userId", userId);
            startActivity(intent);
        });


        // Attach recycler adapter for class list
        binding.classRegister.setAdapter(registerRvAdapter);

        // Create and Attach refresh listener
        binding.classRegisterSwipe.setOnRefreshListener(this::refresh);

        // Create and Attach logout button listener
        binding.btnLogout.setOnClickListener(view -> {
            // Goto Login activity after signout
            firebaseAuth.signOut();
            startActivity(new Intent(ClassRegister.this, Login.class));
            finish();
        });

        // Create and Attach register button listener
        binding.btnClassRegister.setOnClickListener(view -> {
            // Create a popup menu to choose login or create
            PopupMenu popup = new PopupMenu(ClassRegister.this, binding.btnClassRegister);
            popup.getMenuInflater().inflate(R.menu.new_class_choice, popup.getMenu());

            // Create Onclick listener for popup items
            popup.setOnMenuItemClickListener(item -> {
                // If button pressed is to join a class buttonType is true,
                // else it is false meaning to create a class.
                boolean buttonType = getString(R.string.join_class).equals(item.getTitle().toString());

                ClassDialogFragment fragment = ClassDialogFragment.newInstance(buttonType);
                fragment.show(getSupportFragmentManager(), "addClass");
                return true;
            });

            // Show the popup menu
            popup.show();
        });

        // Refresh the class list
        refresh();
    }

    // Function to perform when refresh is called
    private void refresh() {
        binding.classRegisterSwipe.setRefreshing(true);
        refreshClasses();
        // remove refreshing animation after 10 seconds
        new Handler().postDelayed(() -> binding.classRegisterSwipe.setRefreshing(false), 10000);
    }


    @Override
    public void onSubmit(boolean buttonType, String classname, String teacherName) {
        // If button pressed is to join join class
        if (buttonType) {
            ClassHelperClass classObject = new ClassHelperClass(classname, CustomUtil.SHA1(teacherName));
            joinClass(classObject);
        } else {
            // Button pressed is to create class
            ClassHelperClass classObject = new ClassHelperClass(classname, userId);
            createClass(classObject);
        }
        binding.classRegister.postDelayed(this::refreshClasses, 1000);

    }

    // Function to create a new class
    private void createClass(@NonNull ClassHelperClass classObject) {
        String key = classObject.generateKey();
        classes.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String message;
                // Check if class exists already
                if (snapshot.exists() && snapshot.getValue(ClassHelperClass.class) != null) {
                    message = "This class already exist!";
                } else {
                    // Create class and add to classes
                    classes.child(key).setValue(classObject);

                    // Add the class to user in DB
                    FirebaseDatabase.getInstance().getReference("users/" + userId)
                            .child("classes/" + key).setValue(classObject.className);

                    message = "Creating a new class: " + classObject.className;
                }
                Toast.makeText(ClassRegister.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // Function to join an existing class
    private void joinClass(@NonNull ClassHelperClass classObject) {
        String key = classObject.generateKey();
        classes.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ClassHelperClass classFromDb = snapshot.getValue(ClassHelperClass.class);
                String message;
                // Check if class exists or not
                if (!snapshot.exists() || classFromDb == null) {
                    message = "Class Doesn't Exist!";
                } else {
                    // Add user to class in DB
                    if (!userId.equals(classFromDb.teacherId))
                        classes.child(key).child("students/" + userId).setValue(true);

                    // Add class to user in DB
                    FirebaseDatabase.getInstance().getReference("users/" + userId)
                            .child("classes/" + key)
                            .setValue(classObject.className);

                    message = "Adding class: " + classFromDb.className;
                }
                Toast.makeText(ClassRegister.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // Function to refresh class list
    private void refreshClasses() {
        DatabaseReference userClasses = FirebaseDatabase.getInstance().getReference("users/" + userId + "/classes");
        userClasses.orderByValue().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Check if user has joined any classes
                if (snapshot.exists()) {
                    // Clear list and add all classes
                    cList.clear();
                    for (DataSnapshot c : snapshot.getChildren()) {
                        // Add each class to the class list
                        ClassRegisterRvModel elt = new ClassRegisterRvModel(c.getKey(), c.getValue(String.class));
                        if (!cList.contains(elt))
                            cList.add(elt);
                    }
                    // Notify change to recycler view
                    binding.classRegister.post(() -> registerRvAdapter.notifyDataSetChanged());
                    binding.classRegisterSwipe.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.classRegisterSwipe.setRefreshing(false);
            }
        });
    }
}
