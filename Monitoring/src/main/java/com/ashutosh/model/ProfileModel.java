package com.ashutosh.model;

public class ProfileModel {
    private long profileId;
    private String firstName;
    private String lastName;

    public ProfileModel() {
        this.profileId = 0;
        this.firstName = "";
        this.lastName = "";
    }

    public long getProfileId() {
        return profileId;
    }

    public void setProfileId(long profileId) {
        this.profileId = profileId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}


