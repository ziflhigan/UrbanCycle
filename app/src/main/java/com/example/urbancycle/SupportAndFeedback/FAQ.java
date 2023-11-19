package com.example.urbancycle.SupportAndFeedback;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.urbancycle.R;

public class FAQ extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public FAQ() {
        // Required empty public constructor
    }

    public static FAQ newInstance(String param1, String param2) {
        FAQ fragment = new FAQ();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_faq, container, false);
    }

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState){
        final int[] i = {0};
        String[]faqs={"What is Urban Cycle?\nIt is a good app.",
                "How do I download Urban Cycle?\nYou can download it from our website.",
                "Is Urban Cycle available for both iOS and Android?\nIt is only available for Android."};
        Button next=view.findViewById(R.id.nextB);
        Button back=view.findViewById(R.id.backB);
        TextView faq=view.findViewById(R.id.FAQTV);
        faq.setText(faqs[i[0]]);
        View.OnClickListener onNext=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((i[0]+1)<faqs.length){
                    i[0]++;
                    faq.setText(faqs[i[0]]);
                }else{
                    i[0]=0;
                    faq.setText(faqs[i[0]]);
                }
            }
        };
        next.setOnClickListener(onNext);
        View.OnClickListener onBack=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((i[0]-1)<0){
                    i[0]=faqs.length-1;
                    faq.setText(faqs[i[0]]);
                }else{
                    i[0]--;
                    faq.setText(faqs[i[0]]);
                }
            }
        };
        back.setOnClickListener(onBack);
    }
}