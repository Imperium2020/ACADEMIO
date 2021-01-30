package com.imperium.academio.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.imperium.academio.CustomUtil.validateField
import com.imperium.academio.R
import com.imperium.academio.databinding.TemplateClassDialogBinding

class ClassDialogFragment : DialogFragment() {
    private lateinit var binding: TemplateClassDialogBinding

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = TemplateClassDialogBinding.inflate(inflater, parent, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val window = dialog?.window ?: return

        // Resize dialog according to space available
        window.attributes.apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch arguments from bundle
        val args = arguments ?: return
        val isJoin = args.getBoolean("isJoin", false)
        // Set the dialog data
        binding.txtClassDialogTitle.text = if (isJoin) getString(R.string.join_class) else getString(R.string.create_class)
        binding.inpJoinTeacherName.visibility = if (isJoin) View.VISIBLE else View.GONE

        // Create and Attach submit button
        binding.classDialogSubmit.setOnClickListener {
            val className = validateField(binding.inpJoinClassName, "text")
            val teacherName = validateField(binding.inpJoinTeacherName, "username")
            val listener = activity as ClassSubmitListener
            if (className == null || isJoin && teacherName == null) {
                // If any of classname, teacher, listener is not implemented return
                return@setOnClickListener
            }

            // Callback to ClassRegister
            listener.onSubmit(isJoin, className.toString(), teacherName!!.toString())
            if (showsDialog) dismiss()
        }

        // Create and Attach cancel button
        binding.classDialogCancel.setOnClickListener {
            if (dialog != null && showsDialog) {
                dismiss()
            }
        }

        // Show soft keyboard and request focus to field
        binding.inpJoinClassName.requestFocus()
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }

    companion object {
        // Create new instance of Dialog (create or join)
        fun newInstance(type: Boolean): ClassDialogFragment = ClassDialogFragment()
                .apply { arguments = Bundle().apply { putBoolean("isJoin", type) } }
    }

    interface ClassSubmitListener {
        // Interface for bubbling up control to super class
        fun onSubmit(buttonType: Boolean, classname: String, teacherName: String)
    }
}