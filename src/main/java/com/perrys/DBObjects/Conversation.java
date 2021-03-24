package com.perrys.DBObjects;

public class Conversation {
    private String userId1;
    private String userId2;

    // Constructor needs to be empty for interaction with AWS
    public Conversation()
    {
    }

    public String getUserId1()
    {
        return userId1;
    }

    public void setUserId1(String userId1)
    {
        this.userId1 = userId1;
    }

    public String getUserId2()
    {
        return userId2;
    }

    public void setUserId2(String userId2)
    {
        this.userId2 = userId2;
    }
}
