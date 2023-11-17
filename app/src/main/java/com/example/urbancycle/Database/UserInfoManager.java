package com.example.urbancycle.Database;

public class UserInfoManager {
    private static UserInfoManager instance;
    private String email;
    private String userName;

    private UserInfoManager() {
        // Private constructor to prevent direct instantiation
    }

    public static synchronized UserInfoManager getInstance() {
        if (instance == null) {
            instance = new UserInfoManager();
        }
        return instance;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserDetails(String email, String userName) {
        this.email = email;
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public String getUserName() {
        return userName;
    }

}
