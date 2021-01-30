package com.imperium.academio.ui.model

class MaterialItemRvModel(val icon: Int?, val title: String?, val subtitle: String?, private val key: String?) {
    override fun equals(other: Any?): Boolean {
        if (other !is MaterialItemRvModel) return false
        return other.key == key && other.title == title
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + key.hashCode()
        return result
    }
}