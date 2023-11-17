package com.example.urbancycle.SupportAndFeedback;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.urbancycle.R;

public class SupportFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_support, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Button feedback=view.findViewById(R.id.feedbackB);
        View.OnClickListener onFeedback=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().replace(R.id.fragment_container,new Feedback()).addToBackStack(null).commit();
            }
        };
        feedback.setOnClickListener(onFeedback);

        Button FAQ=view.findViewById(R.id.FAQB);
        View.OnClickListener onFAQ=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.DFAQ);
            }
        };
        FAQ.setOnClickListener(onFAQ);
    }
}