package com.example.urbancycle.SupportAndFeedback;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.urbancycle.Database.ConnectToDatabase;
import com.example.urbancycle.Database.UserInfoManager;
import com.example.urbancycle.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Feedback extends Fragment implements ConnectToDatabase.DatabaseConnectionListener, InsertUserFeedback.OnFeedbackInsertCompleteListener{
    private Connection databaseConnection;
    private EditText feedback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feedback, container, false);
    }

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState){
        Button submit=view.findViewById(R.id.SubmitFeedbackB);
        new ConnectToDatabase(this).execute();

        View.OnClickListener onSubmit=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feedback = view.findViewById(R.id.feedbackET);
                if(InsertUserFeedback.UserEmail==null)
                    showToast("Please login");
                else if(feedback.getText().toString().equals(""))
                    showToast("Please write feedback!");
                else{
                    sendFeedback();
                }
            }
        };
        submit.setOnClickListener(onSubmit);
    }

    public void sendFeedback() {
        if (databaseConnection != null) {
            String FeedbackText = feedback.getText().toString().trim();
            new InsertUserFeedback(databaseConnection, FeedbackText, (InsertUserFeedback.OnFeedbackInsertCompleteListener) this).execute();
        } else {
            showToast("Database Disconnected!");
        }
    }

    @Override
    public void onConnectionSuccess(Connection connection) {
        this.databaseConnection = connection;
    }

    private void showToast(String message) {
        if (isAdded()) { // Check if Fragment is currently added to its Activity
            Context context = getActivity();
            if (context != null) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onConnectionFailure() {
        showToast("Database Disconnected");
    }

    @Override
    public void onFeedbackInsertComplete(boolean success) {
        if (success) {
            showToast("Feedback Received!");
            feedback.setText(""); // Clear the EditText field
        } else {
            showToast("Submit Failed!");
        }
    }
}

class InsertUserFeedback extends AsyncTask<Void, Void, Boolean> {
    static String UserEmail = UserInfoManager.getInstance().getEmail();
    private final Connection connection;
    private final String Feedback;

    private OnFeedbackInsertCompleteListener listener;

    public interface OnFeedbackInsertCompleteListener {
        void onFeedbackInsertComplete(boolean success);
    }
    public InsertUserFeedback(Connection connection,String Feedback, OnFeedbackInsertCompleteListener listener) {
        this.connection = connection;
        this.Feedback = Feedback;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            String insertQuery = "INSERT INTO Feedback (Email, Comment) VALUES (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
            preparedStatement.setString(1, UserEmail);
            preparedStatement.setString(2, Feedback);

            int result = preparedStatement.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (listener != null) {
            listener.onFeedbackInsertComplete(success);
        }
    }
}
