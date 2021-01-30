package com.imperium.academio.ui.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import com.imperium.academio.CustomUtil.SHA1
import com.imperium.academio.CustomUtil.validateField
import com.imperium.academio.R
import com.imperium.academio.databinding.FragmentAttendanceStudentBinding
import com.imperium.academio.databinding.TemplateAttendanceDateBinding
import com.imperium.academio.fireclass.ClassHelperClass
import java.text.DateFormatSymbols
import java.util.*
import kotlin.collections.ArrayList

class AttendanceStudent : Fragment() {
    private lateinit var binding: FragmentAttendanceStudentBinding
    private lateinit var selectedClass: DatabaseReference
    private var classId: String? = null
    var userId: String? = null
    var absentList: MutableList<Long> = ArrayList()
    var sessionList: MutableList<Long> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        if (bundle != null) {
            classId = bundle.getString("classId")
            userId = bundle.getString("userId")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentAttendanceStudentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity: Activity? = activity
        if (activity == null || classId == null || userId == null) return

        selectedClass = FirebaseDatabase.getInstance().getReference("class/$classId")
        fetchAttendanceSheet()

        // Temporarily fill progress bar
        setProgress(0, 0)

        // Table creation
        val table = binding.attendanceStudentTable
        val shortWeekDays = DateFormatSymbols.getInstance().shortWeekdays

        for (i in 0..6) {
            // For row
            val tr = TableRow(activity)
            for (j in 0..6) {
                // For Element
                val card = TemplateAttendanceDateBinding.inflate(layoutInflater)
                val cardView = card.root as CardView

                // setting table heading
                if (i == 0) {
                    cardView.cardElevation = 10f
                    cardView.setCardBackgroundColor(getColor(R.color.crystal_blue))
                    card.attendanceDateText.text = shortWeekDays[j + 1]
                }
                tr.addView(cardView)
            }
            table.addView(tr)
        }

        table.isStretchAllColumns = true
        table.bringToFront()

        // Default year as current year
        val cal = Calendar.getInstance()
        val months = listOf(*resources.getStringArray(R.array.months))

        binding.run {
            inpAttendanceYear.setText(cal[Calendar.YEAR].toString())

            // Autocomplete text
            inpAttendanceMonth.setAdapter(ArrayAdapter(
                    activity, android.R.layout.simple_list_item_1, months))
            inpAttendanceMonth.setText(months[cal[Calendar.MONTH]])
            btnAttendanceGet.setOnClickListener {
                val y = validateField(binding.attendanceYear, "year")
                val m = validateField(binding.attendanceMonth, "text")
                val mArray = listOf(*resources.getStringArray(R.array.months))
                if (y == null || m == null || !mArray.contains(m)) return@setOnClickListener
                val year = y.toInt()
                val month = mArray.indexOf(m)

                // call function to draw table
                setTable(table, month, year)
            }
        }
    }

