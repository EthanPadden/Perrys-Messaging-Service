package com.perrys.DBObjects;

import com.google.gson.JsonObject;

public class User {
    private String userId;
    private String username;

    // Constructor needs to be empty for interaction with AWS
    public User()
    {
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public JsonObject toJsonObject()
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userId", userId);
        jsonObject.addProperty("username", username);
        return jsonObject;
    }
}
