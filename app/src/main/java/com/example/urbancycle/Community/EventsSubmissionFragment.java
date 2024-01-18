package com.example.urbancycle.Community;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.urbancycle.R;

public class EventsSubmissionFragment extends Fragment {

    private EditText ETEventName;
    private EditText ETEventOrganizer;
    private EditText ETEventLocation;
    private EditText ETEventDate;
    private EditText ETEventTime;


    public EventsSubmissionFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_events_submission, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ETEventName = view.findViewById(R.id.ETEventName);
        ETEventOrganizer = view.findViewById(R.id.ETEventOrg);
        ETEventLocation = view.findViewById(R.id.ETLocation);
        ETEventDate = view.findViewById(R.id.ETDate);
        ETEventTime = view.findViewById(R.id.ETTime);

        //listener for submit button
        Button BtnSubmission = view.findViewById(R.id.BtnSubmission);

        BtnSubmission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                receiveEventData();
            } //when button is click, data inputted will send to EventFragment for display purpose
        });

    }


    private void receiveEventData() {
        String eventName = ETEventName.getText().toString();
        String eventOrganizer = ETEventOrganizer.getText().toString();
        String eventLocation = ETEventLocation.getText().toString();
        String eventDate = ETEventDate.getText().toString();
        String eventTime = ETEventTime.getText().toString();

        Event event = new Event(eventName, eventOrganizer, eventLocation, eventDate, eventTime); //create event object for the received data

        // Use navigation to send the data to EventFragment
        Bundle bundle = new Bundle();
        bundle.putSerializable("event", event);

Navigation.findNavController(requireView()).navigate(R.id.action_DestSubmissionForm_to_DestEvent, bundle);
    }
}
