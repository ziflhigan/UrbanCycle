package com.example.urbancycle.Community;

import android.os.AsyncTask;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RetrieveUserPosts extends AsyncTask<Void, Void, List<RetrieveUserPosts.UserPost>> {
    private final Connection connection;
    private final onPostsRetrievedListener listener;

    public interface onPostsRetrievedListener {
        void onPostsRetrieved(List<UserPost> userPosts);
    }

    public RetrieveUserPosts(Connection connection, onPostsRetrievedListener listener) {
        this.connection = connection;
        this.listener = listener;
    }

    @Override
    protected List<UserPost> doInBackground(Void... voids) {
        List<UserPost> userPosts = new ArrayList<>();
        try {
            String query = "SELECT PostID, username, Posts FROM Community ORDER BY Date DESC";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int postID = resultSet.getInt("PostID");
                String username = resultSet.getString("username");
                String post = resultSet.getString("Posts");
                userPosts.add(new UserPost(postID, username, post));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userPosts;
    }

    @Override
    protected void onPostExecute(List<UserPost> userPosts) {
        listener.onPostsRetrieved(userPosts);
    }

    public static class UserPost {
        private final int postID;
        private final String username;
        private final String post;

        public UserPost(int postID, String username, String post) {
            this.postID = postID;
            this.username = username;
            this.post = post;
        }

        // Getters
        public int getPostID() {
            return postID;
        }

        public String getUsername() {
            return username;
        }

        public String getPost() {
            return post;
        }
    }
}
