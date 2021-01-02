package com.imperium.academio.ui.model;

import androidx.annotation.Nullable;

public class ClassRegisterRvModel {
    private final String name;
    private final String key;

    public ClassRegisterRvModel(String key, String name) {
        this.key = key;
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof ClassRegisterRvModel)) {
            return false;
        }
        return ((ClassRegisterRvModel) obj).key.equals(key) && ((ClassRegisterRvModel) obj).name.equals(name);
    }
}