    // fill table with data
    private fun setTable(table: TableLayout, month: Int, year: Int) {
        // get Set of absent dates, and sessions from database for month
        val absentDateSet: Set<Int> = HashSet(getDatesInRange(absentList, year, month))
        val sessionDateSet: Set<Int> = HashSet(getDatesInRange(sessionList, year, month))
        val calInstance = Calendar.getInstance()
        val currYear = calInstance[Calendar.YEAR]
        val currMonth = calInstance[Calendar.MONTH]

        // Check today is present in the selected view
        val today = if (year == currYear && month == currMonth) calInstance[Calendar.DATE] else -1

        // making custom Instance
        val customInstance = Calendar.getInstance()
        customInstance[calInstance[Calendar.YEAR], if (month < 0) currMonth else month, 1, 0, 0] = 0
        customInstance[Calendar.MILLISECOND] = 0
        val maxDate = customInstance.getActualMaximum(Calendar.DATE)
        val startSpace = customInstance[Calendar.DAY_OF_WEEK] - 1
        var dateCounter = 1


        // Colors
        val black = getColor(R.color.black)
        val sessionColor = getColor(R.color.hunter_green)
        val absentColor = getColor(R.color.candy_red)
        val todayColor = getColor(R.color.moonstone)
        val white = getColor(R.color.white)

        // Looping through the table
        for (i in 1 until table.childCount) {
            val row = table.getChildAt(i) as TableRow
            row.visibility = View.GONE
            for (j in 0 until row.childCount) {
                var date: String

                // getting textView
                val card = row.getChildAt(j) as CardView
                val text = card.getChildAt(0) as TextView

                // per date logic
                card.setCardBackgroundColor(white)
                if (i == 1 && j < startSpace || dateCounter > maxDate) {
                    // no date in element
                    date = " "
                } else {
                    // there is a date in the element

                    // if first element in row, set row as visible
                    if (j == 0 || j == startSpace)
                        row.visibility = View.VISIBLE

                    text.setTextColor(black)
                    when {
                        // set color if the student is absent on date
                        absentDateSet.contains(dateCounter) -> {
                            card.setCardBackgroundColor(absentColor)
                        }

                        // set color if session occurred on date
                        sessionDateSet.contains(dateCounter) -> {
                            card.setCardBackgroundColor(sessionColor)
                            text.setTextColor(white)
                        }

                        // set color if the date is today
                        dateCounter == today -> {
                            card.setCardBackgroundColor(todayColor)
                            text.setTextColor(white)
                        }
                    }

                    date = dateCounter++.toString()
                }
                text.text = date
            }
        }
    }

    private fun fetchAttendanceSheet() {
        selectedClass.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return
                val currentClass = snapshot.getValue(ClassHelperClass::class.java) ?: return
                if (absentList.isNotEmpty()) absentList = ArrayList()
                if (sessionList.isNotEmpty()) sessionList = ArrayList()
                for ((date) in currentClass.sessions) {
                    sessionList.add(date.toLong())
                    val attendanceKey = SHA1(userId + date)
                    val record = currentClass.attendance[attendanceKey]
                    if (record != null) {
                        absentList.add(record.absent_date)
                    }
                }

                // Set progress bar
                val sessionCount = sessionList.size
                val percent = sessionCount - absentList.size
                setProgress(percent, sessionCount)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getDatesInRange(dateList: List<Long>?, year: Int, month: Int): List<Int> {
        if (dateList!!.isEmpty()) {
            fetchAttendanceSheet()
            return emptyList()
        }

        // Set an instance to given year and month
        val cal = Calendar.getInstance()
        cal[year, month, 1, 0, 0] = 0
        cal[Calendar.MILLISECOND] = 0

        // Get both bounds for filtering the absent list
        val minTime = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val maxTime = cal.timeInMillis

        // Filter absentList to get dates of selected range
        val datesInRange: MutableList<Int> = ArrayList()
        for (date in dateList) {
            if (date in minTime until maxTime) {
                cal.timeInMillis = date
                datesInRange.add(cal[Calendar.DATE])
            }
        }
        return datesInRange
    }

    private fun setProgress(present: Int, sessionCount: Int) {
        val progress: Int
        val detailed = binding.attendancePercentDetailed
        if (sessionCount == 0 || present < 0) {
            progress = 100
        } else {
            progress = 100 * present / sessionCount
            val detail = """
                Session count: $sessionCount
                Present count: $present
                Absent  count: ${sessionCount - present}
                """.trimIndent()
            detailed.text = detail
            binding.attendancePercentText.setOnClickListener {
                if (detailed.visibility == View.VISIBLE) {
                    detailed.visibility = View.GONE
                } else {
                    detailed.visibility = View.VISIBLE
                }
            }
        }
        binding.apply {
            attendanceProgressBar.progress = progress
            attendancePercentText.text = String.format("%s%%", progress)
        }
    }

    private fun getColor(resColor: Int): Int {
        return ResourcesCompat.getColor(resources, resColor, null)
    }

    companion object {
        fun newInstance(args: Bundle?): AttendanceStudent {
            return AttendanceStudent().apply {
                arguments = args
            }
        }
    }
}