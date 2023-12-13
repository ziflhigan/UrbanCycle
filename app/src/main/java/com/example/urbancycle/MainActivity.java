package com.example.urbancycle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.urbancycle.Profile.PreferenceFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;



import com.example.urbancycle.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

public class   MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    ActivityMainBinding binding;
    private AppBarConfiguration appBarConfiguration;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setting up the NavController
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.TopActionBar);
        setSupportActionBar(toolbar);

        // Initialize Drawer Layout
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Setting up AppBarConfiguration
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.map, R.id.community, R.id.profile, R.id.support, R.id.reward)
                .setDrawerLayout(drawerLayout)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

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

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.side_menu, menu);
        return true;
    }
}