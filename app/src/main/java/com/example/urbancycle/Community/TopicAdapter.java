package com.example.urbancycle.Community;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.urbancycle.Community.Topic;
import com.example.urbancycle.R;

import java.util.List;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.ViewHolder> {
    private List<Topic> topicList;
    private OnTopicClickListener onTopicClickListener;

    public TopicAdapter(List<Topic> topicList) {

    }

    public interface OnTopicClickListener {
        void onTopicClick(int position);
    }

    public TopicAdapter(List<Topic> topicList, OnTopicClickListener onTopicClickListener) {
        this.topicList = topicList;
        this.onTopicClickListener = onTopicClickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        Button topicButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            topicButton = itemView.findViewById(R.id.topicButton);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_topic, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Topic currentTopic = topicList.get(position);
        holder.topicButton.setBackgroundColor(Color.parseColor(currentTopic.getColor()));

        // Handle button click
        holder.topicButton.setOnClickListener(v -> {
            if (onTopicClickListener != null) {
                onTopicClickListener.onTopicClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return topicList.size();
    }
}
