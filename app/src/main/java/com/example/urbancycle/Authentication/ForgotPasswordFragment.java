package com.example.urbancycle.Authentication;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.urbancycle.Database.ConnectToDatabase;
import com.example.urbancycle.Database.UserInfoManager;
import com.example.urbancycle.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Random;

public class ForgotPasswordFragment extends Fragment implements
        ConnectToDatabase.DatabaseConnectionListener, RetrieveOTPInformation.onRetrievedListener {
    private Connection connection;
    private EditText ETEmail, ETOTP;
    private Button BtnReset, BtnSendEmail;
    private String userEnteredOtp;
    private ProgressDialog progressDialog;

    private class SendEmailTask extends AsyncTask<Void, Void, String> {
        private String email;
        private String subject;
        private String content;

        public SendEmailTask(String email, String subject, String content) {
            this.email = email;
            this.subject = subject;
            this.content = content;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                MailUtil.sendMail(email, subject, content);
                return "Success";
            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("Success")) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "OTP sent to " + email, Toast.LENGTH_SHORT).show();
            } else {
                progressDialog.dismiss();
                showToast("Error sending email: " + result);
            }
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ETEmail = view.findViewById(R.id.ETEmailToVerify);
        ETOTP = view.findViewById(R.id.OTPVerification);
        BtnSendEmail = view.findViewById(R.id.btn_send_email);
        BtnReset = view.findViewById(R.id.btn_reset_password);

        // Initialize the progress dialog
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Sending Email, please wait...");
        progressDialog.setCancelable(false);

        new ConnectToDatabase(this).execute();

        BtnSendEmail.setOnClickListener(v -> {
            String email = ETEmail.getText().toString().trim();

            if (email.isEmpty()) {
                ETEmail.setError("Email Address Cannot Be Empty!");
            } else {
                // save the Email Address to the local class, for updating password purpose
                UserInfoManager.getInstance().setEmail(email);
                verifyUserEmail(email);
            }
        });

        BtnReset.setOnClickListener(v -> {
            String email = ETEmail.getText().toString();
            userEnteredOtp = ETOTP.getText().toString();
            // Verify OTP from the database
            // Validate Email and OTP should not be empty
            if (!email.isEmpty() && !userEnteredOtp.isEmpty()){
                new RetrieveOTPInformation(connection, this).execute(email);
            }else{
             showToast("Email or OTP cannot be empty!");
            }
        });

        setupTextWatchers();
    }

    @Override
    public void onConnectionSuccess(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void onConnectionFailure() {
        showToast("Database is not connected, try again later!");
    }

    /**
     *
     * @param minutes the valid time duration for generated OTP
     * @return the time with added time duration for OTP, to be compared letter with
     * local time and the time stored in database
     */
    public String getCurrentTimePlusMinutes(int minutes) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryTime = now.plusMinutes(minutes);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return expiryTime.format(formatter);
    }

    /**
     * @return Generate a random 5-digits number, used as OTP
     */
    public String generateOtp() {
        Random random = new Random();
        int otp = 10000 + random.nextInt(90000);
        return String.valueOf(otp);
    }

    private void showToast(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), message,
                    Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onRetrieved(RetrieveOTPInformation.OTPDetails otpDetails) {
        if (otpDetails != null && otpDetails.getExpiryTime() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(otpDetails.getExpiryTime())) {
                // OTP is valid
                // validate the OTP entered by the user
                if (otpDetails.getOtp().equals(userEnteredOtp)){
                    // Once being used, delete the OTP
                    navigateToResetPasswordF();
                }else{
                    showToast("Incorrect OTP!");
                }

            } else {
                // OTP has expired
                showToast("The OTP has expired. Please request a new one.");
            }
        } else {
            // Handle null (e.g., OTP not found or an error occurred)
            showToast("Error retrieving OTP. Please try again.");
        }
    }

    public void navigateToResetPasswordF(){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, new ResetPasswordFragment())
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
                ETEmail.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed for this implementation
            }
        };

        ETEmail.addTextChangedListener(textWatcher);
    }

    private void verifyUserEmail(String email) {
        new VerifyUserEmail(email, connection, new VerifyUserEmail.onVerifyListener() {
            @Override
            public void onVerifiedSuccess() {
                // Email is verified, continue with OTP generation and sending
                proceedAfterVerification(email);
            }

            @Override
            public void onVerifiedFailure() {
                ETEmail.setError("Invalid Email Address!");
            }
        }).execute();
    }

    private void proceedAfterVerification(String email) {
        progressDialog.show();
        String otp = generateOtp();
        String expiryTime = getCurrentTimePlusMinutes(2);

        new InsertOTPInformation(connection, email, otp, expiryTime).execute();

        String subject = "UrbanCycle App: Your OTP for Password Reset";
        String content = "Your OTP is: " + otp + ". It is valid for 2 minutes.";

        new SendEmailTask(email, subject, content).execute();
    }

}

