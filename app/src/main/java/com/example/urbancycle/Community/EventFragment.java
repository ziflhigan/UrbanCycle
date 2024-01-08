package com.example.urbancycle.Community;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.urbancycle.R;

import java.util.ArrayList;
import java.util.List;

public class EventFragment extends Fragment {
    private List<Event> eventList = new ArrayList<>();
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    public EventFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Button BtnSubmitEvent = view.findViewById(R.id.BtnSubmitEvent);
        View.OnClickListener OCLEventSubmissionForm = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_DestEvent_to_DestSubmissionForm);
            }
        };
        BtnSubmitEvent.setOnClickListener(OCLEventSubmissionForm);
        initializeRecyclerView(view);
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("event")) {
            Event event = (Event) bundle.getSerializable("event");
            addEventToList(event);
        }
    }
    private void initializeRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.RVEvent);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList);
        recyclerView.setAdapter(eventAdapter);
    }


    public  void addEventToList(Event event) {
        eventList.add(event);
        eventAdapter.notifyDataSetChanged();
    }
}

