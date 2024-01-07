package com.example.urbancycle.Community;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.urbancycle.R;


public class EventsSubmissionFragment extends Fragment {

    private EditText ETEventName;
    private EditText ETEventOrganizer;
    private EditText ETEventLocation;
    private EditText ETEventDate;
    private EditText ETEventTime;
    private ImageView IVEventPoster;
    private Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 1;
    public EventsSubmissionFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_events_submission, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        ETEventName = view.findViewById(R.id.ETEventName);
        ETEventOrganizer = view.findViewById(R.id.ETEventOrg);
        ETEventLocation = view.findViewById(R.id.ETLocation);
        ETEventDate = view.findViewById(R.id.ETDate);
        ETEventTime = view.findViewById(R.id.ETTime);
        IVEventPoster = view.findViewById(R.id.IVEventPoster);

        Button BtnSubmission=view.findViewById(R.id.BtnSubmission);
        Button BtnUpload=view.findViewById(R.id.BtnUpload);
        View.OnClickListener OCLSubmission = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            receiveEventData();
            }
        };
        BtnSubmission.setOnClickListener(OCLSubmission);

        View.OnClickListener OCLUpload = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        };
        BtnUpload.setOnClickListener(OCLUpload);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            imageUri = selectedImageUri;
            IVEventPoster.setImageURI(selectedImageUri);
        }
    }

    private void receiveEventData() {

        String eventName = ETEventName.getText().toString();
        String eventOrganizer = ETEventOrganizer.getText().toString();
        String eventLocation = ETEventLocation.getText().toString();
        String eventDate = ETEventDate.getText().toString();
        String eventTime = ETEventTime.getText().toString();


        Event event = new Event(eventName, eventOrganizer, eventLocation, eventDate, eventTime);
        event.setImageUri(imageUri != null ? imageUri.toString() : null);

        Fragment navHostFragment = getParentFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            Fragment eventFragment = navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();

            if (eventFragment instanceof EventFragment) {
                ((EventFragment) eventFragment).addEventToList(event);
                showToast("Submission received!");
                Navigation.findNavController(requireView()).popBackStack();
            } else {
                showToast("Failed to submit event. Please try again.");
            }
        } else {
            showToast("Failed to submit event. Please try again.");
        }


    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
    }
