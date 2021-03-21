package com.perrys.RequestObjects;

public class MessageRequest {
    private String senderUserId;
    private String recipientUserId;
    private String body;

    public MessageRequest()
    {
    }

    public String getSenderUserId()
    {
        return senderUserId;
    }

    public void setSenderUserId(String senderUserId)
    {
        this.senderUserId = senderUserId;
    }

    public String getRecipientUserId()
    {
        return recipientUserId;
    }

    public void setRecipientUserId(String recipientUserId)
    {
        this.recipientUserId = recipientUserId;
    }

    public String getBody()
    {
        return body;
    }

    public void setBody(String body)
    {
        this.body = body;
    }
}