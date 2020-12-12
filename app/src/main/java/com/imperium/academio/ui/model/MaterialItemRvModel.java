package com.imperium.academio.ui.model;

import androidx.annotation.Nullable;

public class MaterialItemRvModel {
    private final int icon;
    private final String title;
    private final String subtitle;
    private final String key;

    public MaterialItemRvModel(int icon, String title, String subtitle, String key) {
        this.icon = icon;
        this.title = title;
        this.subtitle = subtitle;
        this.key = key;
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

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof MaterialItemRvModel))
            return false;
        MaterialItemRvModel model = (MaterialItemRvModel) obj;
        return model.key.equals(key) && model.title.equals(title);
    }
}
