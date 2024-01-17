package com.example.urbancycle.Community;

import com.example.urbancycle.R;

import java.util.ArrayList;
import java.util.List;

public class DailyTip {
    private String tipText;
    private int imageResources;

    public DailyTip(String tipText, int imageResources) {
        this.tipText = tipText;
        this.imageResources = imageResources;
    }

    public String getTipText() {
        return tipText;
    }

    public int getImageResources() {
        return imageResources;
    }

    public void setTipText(String tipText) {
        this.tipText = tipText;
    }

    public void setImageResources(int imageResources) {
        this.imageResources = imageResources;
    }
}
