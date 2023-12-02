package com.example.urbancycle.Rewards;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.sql.Connection;

import com.example.urbancycle.Database.ConnectToDatabase;
import com.example.urbancycle.Database.UserInfoManager;
import com.example.urbancycle.R;

import org.w3c.dom.Text;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RewardFragment extends Fragment implements ConnectToDatabase.DatabaseConnectionListener,
        RetrieveRewardsInformation.RewardsDataListener, RetrieveUserPoints.UserPointsListener {

    private double userPoints;

    private String userName=UserInfoManager.getInstance().getUserName();

    //Define all TextView ArrayList
    ArrayList<TextView> RnameList = new ArrayList<>();
    ArrayList<TextView> RdescriptionList = new ArrayList<>();
    ArrayList<TextView> RnumberLeftList = new ArrayList<>();
    ArrayList<TextView> RpointRequiredList = new ArrayList<>();
    ArrayList<Button> RButton = new ArrayList<>();
    TextView RuserName;
    TextView RuserPoint;

    private Connection connection;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reward, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // @Qiao, This is where you will use 'view' to initialize the items, i.e., texts, buttons, etc...

        //Link all TextView
        for (int i = 1; i <= 5; i++) {
            RdescriptionList.add(view.findViewById(getResources().getIdentifier("RDescription" + i, "id", requireActivity().getPackageName())));
            RnameList.add(view.findViewById(getResources().getIdentifier("RName" + i, "id", requireActivity().getPackageName())));
            RnumberLeftList.add(view.findViewById(getResources().getIdentifier("RNumbersLeft" + i, "id", requireActivity().getPackageName())));
            RpointRequiredList.add(view.findViewById(getResources().getIdentifier("RPointsRequired" + i, "id", requireActivity().getPackageName())));
            RButton.add(view.findViewById(getResources().getIdentifier("RButton" + i, "id", requireActivity().getPackageName())));
        }
        RuserName = view.findViewById(R.id.RUserName);
        RuserPoint = view.findViewById(R.id.RUserPoint);

        //change Username
        RuserName.setText(userName);

        // Initialize database connection
        new ConnectToDatabase(this).execute();
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
    public void onConnectionSuccess(Connection connection) {
        this.connection = connection;
        showToast("Database Connection Successful!");
        new RetrieveRewardsInformation(connection, this).execute();
    }

    /**
     * This is the Interface that's being implemented after successfully retrieved the Reward information
     */
    @Override
    public void onRewardsDataRetrieved(List<Integer> IDs, List<String> names, List<String> descriptions,
                                       List<Double> pointsRequired, List<Integer> numbersLeft) {
        new RetrieveUserPoints(connection, this).execute(); // This is used to retrieve the user points, and I have stored the result in the variable 'userPoints'

        for (int j=0;j<5;j++){
            RnameList.get(j).setText(names.get(j));
            RdescriptionList.get(j).setText(descriptions.get(j));
            RpointRequiredList.get(j).setText("Price: "+ String.valueOf(pointsRequired.get(j)));
            RnumberLeftList.get(j).setText(String.valueOf(numbersLeft.get(j) + " Units"));
        }
        int i;
        //what happen when button is clicked
        for (i = 0; i < RButton.size(); i++) {
            final int rewardId = i + 1;
            final int currentNumbersLeft = numbersLeft.get(rewardId - 1);
            final double currentUserpointRequired = pointsRequired.get(rewardId - 1);
            final double newUserPoints = userPoints - pointsRequired.get(rewardId - 1);
            RButton.get(rewardId-1).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentNumbersLeft > 0) {
                        if (userPoints >= currentUserpointRequired && currentNumbersLeft > 0){
                            showToast("You just redeem one" + names.get(rewardId - 1));
                            new UpdateNumbersLeft(connection, rewardId, currentNumbersLeft - 1).execute();
                            numbersLeft.set(rewardId - 1,currentNumbersLeft - 1);
                            RnumberLeftList.get(rewardId-1).setText(String.valueOf((currentNumbersLeft - 1) + " Units"));

                            new UpdateUserPoints(connection, newUserPoints);
                            userPoints=newUserPoints;
                            RuserPoint.setText(String.valueOf(userPoints));
                        }
                        else
                        {showToast("You need " + currentNumbersLeft + " points to redeem"+ names.get(rewardId-1));}
                    }
                else
                {showToast("Sorry!" + names.get(rewardId-1) + "is out of stock");}
            }
            });
        }
    }

    @Override
    public void onConnectionFailure() {
        showToast("Failed to connect database, try again!");
    }

    @Override
    public void onUserPointsRetrieved(double points) {
        // Handle the retrieved points (e.g., store them, compare with reward points, etc.)
        userPoints = points;
        RuserPoint.setText(String.valueOf(userPoints));
    }

    @Override
    public void onRetrievalFailed() {
        // Handle the failure (e.g., show an error message)
    }
}

