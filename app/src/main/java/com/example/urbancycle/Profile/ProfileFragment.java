package com.example.urbancycle.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
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

    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    Connection connection;
    private ImageView avatarImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        TextView tv = view.findViewById(R.id.UsernameDisp);
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
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_profile_to_galleryActivity);
            }
        });
        avatarImageView = view.findViewById(R.id.Avatar);

        avatarImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGalleryActivity();
            }
        });
        Glide.with(this)
                .load(R.drawable.profile)
                .into(avatarImageView);

        return view;



    }
    private void startGalleryActivity() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);

    }

    // update
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == GalleryActivity.RESULT_OK && data != null) {
            String selectedImageUri = data.getStringExtra("selectedImageUri");
            updateAvatar(selectedImageUri);
        }
    }

    private void updateAvatar(String selectedImageUri) {

        Glide.with(this)
                .load(selectedImageUri)
                .into(avatarImageView);
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
