package com.imperium.academio.ui.fragment;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.imperium.academio.CustomUtil;
import com.imperium.academio.R;
import com.imperium.academio.databinding.TemplateMaterialDialogBinding;
import com.imperium.academio.ui.model.MaterialTopicRvModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MaterialDialogFragment extends DialogFragment {
    Context context;
    TemplateMaterialDialogBinding binding;
    String selectedType;
    List<String> types;
    List<MaterialTopicRvModel> topics;
    SubmitListener listener;
    ActivityResultLauncher<String> mGetContent;
    Uri mFileURI;

    public MaterialDialogFragment() {
        // Empty constructor required for DialogFragment
    }

    public MaterialDialogFragment(MaterialFragment fragment) {
        try {
            listener = fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    fragment.toString() + " must implement OnPlayerSelectionSetListener");
        }
    }

    public static MaterialDialogFragment newInstance(MaterialFragment materialFragment, List<MaterialTopicRvModel> topicItems) {
        MaterialDialogFragment fragment = new MaterialDialogFragment(materialFragment);
        fragment.topics = topicItems;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, parent, savedInstanceState);
        binding = DataBindingUtil.inflate(inflater, R.layout.template_material_dialog, parent, true);
        return binding.getRoot();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        context = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        Dialog d = getDialog();
        if (d == null) return;
        WindowManager.LayoutParams params = d.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        d.getWindow().setAttributes(params);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        types = Arrays.asList(getResources().getStringArray(R.array.types));

        // Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Autocomplete text
        List<String> topicText = new ArrayList<>(topics.size());
        for (MaterialTopicRvModel topic : topics) {
            topicText.add(topic.getTopic());
        }
        ArrayAdapter<String> textAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, topicText);
        binding.inpMaterialTopicText.setAdapter(textAdapter);


        Spinner spinner = binding.inpMaterialTypeSpinner;
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                setTypeSelected(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedType = null;
            }
        });


        // file chooser
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    // Handle the returned Uri
                    if (uri == null) return;
                    mFileURI = uri;
                    binding.inpMaterialNoteTxt.setText(uri.getPath());
                    ImageView img = new ImageView(context);
                    img.setImageURI(uri);
                    binding.materialAddTableLayout.addView(img, 6);
                }
        );

        // attaching chooser to button
        binding.inpMaterialNote.setOnClickListener(view1 -> {
            if (selectedType == null) return;
            mGetContent.launch("image/*");
        });

        // on submit button
        binding.materialDialogSubmit.setOnClickListener(v -> {
            if (listener == null) {
                Toast.makeText(context, "Listener Not Attached!", Toast.LENGTH_SHORT).show();
                return;
            }

            String title = CustomUtil.validateField(binding.inpMaterialTitle, "text");
            String topic = CustomUtil.validateField(binding.inpMaterialTopic, "text");
            String text = CustomUtil.validateField(binding.inpMaterialText, "text");
            String link = CustomUtil.validateField(binding.inpMaterialLink, "link");
            if (title == null || topic == null)
                return;
            if (selectedType == null) {
                Toast.makeText(context, "Please Select a type!", Toast.LENGTH_SHORT).show();
            } else if (selectedType.equals("Notes") || selectedType.equals("Videos")) {
                if (mFileURI == null) {
                    Toast.makeText(context, "Select a file! (use link for urls)", Toast.LENGTH_SHORT).show();
                    return;
                }
                else {
                    link = mFileURI.getPath();
                }
            } else if (selectedType.equals("Links")) {
                if (text == null || link == null)
                    return;
            } else if (selectedType.equals("Alerts")) {
                if (text == null)
                    return;
            }
            // callback to ClassRegister
            listener.onSubmit(selectedType, title, topic, text, link);
            if (getDialog() != null && getShowsDialog()) {
                dismiss();
            }
        });

        // cancel button dismiss the layout
        binding.materialDialogCancel.setOnClickListener(v -> {
            if (getDialog() != null && getShowsDialog()) {
                dismiss();
            }
        });

        // Show soft keyboard automatically and request focus to field
        binding.inpMaterialTitle.requestFocus();
        Dialog d = getDialog();
        if (d != null) {
            d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

    private void setTypeSelected(int pos) {
        binding.inpMaterialLinkRow.setVisibility(View.GONE);
        binding.inpMaterialNoteRow.setVisibility(View.GONE);
        selectedType = types.get(pos);
        switch (pos) {
            case 0: //  Notes
            case 1: //  Videos
                binding.inpMaterialNoteRow.setVisibility(View.VISIBLE);
                break;
            case 2: //  Links
                binding.inpMaterialLinkRow.setVisibility(View.VISIBLE);
                break;
            case 3: //  Alerts
                break;
            default:
                Toast.makeText(context, "Unknown Item Selected", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public interface SubmitListener {
        void onSubmit(String type, String title, String topic, String text, String link);
    }
}

/*
"@+id/inp_material_title" (submit)
"@+id/inp_material_type_spinner" (choose fields and submit)
"@+id/inp_material_topic" (submit)
"@+id/inp_material_topic_text" (auto fill)

"@+id/inp_material_link" (if type link, show and submit)
"@+id/inp_material_note_txt" (if type note or video, show and submit)
"@+id/inp_material_note" (if note/video, button to get material)

"@+id/inp_material_text" ( text )
"@+id/material_dialog_cancel"
"@+id/material_dialog_submit"
*/