package com.imperium.academio.ui.adapters.helper

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.imperium.academio.R

class TopicRvViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    @JvmField
    val topic: TextView = itemView.findViewById(R.id.material_topic_text)

    @JvmField
    val linearLayout: LinearLayout = itemView.findViewById(R.id.material_topic_rvl)

}