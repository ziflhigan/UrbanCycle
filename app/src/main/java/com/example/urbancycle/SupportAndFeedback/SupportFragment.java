package com.example.urbancycle.SupportAndFeedback;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.urbancycle.R;

public class SupportFragment extends Fragment {
    private TextView TVSupport;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_support, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button feedbackButton = view.findViewById(R.id.feedbackB);
        Button faqButton = view.findViewById(R.id.FAQB);

        feedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(SupportFragment.this)
                        .navigate(R.id.action_supportFragment_to_feedbackFragment);
            }
        });

        faqButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(SupportFragment.this)
                        .navigate(R.id.action_supportFragment_to_faqFragment);
            }
        });
    }
}
