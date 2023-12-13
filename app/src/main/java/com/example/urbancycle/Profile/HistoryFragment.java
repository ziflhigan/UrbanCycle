package com.example.urbancycle.Profile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.urbancycle.Database.ConnectToDatabase;
import com.example.urbancycle.R;


import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment implements ConnectToDatabase.DatabaseConnectionListener, RetrieveSavingHistory.onRetrievedHistoryListener{

    private Connection connection;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    private void showToast(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onConnectionSuccess(Connection connection) {
        this.connection = connection;
        showToast("Database Connection Successful!");
        new RetrieveSavingHistory(connection, this).execute();
    }

    @Override
    public void onConnectionFailure() {

    }

    /*
    @天皇，The method returns a list of CarbonSavings and a list of Date retrieved from the database
     */
    @Override
    public void onRetrieved(RetrieveSavingHistory.CarbonSavingHistory carbonSavingHistory) {
        List<Double> carbonSavingsList = carbonSavingHistory.getCarbonSavingsList();
        List<LocalDateTime> routeDateList = carbonSavingHistory.getRouteDateList();

    }
}