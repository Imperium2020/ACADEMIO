package com.imperium.academio.ui.fragment

import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.imperium.academio.CustomUtil.SHA1
import com.imperium.academio.Login
import com.imperium.academio.MaterialVideoView
import com.imperium.academio.R
import com.imperium.academio.databinding.FragmentMaterialBinding
import com.imperium.academio.fireclass.MaterialHelperClass
import com.imperium.academio.ui.adapters.MaterialItemRvAdapter
import com.imperium.academio.ui.adapters.MaterialTopicRvAdapter
import com.imperium.academio.ui.adapters.MaterialTopicRvAdapter.OnTopicItemClickListener
import com.imperium.academio.ui.fragment.MaterialDialogFragment.Companion.newInstance
import com.imperium.academio.ui.model.MaterialItemRvModel
import com.imperium.academio.ui.model.MaterialTopicRvModel
import java.io.FileNotFoundException
import java.util.*
import kotlin.collections.ArrayList


class MaterialFragment : Fragment(), MaterialDialogFragment.SubmitListener {
    private lateinit var binding: FragmentMaterialBinding
    private lateinit var topicRvAdapter: MaterialTopicRvAdapter
    private lateinit var itemRvAdapter: MaterialItemRvAdapter
    private lateinit var databaseListener: ValueEventListener
    private lateinit var classStorage: StorageReference
    private lateinit var materials: DatabaseReference

    private var selectedType: String? = null
    private var classId: String? = null
    var selectedTopic: String? = null
    var teacherId: String? = null

    var userId: String? = null
    var dbItems = ArrayList<MaterialHelperClass>()

    var topicItems = ArrayList<MaterialTopicRvModel>()
    private var items = ArrayList<MaterialItemRvModel>()
    private var materialTypes: List<String> = ArrayList()
    private var materialTypeIcons: List<Int> = ArrayList()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Get data from intent
        arguments?.let { bundle ->
            classId = bundle.getString("classId")
            userId = bundle.getString("userId")
            teacherId = bundle.getString("teacherId")
        }
        binding = FragmentMaterialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        classStorage = FirebaseStorage.getInstance().getReference(classId!!)
        materials = FirebaseDatabase.getInstance().getReference("materials")

        // Redirect to login if user or class is not set from intent
        if (userId == null || classId == null || teacherId == null) {
            requireActivity().run {
                startActivity(Intent(requireActivity(), Login::class.java))
                finish()
            }
            return
        }

        // Allow teacher to add material
        if (userId == teacherId) {
            binding.materialAddItem.visibility = View.VISIBLE
        }

        // Data Arrays
        dbItems = ArrayList()
        topicItems = ArrayList()
        items = ArrayList()

        // Attach listener to class
        val query = materials.orderByChild("classId").equalTo(classId)
        databaseListener = getDbListener()
        query.addValueEventListener(databaseListener)


        // Populate lists for type selection
        materialTypes = listOf(*resources.getStringArray(R.array.types))
        materialTypeIcons = listOf(R.drawable.note_icon, R.drawable.video_icon,
                R.drawable.link_icon, R.drawable.message_icon)

        // Check if list sizes match and if yes, assign size to typeCount
        if (materialTypes.size != materialTypeIcons.size) return

        val typeCount: Int = materialTypes.size
        val width = binding.materialType.width
        for (i in 0 until typeCount) {
            // Inflate chip and set values
            val typeChip = layoutInflater.inflate(
                    R.layout.template_choice_chip, binding.materialType, false) as Chip
            typeChip.text = materialTypes[i]
            typeChip.setChipIconResource(materialTypeIcons[i])
            typeChip.width = width / typeCount

            // Add inflated chip to chipGroup
            binding.materialType.addView(typeChip)
        }

        // Create adapters
        topicRvAdapter = MaterialTopicRvAdapter(requireActivity(), topicItems)
        itemRvAdapter = MaterialItemRvAdapter(requireActivity(), items)
        prepareAdapter(binding.materialTopic, topicRvAdapter, LinearLayoutManager.HORIZONTAL)
        prepareAdapter(binding.materialItem, itemRvAdapter, LinearLayoutManager.VERTICAL)

        // Create and attach listener to topic recyclerview
        topicRvAdapter.setOnTopicItemClickListener(object : OnTopicItemClickListener {
            override fun onTopicItemClick(itemView: View?, position: Int) {
                val clickedTopic = topicItems[position].topic
                selectedTopic = if (selectedTopic == null || selectedTopic != clickedTopic) clickedTopic else null
                refresh()
            }
        })

        // Create and attach listener to type chipGroup
        binding.materialType.setOnCheckedChangeListener { group: ChipGroup, chipId: Int ->
            selectedType = when (chipId) {
                View.NO_ID -> null
                else -> {
                    val chip: Chip = group.findViewById(chipId)
                    chip.text.toString()
                }
            }
            refresh()
        }

