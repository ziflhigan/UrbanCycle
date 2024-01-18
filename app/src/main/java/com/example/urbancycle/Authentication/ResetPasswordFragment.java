package com.example.urbancycle.Authentication;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.urbancycle.Database.ConnectToDatabase;
import com.example.urbancycle.Database.UserInfoManager;
import com.example.urbancycle.R;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ResetPasswordFragment extends Fragment implements ConnectToDatabase.DatabaseConnectionListener, UpdateUserPassword.onUpdatedSuccessfulListener {
    private Connection connection;
    private TextView requirement1, requirement2, requirement3;
    private Button BtnSubmit;
    private EditText ETNewPassword;
    private ProgressDialog progressDialog;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reset_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new ConnectToDatabase(this).execute();

        requirement1 = view.findViewById(R.id.passwordRequirement1);
        requirement2 = view.findViewById(R.id.passwordRequirement2);
        requirement3 = view.findViewById(R.id.passwordRequirement3);

        BtnSubmit = view.findViewById(R.id.BtnSubmit);

        ETNewPassword = view.findViewById(R.id.ETNewPassword);

        // Initialize the progress dialog
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Updating...");
        progressDialog.setCancelable(false);

        ETNewPassword.addTextChangedListener(new TextWatcher() {
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

        BtnSubmit.setOnClickListener(v->{

            if (arePasswordRequirementsMet()){
                progressDialog.show();
                String password = ETNewPassword.getText().toString().trim();
                // Hash the password
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

                // Update the password
                new UpdateUserPassword(connection, hashedPassword, this).execute();
            }else{
                ETNewPassword.setError("password Pattern didn't follow the requirements!");
            }
        });

        setupTextWatchers();
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

    private boolean arePasswordRequirementsMet() {
        String password = ETNewPassword.getText().toString();

        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasMinimumLength = password.length() >= 6;
        boolean hasNumber = password.matches(".*\\d.*");

        return hasUppercase && hasMinimumLength && hasNumber;
    }

    @Override
    public void onConnectionSuccess(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void onConnectionFailure() {
        showToast("Database Connection Is Not Established! Please try later");
    }

    private void showToast(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onUpdated() {

        String userEmail = UserInfoManager.getInstance().getEmail();
        new DeleteOTPInfo(connection, userEmail, new DeleteOTPInfo.onDeleteSuccessListener() {
            @Override
            public void onDeleteSuccess() {
                progressDialog.dismiss();
            }

            @Override
            public void onDeleteFailure(String message) {
                progressDialog.dismiss();
                showToast("Failed to delete OTP record: " + message);
            }
        }).execute();

        navigateToLoginFragment();
    }

    @Override
    public void onUpdateFailed(String message) {
        showToast("Update failed: " + message);
    }

    private void navigateToLoginFragment(){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment())
                .commit();
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
                ETNewPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed for this implementation
            }
        };

        ETNewPassword.addTextChangedListener(textWatcher);
    }

}

class UpdateUserPassword extends AsyncTask<Void, Void, Boolean> {
    private final Connection connection;
    private final String userEmail = UserInfoManager.getInstance().getEmail();
    private final String hashedPassword;
    private final onUpdatedSuccessfulListener listener;
    private String errorMessage = ""; // To store the error message

    public interface onUpdatedSuccessfulListener {
        void onUpdated();
        void onUpdateFailed(String message); // New method to handle update failure
    }

    public UpdateUserPassword(Connection connection, String hashedPassword,
                              onUpdatedSuccessfulListener listener){
        this.connection = connection;
        this.hashedPassword = hashedPassword;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        String query = "UPDATE Users SET Password = ? WHERE Email = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, hashedPassword);
            preparedStatement.setString(2, userEmail);

            int updatedRows = preparedStatement.executeUpdate();
            return updatedRows > 0;

        } catch (SQLException e) {
            Log.e("UpdateUserPassword", "SQL Error: ", e);
            errorMessage = e.getMessage(); // Store the error message
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (aBoolean){
            listener.onUpdated();
        } else {
            listener.onUpdateFailed(errorMessage); // Pass the error message to the listener
        }
    }
}
class DeleteOTPInfo extends AsyncTask<Void, Void, Boolean> {
    private final Connection connection;
    private final String userEmail;
    private String errorMessage = "";
    private final onDeleteSuccessListener listener;

    public interface onDeleteSuccessListener {
        void onDeleteSuccess();

        void onDeleteFailure(String message);
    }

    public DeleteOTPInfo(Connection connection, String userEmail,
                         onDeleteSuccessListener listener) {
        this.connection = connection;
        this.userEmail = userEmail;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        String query = "DELETE FROM ResetOTP WHERE Email = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, userEmail);

            int deletedRows = preparedStatement.executeUpdate();
            return deletedRows > 0;

        } catch (SQLException e) {
            Log.e("DeleteUserInfo", "SQL Error: ", e);
            errorMessage = e.getMessage(); // Store the error message
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (aBoolean) {
            listener.onDeleteSuccess();
        } else {
            listener.onDeleteFailure(errorMessage);
        }
    }
}
