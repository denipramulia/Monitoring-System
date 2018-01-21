package com.ashutosh.model;

public class UserModel {

    private long userId;
    private String userName;
    private String password;
    private ProfileModel profileModel;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setProfileModel(ProfileModel profileModel) {
        this.profileModel = profileModel;
    }

    public ProfileModel getProfileModel() {
        return profileModel;
    }
}
