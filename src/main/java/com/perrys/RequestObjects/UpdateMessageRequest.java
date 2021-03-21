package com.perrys.RequestObjects;

public class UpdateMessageRequest {
    private String messageId;
    private String body;

    public UpdateMessageRequest()
    {
    }

    public String getMessageId()
    {
        return messageId;
    }

    public void setMessageId(String messageId)
    {
        this.messageId = messageId;
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
