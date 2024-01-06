package com.example.urbancycle.Profile;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.urbancycle.Database.ConnectToDatabase;
import com.example.urbancycle.Database.UserInfoManager;
import com.example.urbancycle.R;
import com.example.urbancycle.databinding.FragmentProfileBinding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment implements ConnectToDatabase.DatabaseConnectionListener, RetrieveUserName.UserNameDataListener {

 FragmentProfileBinding binding;
     String userName = UserInfoManager.getInstance().getUserName();

    Connection connection;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        TextView tv=view.findViewById(R.id.UsernameDisp);
        tv.setText(userName);

        binding.History.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Navigation.findNavController(view).navigate(R.id.action_profile_to_history);
            }
        });

        binding.Setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_profile_to_setting);
            }
        });

        binding.Preference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_profile_to_preference);
            }
        });

        return view;
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
        new RetrieveUserName(connection, this).execute();
    }


    @Override
    public void onConnectionFailure() {

    }


    @Override
    public void onUserNameDataRetrieved(List<String> names) {

            // Handle the retrieved list of names here
            if (names != null && !names.isEmpty()) {
                // Set the TextView with the first retrieved username
                String username = names.get(0);
                binding.UsernameDisp.setText(username);
            } else {
                showToast("No usernames retrieved from the database.");
            }
        }


}
class RetrieveUserName extends AsyncTask<Void, Void, Boolean> {
    private final Connection connection;
    private final ProfileFragment listener;

    List<String> names = new ArrayList<>();
    public interface UserNameDataListener {
        void onUserNameDataRetrieved(List<String> names);
    }

    RetrieveUserName(Connection connection, ProfileFragment listener) {
        this.connection = connection;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            String query = "SELECT  Name FROM User";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                names.add(resultSet.getString("Name"));

            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    protected void onPostExecute(Boolean success) {
        if (success) {
            listener.onUserNameDataRetrieved( names);
        } else {
            // Handle failure
        }
    }


}
