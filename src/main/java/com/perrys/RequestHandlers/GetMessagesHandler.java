package com.perrys.RequestHandlers;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.perrys.GatewayResponse;

import java.util.List;
import java.util.Map;

public class GetMessagesHandler implements RequestHandler<Object, GatewayResponse> {
    private DynamoDB dynamoDB;
    private AmazonDynamoDBClient client;
    private String DB_MESSAGES_TABLE_NAME = "Messages";
    private Regions REGION = Regions.EU_WEST_1;

    @Override
    public GatewayResponse handleRequest(Object o, Context context)
    {
        // Create response object
        GatewayResponse response;

        try {
            // Initialise DynamoDB client
            client = new AmazonDynamoDBClient();
            client.setRegion(Region.getRegion(REGION));
            this.dynamoDB = new DynamoDB(client);

            // Execute scan request and retrieve a list of DB items
            ScanRequest scanRequest = new ScanRequest().withTableName(DB_MESSAGES_TABLE_NAME);
            ScanResult scanResult = client.scan(scanRequest);
            List<Map<String, AttributeValue>> items = scanResult.getItems();

            // Create JSON array for response body
            JsonArray jsonMessageList = new JsonArray();
            for (Map<String, AttributeValue> item : items) {
                try {
                    String messageId = item.get("messageId").getS();
                    String timestamp = item.get("timestamp").getN();
                    String body = item.get("body").getS();
                    String recipientUserId = item.get("recipientUserId").getS();
                    String senderUserId = item.get("senderUserId").getS();

                    JsonObject jsonMessage = new JsonObject();
                    jsonMessage.addProperty("messageId", messageId);
                    jsonMessage.addProperty("timestamp", timestamp);
                    jsonMessage.addProperty("body", body);
                    jsonMessage.addProperty("recipientUserId", recipientUserId);
                    jsonMessage.addProperty("senderUserId", senderUserId);
                    jsonMessageList.add(jsonMessage);
                } catch (NullPointerException e) {
                    // In this case, the message has been entered incorrectly into the database
                    // i.e. Some fields are not filled out
                    // We do not return malformed messages
                }
            }

            response = new GatewayResponse(jsonMessageList.toString(), 200);
        } catch (Exception e) {
            response = new GatewayResponse("There was an error accessing the database", 500);
        }

        return response;
    }
}
