package com.imperium.academio.ui.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.imperium.academio.CustomUtil;
import com.imperium.academio.R;
import com.imperium.academio.databinding.TemplateClassDialogBinding;

public class ClassDialogFragment extends DialogFragment {
    public TemplateClassDialogBinding binding;

    public ClassDialogFragment() {
        // Empty constructor is required for DialogFragment
    }

    public static ClassDialogFragment newInstance(boolean type) {
        // Create new instance of Dialog (create or join)
        ClassDialogFragment fragment = new ClassDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean("isJoin", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.template_class_dialog, parent, false);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        Dialog d = getDialog();
        if (d == null) return;

        // Resize dialog according to space available
        LayoutParams params = d.getWindow().getAttributes();
        params.width = LayoutParams.MATCH_PARENT;
        params.height = LayoutParams.WRAP_CONTENT;
        d.getWindow().setAttributes(params);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Fetch arguments from bundle
        Bundle args = getArguments();
        if (args == null) return;


        boolean isJoin = args.getBoolean("isJoin", false);
        // Set the dialog data
        binding.txtClassDialogTitle.setText(isJoin ? getString(R.string.join_class) : getString(R.string.create_class));
        binding.inpJoinTeacherName.setVisibility(isJoin ? View.VISIBLE : View.GONE);

        // Create and Attach submit button
        binding.classDialogSubmit.setOnClickListener(v -> {
            String className = CustomUtil.validateField(binding.inpJoinClassName, "text");
            String teacherName = CustomUtil.validateField(binding.inpJoinTeacherName, "username");
            SubmitListener listener = (SubmitListener) getActivity();

            if (className == null || (isJoin && (teacherName == null)) || listener == null) {
                // If any of classname, teacher, listener is not implemented return
                return;
            }

            // Callback to ClassRegister
            listener.onSubmit(isJoin, className, teacherName);
            if (getShowsDialog()) dismiss();
        });

        // Create and Attach cancel button
        binding.classDialogCancel.setOnClickListener(v -> {
            if (getDialog() != null && getShowsDialog()) {
                dismiss();
            }
        });

        // Show soft keyboard and request focus to field
        binding.inpJoinClassName.requestFocus();
        Dialog d = getDialog();
        if (d != null)
            d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public interface SubmitListener {
        // Interface for bubbling up control to super class
        void onSubmit(boolean isJoin, String classname, String teacherName);
    }
}
