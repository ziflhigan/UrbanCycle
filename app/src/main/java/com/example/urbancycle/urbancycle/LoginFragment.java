package com.example.urbancycle.urbancycle;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.urbancycle.R;

import java.sql.Connection;

public class LoginFragment extends Fragment implements ConnectToDatabase.DatabaseConnectionListener{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);

    }


    @Override
    public void onConnectionSuccess(Connection connection) {

    }

    @Override
    public void onConnectionFailure() {

    }
}