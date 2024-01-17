package com.example.urbancycle.Community;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.urbancycle.Community.DailyTip;
import com.example.urbancycle.R;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TipsFragment extends Fragment {
    private List<DailyTip> dailyTipsList = new ArrayList<>();

    public TipsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tips, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Adding tips into List
        dailyTipsList.add(new DailyTip("Use public transportation to reduce individual carbon footprint.", R.drawable.publictransportation));
        dailyTipsList.add(new DailyTip("Consider carpooling or ridesharing to share commuting costs and reduce emissions.", R.drawable.sustainabletravel));
        dailyTipsList.add(new DailyTip("Opt for walking or cycling for short distances instead of using a vehicle.", R.drawable.bike));
        dailyTipsList.add(new DailyTip("Conserve energy by turning off lights and electronics when not in use.", R.drawable.saveenergy));
        dailyTipsList.add(new DailyTip("Invest in an electric vehicle for a sustainable and low-emission commute.", R.drawable.greentravel));
        dailyTipsList.add(new DailyTip("Participate in car-free days to raise awareness about the impact of transportation.", R.drawable.publictransportation));
        dailyTipsList.add(new DailyTip("Advocate for improved public transportation infrastructure in your community", R.drawable.sustainabletravel));

        DailyTip currentDayTip = getTipForCurrentDay();

        // Display the tip in the UI
        displayTip(currentDayTip, view);
    }

    // Display tip according to day of the week
    private DailyTip getTipForCurrentDay() {
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        int tipIndex = (dayOfWeek - 1) % dailyTipsList.size();

        // Make sure tipIndex is non-negative
        if (tipIndex < 0) {
            tipIndex += dailyTipsList.size();
        }

        return dailyTipsList.get(tipIndex);
    }

    // Binding content of tips from List to the UI
    private void displayTip(DailyTip tip, View view) {
        TextView tipTitleTextView = view.findViewById(R.id.TVTipsDisplay);
        ImageView tipImageView = view.findViewById(R.id.IVTipsDisplay);

        tipTitleTextView.setText(tip.getTipText());
        tipImageView.setImageResource(tip.getImageResources());
    }
}
