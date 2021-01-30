package com.imperium.academio.ui.adapters.helper

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.imperium.academio.R

class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    @JvmField
    val icon: ImageView = itemView.findViewById(R.id.material_item_icon)

    @JvmField
    val title: TextView = itemView.findViewById(R.id.material_item_title)

    @JvmField
    val text: TextView = itemView.findViewById(R.id.material_item_text)

    @JvmField
    val constraintLayout: ConstraintLayout = itemView.findViewById(R.id.material_item_rvl)

}