class InsertOTPInformation extends AsyncTask<Void, Void, Boolean>{
    private final Connection connection;
    private final String Email, OTP, ExpiryTime;

    InsertOTPInformation(Connection connection, String Email, String OTP, String ExpiryTime){
        this.connection = connection;
        this.Email = Email;
        this.OTP = OTP;
        this.ExpiryTime = ExpiryTime;
    }
    @Override
    protected Boolean doInBackground(Void... voids) {

        try {
            String insertQuery = "INSERT INTO ResetOTP (Email, OTP, ExpiryTime) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
            preparedStatement.setString(1, Email);
            preparedStatement.setString(2, OTP);
            preparedStatement.setString(3, ExpiryTime);

            int result = preparedStatement.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}

class RetrieveOTPInformation extends AsyncTask<String, Void, RetrieveOTPInformation.OTPDetails> {

    private final Connection connection;
    private final onRetrievedListener listener;
    public interface onRetrievedListener {
        void onRetrieved(OTPDetails otpDetails);
    }

    public RetrieveOTPInformation(Connection connection, onRetrievedListener listener) {
        this.connection = connection;
        this.listener = listener;
    }

    @Override
    protected OTPDetails doInBackground(String... params) {
        if (params.length != 1) {
            return null; // Expecting one parameter: the email
        }

        String email = params[0];
        try {
            String query = "SELECT OTP, ExpiryTime FROM ResetOTP WHERE Email = ? ORDER " +
                    "BY ExpiryTime DESC LIMIT 1";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String otp = resultSet.getString("OTP");
                String expiryTime = resultSet.getString("ExpiryTime");
                return new OTPDetails(otp, expiryTime);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // OTP not found or an error occurred
    }

    public static class OTPDetails {
        public final String otp;
        private final String expiryTimeString;
        private LocalDateTime expiryTime;

        public OTPDetails(String otp, String expiryTime) {
            this.otp = otp;
            this.expiryTimeString = expiryTime;
            parseExpiryTime();
        }

        private void parseExpiryTime() {
            try {
                DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                        .appendPattern("yyyy-MM-dd HH:mm:ss")
                        .optionalStart()
                        .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9,
                                true)
                        .optionalEnd()
                        .toFormatter();

                this.expiryTime = LocalDateTime.parse(expiryTimeString, formatter);
            } catch (DateTimeParseException e) {
                e.printStackTrace();
                this.expiryTime = null;
            }
        }


        public LocalDateTime getExpiryTime() {
            return expiryTime;
        }

        public String getOtp() {
            return otp;
        }
    }


    @Override
    protected void onPostExecute(OTPDetails otpDetails) {
        listener.onRetrieved(otpDetails);
    }

}

class VerifyUserEmail extends AsyncTask<Void, Void, Boolean>{

    private String userEmail;
    private final Connection connection;
    private final onVerifyListener listener;
    public interface onVerifyListener{
        void onVerifiedSuccess();
        void onVerifiedFailure();
    }
    public VerifyUserEmail(String userEmail, Connection connection, onVerifyListener listener){
        this.userEmail = userEmail;
        this.connection = connection;
        this.listener = listener;
    }
    @Override
    protected Boolean doInBackground(Void... voids) {
        try{
            String query = "SELECT Email FROM Users WHERE Email = ?";
            PreparedStatement verifyStmt = connection.prepareStatement(query);
            verifyStmt.setString(1, userEmail);
            ResultSet resultSet = verifyStmt.executeQuery();

            return resultSet.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {

        if (aBoolean){
            listener.onVerifiedSuccess();
        }else{
            listener.onVerifiedFailure();
        }
    }
}