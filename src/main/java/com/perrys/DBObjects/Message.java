package com.perrys.DBObjects;

public class Message {
    private String senderUserId;
    private String recipientUserId;
    private String body;
    private String messageId;
    private long lastUpdated;

    // Constructor needs to be empty for interaction with AWS
    public Message()
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

    public String getMessageId()
    {
        return messageId;
    }

    public void setMessageId(String messageId)
    {
        this.messageId = messageId;
    }

    public long getLastUpdated()
    {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated)
    {
        this.lastUpdated = lastUpdated;
    }
}
