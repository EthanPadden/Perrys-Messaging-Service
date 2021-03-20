package com.perrys;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetConversationHandler implements RequestHandler<ConversationRequest, GatewayResponse> {
    private DynamoDB dynamoDB;
    private AmazonDynamoDBClient client;
    private String DYNAMODB_TABLE_NAME = "Messages";
    private Regions REGION = Regions.EU_WEST_1;

    @Override
    public GatewayResponse handleRequest(ConversationRequest conversationRequest, Context context)
    {
        this.initDynamoDbClient();
        Map<String, AttributeValue> expressionAttributeValues =
                new HashMap<String, AttributeValue>();
        expressionAttributeValues.put(":id1", new AttributeValue().withS(conversationRequest.getUserId1()));
        expressionAttributeValues.put(":id2", new AttributeValue().withS(conversationRequest.getUserId2()));

        String filterExpression =
                "senderUserId = :id1"
                        + " OR senderUserId = :id2"
                        + " OR recipientUserId = :id1"
                        + " OR recipientUserId = :id2";

        ScanRequest scanRequest = new ScanRequest()
                .withTableName("Messages")
                .withFilterExpression(filterExpression)
                .withExpressionAttributeValues(expressionAttributeValues);
        ScanResult scanResult =  client.scan(scanRequest);
        List<Map<String, AttributeValue>> items = scanResult.getItems();
        JsonObject jsonMessageList = new JsonObject();
        for (int i = 0; i < items.size(); i++) {
            String messageId = items.get(i).get("messageId").toString();
            String timestamp = items.get(i).get("timestamp").toString();
            String body = items.get(i).get("body").toString();
            String recipientUserId = items.get(i).get("recipientUserId").toString();
            String senderUserId = items.get(i).get("senderUserId").toString();

            JsonObject jsonMessage = new JsonObject();
            jsonMessage.addProperty("messageId", messageId);
            jsonMessage.addProperty("timestamp", timestamp);
            jsonMessage.addProperty("body", body);
            jsonMessage.addProperty("recipientUserId", recipientUserId);
            jsonMessage.addProperty("senderUserId", senderUserId);
            jsonMessageList.add(Integer.toString(i), jsonMessage);
        }
        GatewayResponse response = new GatewayResponse(jsonMessageList.toString(), 200);
        return response;
    }

    private void initDynamoDbClient()
    {
        client = new AmazonDynamoDBClient();
        client.setRegion(Region.getRegion(REGION));
        this.dynamoDB = new DynamoDB(client);
    }
}
