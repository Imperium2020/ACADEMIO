package com.imperium.academio.ui.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.imperium.academio.R
import com.imperium.academio.ui.adapters.helper.TopicRvViewHolder
import com.imperium.academio.ui.model.MaterialTopicRvModel

class MaterialTopicRvAdapter(val activity: Activity, private val items: List<MaterialTopicRvModel>) : RecyclerView.Adapter<TopicRvViewHolder>() {
    private var listener: OnTopicItemClickListener? = null
    private var selectedTopicIndex = -1
    fun setOnTopicItemClickListener(listener: OnTopicItemClickListener?) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicRvViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.template_material_topic, parent, false)
        return TopicRvViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: TopicRvViewHolder, position: Int) {
        val item = items[position]
        viewHolder.topic.text = item.topic
        viewHolder.linearLayout.setOnClickListener {
            selectedTopicIndex = if (selectedTopicIndex == position) -1 else position
            listener!!.onTopicItemClick(viewHolder.itemView, position)
        }
        viewHolder.linearLayout.setBackgroundResource(
                if (selectedTopicIndex == position) R.drawable.material_rvi_selected_bg else R.drawable.material_rvi_bg
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    interface OnTopicItemClickListener {
        fun onTopicItemClick(itemView: View?, position: Int)
    }
}