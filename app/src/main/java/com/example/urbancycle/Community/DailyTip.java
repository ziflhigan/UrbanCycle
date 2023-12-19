package com.example.urbancycle.Community;

import com.example.urbancycle.R;

import java.util.ArrayList;
import java.util.List;

public class DailyTip {
    private static String tipText;
    private static int imageResources;


    public DailyTip(String tipText,int imageResources) {
        this.tipText = tipText;
        this.imageResources=imageResources;
    }

    public void setTipText(String tipText) {
        this.tipText = tipText;
    }
    public static String getTipText() {
        return tipText;
    }

    public static int getImageResources() {
        return imageResources;
    }

    public void setImageResources(int imageResources) {
        this.imageResources = imageResources;
    }

}


