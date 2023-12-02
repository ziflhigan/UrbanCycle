package com.example.urbancycle.Rewards;

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
            RpointRequiredList.add(view.findViewById(getResources().getIdentifier("RPointRequired" + i, "id", requireActivity().getPackageName())));
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
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
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
        for (int j=0;j<IDs.size();j++){
            RnameList.get(j).setText(names.get(j));
            RdescriptionList.get(j).setText(descriptions.get(j));
            RpointRequiredList.get(j).setText(pointsRequired.get(j).toString());
            RnumberLeftList.get(j).setText(numbersLeft.get(j));
        }
        // Handle the retrieved data, e.g., display in a list or UI component
        // Maybe you can also handle the actions when user has clicked the button 'retrieve' here as well, then you will need to call another two classes

        // Assuming we have a button for redeeming rewards

        // For example, let's say the user clicks to redeem the first reward
        // By the way, you need to handle the checking for whether user has enough points or not, by using the class 'RetrieveUserPoints'
        new RetrieveUserPoints(connection, this).execute(); // This is used to retrieve the user points, and I have stored the result in the variable 'userPoints'

        int rewardId = 1;
        int currentNumbersLeft = numbersLeft.get(rewardId - 1); // Adjust index as necessary, since index in the ArrayList starts from zero
        int newNumbersLeft = currentNumbersLeft - 1; // Also remember to add Edge check, ensure it is not negative

        // After successfully redeemed
        // Update the NumbersLeft in the database
        new UpdateNumbersLeft(connection, rewardId, newNumbersLeft).execute();

        // Update the UserPoints in the database
        double newUserPoints = userPoints - pointsRequired.get(rewardId-1);
        new UpdateUserPoints(connection, userPoints);

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
    private double userPoints;
    private final String userEmail = UserInfoManager.getInstance().getEmail();

    public UpdateUserPoints(Connection connection, double userPoints) {
        this.connection = connection;
        this.userPoints = userPoints;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            String query = "UPDATE Users SET PointsEarned = ? WHERE Email = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setDouble(1,  userPoints);
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