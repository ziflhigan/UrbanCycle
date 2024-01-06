package com.example.urbancycle.Community;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.urbancycle.R;

public class CommunityFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Button BtnEvent = view.findViewById(R.id.button_events);
        Button BtnTips = view.findViewById(R.id.button_tips);
        Button BtnForum = view.findViewById(R.id.button_forum);

        View.OnClickListener OCLEvent = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_community_to_eventFragment);
            }
        };
        BtnEvent.setOnClickListener(OCLEvent);

        View.OnClickListener OCLTips = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_community_to_tipsFragment);
            }
        };
        BtnTips.setOnClickListener(OCLTips);

        View.OnClickListener OCLForum = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_community_to_DestForum);
            }
        };
        BtnForum.setOnClickListener(OCLForum);

    }
}
