package com.imperium.academio.ui.adapters.helper

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.imperium.academio.R

class ClassViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    @JvmField
    val name: TextView = itemView.findViewById(R.id.class_item_name)

    @JvmField
    val constraintLayout: ConstraintLayout = itemView.findViewById(R.id.class_register_rvl)

}