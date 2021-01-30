package com.imperium.academio.ui.model

class ClassRegisterRvModel(val key: String?, val name: String?) {
    override fun equals(other: Any?): Boolean {
        return if (other !is ClassRegisterRvModel) {
            false
        } else other.key == key && other.name == name
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}