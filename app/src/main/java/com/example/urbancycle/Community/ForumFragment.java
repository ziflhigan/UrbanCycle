package com.example.urbancycle.Community;

// ForumFragment.java

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.urbancycle.Community.RetrieveUserPosts.UserPost;
import com.example.urbancycle.Community.RetrieveUserPosts.onPostsRetrievedListener;
import com.example.urbancycle.Database.ConnectToDatabase;
import com.example.urbancycle.R;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ForumFragment extends Fragment implements ConnectToDatabase.DatabaseConnectionListener{

    private List<Post> posts;
    private PostAdapter postAdapter;
    private Connection databaseConnection;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forum, container, false);
        // Initialize the RecyclerView
        initializeRecyclerView(view);
        // Initialize the "Create Post" button
        initializeCreatePostButton(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initiate connection to our database
        new ConnectToDatabase(this).execute();
    }

    // Initializes the RecyclerView to display the post
    private void initializeRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        // Initializes the list of posts and the adapter
        posts = new ArrayList<>();
        postAdapter = new PostAdapter(posts);
        recyclerView.setAdapter(postAdapter);
    }

    // Listener for "Create Post" button
    private void initializeCreatePostButton(View view) {
        Button buttonPost = view.findViewById(R.id.buttonPost);
        EditText editTextPostContent = view.findViewById(R.id.editTextPostContent);
        buttonPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get post content and insert into database
                String userPost = editTextPostContent.getText().toString();
                new InsertUserPosts(databaseConnection, userPost).execute();
                // Call method to retrieve and display  user post from DB
                retrieveAndDisplayUserPosts();
                editTextPostContent.getText().clear();
                Toast.makeText(requireContext(), "Posted successfully!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to retrieve and display user posts from the DB
    private void retrieveAndDisplayUserPosts() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String formattedDateTime = currentDateTime.format(formatter);
        new RetrieveUserPosts(databaseConnection, new onPostsRetrievedListener() {
            @Override
            public void onPostsRetrieved(List<UserPost> userPosts) {
                // Converts retrieved user posts to displayable format and updates the RecyclerView
                for (UserPost userPost : userPosts) {
                    posts.add(new Post(userPost.getUsername(), userPost.getPost(), formattedDateTime));
                }
                postAdapter.notifyDataSetChanged(); // Notify the adapter that data has changed
            }
        }).execute();
    }

    // Callback on successful database connection
    @Override
    public void onConnectionSuccess(Connection connection) {
        this.databaseConnection = connection;
        // Retrieves and displays user posts after successful connection
        retrieveAndDisplayUserPosts();
    }

    // Callback on failed database connection
    @Override
    public void onConnectionFailure() {
        Toast.makeText(requireContext(), "Cannot connect to the database", Toast.LENGTH_SHORT).show();
    }
}
