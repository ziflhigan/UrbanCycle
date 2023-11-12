package com.example.urbancycle.urbancycle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.example.urbancycle.R;
import com.example.urbancycle.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        replaceFragment(new MapsFragment());
        binding.bottomNavigationView.setBackground(null);
        binding.bottomNavigationView.setSelectedItemId(R.id.map);

        binding.bottomNavigationView.setOnNavigationItemSelectedListener(item -> {


            int id = item.getItemId();
            if (id == R.id.map){
                replaceFragment(new MapsFragment());
            } else if (id == R.id.community) {
                replaceFragment(new CommunityFragment());
            } else if (id == R.id.profile) {
                replaceFragment(new ProfileFragment());
            } else if (id == R.id.support) {
                replaceFragment(new SupportFragment());
            } else if (id == R.id.reward) {
                replaceFragment(new RewardFragment());
            }

            return true;
        });
    }

    private void replaceFragment(Fragment fragment){

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}