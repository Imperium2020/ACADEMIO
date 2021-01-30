package com.imperium.academio

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.*
import com.imperium.academio.databinding.ActivityMainMenuBinding
import com.imperium.academio.fireclass.ClassHelperClass
import com.imperium.academio.ui.fragment.AttendanceStudent
import com.imperium.academio.ui.fragment.AttendanceTeacher
import com.imperium.academio.ui.fragment.MaterialFragment
import com.imperium.academio.ui.fragment.TemplateFragment
import java.util.*

class MainMenu : AppCompatActivity() {
    private lateinit var selectedClass: DatabaseReference
    private lateinit var binding: ActivityMainMenuBinding

    lateinit var defaultArgs: Bundle
    private var mediator: TabLayoutMediator? = null
    private var stateAdapter: FragmentStateAdapter? = null
    var fragments: MutableList<NamedFragment> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.extras == null) return
        defaultArgs = intent.extras!!

        val path = "class/${defaultArgs.getString("classId")}"
        selectedClass = FirebaseDatabase.getInstance().getReference(path)
        pauseForDB()
    }

    private fun pauseForDB() {
        // Check if User is a Teacher for this class
        // If yes, allow to add new materials
        selectedClass.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Get teacherId of current class
                    val selectedClassObject = snapshot.getValue(ClassHelperClass::class.java)
                            ?: return

                    // continue after fetching teacher id
                    defaultArgs.putString("teacherId", selectedClassObject.teacherId)
                    continueAfterDB(defaultArgs)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun continueAfterDB(args: Bundle) {
        val userId = args.getString("userId")
        val teacherId = args.getString("teacherId")

        // Add Material fragment
        fragments.add(NamedFragment(MaterialFragment.newInstance(args), "Materials"))
        if (teacherId != null && userId != null) {
            // If user is teacher, add teacher fragment accordingly
            if (userId == teacherId) {
                val tFragment = AttendanceTeacher.newInstance(args)

                // Student button listener
                tFragment.studentViewer = object : AttendanceTeacher.StudentViewer {
                    override fun createViewer(studentArgs: Bundle?, studentName: String?) {
                        // Detach tab mediator
                        mediator?.detach()

                        // If student fragment exist, remove and notify adapter
                        if (fragments.size == 3) {
                            fragments.removeAt(2)
                            stateAdapter?.notifyItemRemoved(2)
                        }

                        // Add student fragment and notify adapter
                        val ft = NamedFragment(AttendanceStudent.newInstance(studentArgs), studentName)
                        fragments.add(ft)
                        stateAdapter?.notifyItemInserted(2)

                        // Attach mediator after transaction
                        mediator?.attach()
                    }
                }


                fragments.add(NamedFragment(tFragment, "Attendance"))
            } else {
                // Add student fragment
                fragments.add(NamedFragment(
                        AttendanceStudent.newInstance(args), "Attendance"))
            }
        }
        stateAdapter = object : FragmentStateAdapter(this@MainMenu) {
            override fun createFragment(position: Int): Fragment {
                return if (fragments.size > position) {
                    fragments[position].fragment
                } else TemplateFragment.newInstance("Whoops! Future Feature!")
            }

            override fun getItemCount(): Int {
                return fragments.size
            }
        }

        binding.viewPager.apply {
            adapter = stateAdapter
            isUserInputEnabled = false
        }
        mediator = TabLayoutMediator(binding.tabs, binding.viewPager)
        { tab: TabLayout.Tab, position: Int ->
            if (fragments.size > position)
                tab.text = fragments[position].name
            else
                tab.text = "Future Feature"
        }.also { it.attach() }
    }
}

data class NamedFragment(val fragment: Fragment, val name: String?)