package com.example.urbancycle.Profile;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.urbancycle.Database.ConnectToDatabase;
import com.example.urbancycle.Database.UserInfoManager;
import com.example.urbancycle.R;
import com.example.urbancycle.databinding.FragmentProfileBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class ProfileFragment extends Fragment implements ConnectToDatabase.DatabaseConnectionListener,
        UpdateUserNameTask.onUsernameUpdatedListener{
    private FragmentProfileBinding binding;
    private String userName = UserInfoManager.getInstance().getUserName();
    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    private Connection connection;
    private ImageView avatarImageView;
    private Button BtnLogout, UpdateProfile;
    private EditText ETUsername, ETFullName;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);

        avatarImageView = binding.Avatar;

        new ConnectToDatabase(this).execute();

        loadProfileImage();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        loadProfileImage();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        UpdateProfile = binding.BtnUpdateProfile;

        // Logout Button
        BtnLogout = binding.BtnLogout;
        BtnLogout.setOnClickListener(view1 -> {
            NavController navController = NavHostFragment.findNavController
                    (ProfileFragment.this);

            // Navigate to the mainActivity_Authentication fragment
            navController.navigate(R.id.action_profile_to_mainActivity_Authentication);

            // Remove the SettingFragment from the back stack
            navController.popBackStack(null, false);
        });

        // Set up the progress bar
        progressBar = binding.progressBar;
        setInProgress(false);

        // Set the Edit Text as user name
        ETUsername = binding.editTextUserName;
        ETUsername.setText(userName);

        // Update the new user name
        UpdateProfile.setOnClickListener(v ->{

            String newUsername = ETUsername.getText().toString().trim();

            if (Objects.equals(userName, newUsername)){
                showToast("Please enter a new user name to be updated! ");
                return;
            }

            setInProgress(true);
            new UpdateUserNameTask(connection, newUsername, this).execute();
        });

        // Full Name
        ETFullName = binding.editTextFullName;
        ETFullName.setText(UserInfoManager.getInstance().getFullName());

        // History Button
        binding.History.setOnClickListener(v -> Navigation.findNavController(view).
                navigate(R.id.action_profile_to_history));

        // Change Profile Picture button
        binding.BtnChangeProfilePicture.setOnClickListener(v -> startGalleryActivity());

        Glide.with(this)
                .load(R.drawable.profile)
                .into(avatarImageView);
    }

    public void setInProgress(boolean inProgress){

        if (inProgress){
            progressBar.setVisibility(View.VISIBLE);
            UpdateProfile.setVisibility(View.GONE);
        }else{
            progressBar.setVisibility(View.GONE);
            UpdateProfile.setVisibility(View.VISIBLE);
        }
    }

    private void startGalleryActivity() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    // update
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver()
                        , selectedImageUri);
                String imagePath = saveImageLocally(bitmap);
                if (imagePath != null) {
                    saveImagePath(imagePath);
                    loadProfileImage();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateAvatar(Uri selectedImageUri) {
        Glide.with(this)
                .load(selectedImageUri)
                .into(avatarImageView);
    }

    private String saveImageLocally(Bitmap imageBitmap) {
        if (getContext() == null) return null;

        ContextWrapper wrapper = new ContextWrapper(getContext());
        File file = wrapper.getDir("profileImages", Context.MODE_PRIVATE);
        file = new File(file, "UniqueFileName.jpg"); // Unique name for each image

        try {
            OutputStream stream = new FileOutputStream(file);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file.getAbsolutePath();
    }


    private void saveImagePath(String path) {
        if (getContext() == null) return;

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("ProfileImagePath", path);
        editor.apply();
    }

    private void loadProfileImage() {
        if (getContext() == null) return;

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        String imagePath = sharedPreferences.getString("ProfileImagePath", null);
        if (imagePath != null) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                avatarImageView.setImageBitmap(myBitmap);
            }
        }
    }



    private void showToast(String message) {
        if (isAdded()) {
            Context context = getActivity();
            if (context != null) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onConnectionSuccess(Connection connection) {
        this.connection = connection;

    }

    @Override
    public void onConnectionFailure() {

    }

    @Override
    public void onUpdateSuccess() {
        String newUsername = ETUsername.getText().toString();
        UserInfoManager.getInstance().setUserName(newUsername);
        ETUsername.setText(newUsername);

        setInProgress(false);
        showToast("Username updated successfully!");
    }
}

class UpdateUserNameTask extends AsyncTask<Void, Void, Boolean>{
    private final String newUsername;
    private final String userEmail = UserInfoManager.getInstance().getEmail();
    private final Connection connection;
    private final onUsernameUpdatedListener listener;
    public interface onUsernameUpdatedListener{
        void onUpdateSuccess();
    }
    public UpdateUserNameTask(Connection connection, String newUsername,
                              onUsernameUpdatedListener listener){

        this.connection = connection;
        this.newUsername = newUsername;
        this.listener = listener;
    }
    @Override
    protected Boolean doInBackground(Void... voids) {

        String query = "UPDATE Users SET UserName = ? WHERE Email = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, newUsername);
            preparedStatement.setString(2, userEmail);

            int updatedRows = preparedStatement.executeUpdate();
            return updatedRows > 0;

        } catch (SQLException e) {
            Log.e("UpdateUserPassword", "SQL Error: ", e);
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {

        if (aBoolean){
            listener.onUpdateSuccess();
        }
    }
}