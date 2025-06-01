package com.raven.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Model_User_Account {

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Model_User_Account(int userID, String userName, String email, String gender, String image, boolean status) {
        this.userID = userID;
        this.userName = userName;
        this.email = email;
        this.gender = gender;
        this.image = image;
        this.status = status;
    }

    public Model_User_Account(Object json) {
        JSONObject obj = (JSONObject) json;
        try {
            userID = obj.getInt("userID");
            userName = obj.getString("userName");
            email = obj.getString("email");
            gender = obj.getString("gender");
            image = obj.getString("image");
            status = obj.getBoolean("status");
        } catch (JSONException e) {
            System.err.println(e);
        }
    }

    private int userID;
    private String userName;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private String email;
    private String gender;
    private String image;
    private boolean status;
}
