package com.imperium.academio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TemplateFragment extends Fragment {

    private static final String MSG = "param1";
    private String msg;

    public TemplateFragment() {
        // Required empty public constructor
    }
    public static TemplateFragment newInstance() {
        return new TemplateFragment();
    }

    public static TemplateFragment newInstance(String m) {
        TemplateFragment fragment = new TemplateFragment();
        Bundle args = new Bundle();
        args.putString(MSG, m);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            msg = getArguments().getString(MSG);
        }
        else {
            msg = "Hello World";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.template_fragment, container, false);
        TextView t = v.findViewById(R.id.tv_counter);
        t.setText(msg);
        return  v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}