package com.example.urbancycle.SupportAndFeedback;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.urbancycle.Database.ConnectToDatabase;
import com.example.urbancycle.Database.UserInfoManager;
import com.example.urbancycle.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Feedback extends Fragment implements ConnectToDatabase.DatabaseConnectionListener{
    private Connection databaseConnection;
    private EditText feedback;

    public Feedback() {
        // Required empty public constructor
    }

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
        Button submit=view.findViewById(R.id.submitB);
        View.OnClickListener onSubmit=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feedback = view.findViewById(R.id.feedbackET);

            }
        };
    }

    public void sendFeedback(){
        if (databaseConnection != null){

            String Feedback = feedback.getText().toString().trim();
            new InsertUserFeedback(databaseConnection, Feedback);
        }
        else{
            // Handle the case when database connection unsuccessful
        }

    }

    @Override
    public void onConnectionSuccess(Connection connection) {
        this.databaseConnection = connection;
    }

    @Override
    public void onConnectionFailure() {

    }
}

class InsertUserFeedback extends AsyncTask<Void, Void, Boolean> {
    private final String UserEmail = UserInfoManager.getInstance().getEmail();
    private final Connection connection;
    private final String Feedback;

    public interface OnRegistrationCompleteListener {
        void onRegistrationComplete(boolean success);
    }
    public InsertUserFeedback(Connection connection,String Feedback) {
        this.connection = connection;
        this.Feedback = Feedback;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            String insertQuery = "INSERT INTO Feedback WHERE Email = ? ";
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
            preparedStatement.setString(1, UserEmail);

            int result = preparedStatement.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
    }
}
