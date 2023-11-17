package com.example.urbancycle.Database;

import android.os.AsyncTask;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectToDatabase extends AsyncTask<Void, Void, Connection> {
    private static final String DATABASE_URL = "jdbc:mysql://database-urbancycle.cdcxeeb7yt2i.ap-southeast-2.rds.amazonaws.com:3306/UrbanCycle";
    private static final String USER = "Fang";
    private static final String PASSWORD = "20030330";

    // This interface can be used to implement callback methods
    public interface DatabaseConnectionListener {
        void onConnectionSuccess(Connection connection);
        void onConnectionFailure();
    }

    private DatabaseConnectionListener listener;

    public ConnectToDatabase(DatabaseConnectionListener listener) {
        this.listener = listener;
    }

    @Override
    protected Connection doInBackground(Void... voids) {
        try {
            // Attempting database connection
            return DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    protected void onPostExecute(Connection connection) {
        if (isCancelled()) {
            // Close the connection if it's not null
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return;
        }

        // Notify listener based on connection status
        if (connection != null) {
            listener.onConnectionSuccess(connection);
        } else {
            listener.onConnectionFailure();
        }
    }
    @Override
    protected void onCancelled() {
        if (listener != null) {
            listener.onConnectionFailure();
        }
    }
}
