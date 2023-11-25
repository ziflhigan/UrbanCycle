package com.example.urbancycle.Community;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.urbancycle.R;

import java.util.ArrayList;
import java.util.List;

public class CommunityFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

//        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        List<Topic> topicList = new ArrayList<>();
        topicList.add(new Topic("#00FF00")); // Green topic
        topicList.add(new Topic("#00FF00")); // Green topic
        topicList.add(new Topic("#00FF00")); // Green topic



        TopicAdapter adapter = new TopicAdapter(topicList);
//        recyclerView.setAdapter(adapter);


        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
//        recyclerView.setLayoutManager(layoutManager);

        return view;
    }

}