        // Create and attach listener to material item recyclerview
        itemRvAdapter.setOnItemClickListener(object : MaterialItemRvAdapter.OnItemClickListener {
            override fun onItemClick(itemView: View?, position: Int) {
                val material = getDbItem(position) ?: return
                when (material.getType()) {
                    "Links", "Alerts" -> {
                        val dialog = buildTextDialog(material)
                        dialog.show()
                    }
                    "Notes" -> broadcastIntent(material)
                    "Videos" -> {
                        if (material.link == null) return
                        startActivity(Intent(activity, MaterialVideoView::class.java).apply {
                            putExtra("uriString", material.getLink())
                            putExtra("title", material.title)
                            putExtra("text", material.text ?: "No description given")
                        })
                    }
                    else -> {
                    }
                }
            }

            override fun onItemLongPressed(itemView: View?, position: Int) {
                // If not teacher, do nothing
                if (userId != teacherId) return

                // Show confirm delete dialog
                val material = getDbItem(position) ?: return
                val alertDialog = buildDeleteDialog(material)
                alertDialog.show()
            }
        })

        // Add material button listener
        binding.materialAddItem.setOnClickListener {
            val fragment = newInstance(this, topicItems)
            fragment.show(requireActivity().supportFragmentManager, "addMaterial")
        }
        binding.materialBtnRefresh.setOnClickListener { refresh() }

