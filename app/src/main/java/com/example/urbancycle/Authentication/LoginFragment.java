package com.example.urbancycle.Authentication;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.urbancycle.R;
import com.example.urbancycle.Database.ConnectToDatabase;
import com.example.urbancycle.MainActivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginFragment extends Fragment implements ConnectToDatabase.DatabaseConnectionListener {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, backButton;
    private Connection databaseConnection;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        backButton = view.findViewById(R.id.BtnBackLogin);
        backButton.setOnClickListener(v->{

            if (getFragmentManager() != null) {
                getFragmentManager().popBackStack();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize EditTexts
        emailEditText = view.findViewById(R.id.LoginEmail);
        passwordEditText = view.findViewById(R.id.LoginPassword);

        // Initialize Login Button
        loginButton = view.findViewById(R.id.SignIn);
        loginButton.setOnClickListener(v -> loginUser());

        // Establish a database connection
        new ConnectToDatabase(this).execute();

        // Set up text watchers
        setupTextWatchers();
    }

    @Override
    public void onConnectionSuccess(Connection connection) {
        this.databaseConnection = connection;
    }

    @Override
    public void onConnectionFailure() {
        // Handle connection failure
        showToast("Unable to connect to the database. Please try again later.");
    }

    private void loginUser() {
        if (databaseConnection != null) {
            // Retrieve user input
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Verify user credentials
            new VerifyUserCredentialsTask(databaseConnection, email, password, new VerifyUserCredentialsTask.LoginListener() {
                @Override
                public void onLoginSuccess() {
                    // Handle login success: navigate to MainActivity
                    showToast("Login Successful, hold on...");
                    navigateToMainActivity();
                }

                @Override
                public void onLoginFailure() {
                    // Handle login failure: show error message and mark EditTexts
                    showToast("Email or Password incorrect");
                    markAsError(emailEditText, passwordEditText);
                }
            }).execute();
        } else {
            // Handle the case where the database connection is not established
            showToast("Database connection is not established. Please try again.");
        }
    }

    private void navigateToMainActivity() {
        // Logic to navigate to MainActivity
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        getActivity().finish(); // Finish the current activity

    }

    private void markAsError(EditText... editTexts) {
        for (EditText editText : editTexts) {
            editText.setError("Incorrect");
        }
    }

    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * Used to remove the error message appeared when user try for typing next inputs on email or password
     */
    private void setupTextWatchers() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed for this implementation
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Clear the error when the user starts typing
                emailEditText.setError(null);
                passwordEditText.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed for this implementation
            }
        };

        emailEditText.addTextChangedListener(textWatcher);
        passwordEditText.addTextChangedListener(textWatcher);
    }

}

class VerifyUserCredentialsTask extends AsyncTask<Void, Void, Boolean> {
    private Connection connection;
    private String email, password;
    private LoginListener listener;

    public interface LoginListener {
        void onLoginSuccess();
        void onLoginFailure();
    }

    public VerifyUserCredentialsTask(Connection connection, String email, String password, LoginListener listener) {
        this.connection = connection;
        this.email = email;
        this.password = password;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            String query = "SELECT * FROM Users WHERE Email = ? AND Password = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next(); // If the user exists, returns true
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            listener.onLoginSuccess();
        } else {
            listener.onLoginFailure();
        }
    }
}
