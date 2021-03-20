package com.perrys;

import java.util.Map;

public class GatewayResponse {
    private String body;
    private Integer statusCode;

    public GatewayResponse(String body, Integer statusCode)
    {
        this.body = body;
        this.statusCode = statusCode;
    }

    public String getBody()
    {
        return body;
    }

    public void setBody(String body)
    {
        this.body = body;
    }

    public Integer getStatusCode()
    {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode)
    {
        this.statusCode = statusCode;
    }
}
