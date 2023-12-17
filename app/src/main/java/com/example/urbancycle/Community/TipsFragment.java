package com.example.urbancycle.Community;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.urbancycle.R;

import java.util.ArrayList;
import java.util.List;

public class TipsFragment extends Fragment {
    List<DailyTip> dailyTipsList=new ArrayList<>();

    public TipsFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tips, container, false);
    }

    //use image loading library to retrieve imagepath stored from db (picasso/glide)
}