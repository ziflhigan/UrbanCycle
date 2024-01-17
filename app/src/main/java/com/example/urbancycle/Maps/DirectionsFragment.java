package com.example.urbancycle.Maps;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import android.net.Uri;
import android.widget.ImageButton;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;

import com.example.urbancycle.Database.ConnectToDatabase;
import com.example.urbancycle.Database.UserInfoManager;
import com.example.urbancycle.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import android.location.Location;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import android.widget.Toast;


import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DirectionsFragment extends Fragment implements ConnectToDatabase.DatabaseConnectionListener {

    private Place selectedPlace; // To store the selected place
    private Connection connection;
    private double carSavings = 0.0;
    private GoogleMap mMap;
    private EditText originInput, destinationInput;
    private Button walkingButton, cyclingButton, transportButton, startRouteButton;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int YOUR_REQUEST_CODE = 1;
    private String lastSelectedMode;
    private static final String MODE_WALKING = "walking";
    private static final String MODE_CYCLING = "cycling";
    private static final String MODE_TRANSIT = "transit";


    LatLng userOrigin;      // User's starting location
    private AutocompleteSupportFragment autocompleteDestinationFragment;

    // Callback for map readiness
    private final OnMapReadyCallback mapReadyCallback = googleMap -> {
        mMap = googleMap;
        fetchUserLocation();

        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                // Enable the My Location layer
                mMap.setMyLocationEnabled(true);

                // Get the current location and update the map camera
                fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15)); // Adjust zoom level as needed
                    }
                });
            } catch (SecurityException e) {
                // Handle the exception if the permission is not granted
            }
        } else {
            // Request location permission
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
        // Set up any additional map configurations here
    };

    // Method to fetch user's current location
    private void fetchUserLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    userOrigin = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userOrigin, 15)); // Adjust zoom level as needed
                }
            });
        } else {
            // Request location permission
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (!Places.isInitialized()) {
            Places.initialize(requireActivity().getApplicationContext(), getString(R.string.google_maps_key));
        }
        View view = inflater.inflate(R.layout.fragment_directions, container, false);

        ImageButton backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> navigateBackToMapFragment());

        if (getArguments() != null && getArguments().containsKey("pointsEarned")) {
            view.post(() -> {
                int pointsEarned = getArguments().getInt("pointsEarned");
                Snackbar.make(view, "Points earned: " + pointsEarned, Snackbar.LENGTH_LONG).show();
            });
        }

        startRouteButton = view.findViewById(R.id.startRouteButton);
        startRouteButton.setOnClickListener(v -> {
            if (selectedPlace != null) {
                String destination = selectedPlace.getName(); // Use the selected place's name
                String mode = lastSelectedMode; // Assuming lastSelectedMode holds the transport mode

                Bundle bundle = new Bundle();
                bundle.putString("destination", destination);
                bundle.putString("mode", mode);

                NavHostFragment.findNavController(DirectionsFragment.this)
                        .navigate(R.id.action_directionsFragment_to_routes, bundle);
            } else {
                Toast.makeText(getContext(), "Please select a destination", Toast.LENGTH_SHORT).show();
            }
        });

        setupModeButtons(view);
        setupMapFragment(view);
        setupAutocompleteFragment();
        setupStartRouteButton(view);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        return view;
    }
    private void setupStartRouteButton(View view) {
        // Method to set up the Start Route button
        startRouteButton = view.findViewById(R.id.startRouteButton);
        startRouteButton.setOnClickListener(v -> {
            if (selectedPlace != null) {
                navigateToRoutes();
            } else {
                Toast.makeText(getContext(), "Please select a destination", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void navigateToRoutes() {
        // Method to navigate to the Routes fragment
        if (selectedPlace != null && userOrigin != null) {
            Bundle bundle = new Bundle();
            bundle.putDouble("originLat", userOrigin.latitude);
            bundle.putDouble("originLng", userOrigin.longitude);
            bundle.putDouble("destinationLat", selectedPlace.getLatLng().latitude);
            bundle.putDouble("destinationLng", selectedPlace.getLatLng().longitude);
            bundle.putString("mode", lastSelectedMode);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_directionsFragment_to_routes, bundle);
        } else {
            Toast.makeText(getContext(), "Please select destination", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupAutocompleteFragment() {
        // Method to set up the AutocompleteFragment for destination input
        autocompleteDestinationFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_destination_fragment);

        if (autocompleteDestinationFragment != null) {
            autocompleteDestinationFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
            autocompleteDestinationFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    selectedPlace = place; // Update the selected place
                    Log.d("DirectionsFragment", "Place selected: " + place.getName());
                }

                @Override
                public void onError(@NonNull Status status) {
                    Toast.makeText(getContext(), "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void setupModeButtons(View view) {
        // Initialize buttons
        walkingButton = view.findViewById(R.id.walkingButton);
        cyclingButton = view.findViewById(R.id.cyclingButton);
        transportButton = view.findViewById(R.id.transportButton);

        // Setup listeners
        walkingButton.setOnClickListener(v -> handleModeSelection(MODE_WALKING));
        cyclingButton.setOnClickListener(v -> handleModeSelection(MODE_CYCLING));
        transportButton.setOnClickListener(v -> handleModeSelection(MODE_TRANSIT));
    }
    private void handleModeSelection(String mode) {
        // Update the last selected mode
        lastSelectedMode = mode;

        // Change the button appearance to show the selected mode
        updateButtonStyles(mode);
    }
    private void updateButtonStyles(String selectedMode) {
        // Method to update button styles based on selected mode
        setButtonAlpha(walkingButton, MODE_WALKING.equals(selectedMode) ? 1.0f : 0.6f);
        setButtonAlpha(cyclingButton, MODE_CYCLING.equals(selectedMode) ? 1.0f : 0.6f);
        setButtonAlpha(transportButton, MODE_TRANSIT.equals(selectedMode) ? 1.0f : 0.6f);
        // Set the alpha for the selected button to full opacity
        // and for unselected buttons to a lower value for transparency
    }
    // Method to set the alpha of a button
    private void setButtonAlpha(Button button, float alpha) {
        button.setAlpha(alpha);
    }


    private void setupMapFragment(View view) {
        // Method to set up the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(mapReadyCallback);
        }
    }



    private void fetchDirectionsFromCurrentLocation(String mode) {
        // Method to fetch directions from the user's current location
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                String origin = location.getLatitude() + "," + location.getLongitude();
                                String destination = destinationInput.getText().toString();

                                if (!destination.isEmpty()) {
                                    new FetchDirectionsTask().execute(origin, destination, mode);
                                } else {
                                    // Prompt the user to enter a destination
                                }
                            }
                        }
                    });
        } else {
            // Request the permission
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    YOUR_REQUEST_CODE); // Replace YOUR_REQUEST_CODE with an int constant
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == YOUR_REQUEST_CODE) { // Replace YOUR_REQUEST_CODE with the same int constant used above
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted
                fetchDirectionsFromCurrentLocation(lastSelectedMode); // lastSelectedMode should be stored when the user clicks any mode button
            } else {
                // Permission was denied

            }
        }
    }

    private void navigateBackToMapFragment() {
        // Method to navigate back to the Map fragment

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_directionsFragment_to_map);
    }

    @Override
    public void onConnectionSuccess(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void onConnectionFailure() {
        // Handle connection failure
    }

    // Inner class for AsyncTask to fetch directions
    private class FetchDirectionsTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            // Background task to fetch directions
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
            // Process the fetched directions
            super.onPostExecute(result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray routes = jsonObject.getJSONArray("routes");
                // Iterate over routes and extract transit details
                for (int i = 0; i < routes.length(); i++) {
                    JSONObject route = routes.getJSONObject(i);
                    JSONArray legs = route.getJSONArray("legs");
                    // Process each leg for transit details
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    // Method to build the URL for the Google Directions API
    private String buildDirectionsUrl(String origin, String destination, String mode) {
        // Build the URL for directions
        String encodedOrigin = Uri.encode(origin);
        String encodedDestination = Uri.encode(destination);
        String apiKey = "AIzaSyDjkjvP2QaWCdRqh7-AWw1vKcXNbGHNXzw";

        StringBuilder urlBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        urlBuilder.append("origin=").append(encodedOrigin);
        urlBuilder.append("&destination=").append(encodedDestination);
        urlBuilder.append("&mode=").append(mode);

        if ("transit".equals(mode)) {
            // For transit mode, you can add additional parameters like departure_time
            urlBuilder.append("&departure_time=now");
            urlBuilder.append("&transit_mode=bus");
        }

        urlBuilder.append("&key=").append(apiKey);
        return urlBuilder.toString();
    }

    // Method to perform the HTTP request
    private String performHttpRequest(String urlString) {
        // Perform the HTTP request and return the response
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





    private void displayTransitRoutes(JSONArray routes) {
        for (int i = 0; i < routes.length(); i++) {
            try {
                JSONObject route = routes.getJSONObject(i);
                JSONArray legs = route.getJSONArray("legs");
                for (int j = 0; j < legs.length(); j++) {
                    JSONObject leg = legs.getJSONObject(j);
                    JSONArray steps = leg.getJSONArray("steps");

                    for (int k = 0; k < steps.length(); k++) {
                        JSONObject step = steps.getJSONObject(k);
                        if (step.has("transit_details")) {
                            // Extract transit details
                            JSONObject transitDetails = step.getJSONObject("transit_details");
                            JSONObject line = transitDetails.getJSONObject("line");
                            String busNumber = line.getString("short_name");
                            JSONObject departureStop = transitDetails.getJSONObject("departure_stop");
                            String departureName = departureStop.getString("name");
                            JSONObject arrivalStop = transitDetails.getJSONObject("arrival_stop");
                            String arrivalName = arrivalStop.getString("name");

                            // Display these details in your UI
                            updateUIWithTransitDetails(busNumber, departureName, arrivalName);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateUIWithTransitDetails(String busNumber, String departureName, String arrivalName) {
        // Update UI here, e.g., adding this information to a list or view
    }

    private double calculateCarbonEmissions(double distance, double emissionFactor) {
        return distance * emissionFactor;
    }



}

/**
 * Use this class to insert the information to the database
 * You can call it like this: new InsertRouteInformationTask(...Parameters).execute
 */
class InsertRouteInformationTask extends AsyncTask<Void, Void, Boolean> {
    private final Connection connection;
    private final String startLocation, endLocation;
    private final double carbonSavings;
    private final String userEmail = UserInfoManager.getInstance().getEmail();

    public InsertRouteInformationTask(Connection connection, String startLocation, String endLocation, double carbonSavings) {
        this.connection = connection;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.carbonSavings = carbonSavings;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            String insertQuery = "INSERT INTO Routes (Email, StartLocation, EndLocation, CarbonSavings, Date) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
            preparedStatement.setString(1, userEmail);
            preparedStatement.setString(2, startLocation);
            preparedStatement.setString(3, endLocation);
            preparedStatement.setDouble(4, carbonSavings);
            preparedStatement.setString(5, getCurrentDateTime());

            int result = preparedStatement.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

}
