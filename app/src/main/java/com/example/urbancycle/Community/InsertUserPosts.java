package com.example.urbancycle.Community;

import android.os.AsyncTask;

import com.example.urbancycle.Database.UserInfoManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Yourisha You can call this class like this: new InsertUserPosts(connection, userPost).execute();
 * in the fragment where user will post something, after clicked button like 'Post'

 * It only takes two parameters: The connection and the current user's post, other staffs
 * it will handle for you
 */

public class InsertUserPosts extends AsyncTask<Void, Void, Boolean> {
    private final Connection connection;
    private final String post;
    private final String userName;
    private final String userEmail;

    public InsertUserPosts(Connection connection, String post) {
        this.connection = connection;
        this.post = post;
        this.userName = UserInfoManager.getInstance().getUserName();
        this.userEmail = UserInfoManager.getInstance().getEmail();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            String query = "INSERT INTO Community (username, Email, Posts, Date) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, userEmail);
            preparedStatement.setString(3, post);
            preparedStatement.setString(4, getCurrentDateTime());

            int result = preparedStatement.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            // Handle successful post insertion (e.g., show a message or update UI)
        } else {
            // Handle insertion failure (e.g., show error message)
        }
    }
}
