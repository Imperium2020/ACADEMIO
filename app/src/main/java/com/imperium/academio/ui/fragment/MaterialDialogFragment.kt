package com.imperium.academio.ui.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.fragment.app.DialogFragment
import com.imperium.academio.CustomUtil.validateField
import com.imperium.academio.R
import com.imperium.academio.databinding.TemplateMaterialDialogBinding
import com.imperium.academio.ui.model.MaterialTopicRvModel
import java.util.*
import kotlin.collections.ArrayList

class MaterialDialogFragment : DialogFragment {
    lateinit var binding: TemplateMaterialDialogBinding

    private var selectedType: String? = null
    private var types: List<String> = ArrayList()

    var topics: List<MaterialTopicRvModel> = ArrayList()
    var listener: SubmitListener? = null

    private var mGetContent: ActivityResultLauncher<String>? = null
    private var mFileURI: Uri? = null

    constructor() {
        // Empty constructor required for DialogFragment
    }

    constructor(fragment: MaterialFragment) {
        listener = try {
            fragment
        } catch (e: ClassCastException) {
            throw ClassCastException("$fragment must implement OnPlayerSelectionSetListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, parent, savedInstanceState)
        binding = TemplateMaterialDialogBinding.inflate(inflater, parent, true)
        return binding.root
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onResume() {
        super.onResume()
        val d = dialog ?: return
        val params = d.window!!.attributes
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        d.window!!.attributes = params
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        types = listOf(*resources.getStringArray(R.array.types))

        // Spinner
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Autocomplete text
        val topicText: MutableList<String?> = ArrayList(topics.size)
        for (topic in topics) {
            topicText.add(topic.topic)
        }
        val textAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, topicText)
        binding.inpMaterialTopicText.setAdapter(textAdapter)
        val spinner = binding.inpMaterialTypeSpinner
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, pos: Int, id: Long) {
                setTypeSelected(pos)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                selectedType = null
            }
        }


        // file chooser
        mGetContent = registerForActivityResult(GetContent()
        ) { uri: Uri? ->
            // Handle the returned Uri
            if (uri == null || selectedType == null) return@registerForActivityResult
            mFileURI = uri
            binding.inpMaterialNoteTxt.text = uri.path
            if (selectedType == "Videos") attachVideoView(uri)
        }

        // attaching chooser to button
        binding.inpMaterialNote.setOnClickListener {
            if (selectedType != null && selectedType == "Videos") {
                mGetContent!!.launch("video/*")
            } else mGetContent!!.launch("*/*")
        }

        // on submit button
        binding.materialDialogSubmit.setOnClickListener {
            if (listener == null) {
                requireContext().toast("Listener Not Attached!")
                return@setOnClickListener
            }
            val title = validateField(binding.inpMaterialTitle, "text")
            val topic = validateField(binding.inpMaterialTopic, "text")
            val text = validateField(binding.inpMaterialText, "text")
            val link = validateField(binding.inpMaterialLink, "link")
            if (title == null || topic == null) return@setOnClickListener
            if (selectedType == null) {
                requireContext().toast("Please Select a type!")
            } else if (selectedType == "Links") {
                if (text == null || link == null) {
                    return@setOnClickListener
                }
            } else if (selectedType == "Alerts") {
                if (text == null) {
                    return@setOnClickListener
                }
            }
            // callback to ClassRegister
            if (selectedType == "Notes" || selectedType == "Videos") {
                if (mFileURI == null) {
                    requireContext().toast("Select a file! (use link for urls)")
                    return@setOnClickListener
                }
                listener!!.onSubmit(selectedType, title, topic, text, mFileURI)
            } else {
                listener!!.onSubmit(selectedType, title, topic, text, link)
            }
            if (dialog != null && showsDialog) {
                dismiss()
            }
        }

        // cancel button dismiss the layout
        binding.materialDialogCancel.setOnClickListener {
            if (dialog != null && showsDialog) {
                dismiss()
            }
        }

        // Show soft keyboard automatically and request focus to field
        binding.inpMaterialTitle.requestFocus()
        val d = dialog
        if (d != null) {
            d.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
    }

    private fun setTypeSelected(pos: Int) {
        selectedType = types[pos]

        binding.run {
            inpMaterialLinkRow.visibility = View.GONE
            inpMaterialNoteRow.visibility = View.GONE
            when (pos) {
                // Notes, Videos
                0, 1 -> inpMaterialNoteRow.visibility = View.VISIBLE
                // Links
                2 -> inpMaterialLinkRow.visibility = View.VISIBLE
                else -> requireContext().toast("Unknown Item Selected")
            }
        }
    }

    interface SubmitListener {
        fun onSubmit(type: String?, title: String?, topic: String?, text: String?, link: String?)
        fun onSubmit(type: String?, title: String?, topic: String?, text: String?, link: Uri?)
    }

    private fun attachVideoView(uri: Uri) {
        binding.materialDisplay.visibility = View.VISIBLE
        binding.materialDisplay.run {
            setVideoURI(uri)
            setMediaController(MediaController(activity).apply { show() })
            start()

            // Stop video preview after 10 seconds
            postDelayed({ if (isPlaying) stopPlayback() }, 10000)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(materialFragment: MaterialFragment, topicItems: List<MaterialTopicRvModel>): MaterialDialogFragment {
            val fragment = MaterialDialogFragment(materialFragment)
            fragment.topics = topicItems
            return fragment
        }
    }

    private fun Context.toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}