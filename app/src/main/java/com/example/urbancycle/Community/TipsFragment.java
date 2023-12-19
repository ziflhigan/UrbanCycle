package com.example.urbancycle.Community;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.urbancycle.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TipsFragment extends Fragment {
    List<DailyTip> dailyTipsList=new ArrayList<>();

    public TipsFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tips, container, false);

        // Inflate the layout for this fragment
        dailyTipsList.add(new DailyTip("Use public transportation to reduce individual carbon footprint.\n", R.drawable.publictransportation));
        dailyTipsList.add(new DailyTip("Consider carpooling or ridesharing to share commuting costs and reduce emissions.\n", R.drawable.sustainabletravel));
        dailyTipsList.add(new DailyTip("Opt for walking or cycling for short distances instead of using a vehicle.\n", R.drawable.bike));
        dailyTipsList.add(new DailyTip("Conserve energy by turning off lights and electronics when not in use.\n ", R.drawable.saveenergy));
        dailyTipsList.add(new DailyTip("Invest in an electric vehicle for a sustainable and low-emission commute.\n", R.drawable.greentravel));

        DailyTip currentDayTip = getTipForCurrentDay();

        // Display the tip in the UI
        displayTip(currentDayTip, view);

        return view;
    }

    private DailyTip getTipForCurrentDay() {
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        int tipIndex = (dayOfWeek - 1) % dailyTipsList.size();

        return dailyTipsList.get(tipIndex);
    }

    private void displayTip(DailyTip tip, View view) {
        TextView tipTitleTextView = view.findViewById(R.id.TVTipsDisplay);
        ImageView tipImageView = view.findViewById(R.id.IVTipsDisplay);

        tipTitleTextView.setText(tip.getTipText());
        tipImageView.setImageResource(tip.getImageResources());
    }



    // can use image loading library to retrieve imagepath stored (picasso/glide)
}