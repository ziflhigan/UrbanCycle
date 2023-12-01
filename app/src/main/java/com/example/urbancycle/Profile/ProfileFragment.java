package com.example.urbancycle.Profile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.preference.Preference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.urbancycle.R;
import com.example.urbancycle.databinding.FragmentProfileBinding;


public class ProfileFragment extends Fragment {
    // Declare the binding variable
    FragmentProfileBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();


         binding.History.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle History button click
                    replaceFragment(new HistoryFragment());
                }
         });
        binding.Setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Settings button click
                replaceFragment(new SettingFragment());
            }
        });
binding.Preference.setOnClickListener(new View.OnClickListener(){
    public void onClick(View v) {
        // Handle Profile button click
        replaceFragment(new PreferenceFragment());
    }
});

        return view;
    }

            private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout, fragment);
        transaction.addToBackStack(null);  // Optional: Add transaction to the back stack
        transaction.commit();
    }
}
