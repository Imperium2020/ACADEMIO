package com.imperium.academio.ui.model;

public class MaterialItemRvModel {
    private final int icon;
    private final String title;
    private final String subtitle;

    public MaterialItemRvModel(int icon, String title, String subtitle) {
        this.icon = icon;
        this.title = title;
        this.subtitle = subtitle;
    }

    public int getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }
}