/**
 * This class is used to retrieve the information of the Reward, it should have:
 * String Arraylist name, description
 * Double Arraylist pointsRequired
 * Int ArrayList numbersLeft
 * It has an interface Listener that will return ArrayLists that are ready for you to Handle what you want
 */
class RetrieveRewardsInformation extends AsyncTask<Void, Void, Boolean> {
    private final Connection connection;
    List<Integer> IDs = new ArrayList<>();
    List<String> names = new ArrayList<>();
    List<String> descriptions = new ArrayList<>();
    List<Double> pointsRequired = new ArrayList<>();
    List<Integer> numbersLeft = new ArrayList<>();

    public interface RewardsDataListener {
        void onRewardsDataRetrieved(List<Integer> IDs, List<String> names, List<String> descriptions, List<Double> pointsRequired, List<Integer> numbersLeft);
    }

    private final RewardsDataListener listener;

    public RetrieveRewardsInformation(Connection connection, RewardsDataListener listener) {
        this.connection = connection;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            String query = "SELECT RewardID, Name, Description, PointsRequired, NumbersLeft FROM Rewards";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                IDs.add(resultSet.getInt("RewardID"));
                names.add(resultSet.getString("Name"));
                descriptions.add(resultSet.getString("Description"));
                pointsRequired.add(resultSet.getDouble("PointsRequired"));
                numbersLeft.add(resultSet.getInt("NumbersLeft"));
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            listener.onRewardsDataRetrieved(IDs, names, descriptions, pointsRequired, numbersLeft);
        } else {
            // Handle failure
        }
    }
}

/**
 * This class is used to retrieve the user points, whenever a User clicked 'retrieve'
 * You should call this class, and it shall pass you the user points for the current user
 * because we already have the user information 'email', I will use that to retrieve the point
 */
class RetrieveUserPoints extends AsyncTask<Void, Void, Boolean> {
    private final Connection connection;
    private final String userEmail = UserInfoManager.getInstance().getEmail();
    private double userPoints;

    public interface UserPointsListener {
        void onUserPointsRetrieved(double points);
        void onRetrievalFailed();
    }

    private UserPointsListener listener;

    public RetrieveUserPoints(Connection connection, UserPointsListener listener) {
        this.connection = connection;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            String query = "SELECT PointsEarned FROM Users WHERE Email = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, userEmail);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                userPoints = resultSet.getDouble("PointsEarned");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            listener.onUserPointsRetrieved(userPoints);
        } else {
            listener.onRetrievalFailed();
        }
    }
}

/**
 * Update the user points after he has redeemed the reward successfully
 */
class UpdateUserPoints extends AsyncTask<Void, Void, Boolean> {
    private final Connection connection;
    private double newUserPoints;
    private final String userEmail = UserInfoManager.getInstance().getEmail();

    public UpdateUserPoints(Connection connection, double newUserPoints) {
        this.connection = connection;
        this.newUserPoints = newUserPoints;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            String query = "UPDATE Users SET PointsEarned = ? WHERE Email = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setDouble(1, newUserPoints);
            preparedStatement.setString(2, userEmail);

            int updatedRows = preparedStatement.executeUpdate();
            return updatedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

/**
 * This class is used to update the numbers left of a reward,
 * when a user have retrieved a corresponding reward successfully
 */
class UpdateNumbersLeft extends AsyncTask<Void, Void, Boolean> {
    private Connection connection;
    private int rewardId;
    private int newNumbersLeft;

    public UpdateNumbersLeft(Connection connection, int rewardId, int newNumbersLeft) {
        this.connection = connection;
        this.rewardId = rewardId;
        this.newNumbersLeft = newNumbersLeft;
    }


    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            String query = "UPDATE Rewards SET NumbersLeft = ? WHERE RewardID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, newNumbersLeft);
            preparedStatement.setInt(2, rewardId);

            int updatedRows = preparedStatement.executeUpdate();
            return updatedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            // Handle successful update, such as notifying the user or updating the UI
        } else {
            // Handle failure
        }
    }
}