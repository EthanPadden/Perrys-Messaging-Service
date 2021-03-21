package com.perrys;

import java.util.Map;

public class GatewayResponse {
    private Object body;
    private Integer statusCode;

    public GatewayResponse(Object body, Integer statusCode)
    {
        this.body = body;
        this.statusCode = statusCode;
    }

    public Object getBody()
    {
        return body;
    }

    public void setBody(Object body)
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
