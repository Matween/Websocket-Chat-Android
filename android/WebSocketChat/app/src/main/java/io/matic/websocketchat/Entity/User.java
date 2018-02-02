package io.matic.websocketchat.Entity;

/**
 * Created by Matic on 12/09/2017.
 */

public class User {
    private String userID;
    private String fullName;
    private String email;
    private String profilePic;

    public User() {
    }

    public User(String userID, String fullName, String email, String profilePic) {
        this.userID = userID;
        this.fullName = fullName;
        this.email = email;
        this.profilePic = profilePic;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }
}
