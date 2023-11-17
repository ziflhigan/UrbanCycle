package com.example.urbancycle.SupportAndFeedback;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.urbancycle.R;

public class Feedback extends Fragment {

    public Feedback() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feedback, container, false);
    }

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState){
        Button submit=view.findViewById(R.id.submitB);
        View.OnClickListener onSubmit=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText feedback=view.findViewById(R.id.feedbackET);

            }
        };
    }
}