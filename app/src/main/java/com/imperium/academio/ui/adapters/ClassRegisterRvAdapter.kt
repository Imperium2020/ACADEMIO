package com.imperium.academio.ui.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.imperium.academio.R
import com.imperium.academio.ui.adapters.helper.ClassViewHolder
import com.imperium.academio.ui.model.ClassRegisterRvModel

class ClassRegisterRvAdapter(val activity: Activity, private val classes: List<ClassRegisterRvModel>) : RecyclerView.Adapter<ClassViewHolder>() {
    private var listener: OnItemClickListener? = null

    // Function to attach OnClick Listener
    fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.template_class_register_item, parent, false)
        return ClassViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ClassViewHolder, position: Int) {
        // Create class item from data
        val item = classes[position]
        viewHolder.name.text = item.name

        // Attach click listener to items
        viewHolder.constraintLayout.setOnClickListener { v: View? -> listener!!.onItemClick(v, position) }
    }

    override fun getItemCount(): Int {
        return classes.size
    }

    // Interface to bubble up clicks
    interface OnItemClickListener {
        fun onItemClick(itemView: View?, position: Int)
    }
}