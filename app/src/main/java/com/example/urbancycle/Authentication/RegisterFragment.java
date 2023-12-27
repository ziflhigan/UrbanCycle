package com.example.urbancycle.Authentication;

import android.graphics.Color;
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
import android.widget.TextView;
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
    private TextView requirement1, requirement2, requirement3;

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
        requirement1 = view.findViewById(R.id.passwordRequirement1);
        requirement2 = view.findViewById(R.id.passwordRequirement2);
        requirement3 = view.findViewById(R.id.passwordRequirement3);
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Check password strength on text change
                checkPasswordStrength(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not used here
            }
        });

        // Initialize SignUp Button
        Button signUpButton = view.findViewById(R.id.BtnSignUp);
        signUpButton.setOnClickListener(View -> {

            if (isValidEmailAddressPattern(emailEditText) && isValidUserName(userNameEditText))
                registerUser();
            }
        );

        // Establish a database connection
        new ConnectToDatabase(this).execute();

        // To clear the errors when user try to type again
        setupTextWatchers();
    }

    @Override
    public void onConnectionSuccess(Connection connection) {
        this.databaseConnection = connection;
    }

    @Override
    public void onConnectionFailure() {
        showToast("Failed to connect to database");
    }

    private void registerUser() {
        if (databaseConnection != null) {
            // Checks whether typed password match the requirements or not
            if (arePasswordRequirementsMet()){
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
            }else{
                passwordEditText.setError("Password Does Not Meet Requirements!");
            }
        } else {
            showToast("Database connection is not established");
        }
    }

    /**
     * This could be the first way to implement the listener interface
     * The other class receives the listener as a parameter that links to its corresponding method
     */
    @Override
    public void onRegistrationComplete(boolean success) {
        if (!success) {
            emailEditText.setError("Email Already Exist!");
        } else {
            showToast("Registration Successful!");
            navigateToLoginFragment();
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

    private void checkPasswordStrength(String password) {
        // Check for uppercase character
        if (password.matches(".*[A-Z].*")) {
            requirement1.setTextColor(Color.GREEN);
        } else {
            requirement1.setTextColor(Color.RED);
        }

        // Check for password length
        if (password.length() >= 6) {
            requirement2.setTextColor(Color.GREEN);
        } else {
            requirement2.setTextColor(Color.RED);
        }

        // Check for at least one number
        if (password.matches(".*\\d.*")) {
            requirement3.setTextColor(Color.GREEN);
        } else {
            requirement3.setTextColor(Color.RED);
        }
    }

    private boolean isValidEmailAddressPattern(EditText emailEditText){

        String emailInput = emailEditText.getText().toString();

        if (!emailInput.isEmpty()&& Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()){
//            showToast("Email Validated Successfully!");
            return true;
        }
        emailEditText.setError("Invalid Email Input!");
        return false;
    }

    private boolean isValidUserName (EditText userNameEditText){
        if (userNameEditText.getText().toString().isEmpty()){
            userNameEditText.setError("Username cannot be empty!");
            return false;
        }
        return true;
    }

    private boolean arePasswordRequirementsMet() {
        String password = passwordEditText.getText().toString();

        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasMinimumLength = password.length() >= 6;
        boolean hasNumber = password.matches(".*\\d.*");

        return hasUppercase && hasMinimumLength && hasNumber;
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
            if (e.getSQLState().startsWith("23")) { // Check if it's a constraint violation
                return false; // Indicates email already registered
            }
            e.printStackTrace();
            return null; // Indicates a different SQL error
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        listener.onRegistrationComplete(success);
    }
}