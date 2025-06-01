package com.raven.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Model_Login {

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

    public Model_Login(String userName, String email, String password) {
        this.userName = userName;
        this.email = email;
        this.password = password;
    }

    public Model_Login() {
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
            JSONObject obj = new JSONObject();
            obj.put("userName", userName);
            obj.put("password", password);
            obj.put("email", email);
            return obj;
        } catch (JSONException e) {
            return null;
        }
    }
}
