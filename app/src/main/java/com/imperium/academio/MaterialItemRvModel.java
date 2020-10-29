package com.imperium.academio;

public class MaterialItemRvModel {
    private int icon;
    private String title;
    private String subtitle;

    public int getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public MaterialItemRvModel(int icon, String title, String subtitle) {
        this.icon = icon;
        this.title = title;
        this.subtitle = subtitle;
    }
}
