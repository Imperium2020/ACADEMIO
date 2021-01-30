package com.imperium.academio.ui.model

class MaterialTopicRvModel(val topic: String?) {
    override fun equals(other: Any?): Boolean {
        return if (other !is MaterialTopicRvModel) false else other.topic == topic
    }

    override fun hashCode(): Int {
        return topic.hashCode()
    }
}