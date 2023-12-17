package com.example.urbancycle.Community;

import com.example.urbancycle.R;

import java.util.ArrayList;
import java.util.List;

public class DailyTip {
    private String tipText;
    private String imageResources;


    public DailyTip(String tipText,String imageResources) {
        this.tipText = tipText;
        this.imageResources=imageResources;
    }

    public void setTipText(String tipText) {
        this.tipText = tipText;
    }
    public String getTipText() {
        return tipText;
    }

    public String getImageResources() {
        return imageResources;
    }

    public void setImageResources(String imageResources) {
        this.imageResources = imageResources;
    }

}


