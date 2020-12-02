package com.imperium.academio;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.imperium.academio.ui.adapters.ClassRegisterRvAdapter;
import com.imperium.academio.ui.model.ClassRegisterRvModel;

import java.util.ArrayList;
import java.util.List;

public class ClassRegister extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    Button addClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_register);
        firebaseAuth = FirebaseAuth.getInstance();
        Button lout = (Button) findViewById(R.id.logout);
        lout.setOnClickListener(view -> {
            firebaseAuth.signOut();
            startActivity(new Intent(ClassRegister.this,Login.class));
            finish();
        });
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

        addClass = findViewById(R.id.btn_class_register);
        addClass.setOnClickListener(view -> {
            // create view and add here!
            Toast.makeText(this, "Button Pressed", Toast.LENGTH_SHORT).show();
        });
    }
}
