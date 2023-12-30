package com.example.urbancycle.Community;

// ForumFragment.java


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

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

public class ForumFragment extends Fragment implements ConnectToDatabase.DatabaseConnectionListener{

    private List<Post> posts;
    private PostAdapter postAdapter;
    private Connection databaseConnection;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forum, container, false);
        initializeRecyclerView(view);
        initializeCreatePostButton(view);
        // This one is causing crashing since nothing is there initially
        // retrieveAndDisplayUserPosts(); // Retrieve and display posts when the fragment is created
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new ConnectToDatabase(this).execute();
    }

    private void initializeRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        posts = new ArrayList<>();
        postAdapter = new PostAdapter(posts);
        recyclerView.setAdapter(postAdapter);
    }

    private void initializeCreatePostButton(View view) {
        Button buttonPost = view.findViewById(R.id.buttonPost);
        EditText editTextPostContent = view.findViewById(R.id.editTextPostContent);

        buttonPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userPost = editTextPostContent.getText().toString();
                new InsertUserPosts(databaseConnection, userPost).execute();
                retrieveAndDisplayUserPosts(); // Retrieve and display posts after a new post is inserted
                editTextPostContent.getText().clear();
            }
        });
    }

    private void retrieveAndDisplayUserPosts() {
        new RetrieveUserPosts(databaseConnection, new onPostsRetrievedListener() {
            @Override
            public void onPostsRetrieved(List<UserPost> userPosts) {
                posts.clear(); // Clear existing posts
                for (UserPost userPost : userPosts) {
                    posts.add(new Post(userPost.getUsername(), userPost.getPost(), "CurrentTimestamp"));
                }
                postAdapter.notifyDataSetChanged(); // Notify the adapter that data has changed
            }
        }).execute();
    }

    @Override
    public void onConnectionSuccess(Connection connection) {
        this.databaseConnection = connection;
    }

    @Override
    public void onConnectionFailure() {

    }
}

