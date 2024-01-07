package com.example.urbancycle.Profile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.urbancycle.R;
import com.example.urbancycle.SupportAndFeedback.SupportFragment;
import com.example.urbancycle.databinding.FragmentProfileBinding;

import kotlinx.coroutines.FlowPreview;

/**
 * A simple {@link Fragment} subclass.

 * create an instance of this fragment.
 */
public class SettingFragment extends Fragment {

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button FAQ = view.findViewById(R.id.Support);
        Button logout=view.findViewById(R.id.Logout);
        FAQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SettingFragment.this)
                        .navigate(R.id.action_settingFragment_to_support);
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavController navController = NavHostFragment.findNavController(SettingFragment.this);

                // Navigate to the mainActivity_Authentication fragment
                navController.navigate(R.id.action_settingFragment_to_mainActivity_Authentication);

                // Remove the SettingFragment from the back stack
                navController.popBackStack(null, false);
            }
        });
    } @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }
}