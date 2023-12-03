package com.example.urbancycle.SupportAndFeedback;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.example.urbancycle.Database.ConnectToDatabase;
import com.example.urbancycle.Database.UserInfoManager;
import com.example.urbancycle.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Rating extends Fragment implements ConnectToDatabase.DatabaseConnectionListener, InsertUserRating.OnRatingInsertCompleteListener{
    private Connection databaseConnection;
    private RatingBar UI,Functionality,Stability;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rating, container, false);
    }

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState){
        Button submit=view.findViewById(R.id.SubmitRatingB);
        new ConnectToDatabase(this).execute();

        View.OnClickListener onSubmit=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UI = view.findViewById(R.id.UIRB);
                Functionality = view.findViewById(R.id.functionalityRB);
                Stability = view.findViewById(R.id.stabilityRB);
                if(InsertUserRating.UserEmail==null)
                    Toast.makeText(getActivity(), "please login", Toast.LENGTH_SHORT).show();
                else if(UI.getRating()==0|Functionality.getRating()==0|Stability.getRating()==0)
                    Toast.makeText(getContext(),"please rate",Toast.LENGTH_LONG).show();
                else{
                    sendRating();
                }
            }
        };
        submit.setOnClickListener(onSubmit);
    }

    public void sendRating() {
        if (databaseConnection != null) {
            new InsertUserRating(databaseConnection, UI.getRating(),Functionality.getRating(),Stability.getRating(), (InsertUserRating.OnRatingInsertCompleteListener) this).execute();
        } else {
            Toast.makeText(getContext(),"database disconnected 1",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionSuccess(Connection connection) {
        this.databaseConnection = connection;
        //Toast.makeText(getContext(),"database connected",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailure() {
        Toast.makeText(getContext(),"database disconnected 3",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRatingInsertComplete(boolean success) {
        if (success) {
            Toast.makeText(getActivity(), "Feedback received", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(),"submit failed",Toast.LENGTH_LONG).show();
        }
    }
}

class InsertUserRating extends AsyncTask<Void, Void, Boolean> {
    static String UserEmail = UserInfoManager.getInstance().getEmail();
    private final Connection connection;
    private final double UI,functionality,stability;
    private OnRatingInsertCompleteListener listener;
    public interface OnRatingInsertCompleteListener {
        void onRatingInsertComplete(boolean success);
    }
    public InsertUserRating(Connection connection,double UI,double functionality,double stability, OnRatingInsertCompleteListener listener) {
        this.connection = connection;
        this.UI=UI;
        this.functionality=functionality;
        this.stability=stability;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            String insertQuery = "INSERT INTO Rating (Email, UIRating, FunctionalityRating, StabilityRating) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
            preparedStatement.setString(1, UserEmail);
            preparedStatement.setDouble(2, UI);
            preparedStatement.setDouble(3, functionality);
            preparedStatement.setDouble(4, stability);
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
            listener.onRatingInsertComplete(success);
        }
    }
}