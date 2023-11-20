package com.example.urbancycle.Authentication;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.urbancycle.R;
import com.example.urbancycle.Database.ConnectToDatabase;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegisterFragment extends Fragment implements ConnectToDatabase.DatabaseConnectionListener, InsertUserDataTask.OnRegistrationCompleteListener {

    private EditText userNameEditText, firstNameEditText, lastNameEditText, emailEditText, passwordEditText;
    private Connection databaseConnection;

    // Implement the listener interface of the class CheckEmailRegisteredTask
    // I have figure out 2 ways to implement the listener interface defined in other class, this is the second way
    private final CheckEmailRegisteredTask.EmailCheckListener emailCheckListener = new CheckEmailRegisteredTask.EmailCheckListener() {
        @Override
        public void onEmailChecked(boolean isRegistered) {
            if (isRegistered) {
                showToast("Email already registered.");
            } else {

                // Verify whether valid user name


                // Verify whether strong password or not


                // Proceed with registration
                registerUser();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_register, container, false);
        Button backButton = view.findViewById(R.id.BtnBackRegister);
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
        signUpButton.setOnClickListener(View -> {
                checkEmailBeforeRegister(emailEditText);
            }
        );

        // Establish a database connection
        new ConnectToDatabase(this).execute();

        // To clear the errors when user try to type again
        setupTextWatchers();
    }

    @Override
    public void onConnectionSuccess(Connection connection) {
        // Save the connection to use later
        this.databaseConnection = connection;
        showToast("Database connection successful");
    }

    @Override
    public void onConnectionFailure() {
        // To Do: Handle the case when cannot connect database, either let user check Internet Connection, or saying the database server is under maintenance
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

            // Hash the password
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            // Insert user data into database
            new InsertUserDataTask(databaseConnection, userName, firstName, lastName, email, hashedPassword, (InsertUserDataTask.OnRegistrationCompleteListener) this).execute();
        } else {
            // Handle the case where the database connection is not established
        }
    }

    /**
     * This could be the first way to implement the listener interface
     * The other class receives the listener as a parameter that links to its corresponding method
     */
    @Override
    public void onRegistrationComplete(boolean success) {
        if (success) {
            String message = "Registration Successful!";
            Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
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

    private boolean isValidEmailAddressPattern(EditText emailEditText){

        String emailInput = emailEditText.getText().toString();

        if (!emailInput.isEmpty()&& Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()){

            showToast("Email Validated Successfully!");
            return true;
        }
        emailEditText.setError("Invalid Email Input!");
        return false;
    }

    private void checkEmailBeforeRegister(EditText emailEditText) {
        String email = emailEditText.getText().toString().trim();

        // Validate email
        if (!isValidEmailAddressPattern(emailEditText)) {
            return;
        }

        // Check if email is already registered
        new CheckEmailRegisteredTask(databaseConnection, emailCheckListener).execute(email);
    }

    private boolean isValidUserName (EditText userNameEditText){

        return true;
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
            preparedStatement.setString(5, password);

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

/**
 * This class will query the database with the input email address
 * If the database returns a row, the boolean that it will pass is true, which means email already in use
 * Otherwise false
 */
class CheckEmailRegisteredTask extends AsyncTask<String, Void, Boolean> {
    private Connection connection;
    private EmailCheckListener listener;

    public interface EmailCheckListener {
        void onEmailChecked(boolean isRegistered);
    }

    public CheckEmailRegisteredTask(Connection connection, EmailCheckListener listener) {
        this.connection = connection;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String email = params[0];
        try {
            String checkQuery = "SELECT * FROM Users WHERE Email = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(checkQuery);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean isRegistered) {
        listener.onEmailChecked(isRegistered);
    }
}