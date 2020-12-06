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
import androidx.recyclerview.widget.RecyclerView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_class_register);
        firebaseAuth = FirebaseAuth.getInstance();
        classes = FirebaseDatabase.getInstance().getReference("class");

        RecyclerView classRv;
        ClassRegisterRvAdapter registerRvAdapter;

        List<ClassRegisterRvModel> classes = new ArrayList<>();

        classes.add(new ClassRegisterRvModel("Design Project"));
        classes.add(new ClassRegisterRvModel("System Software"));
        classes.add(new ClassRegisterRvModel("Data Communication"));

        classRv = findViewById(R.id.class_register);
        registerRvAdapter = new ClassRegisterRvAdapter(classRv, this, classes);
        classRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        classRv.setAdapter(registerRvAdapter);

        registerRvAdapter.setOnItemClickListener((itemView, position) -> {
            String name = classes.get(position).getName();
            Toast.makeText(this, name + " was clicked!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ClassRegister.this, MainMenu.class);
            startActivity(intent);
        });

        registerRvAdapter.setLoadMore(() -> {
            if (classes.size() < 6) {
                classes.add(null);
                classRv.post(() -> registerRvAdapter.notifyItemInserted(classes.size() - 1));
                new Handler().postDelayed(() -> {
                    classes.remove(null);
                    classes.add(new ClassRegisterRvModel("Theory of Computation"));
                    classes.add(new ClassRegisterRvModel("Graph Theory and Combinatorics"));
                    classes.add(new ClassRegisterRvModel("Microprocessors and Microcontrollers"));
                    registerRvAdapter.notifyDataSetChanged();
                    registerRvAdapter.setLoaded();
                }, 2000);

            }
        });


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
        String username = user.getDisplayName();
        if (isJoin) {
            ClassHelperClass classObject = new ClassHelperClass(classname, CustomUtil.SHA1(teacherName));
            joinClass(classObject, username);
        } else {
            // create class
            ClassHelperClass classObject = new ClassHelperClass(classname, CustomUtil.SHA1(username));
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
                    FirebaseDatabase.getInstance().getReference("users/" + classObject.teacherId)
                            .child("classes/" + key)
                            .setValue(true);

                    message = "Creating a new class: " + classObject.className;

                    // Intent to the class
                    ClassRegister.this.startActivity(
                            new Intent(ClassRegister.this, MainMenu.class)
                                    .putExtra("UserId", classObject.teacherId)
                                    .putExtra("ClassId", key)
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

    private void joinClass(ClassHelperClass classObject, String username) {
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
                    String userId = CustomUtil.SHA1(username);
                    if (!userId.equals(classFromDb.teacherId))
                        classes.child(key).child("students/" + userId).setValue(true);

                    // add class to user
                    FirebaseDatabase.getInstance().getReference("users/" + classObject.teacherId)
                            .child("classes/" + key)
                            .setValue(true);

                    // Intent to the class
                    ClassRegister.this.startActivity(
                            new Intent(ClassRegister.this, MainMenu.class)
                                    .putExtra("ClassId", key)
                                    .putExtra("UserId", username)
                    );
                    message = "Opening class: " + classFromDb.className;
                }
                Toast.makeText(ClassRegister.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

}


/*
    private void joinClass(DatabaseReference reference, Activity activity, ClassHelperClass cObj, String username) {
        String key = cObj.generateKey();
        reference.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ClassHelperClass klass = snapshot.getValue(ClassHelperClass.class);
                String message;
                if (!snapshot.exists() || klass == null) {
                    message = "Class Doesn't Exist!";
                } else {
                    activity.startActivity(new Intent(activity, MainMenu.class).putExtra("ClassId", key).putExtra("UserId", username));
                    message = "Opening class: " + klass.className;
                }
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Error while trying to access classes.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

 */