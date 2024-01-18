package com.example.urbancycle.Community;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.urbancycle.R;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts;

    // Constructor to initialize the list of posts
    public PostAdapter(List<Post> posts) {
        this.posts = posts;
    }

    // Called when a new ViewHolder is created
    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflates the layout for individual post items
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    // Bind data from Post objects to the corresponding views
    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.textViewUsername.setText(post.getUsername());
        holder.textViewContent.setText(post.getContent());
        holder.textViewTimestamp.setText(post.getTimestamp());
    }

    // Returns the total number of posts in the list
    @Override
    public int getItemCount() {
        return posts.size();
    }

    // ViewHolder class to hold and reference views for individual post items
    public static class PostViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewUsername;
        public TextView textViewContent;
        public TextView textViewTimestamp;

        // Constructor to initialize views
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewContent = itemView.findViewById(R.id.textViewContent);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
        }
    }
}
