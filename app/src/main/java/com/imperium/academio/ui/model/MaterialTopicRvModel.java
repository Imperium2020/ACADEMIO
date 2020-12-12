package com.imperium.academio.ui.model;

import androidx.annotation.Nullable;

public class MaterialTopicRvModel {
    private final String topic;

    public MaterialTopicRvModel(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof MaterialTopicRvModel))
            return false;
        return ((MaterialTopicRvModel) obj).topic.equals(topic);
    }
}
