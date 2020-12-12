package com.imperium.academio.fireclass;

import com.imperium.academio.CustomUtil;

public class MaterialHelperClass {
    public String classId;
    public String link;
    public String text;
    public String title;
    public String topic;
    public String type;

    public MaterialHelperClass() {
    }

    public MaterialHelperClass(String title) {
        this.title = title;
    }

    public MaterialHelperClass(String classId, String link, String text, String title, String topic, String type) {
        this.classId = classId;
        this.link = link;
        this.text = text;
        this.title = title;
        this.topic = topic;
        this.type = type;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String generateKey() {
        return CustomUtil.SHA1(title);
    }
}
