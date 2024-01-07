package com.example.urbancycle.Profile;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.urbancycle.Database.ConnectToDatabase;
import com.example.urbancycle.R;


import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistoryFragment extends Fragment implements ConnectToDatabase.DatabaseConnectionListener, RetrieveSavingHistory.onRetrievedHistoryListener {

    private Connection connection;
    private ArrayList<TextView> dailyamount = new ArrayList<>();
    private ArrayList<TextView> startLocation = new ArrayList<>();
    private ArrayList<TextView> EndLocation = new ArrayList<>();
    private ArrayList<TextView> Dates = new ArrayList<>();
    private TableLayout tableLayout;
    private Button total;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Initialize database connection
        new ConnectToDatabase(this).execute();

        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
      tableLayout = view.findViewById(R.id.myTableLayout);
      total = view.findViewById(R.id.btnCalculateTotalAmount);

      for(int i = 1; i <= 6; i++) {
          Dates.add(view.findViewById(getResources().getIdentifier("Date" + i, "id", requireActivity().getPackageName())));
          startLocation.add(view.findViewById(getResources().getIdentifier("StartLocation" + i, "id", requireActivity().getPackageName())));
          EndLocation.add(view.findViewById(getResources().getIdentifier("EndLocation" + i, "id", requireActivity().getPackageName())));
          dailyamount.add(view.findViewById(getResources().getIdentifier("Dailyamount" + i, "id", requireActivity().getPackageName())));
      }

        total = view.findViewById(R.id.btnCalculateTotalAmount);
        total.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double totalAmount = 0;

                System.out.println( totalAmount);
            }
        });
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


    //The method returns a list of CarbonSavings and a list of Date retrieved from the database

    @Override
    public void onRetrieved(RetrieveSavingHistory.CarbonSavingHistory carbonSavingHistory) {

        List<Double> carbonSavingsList = carbonSavingHistory.getCarbonSavingsList();
        List<LocalDateTime> routeDateList = carbonSavingHistory.getRouteDateList();
        List<String> startLocationList = carbonSavingHistory.getStartLocation();  // Add this line
        List<String> endLocationList = carbonSavingHistory.getEndLocation();  // Add this line


        for (int i = 0; i < carbonSavingsList.size(); i++) {
            double carbonSaving = carbonSavingsList.get(i);
            LocalDateTime routeDate = routeDateList.get(i);
            String startLocation = startLocationList.get(i);  // Add this line
            String endLocation = endLocationList.get(i);  // Add this line
            TableRow tableRow = new TableRow(getContext());

            TextView StartLo = new TextView(getContext());
            TextView EndLo =new TextView(getContext());
            TextView dateTextView = new TextView(getContext());
            TextView dailyAmountTextView = new TextView(getContext());

            StartLo.setText(startLocation);  // Set startLocation
            EndLo.setText(endLocation);;  // Set endLocation
            dateTextView.setText(routeDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            dailyAmountTextView.setText(String.valueOf(carbonSaving));

            tableRow.addView(StartLo);
            tableRow.addView(EndLo);
            tableRow.addView(dateTextView);
            tableRow.addView(dailyAmountTextView);
            tableLayout.addView(tableRow);
        }

    }
}