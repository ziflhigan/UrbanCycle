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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DirectionsFragment extends Fragment implements ConnectToDatabase.DatabaseConnectionListener {

    private static final double EMISSION_FACTOR_WALKING = 1; // Assuming minimal emissions or same with cycling ?
    private static final double EMISSION_FACTOR_CYCLING = 5; // 5 grams of CO2 per km
    private static final double EMISSION_FACTOR_TRANSIT = 75; // An example value for buses
    private static final double EMISSION_FACTOR_CAR = 150; // An example value for cars
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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (!Places.isInitialized()) {
            Places.initialize(requireActivity().getApplicationContext(), getString(R.string.google_maps_key));
        }
        View view = inflater.inflate(R.layout.fragment_directions, container, false);

        // Initialize destinationInput EditText
//        destinationInput = view.findViewById(R.id.autocomplete_destination_fragment); // Make sure ID matches with your layout
        ImageButton backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> navigateBackToMapFragment());
        
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
    }   private void setupStartRouteButton(View view) {
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
    }private void updateButtonStyles(String selectedMode) {
        // Reset styles for all buttons to default
        resetButtonStyles(walkingButton, MODE_WALKING, selectedMode);
        resetButtonStyles(cyclingButton, MODE_CYCLING, selectedMode);
        resetButtonStyles(transportButton, MODE_TRANSIT, selectedMode);

        // Set the style for the selected button
        Button selectedButton = getButtonForMode(selectedMode);
        if (selectedButton != null) {
            selectedButton.setAlpha(1.0f);
            selectedButton.setTextColor(getResources().getColor(R.color.selected_mode_text));
            selectedButton.setBackgroundResource(R.drawable.selected_mode_background);
        }
    }

    private void resetButtonStyles(Button button, String mode, String selectedMode) {
        button.setAlpha(mode.equals(selectedMode) ? 1.0f : 0.6f);
        button.setTextColor(getResources().getColor(R.color.unselected_mode_text));
        button.setBackgroundResource(R.drawable.unselected_mode_background);
    }

    private Button getButtonForMode(String mode) {
        switch (mode) {
            case MODE_WALKING:
                return walkingButton;
            case MODE_CYCLING:
                return cyclingButton;
            case MODE_TRANSIT:
                return transportButton;
            default:
                return null;
        }
    }




    private void setupMapFragment(View view) {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(mapReadyCallback);
        }
    }



    private void fetchDirectionsFromCurrentLocation(String mode) {
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
                // Handle the case where the user denies the permission
            }
        }
    }
    private void navigateBackToMapFragment() {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_directionsFragment_to_map);
    }

    @Override
    public void onConnectionSuccess(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void onConnectionFailure() {

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

    private void calculateDirections(String mode) {
        String originText = originInput.getText().toString();
        String destinationText = destinationInput.getText().toString();

        if (originText.isEmpty() || destinationText.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter origin and destination", Toast.LENGTH_SHORT).show();
            return;
        }

        new FetchDirectionsTask().execute(originText, destinationText, mode);
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

    private double calculateCarbonSavings(double distance, double emissionFactor) {
        double emissionsForMode = calculateCarbonEmissions(distance, emissionFactor);
        double baselineEmissions = calculateCarbonEmissions(distance, EMISSION_FACTOR_CAR);
        return baselineEmissions - emissionsForMode;
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
