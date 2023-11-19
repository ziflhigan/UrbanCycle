package com.example.urbancycle.Database;

import android.widget.Toast;

import java.sql.Connection;

public class GeneralConnection implements ConnectToDatabase.DatabaseConnectionListener{
    private Connection connection;

    public GeneralConnection() {
        new ConnectToDatabase(this).execute();
    }

    @Override
    public void onConnectionSuccess(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void onConnectionFailure() {

    }
}
