package com.imperium.academio;

public class MaterialTypeRvModel {
    private int image;
    private String text;

    public int getImage() {
        return image;
    }

    public String getText() {
        return text;
    }

    public MaterialTypeRvModel(int image, String text) {
        this.image = image;
        this.text = text;
    }
}
