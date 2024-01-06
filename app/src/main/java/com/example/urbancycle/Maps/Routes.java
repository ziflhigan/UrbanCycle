package com.example.urbancycle.Maps;

// Import necessary classes
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
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
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.graphics.Color;


public class Routes extends Fragment {
    private GoogleMap mMap;
    private String destination = "default_destination"; // Set a default or get from arguments
    private String mode = "walking"; // Default to walking
    private FusedLocationProviderClient locationClient;
    private LatLng userLatLng; // Store user's location


    private void fetchCurrentLocation() {
        locationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return; // Handle lack of permissions
        }

        locationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                // Update map location and add marker for user's location
                userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));
                mMap.addMarker(new MarkerOptions().position(userLatLng).title("Your Location"));

                // Now fetch directions from user's location to destination
                String origin = location.getLatitude() + "," + location.getLongitude();
                fetchDirections(origin, destination, mode);
            }
        });
    }

    private void updateMapLocation(Location location) {
        if (mMap != null) {
            LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(userLatLng).title("Your Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15)); // Adjust the zoom level as needed
        }
    }

    private String origin;
    // The callback when Map is ready
    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            fetchCurrentLocation(); // Fetch current location when map is ready
        }
    };

    // Add the fetchDirections method
    private void fetchDirections(String origin, String destination, String mode) {
        RequestQueue requestQueue = Volley.newRequestQueue(requireActivity());

        String url = Uri.parse("https://maps.googleapis.com/maps/api/directions/json")
                .buildUpon()
                .appendQueryParameter("destination", destination)
                .appendQueryParameter("origin", origin)
                .appendQueryParameter("mode", mode)
                .appendQueryParameter("key", "AIzaSyDjkjvP2QaWCdRqh7-AWw1vKcXNbGHNXzwAIzaSyDjkjvP2QaWCdRqh7-AWw1vKcXNbGHNXzw")
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

    // Method to draw polyline on the map
    private void drawPolylineOnMap(List<LatLng> path) {
        if (mMap != null && path != null && !path.isEmpty()) {
            PolylineOptions polylineOptions = new PolylineOptions().addAll(path)
                    .width(12f)
                    .color(Color.BLUE);
            mMap.addPolyline(polylineOptions);

            // Add marker at the origin
            LatLng origin = path.get(0);
            mMap.addMarker(new MarkerOptions().position(origin).title("Origin"));

            // Add marker at the destination
            LatLng destinationLatLng = path.get(path.size() - 1);
            mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("Destination"));

            setMapBounds(origin, destinationLatLng);
        }
    }
    // Add the decodePoly method
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
        return inflater.inflate(R.layout.fragment_routes, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        fetchDestinationCoordinates(destination);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        if (getArguments() != null) {
            destination = getArguments().getString("destination", "default_destination");
            mode = getArguments().getString("mode", "default_mode");



        }
    }
    private void setMapBounds(LatLng origin, LatLng destination) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(origin);
        builder.include(destination);

        int padding = 200; // offset from edges of the map in pixels
        LatLngBounds bounds = builder.build();

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
    }
    public void onResponse(JSONObject response) {
        try {
            String status = response.getString("status");
            if ("OK".equals(status)) {
                JSONArray routes = response.getJSONArray("routes");
                for (int i = 0; i < routes.length(); i++) {
                    JSONObject route = routes.getJSONObject(i);
                    JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                    String encodedPath = overviewPolyline.getString("points");
                    List<LatLng> path = decodePoly(encodedPath);
                    drawPolylineOnMap(path);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void fetchDestinationCoordinates(String destinationName) {
        RequestQueue requestQueue = Volley.newRequestQueue(requireActivity());
        String apiKey = "AIzaSyDjkjvP2QaWCdRqh7-AWw1vKcXNbGHNXzw"; // Replace with your actual API key
        String url = Uri.parse("https://maps.googleapis.com/maps/api/geocode/json")
                .buildUpon()
                .appendQueryParameter("address", destinationName)
                .appendQueryParameter("key", apiKey)
                .toString();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        if (results.length() > 0) {
                            JSONObject location = results.getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
                            double lat = location.getDouble("lat");
                            double lng = location.getDouble("lng");
                            String destinationCoords = lat + "," + lng;
                            fetchDirections(origin, destinationCoords, mode);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Handle the error
                });

        requestQueue.add(jsonObjectRequest);
    }

}