        // Add listener to swipe gesture on items
        binding.materialItemSwipe.run {
            setOnRefreshListener { refresh() }
            postDelayed({ refresh() }, 600)
        }
    }

    private fun buildDeleteDialog(material: MaterialHelperClass): AlertDialog {
        return AlertDialog.Builder(activity).run {
            setTitle("Delete")
            setMessage("Are you sure you want to delete this material?\nTitle: ${material.getTitle()}")
            setPositiveButton("yes") { _: DialogInterface?, _: Int -> deleteMaterial(material) }
            setNegativeButton("No") { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
            create()
        }
    }

    private fun buildTextDialog(material: MaterialHelperClass): AlertDialog {
        val builder = AlertDialog.Builder(activity)
        val showText = TextView(builder.context)
        if (material.link != null) {
            val message = "${material.text}\n\nLink: ${material.getLink()}"
            showText.text = message
            builder.setPositiveButton("Visit Link") { _: DialogInterface?, _: Int -> broadcastIntent(material) }
        } else {
            showText.text = material.text
        }
        showText.setPadding(getDp(24), getDp(5), getDp(12), getDp(10))
        showText.setTextIsSelectable(true)
        builder.setView(showText)
        builder.setTitle(material.getTitle())
        builder.setCancelable(true)
        return builder.create()
    }

    override fun onSubmit(type: String?, title: String?, topic: String?, text: String?, link: String?) {
        createMaterial(classId, link, text, title, topic, type)
    }

    override fun onSubmit(type: String?, title: String?, topic: String?, text: String?, link: Uri?) {
        binding.progressbar.visibility = View.VISIBLE
        // get reference link and attach link to material object link
        val mStorage = classStorage.child(SHA1(title!!))
        try {
            val fileStream = requireActivity().contentResolver.openInputStream(link!!)
            val uploadTask = mStorage.putStream(fileStream!!)
            uploadTask.continueWithTask { task: Task<UploadTask.TaskSnapshot?> ->
                if (!task.isSuccessful && task.exception != null) {
                    throw Throwable(task.exception)
                }
                getMimeType(link)?.let {
                    mStorage.updateMetadata(StorageMetadata.Builder().setContentType(it).build())
                }
                // Return url
                mStorage.downloadUrl
            }.addOnCompleteListener { task: Task<Uri?> ->
                if (task.isSuccessful && task.result != null) {
                    val dbLink = task.result.toString()
                    createMaterial(classId, dbLink, text, title, topic, type)
                } else {
                    requireActivity().toast("Upload got interrupted.")
                }
            }
        } catch (fnf: FileNotFoundException) {
            requireActivity().toast("File Not Found!")
            Log.d("MaterialFragment", "onSubmit(Uri): File Not Found", fnf)
        }
    }

    private fun prepareAdapter(recyclerView: RecyclerView, holder: RecyclerView.Adapter<*>, orientation: Int) {
        // set recyclerview properties and attach adapter
        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity, orientation, false)
            adapter = holder
        }.run { setHasFixedSize(true) }
    }

    private fun broadcastIntent(material: MaterialHelperClass) {
        if (material.link == null) return
        val viewIntent = Intent(Intent.ACTION_VIEW)
        val chooserTitle = "Select an app for viewing"
        if (material.type == "Link") {
            viewIntent.data = Uri.parse(material.getLink())
            startActivity(Intent.createChooser(viewIntent, chooserTitle))
            return
        }
        val mStorage = classStorage.child(SHA1(material.title))
        mStorage.metadata.addOnSuccessListener { storageMetadata: StorageMetadata ->
            try {
                val uri = Uri.parse(material.getLink())
                if (storageMetadata.contentType != null) {
                    viewIntent.setDataAndType(uri, storageMetadata.contentType)
                } else {
                    viewIntent.data = uri
                }
                val chooser = Intent.createChooser(viewIntent, chooserTitle)
                startActivity(chooser)
            } catch (e: Exception) {
                activity?.toast("Could not find app")
                Log.e("MaterialFragment", "onItemClick: ", e)
            }
        }
    }

    // create material function
    private fun createMaterial(classId: String?, link: String?, text: String?, title: String?, topic: String?, type: String?) {
        val materialObject = MaterialHelperClass(classId, link, text, title, topic, type)
        val key = materialObject.generateKey()
        materials.child(key).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    // add material to list of materials in db
                    materials.child(key).setValue(materialObject)
                    requireActivity().toast("Creating material: $title")
                    binding.progressbar.visibility = View.GONE
                } else {
                    requireActivity().toast("This Material Title already exist! Please use another title")
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun deleteMaterial(material: MaterialHelperClass) {
        val storageType = listOf("Videos", "Notes")
        val key = material.generateKey()
        materials.child(key).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val item = snapshot.getValue(MaterialHelperClass::class.java) ?: return
                    if (storageType.contains(item.getLink())) {
                        classStorage.child(key).delete()
                                .addOnSuccessListener { materials.child(key).removeValue() }
                    } else {
                        materials.child(key).removeValue()
                    }
                    refresh()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Function to perform when refresh is called
    private fun refresh() {
        binding.materialBtnRefresh.visibility = View.GONE
        binding.materialItemSwipe.apply {
            isRefreshing = true

            // remove refreshing animation after 10 seconds
            postDelayed({ isRefreshing = false }, 10000)
        }
        reloadItems()
    }

    private fun reloadItems() {
        binding.materialItemSwipe.post {
            topicItems.clear()
            items.clear()

            // For each item in database list add item according to filters
            for (item in dbItems) {

                // get icon drawable
                var icon = -1
                if (materialTypes.contains(item.getType())) {
                    icon = materialTypeIcons[materialTypes.indexOf(item.type!!)]
                }

                // Create and add type items
                val tItem = MaterialTopicRvModel(item.getTopic())
                if (!topicItems.contains(tItem)) {
                    topicItems.add(tItem)
                }

                // Check if any filter is selected and if yes, item matches the filter
                if (selectedType != null && selectedType != item.type) continue
                if (selectedTopic != null && selectedTopic != item.topic) continue
                // Add item to list
                val iItem = MaterialItemRvModel(icon, item.getTitle(), item.getText(), item.generateKey())
                if (!items.contains(iItem)) items.add(iItem)
            }

            // Remove any null element and notify the changes to adapters
            topicItems.removeAll(setOf<Any?>(null))
            items.removeAll(setOf<Any?>(null))
            topicRvAdapter.notifyDataSetChanged()
            itemRvAdapter.notifyDataSetChanged()

            // Remove swipe refreshing animation
            binding.materialItemSwipe.isRefreshing = false
        }
    }

    // Create ValueEvent listener for material database
    private fun getDbListener(): ValueEventListener {
        return object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Clear out current background list and get from database cache
                    val tempItemList: MutableList<MaterialHelperClass> = ArrayList()
                    for (c in snapshot.children) {
                        c.getValue(MaterialHelperClass::class.java)?.let {
                            if (!tempItemList.contains(it)) {
                                tempItemList.add(it)
                            }
                        }
                    }
                    // Notify user to reload if new item found.
                    if (0 < dbItems.size && dbItems.size != tempItemList.size) {
                        activity?.toast("Stream has been updated.")
                    }
                    dbItems.run {
                        clear()
                        addAll(tempItemList)
                        sortWith { m1: MaterialHelperClass, m2: MaterialHelperClass -> (m2.timestamp - m1.timestamp).toInt() }
                    }
                    binding.materialBtnRefresh.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }
    }

    private fun getDbItem(position: Int): MaterialHelperClass? {
        return dbItems.find { it.title == items[position].title }
    }

    private fun getDp(dp: Int): Int {
        return (dp * resources.displayMetrics.density + 0.5f).toInt()
    }

    private fun getMimeType(uri: Uri): String? {
        val mimeType: String?
        mimeType = if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            val cr = requireActivity().applicationContext.contentResolver
            cr.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase(Locale.getDefault()))
        }
        return mimeType
    }

    companion object {
        fun newInstance(args: Bundle?): MaterialFragment {
            // Create instance of fragment
            val fragment = MaterialFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private fun Context.toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}