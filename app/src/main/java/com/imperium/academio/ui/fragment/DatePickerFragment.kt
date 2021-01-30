package com.imperium.academio.ui.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.util.*

class DatePickerFragment : DialogFragment() {
    var datePicker: DatePickerDialog? = null
    var listener: DatePickerDialog.OnDateSetListener? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val year = c[Calendar.YEAR]
        val month = c[Calendar.MONTH]
        val day = c[Calendar.DAY_OF_MONTH]
        datePicker = DatePickerDialog(requireActivity(), listener, year, month, day)
        return datePicker!!
    }
}