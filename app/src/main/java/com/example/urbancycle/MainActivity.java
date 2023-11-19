package com.example.urbancycle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import android.os.Bundle;
import android.view.View;
import com.google.android.material.floatingactionbutton.FloatingActionButton;



import com.example.urbancycle.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setting up the NavController
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        // Setting up AppBarConfiguration
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.map, R.id.community, R.id.profile, R.id.support, R.id.reward)
                .build();

        // Setting up the BottomNavigationView with NavController
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);
        // Initialize FloatingActionButton and set initial visibility
        View fabDirections = findViewById(R.id.fabDirections);
        fabDirections.setVisibility(View.GONE); // Initially hidden

        // Listen to navigation changes
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.map) {
                // Show FAB only when in MapsFragment
                fabDirections.setVisibility(View.VISIBLE);
            } else {
                // Hide FAB in other fragments
                fabDirections.setVisibility(View.GONE);
            }
        });
    }

    public void onFabDirectionsClicked(View view) {
        // Implement navigation to DirectionsFragment
        Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.directionsFragment);
    }
}