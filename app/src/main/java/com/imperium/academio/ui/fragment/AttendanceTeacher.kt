package com.imperium.academio.ui.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import com.imperium.academio.R
import com.imperium.academio.databinding.FragmentAttendanceTeacherBinding
import com.imperium.academio.databinding.TemplateAttendanceTeacherRowBinding
import com.imperium.academio.fireclass.AttendanceHelperClass
import com.imperium.academio.fireclass.UserHelperClass
import java.util.*
import kotlin.collections.ArrayList

class AttendanceTeacher : Fragment() {
    lateinit var binding: FragmentAttendanceTeacherBinding

    private lateinit var usersReference: DatabaseReference
    private lateinit var selectedClass: DatabaseReference

    private var classId: String? = null
    private var teacherId: String? = null

    private var isDateSelected = false
    private var date = 0
    private var month = 0
    private var year = 0

    private var userList: MutableList<UserHelperClass> = ArrayList()

    var studentViewer: StudentViewer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // Get id from intent
            classId = it.getString("classId")
            teacherId = it.getString("teacherId")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAttendanceTeacherBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = activity
        if (activity == null || classId == null || teacherId == null) return
        selectedClass = FirebaseDatabase.getInstance().getReference("class/$classId")
        usersReference = FirebaseDatabase.getInstance().getReference("users")

        // Pick date
        binding.btnDatePick.setOnClickListener {
            val newFragment = DatePickerFragment()
            newFragment.listener = DatePickerDialog.OnDateSetListener { _, y, m, d ->
                isDateSelected = true
                date = d
                month = m
                year = y
                val selectedDate = "Selected Date: " + date + "/" + (month + 1) + "/" + year
                binding.txtSelectedDate.text = selectedDate
            }
            newFragment.show(activity.supportFragmentManager, "datePicker")
        }

        // Table binding
        binding.table.isStretchAllColumns = true
        binding.table.bringToFront()

        // Setting up select all button
        binding.checkbox.setOnCheckedChangeListener { _: CompoundButton?, b: Boolean ->
            for (i in 1 until binding.table.childCount) {
                // getting the checkbox which is inside linear layout, again inside the table row
                val tableRow = binding.table.getChildAt(i) as TableRow
                val viewStub = tableRow.getChildAt(2) as LinearLayout
                val cb = viewStub.getChildAt(0) as CheckBox
                cb.isChecked = b
            }
        }

        // List of students in class
        populateStudentList()

        // Setting up submit button
        binding.btnSubmit.setOnClickListener {
            val absent: MutableMap<String, Any?> = HashMap()
            val cal = Calendar.getInstance()
            if (!isDateSelected) {
                Toast.makeText(activity, "Select Date!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            cal[year, month, date, 0, 0] = 0
            cal[Calendar.MILLISECOND] = 0
            val timestamp = cal.timeInMillis
            for (i in 1 until binding.table.childCount) {
                // getting the checkbox which is inside linear layout, again inside the table row
                val tableRow = binding.table.getChildAt(i) as TableRow
                val viewStub = tableRow.getChildAt(2) as LinearLayout
                val cb = viewStub.getChildAt(0) as CheckBox

                // update absent record of the selected date
                val userId = userList[i - 1].generateKey()
                val tempAtt = AttendanceHelperClass(userId, timestamp)
                absent[tempAtt.generateKey()] = if (cb.isChecked) null else tempAtt
            }
            selectedClass.apply {
                child("attendance").updateChildren(absent)
                child("sessions/$timestamp").setValue(true)
            }
            Toast.makeText(activity, "Submitted attendance record", Toast.LENGTH_SHORT).show()
        }
    }

    private fun populateStudentList() {
        // Get list of student ids from class
        val idList: MutableList<String> = ArrayList()
        selectedClass.child("students").orderByValue().addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists()) return
                        for (user in snapshot.children)
                            user.key?.let { idList.add(it) }

                        // Get username of students in list
                        getUserNames(idList)
                    }

                    override fun onCancelled(error: DatabaseError) {}
                }
        )
    }

    private fun getUserNames(userIdList: List<String>) {
        // Get username of students in list
        usersReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return
                for (id in userIdList) {
                    val u = snapshot.child(id).getValue(UserHelperClass::class.java) ?: continue
                    userList.add(u)
                }
                // Sort users according to name
                userList.sortWith { u1: UserHelperClass, u2: UserHelperClass -> u1.fname.compareTo(u2.fname) }

                // Inflate the rows of table, adding users
                for (u in userList) {
                    val rowId = binding.table.childCount
                    val row: TemplateAttendanceTeacherRowBinding = DataBindingUtil.inflate(
                            layoutInflater, R.layout.template_attendance_teacher_row, binding.table, true
                    )
                    row.roll.text = rowId.toString()
                    row.name.text = u.getFname()
                    row.name.setOnClickListener {
                        val args = Bundle()
                        args.putString("userId", u.generateKey())
                        args.putString("classId", classId)

                        // Bubble up request to student attendance viewer
                        studentViewer?.createViewer(args, u.getFname())
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    interface StudentViewer {
        fun createViewer(studentArgs: Bundle?, studentName: String?)
    }

    companion object {
        fun newInstance(args: Bundle?): AttendanceTeacher {
            val fragment = AttendanceTeacher()
            fragment.arguments = args
            return fragment
        }
    }
}