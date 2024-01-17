package com.example.urbancycle.Maps;

// Import necessary classes
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import android.net.Uri;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.example.urbancycle.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;
import android.widget.Button;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.app.AlertDialog;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.graphics.Color;
import android.widget.ImageButton;
import android.widget.TextView;


public class Routes extends Fragment {
    private GoogleMap mMap;
    private String destination = "default_destination"; // Default destination or from arguments
    private String mode = "walking"; // Default mode is walking
    private FusedLocationProviderClient locationClient;
    private LatLng userLatLng; // Store user's location
    private static final double EMISSION_FACTOR_WALKING = 1;
    private static final double EMISSION_FACTOR_CYCLING = 5;
    private static final double EMISSION_FACTOR_TRANSIT = 75;
    private static final double EMISSION_FACTOR_CAR = 150;
    private TextView tvCarbonEstimator;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng userOrigin;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private String destinationLatLng;
    private boolean isRouteActive = false;
    private LatLng initialLocation;

    // Fetch the current location of the user
    private void fetchCurrentLocation() {
        locationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permissions if not granted
            return;
        }
        locationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                // Update map location and add a marker for the user's location
                userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));
                mMap.addMarker(new MarkerOptions().position(userLatLng).title("Your Location"));

                // Fetch directions from user's location to the destination
                String origin = location.getLatitude() + "," + location.getLongitude();
                fetchDirections(origin, destination, mode);

                // Calculate distance matrix after ensuring userLatLng is not null
                if (getArguments() != null) {
                    destinationLatLng = getArguments().getString("destinationLatLng");
                    calculateDistanceMatrix(userLatLng, destinationLatLng);
                }
            }
        });
    }

    // Callback when Map is ready
    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            fetchCurrentLocation(); // Fetch current location when the map is ready
        }
    };

    // Fetch directions from origin to destination using Google Maps API
    private void fetchDirections(String origin, String destination, String mode) {
        RequestQueue requestQueue = Volley.newRequestQueue(requireActivity());
        String url = Uri.parse("https://maps.googleapis.com/maps/api/directions/json")
                .buildUpon()
                .appendQueryParameter("origin", origin)
                .appendQueryParameter("destination", destination)
                .appendQueryParameter("mode", mode)
                .appendQueryParameter("key", getString(R.string.google_maps_key))
                .toString();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if ("OK".equals(response.getString("status"))) {
                            JSONArray routes = response.getJSONArray("routes");
                            for (int i = 0; i < routes.length(); i++) {
                                JSONObject route = routes.getJSONObject(i);
                                JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                                String encodedPath = overviewPolyline.getString("points");
                                List<LatLng> path = decodePoly(encodedPath);
                                drawPolylineOnMap(path);

                                // Extract duration and update UI
                                JSONObject leg = route.getJSONArray("legs").getJSONObject(0);
                                JSONObject duration = leg.getJSONObject("duration");
                                String durationText = duration.getString("text");
                                updateTrafficUpdatesTextView(durationText);

                                calculateEmissionsFromPath(path);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Handle the error
                });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                8000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsonObjectRequest);
    }

    // Draw a polyline on the map
    private void drawPolylineOnMap(List<LatLng> path) {
        if (mMap != null && path != null && !path.isEmpty()) {
            mMap.clear(); // Clear previous routes and markers

            PolylineOptions polylineOptions = new PolylineOptions().addAll(path)
                    .width(12f)
                    .color(Color.BLUE); // Change color if needed
            mMap.addPolyline(polylineOptions);

            // Add markers at the origin and destination
            LatLng origin = path.get(0);
            LatLng destinationLatLng = path.get(path.size() - 1);
            mMap.addMarker(new MarkerOptions().position(origin).title("Origin"));
            mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("Destination"));

            // Adjust the camera to show the entire route
            setMapBounds(origin, destinationLatLng);
        }
    }

    // Decode the polyline points from Google Maps API response to show different route
    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng(((double) lat / 1E5), ((double) lng / 1E5));
            poly.add(p);
        }

        return poly;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_routes, container, false);
        tvCarbonEstimator = view.findViewById(R.id.carbonEstimator);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
        Button startRouteButton = view.findViewById(R.id.startRouteButton);
        startRouteButton.setOnClickListener(v -> {
            if (isRouteActive) {
                // Logic for ending the route
                endRoute();
                startRouteButton.setText(R.string.start); // Change button text to "Start"
                isRouteActive = false;
            } else {
                // Logic for starting the route
                startRoute();
                startRouteButton.setText(R.string.end_route); // Change button text to "End Route"
                isRouteActive = true;
            }
        });
        ImageButton backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> goBackToDirectionsFragment());

        if (getArguments() != null) {
            double originLat = getArguments().getDouble("originLat");
            double originLng = getArguments().getDouble("originLng");
            double destinationLat = getArguments().getDouble("destinationLat");
            double destinationLng = getArguments().getDouble("destinationLng");
            mode = getArguments().getString("mode", "default_mode");

            String origin = originLat + "," + originLng;
            String destination = destinationLat + "," + destinationLng;

            fetchDirections(origin, destination, mode);
        }
    }

    // Adjust the map camera bounds to include both origin and destination
    private void setMapBounds(LatLng origin, LatLng destination) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(origin);
        builder.include(destination);

        int padding = 200; // Offset from edges of the map in pixels
        LatLngBounds bounds = builder.build();

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
    }

    // Calculate distance matrix using Google Maps API
    private void calculateDistanceMatrix(LatLng origin, String destination) {
        if (origin == null) {
            Log.e("RoutesFragment", "Origin is null, cannot calculate distance matrix");
            return;
        }

        String origins = origin.latitude + "," + origin.longitude;
        String destinations = destination;
        String url = "https://maps.googleapis.com/maps/api/distancematrix/json"
                + "?origins=" + origins
                + "&destinations=" + destinations
                + "&key=" + getString(R.string.google_maps_key);

        Log.d("RoutesFragment", "Request URL: " + url);

        RequestQueue requestQueue = Volley.newRequestQueue(requireActivity());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d("RoutesFragment", "Full Distance Matrix Response: " + response.toString());

                    try {
                        JSONArray rows = response.getJSONArray("rows");
                        if (rows.length() > 0) {
                            JSONObject elements = rows.getJSONObject(0).getJSONArray("elements").getJSONObject(0);
                            String status = elements.getString("status");

                            if ("OK".equals(status)) {
                                JSONObject distanceObj = elements.getJSONObject("distance");
                                double distance = distanceObj.getDouble("value") / 1000.0; // Convert meters to kilometers
                                double emissionFactor = getEmissionFactor(mode);
                                double emissions = calculateCarbonEmissions(distance, emissionFactor);

                                Log.d("RoutesFragment", "Distance calculated: " + distance + " km, Emissions: " + emissions + " grams of CO2");

                                getActivity().runOnUiThread(() -> {
                                    View anchorView = getView().findViewById(R.id.carbonEstimator);
                                    if (anchorView != null) {
                                        Snackbar snackbar = Snackbar.make(anchorView, "Emissions: " + String.format("%.2f", emissions) + " grams of CO2", Snackbar.LENGTH_LONG);
                                        snackbar.show();
                                    } else {
                                        Log.e("RoutesFragment", "Anchor view is null, cannot show Snackbar");
                                    }
                                });
                            } else {
                                Log.e("RoutesFragment", "Route not found or error in data: " + status);
                            }
                        } else {
                            Log.e("RoutesFragment", "No rows data available in the response");
                        }
                    } catch (JSONException e) {
                        Log.e("RoutesFragment", "JSON parsing error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("RoutesFragment", "Request error: " + error.toString());
                }
        );

        requestQueue.add(jsonObjectRequest);
    }

    // Get emission factor based on the selected mode of transportation
    private double getEmissionFactor(String mode) {
        switch (mode) {
            case "walking":
                return EMISSION_FACTOR_WALKING;
            case "cycling":
                return EMISSION_FACTOR_CYCLING;
            case "transit":
                return EMISSION_FACTOR_TRANSIT;
            default:
                return EMISSION_FACTOR_WALKING; // Assuming walk as the default mode
        }
    }

    // Calculate carbon emissions based on distance and emission factor
    private double calculateCarbonEmissions(double distance, double emissionFactor) {
        return distance * emissionFactor;
    }

    // Calculate emissions from the path of coordinates
    private void calculateEmissionsFromPath(List<LatLng> path) {
        double totalDistance = 0.0;
        for (int i = 1; i < path.size(); i++) {
            totalDistance += distanceBetween(path.get(i - 1), path.get(i));
        }
        double emissionFactor = getEmissionFactor(mode);
        double emissions = calculateCarbonEmissions(totalDistance, emissionFactor);

        getActivity().runOnUiThread(() -> {
            String emissionText = "Emissions: " + String.format("%.2f", emissions) + " grams of CO2";
            tvCarbonEstimator.setText(emissionText); // Update the TextView

            View anchorView = getView().findViewById(R.id.carbonEstimator);
            if (anchorView != null) {
                Snackbar snackbar = Snackbar.make(anchorView, emissionText, Snackbar.LENGTH_LONG);
                snackbar.show();
            } else {
                Log.e("RoutesFragment", "Anchor view is null, cannot show Snackbar");
            }
        });
    }

    // Calculate distance between two LatLng points
    private double distanceBetween(LatLng latLng1, LatLng latLng2) {
        float[] results = new float[1];
        Location.distanceBetween(latLng1.latitude, latLng1.longitude, latLng2.latitude, latLng2.longitude, results);
        return results[0] / 1000.0; // Convert to kilometers
    }

    // Navigate back to the DirectionsFragment
    private void goBackToDirectionsFragment() {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_routesFragment_to_directionsFragment);
    }

    // Update the traffic updates TextView with ETA
    private void updateTrafficUpdatesTextView(String durationText) {
        getActivity().runOnUiThread(() -> {
            TextView trafficUpdatesTextView = getView().findViewById(R.id.trafficUpdates);
            if (trafficUpdatesTextView != null) {
                String updatedText = "ETA: " + durationText;
                trafficUpdatesTextView.setText(updatedText);
            }
        });
    }

    // Enable user's location on the map
    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            if (mMap != null) {
                try {
                    mMap.setMyLocationEnabled(true);
                    focusOnUserLocation();
                } catch (SecurityException e) {
                    Log.e("RoutesFragment", "Security exception: " + e.getMessage());
                }
            }
        }
    }

    // Focus on the user's current location on the map
    private void focusOnUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 30));
                }
            });
        } else {
            Log.e("RoutesFragment", "Location permission not granted");

        }
    }

    // Handle permission request results for location access
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                // Handle the case where permission is denied
                Log.e("RoutesFragment", "Location permission denied");
            }
        }
    }

    // Enable the My Location layer on the map for starting a route
    private void enableMyLocationLayer() {
        if (mMap != null) {
            try {
                mMap.setMyLocationEnabled(true);
                focusOnUserLocation();
            } catch (SecurityException e) {
                Log.e("RoutesFragment", "Security exception: " + e.getMessage());
            }
        }
    }

    // Start a route by enabling My Location layer
    private void startRoute() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            enableMyLocationLayer();
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    // Store the initial location when the route starts
                    initialLocation = new LatLng(location.getLatitude(), location.getLongitude());
                }
            });
        }
    }
    private double calculateCarbonSavings(double distance, String mode) {
        double emissionsForMode = calculateCarbonEmissions(distance, getEmissionFactor(mode));
        return baselineEmissions - emissionsForMode;
    }

    // End a route by calculating distance and points earned
    private void endRoute() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null && initialLocation != null) {
                    LatLng finalLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    double distance = distanceBetween(initialLocation, finalLocation);
                    double carbonSavings = calculateCarbonSavings(distance, mode);

                    // Calculate points based on carbon savings (1 point for every 10 units of carbon saved)
                    int pointsEarned = (int) (carbonSavings / 10);

                    Bundle bundle = new Bundle();
                    bundle.putInt("pointsEarned", pointsEarned);
                    NavHostFragment.findNavController(Routes.this)
                            .navigate(R.id.action_routesFragment_to_directionsFragment, bundle);
                }
            });
        } else {
            // Handle the case where permission is not granted
            Log.e("RoutesFragment", "Location permission not granted");
            goBackToDirectionsFragment();
        }
    }
}

