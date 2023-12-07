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
import java.util.Arrays;

import android.net.Uri;
import android.widget.ImageButton;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.Place.Field;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
public class DirectionsFragment extends Fragment {

    private GoogleMap mMap;
    private EditText originInput, destinationInput;
    private Button walkingButton, cyclingButton, transportButton, startRouteButton;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int YOUR_REQUEST_CODE = 1;
    private String lastSelectedMode;
    private final OnMapReadyCallback mapReadyCallback = googleMap -> {
        mMap = googleMap;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (!Places.isInitialized()) {
            Places.initialize(requireActivity().getApplicationContext(), getString(R.string.google_maps_key));
        }
        View view = inflater.inflate(R.layout.fragment_directions, container, false);
        initializeUIComponents(view);
        setupMapFragment();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Request location updates
        fetchCurrentLocationAndSetOrigin();

        return view;
    }
    private void fetchCurrentLocationAndSetOrigin() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    updateMapLocation(location);
                }
            });
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    private void updateMapLocation(Location location) {
        if (mMap != null) {
            LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15)); // Adjust the zoom level as needed
        }
    }

    private void initializeUIComponents(View view) {
        originInput = view.findViewById(R.id.originInput);
        destinationInput = view.findViewById(R.id.destinationInput);
        walkingButton = view.findViewById(R.id.walkingButton);
        cyclingButton = view.findViewById(R.id.cyclingButton);
        transportButton = view.findViewById(R.id.transportButton);
        startRouteButton = view.findViewById(R.id.startRouteButton);
        ImageButton backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        setupButtonListeners();
    }

    private void setupMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(mapReadyCallback);
        }
    }

    private void setupButtonListeners() {
        walkingButton.setOnClickListener(v -> handleModeSelection("walking"));
        cyclingButton.setOnClickListener(v -> handleModeSelection("bicycling"));
        transportButton.setOnClickListener(v -> handleModeSelection("transit"));
    }

    private void handleModeSelection(String mode) {
        lastSelectedMode = mode;
        fetchDirectionsFromCurrentLocation(mode);
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

    private void setupAutocomplete() {
        // Setup for origin autocomplete
        AutocompleteSupportFragment autocompleteOriginFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete_origin);
        autocompleteOriginFragment.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME));
        autocompleteOriginFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {


            @Override
            public void onPlaceSelected(@NonNull Place place) {
                LatLng originLatLng = place.getLatLng();
                if (originLatLng != null) {
                    originInput.setText(originLatLng.latitude + "," + originLatLng.longitude);
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(requireContext(), "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Setup for destination autocomplete
        AutocompleteSupportFragment autocompleteDestinationFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete_destination);
        autocompleteDestinationFragment.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME));
        autocompleteDestinationFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                LatLng destinationLatLng = place.getLatLng();
                if (destinationLatLng != null) {
                    destinationInput.setText(destinationLatLng.latitude + "," + destinationLatLng.longitude);
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(requireContext(), "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

    private void displayDirectionsOnMap(String directionsJson) {
        // Parse the JSON response
        // Draw the route using PolylineOptions
    }

    private void showDistanceAndInformation(String directionsJson) {
        // Parse JSON to extract information
        // Display this information to the user
    }

}
