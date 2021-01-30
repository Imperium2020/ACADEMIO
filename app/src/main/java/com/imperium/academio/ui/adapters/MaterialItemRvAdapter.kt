package com.imperium.academio.ui.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.imperium.academio.R
import com.imperium.academio.ui.adapters.helper.ItemViewHolder
import com.imperium.academio.ui.model.MaterialItemRvModel

class MaterialItemRvAdapter(val activity: Activity, private val items: List<MaterialItemRvModel>) : RecyclerView.Adapter<ItemViewHolder>() {
    private var listener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(LayoutInflater.from(activity)
                .inflate(R.layout.template_material_item, parent, false))
    }

    override fun onBindViewHolder(viewHolder: ItemViewHolder, position: Int) {
        val item = items[position]
        viewHolder.icon.setImageResource(item.icon!!)
        viewHolder.title.text = item.title
        val sub = item.subtitle
        if (sub != null && sub.isNotEmpty()) viewHolder.text.text = if (sub.length < 25) sub else sub.substring(0, 20) + "..." else viewHolder.text.text = ""
        viewHolder.constraintLayout.setOnClickListener { v: View? -> listener!!.onItemClick(v, position) }
        viewHolder.constraintLayout.setOnLongClickListener { v: View? ->
            listener!!.onItemLongPressed(v, position)
            notifyDataSetChanged()
            true
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    interface OnItemClickListener {
        fun onItemClick(itemView: View?, position: Int)
        fun onItemLongPressed(itemView: View?, position: Int)
    }
}