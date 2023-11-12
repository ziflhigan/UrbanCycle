package com.example.urbancycle.urbancycle;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.urbancycle.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegisterFragment extends Fragment implements ConnectToDatabase.DatabaseConnectionListener, InsertUserDataTask.OnRegistrationCompleteListener {

    private EditText userNameEditText, firstNameEditText, lastNameEditText, emailEditText, passwordEditText;
    private Connection databaseConnection;
    private Button backButton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_register, container, false);
        backButton = view.findViewById(R.id.BtnBackRegister);
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
        userNameEditText = view.findViewById(R.id.ETUserName);
        firstNameEditText = view.findViewById(R.id.ETFirstName);
        lastNameEditText = view.findViewById(R.id.ETLastName);
        emailEditText = view.findViewById(R.id.ETEmailAddress);
        passwordEditText = view.findViewById(R.id.ETPassword);

        // Initialize SignUp Button
        Button signUpButton = view.findViewById(R.id.BtnSignUp);
        signUpButton.setOnClickListener(v -> registerUser());

        // Establish a database connection
        new ConnectToDatabase(this).execute();
    }

    @Override
    public void onConnectionSuccess(Connection connection) {
        // Save the connection to use later
        this.databaseConnection = connection;
        showToast("Database connection successful");
    }

    @Override
    public void onConnectionFailure() {
        showToast("Failed to connect to database");
    }

    private void registerUser() {
        if (databaseConnection != null) {
            // Retrieve user input
            String userName = userNameEditText.getText().toString().trim();
            String firstName = firstNameEditText.getText().toString().trim();
            String lastName = lastNameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Insert user data into database
            // This should be done in a separate AsyncTask to avoid network operations on the main thread
            new InsertUserDataTask(databaseConnection, userName, firstName, lastName, email, password, (InsertUserDataTask.OnRegistrationCompleteListener) this).execute();
        } else {
            // Handle the case where the database connection is not established
        }
    }

    @Override
    public void onRegistrationComplete(boolean success) {
        if (success) {
            navigateToLoginFragment();
        } else {
            // Handle registration failure
        }
    }
    private void navigateToLoginFragment() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment())
                .addToBackStack(null)
                .commit();
    }

    private void showToast(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show());
        }
    }

}

class InsertUserDataTask extends AsyncTask<Void, Void, Boolean> {
    private final Connection connection;
    private final String userName, firstName, lastName, email, password;

    public interface OnRegistrationCompleteListener {
        void onRegistrationComplete(boolean success);
    }
    private OnRegistrationCompleteListener listener;

    public InsertUserDataTask(Connection connection, String userName, String firstName, String lastName, String email, String password, OnRegistrationCompleteListener listener) {
        this.connection = connection;
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            String insertQuery = "INSERT INTO Users (UserName, FirstName, LastName, Email, Password) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, firstName);
            preparedStatement.setString(3, lastName);
            preparedStatement.setString(4, email);
            preparedStatement.setString(5, password); // Ideally, hash the password before storing

            int result = preparedStatement.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        listener.onRegistrationComplete(success);
    }
}
