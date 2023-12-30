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
        // Button BtnRoom1 = view.findViewById(R.id.btn)

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
                Navigation.findNavController(view).navigate(R.id.action_community_self);
            }
        };
        BtnForum.setOnClickListener(OCLForum);

        // To Do: This one is causing trouble
        View.OnClickListener OCLRoom1 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_community_to_DestForum);
            }
        };
        BtnForum.setOnClickListener(OCLRoom1);


        LinearLayout forumRoomButtonsLayout = view.findViewById(R.id.forumRoomButtons);
        Button addButton = view.findViewById(R.id.button_add_room);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call a method to add a new room button
                addRoomButton(forumRoomButtonsLayout);
            }
        });

    }

    // Method to dynamically add a new room button
    private void addRoomButton(LinearLayout forumRoomButtonsLayout) {
        // Create a new button
        Button newRoomButton = new Button(requireContext());
        newRoomButton.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        newRoomButton.setBackgroundResource(R.drawable.rounded_square); // Set background drawable
        // You can add other styling attributes here
        newRoomButton.setText("New Room " + (forumRoomButtonsLayout.getChildCount() + 1));
        newRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the click event for the new room button
                // You can implement the logic to navigate to the selected room or perform other actions
            }
        });

        // Add the new button to the LinearLayout
        forumRoomButtonsLayout.addView(newRoomButton);
    }
}
