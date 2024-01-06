package com.example.urbancycle.Community;

public class Post {
    private String username;
    private String content;
    private String timestamp;

    public Post(String username, String content, String timestamp) {
        this.username = username;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public String getContent() {
        return content;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
