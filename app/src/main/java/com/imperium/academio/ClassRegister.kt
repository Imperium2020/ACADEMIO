package com.imperium.academio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.imperium.academio.databinding.ActivityClassRegisterBinding
import com.imperium.academio.fireclass.ClassHelperClass
import com.imperium.academio.ui.adapters.ClassRegisterRvAdapter
import com.imperium.academio.ui.fragment.ClassDialogFragment
import com.imperium.academio.ui.model.ClassRegisterRvModel
import java.util.*

class ClassRegister : AppCompatActivity(), ClassDialogFragment.ClassSubmitListener {
    private lateinit var binding: ActivityClassRegisterBinding

    private lateinit var classes: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userClasses: DatabaseReference

    private lateinit var registerRvAdapter: ClassRegisterRvAdapter
    private var cList: MutableList<ClassRegisterRvModel> = ArrayList()

    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()

        binding = ActivityClassRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get user from FirebaseAuth
        val user: FirebaseUser? = firebaseAuth.currentUser
        if (user == null || user.displayName == null) {
            startActivity(Intent(this, Login::class.java))
            return finish()
        }
        userId = CustomUtil.SHA1(user.displayName.toString())

        classes = FirebaseDatabase.getInstance().getReference("class")
        userClasses = FirebaseDatabase.getInstance().getReference("users/$userId/classes")

        // Create recycler adapter for class list
        registerRvAdapter = ClassRegisterRvAdapter(this@ClassRegister, cList)

        // Attach OnClick listener to all classes in class list
        registerRvAdapter.setOnItemClickListener(
                object : ClassRegisterRvAdapter.OnItemClickListener {
                    override fun onItemClick(itemView: View?, position: Int) {
                        toast("Opening class: " + cList[position].name)
                        // Open the class and finish the activity
                        startActivity(Intent(this@ClassRegister, MainMenu::class.java).let
                        { intent ->
                            intent.putExtra("userId", userId)
                            intent.putExtra("classId", cList[position].key)
                        })
                        finish()
                    }
                }
        )

        binding.classRegister.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(
                    this@ClassRegister, LinearLayoutManager.VERTICAL, false)
            adapter = registerRvAdapter
        }


        // Create and Attach refresh listener
        binding.classRegisterSwipe.setOnRefreshListener { refresh() }

        // Create and Attach logout button listener
        binding.btnLogout.setOnClickListener {
            // Goto Login activity after signout
            firebaseAuth.signOut()
            startActivity(Intent(this@ClassRegister, Login::class.java))
            finish()
        }

        // Create and Attach register button listener
        binding.btnClassRegister.setOnClickListener {
            // Create a popup menu to choose login or create
            val popup = PopupMenu(this@ClassRegister, binding.btnClassRegister)
            popup.menuInflater.inflate(R.menu.new_class_choice, popup.menu)

            // Create Onclick listener for popup items
            popup.setOnMenuItemClickListener { item: MenuItem ->
                // If button pressed is to join a class buttonType is true,
                // else it is false meaning to create a class.
                val buttonType = getString(R.string.join_class) == item.title.toString()
                val fragment = ClassDialogFragment.newInstance(buttonType)
                fragment.show(supportFragmentManager, "addClass")
                true
            }

            // Show the popup menu
            popup.show()
        }

        // Refresh the class list
        refresh()
    }

    // Function to perform when refresh is called
    private fun refresh() {
        binding.classRegisterSwipe.apply {
            isRefreshing = true
            // remove refreshing animation after 10 seconds
            postDelayed({ isRefreshing = false }, 10000)
        }
        refreshClasses()
    }

    override fun onSubmit(buttonType: Boolean, classname: String, teacherName: String) {
        // Join or create class according to menu item chosen
        if (buttonType) joinClass(classname, CustomUtil.SHA1(teacherName))
        else createClass(classname)

        binding.classRegister.postDelayed({ refreshClasses() }, 1000)
    }

    // Function to create a new class
    private fun createClass(classname: String) {
        val classObject = ClassHelperClass(classname, userId)
        val key = classObject.generateKey()

        classes.child(key).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                // Check if class exists already
                if (!snapshot.exists() || snapshot.getValue(ClassHelperClass::class.java) == null) {
                    // Create class and add to classes
                    classes.child(key).setValue(classObject)

                    // Add the class to user in DB
                    FirebaseDatabase.getInstance().getReference("users/$userId")
                            .child("classes/$key").setValue(classObject.className)

                    toast("Creating a new class: " + classObject.className)
                } else {
                    toast("This class already exist!")
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Function to join an existing class
    private fun joinClass(classname: String, teacherId: String) {
        val tempClass = ClassHelperClass(classname, teacherId)
        val key = tempClass.generateKey()

        classes.child(key).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val instance = snapshot.getValue(ClassHelperClass::class.java)
                // Check if class exists or not
                if (snapshot.exists() && instance != null) {
                    // Add user to class in DB
                    if (userId != instance.teacherId) {
                        classes.child("$key/students/$userId").setValue(true)
                    }

                    // Add class to user in DB
                    userClasses.child(key).setValue(tempClass.className)
                    toast("Adding class: " + instance.className)
                } else {
                    toast("Class Doesn't Exist!")
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Function to refresh class list
    private fun refreshClasses() {
        userClasses.orderByValue().addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Check if user has joined any classes
                if (snapshot.exists()) {
                    // Clear list and add all classes
                    cList.clear()
                    for (c in snapshot.children) {
                        // Add each class to the class list
                        val elt = ClassRegisterRvModel(c.key, c.getValue(String::class.java))
                        if (!cList.contains(elt)) cList.add(elt)
                    }
                    // Notify change to recycler view
                    binding.classRegister.post { registerRvAdapter.notifyDataSetChanged() }
                    binding.classRegisterSwipe.isRefreshing = false
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.classRegisterSwipe.isRefreshing = false
            }
        })
    }

    fun Context.toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}