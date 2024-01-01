package com.example.urbancycle.Profile;

import android.os.AsyncTask;

import com.example.urbancycle.Database.UserInfoManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

public class RetrieveSavingHistory extends AsyncTask<Void, Void, RetrieveSavingHistory.CarbonSavingHistory> {
    private final Connection connection;
    private final String userEmail = UserInfoManager.getInstance().getEmail();
    private final onRetrievedHistoryListener listener;
    public interface onRetrievedHistoryListener{
        // It passes the retrieved CarbonSaving History when implemented in other class
        void onRetrieved(CarbonSavingHistory carbonSavingHistory);
    }
    public RetrieveSavingHistory(Connection connection, onRetrievedHistoryListener listener){
        this.connection = connection;
        this.listener = listener;
    }

    @Override
    protected CarbonSavingHistory doInBackground(Void... voids) {
        CarbonSavingHistory carbonSavingHistory = new CarbonSavingHistory();

        try {
            String query = "SELECT CarbonSavings, Date FROM Routes WHERE Email = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, userEmail);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                double carbonSavings = resultSet.getDouble("CarbonSavings");
                String date = resultSet.getString("Date");
                carbonSavingHistory.addCarbonSaving(carbonSavings, date);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return carbonSavingHistory;
    }

    @Override
    protected void onPostExecute(CarbonSavingHistory carbonSavingHistory) {
        listener.onRetrieved(carbonSavingHistory);
    }

    public static class CarbonSavingHistory {
        private final List<Double> carbonSavingsList;
        private final List<LocalDateTime> routeDateList;

        public CarbonSavingHistory() {
            this.carbonSavingsList = new ArrayList<>();
            this.routeDateList = new ArrayList<>();
        }

        public void addCarbonSaving(double carbonSavings, String dateString) {
            this.carbonSavingsList.add(carbonSavings);
            this.routeDateList.add(parseDateStringToLocalDateTime(dateString));
        }

        private LocalDateTime parseDateStringToLocalDateTime(String dateString) {
            try {
                DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                        .appendPattern("yyyy-MM-dd HH:mm:ss")
                        .optionalStart()
                        .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                        .optionalEnd()
                        .toFormatter();

                return LocalDateTime.parse(dateString, formatter);
            } catch (DateTimeParseException e) {
                e.printStackTrace();
                return null;
            }
        }

        public List<Double> getCarbonSavingsList() {
            return carbonSavingsList;
        }

        public List<LocalDateTime> getRouteDateList() {
            return routeDateList;
        }
    }

}
