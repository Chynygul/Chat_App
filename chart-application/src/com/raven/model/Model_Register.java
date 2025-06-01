package com.raven.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Model_Register {

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Model_Register(String userName, String password, String email) {
        this.userName = userName;
        this.password = password;
        this.email = email;
    }

    public Model_Register() {
    }

    private String userName;
    private String password;
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public JSONObject toJsonObject() {
        try {
            JSONObject json = new JSONObject();
            json.put("userName", userName);
            json.put("password", password);
            json.put("email", email);
            return json;
        } catch (JSONException e) {
            return null;
        }
    }
}
