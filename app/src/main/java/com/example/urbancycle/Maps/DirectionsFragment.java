package com.example.urbancycle.Maps;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Button;
import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.net.Uri;
import android.widget.ImageButton;

import com.example.urbancycle.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

public class DirectionsFragment extends Fragment {

    private GoogleMap mMap;
    private EditText originInput;
    private EditText destinationInput;
    private Button walkingButton;
    private Button cyclingButton;
    private Button transportButton;
    private Button startRouteButton;

    private final OnMapReadyCallback mapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            mMap = googleMap;
            // Set up any initial map configurations here
            // For example, setting the map type or adding any initial markers
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_directions, container, false);

        // Initialize UI components
        originInput = view.findViewById(R.id.originInput);
        destinationInput = view.findViewById(R.id.destinationInput);
        walkingButton = view.findViewById(R.id.walkingButton);
        cyclingButton = view.findViewById(R.id.cyclingButton);
        transportButton = view.findViewById(R.id.transportButton);
        startRouteButton = view.findViewById(R.id.startRouteButton);

        setupButtonListeners(view);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(mapReadyCallback);
        }
        ImageButton backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        return view;
    }

    private void setupButtonListeners(View view) {
        walkingButton.setOnClickListener(v -> fetchDirections("walking"));
        cyclingButton.setOnClickListener(v -> fetchDirections("bicycling"));
        transportButton.setOnClickListener(v -> fetchDirections("transit"));
        startRouteButton.setOnClickListener(v -> fetchDirections("driving"));
    }

    private void fetchDirections(String mode) {
        String origin = originInput.getText().toString();
        String destination = destinationInput.getText().toString();

        if (!origin.isEmpty() && !destination.isEmpty()) {
            new FetchDirectionsTask().execute(origin, destination, mode);
        } else {
            // Prompt the user to enter both origin and destination
        }
    }
    // Inner class for AsyncTask to fetch directions
    private class FetchDirectionsTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String origin = params[0];
            String destination = params[1];
            String mode = params[2]; // e.g., "driving", "walking", etc.

            // Construct the URL for the Directions API
            String url = buildDirectionsUrl(origin, destination, mode);

            // Perform the HTTP request and return the response
            return performHttpRequest(url);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Parse the JSON result and update the map
            // This is where you would implement parsing of the JSON result
            // and update your map with the directions received
        }
    }

    // Method to build the URL for the Google Directions API
    private String buildDirectionsUrl(String origin, String destination, String mode) {
        // Properly encoded parameters
        String encodedOrigin = Uri.encode(origin);
        String encodedDestination = Uri.encode(destination);

        // Corrected API key without newline and spaces
        String apiKey = "AIzaSyDjkjvP2QaWCdRqh7-AWw1vKcXNbGHNXzw";


        // Build URL using the Directions API
        return "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + encodedOrigin +
                "&destination=" + encodedDestination +
                "&mode=" + mode +
                "&key=" + apiKey;
    }

    // Method to perform the HTTP request
    private String performHttpRequest(String urlString) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        StringBuilder result = new StringBuilder();

        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result.toString();
    }
}

