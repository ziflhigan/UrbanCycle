package com.example.urbancycle.urbancycle;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.urbancycle.R;

import java.sql.Connection;

public class LoginFragment extends Fragment implements ConnectToDatabase.DatabaseConnectionListener{

    private Button backButton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        backButton = view.findViewById(R.id.BtnBackLogin);
        backButton.setOnClickListener(v->{

            if (getFragmentManager() != null) {
                getFragmentManager().popBackStack();
            }
        });

        // Inflate the layout for this fragment
        return view;
    }


    @Override
    public void onConnectionSuccess(Connection connection) {

    }

    @Override
    public void onConnectionFailure() {

    }
}