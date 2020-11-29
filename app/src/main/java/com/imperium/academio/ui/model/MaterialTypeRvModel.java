package com.imperium.academio.ui.model;

public class MaterialTypeRvModel {
    private final int image;
    private final String text;

    public MaterialTypeRvModel(int image, String text) {
        this.image = image;
        this.text = text;
    }

    public int getImage() {
        return image;
    }

    public String getText() {
        return text;
    }
}
