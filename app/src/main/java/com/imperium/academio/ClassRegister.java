package com.imperium.academio;

import android.content.Intent;
import android.os.Bundle;
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
import java.util.Collections;
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

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null || user.getDisplayName() == null) {
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }
        userId = CustomUtil.SHA1(user.getDisplayName());
        cList = (cList == null) ? new ArrayList<>() : cList;

        cList.add(null);
        registerRvAdapter = new ClassRegisterRvAdapter(binding.classRegister, ClassRegister.this, cList);
        binding.classRegister.setLayoutManager(new LinearLayoutManager(
                ClassRegister.this, LinearLayoutManager.VERTICAL, false
        ));
        registerRvAdapter.setOnItemClickListener((itemView, position) -> {
            Intent intent = new Intent(ClassRegister.this, MainMenu.class);
            intent.putExtra("classId", cList.get(position).getKey());
            intent.putExtra("userId", userId);
            startActivity(intent);
            Toast.makeText(this, "Opening class: " + cList.get(position).getName(), Toast.LENGTH_SHORT).show();
            finish();
        });

        registerRvAdapter.setLoadMore(() -> {
            if (!cList.contains(null)) {
                cList.add(null);
                binding.classRegister.post(() -> registerRvAdapter.notifyItemInserted(cList.size() - 1));
            }
            loadClasses();
        });
        binding.classRegister.setAdapter(registerRvAdapter);


        binding.btnLogout.setOnClickListener(view -> {
            firebaseAuth.signOut();
            startActivity(new Intent(ClassRegister.this, Login.class));
            finish();
        });

        binding.btnClassRegister.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(ClassRegister.this, binding.btnClassRegister);
            popup.getMenuInflater().inflate(R.menu.new_class_choice, popup.getMenu());

            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(item -> {
                boolean isJoin = !getString(R.string.create_class).equals(item.getTitle().toString());
                ClassDialogFragment fragment = ClassDialogFragment.newInstance(isJoin);
                fragment.show(getSupportFragmentManager(), "addClass");
                return true;
            });
            popup.show(); //showing popup menu
        });
    }


    @Override
    public void onSubmit(boolean isJoin, String classname, String teacherName) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null || user.getDisplayName() == null) return;
        if (isJoin) {
            ClassHelperClass classObject = new ClassHelperClass(classname, CustomUtil.SHA1(teacherName));
            joinClass(classObject);
        } else {
            // create class
            ClassHelperClass classObject = new ClassHelperClass(classname, userId);
            createClass(classObject);
        }
    }

    // create class function
    private void createClass(@NonNull ClassHelperClass classObject) {
        String key = classObject.generateKey();
        classes.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String message;
                if (snapshot.exists() && snapshot.getValue(ClassHelperClass.class) != null) {
                    message = "This class already exist!";
                } else {
                    // add class to classes
                    classes.child(key).setValue(classObject);

                    // add class to user
                    FirebaseDatabase.getInstance().getReference("users/" + userId)
                            .child("classes/" + key)
                            .setValue(classObject.className);

                    message = "Creating a new class: " + classObject.className;

                    // Intent to the class
                    ClassRegister.this.startActivity(
                            new Intent(ClassRegister.this, MainMenu.class)
                                    .putExtra("userId", classObject.teacherId)
                                    .putExtra("teacherId", classObject.teacherId)
                                    .putExtra("classId", key)
                    );

                }
                Toast.makeText(ClassRegister.this, message, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void joinClass(ClassHelperClass classObject) {
        String key = classObject.generateKey();
        classes.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ClassHelperClass classFromDb = snapshot.getValue(ClassHelperClass.class);
                String message;
                if (!snapshot.exists() || classFromDb == null) {
                    message = "Class Doesn't Exist!";
                } else {

                    // add user to class
                    if (!userId.equals(classFromDb.teacherId))
                        classes.child(key).child("students/" + userId).setValue(true);

                    // add class to user
                    FirebaseDatabase.getInstance().getReference("users/" + userId)
                            .child("classes/" + key)
                            .setValue(classObject.className);

                    // Intent to the class
                    ClassRegister.this.startActivity(
                            new Intent(ClassRegister.this, MainMenu.class)
                                    .putExtra("classId", key)
                                    .putExtra("teacherId", classFromDb.teacherId)
                                    .putExtra("userId", userId)
                    );
                    message = "Opening class: " + classFromDb.className;
                }
                Toast.makeText(ClassRegister.this, message, Toast.LENGTH_SHORT).show();
                ClassRegister.this.finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadClasses() {
        DatabaseReference userClasses = FirebaseDatabase.getInstance().getReference("users/" + userId + "/classes");

        userClasses.orderByValue().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    for (DataSnapshot c : snapshot.getChildren()) {
                        ClassRegisterRvModel elt = new ClassRegisterRvModel(c.getKey(), c.getValue(String.class));
                        if (!cList.contains(elt))
                            cList.add(elt);
                    }
                    cList.removeAll(Collections.singleton(null));
                    registerRvAdapter.notifyDataSetChanged();
                    registerRvAdapter.setLoaded();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
