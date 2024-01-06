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
        initializeRecyclerView(view);
        initializeCreatePostButton(view);
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
                retrieveAndDisplayUserPosts();
                editTextPostContent.getText().clear();
                Toast.makeText(requireContext(), "Posted succesfully!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void retrieveAndDisplayUserPosts() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String formattedDateTime = currentDateTime.format(formatter);
        new RetrieveUserPosts(databaseConnection, new onPostsRetrievedListener() {
            @Override
            public void onPostsRetrieved(List<UserPost> userPosts) {
               // posts.clear(); // Clear existing posts
                for (UserPost userPost : userPosts) {
                    posts.add(new Post(userPost.getUsername(), userPost.getPost(), formattedDateTime));
                }
                postAdapter.notifyDataSetChanged(); // Notify the adapter that data has changed
            }
        }).execute();
    }

    @Override
    public void onConnectionSuccess(Connection connection) {
        this.databaseConnection = connection;
        retrieveAndDisplayUserPosts();
    }

    @Override
    public void onConnectionFailure() {
        Toast.makeText(requireContext(), "Cannot connect to the database", Toast.LENGTH_SHORT).show();
    }
}

