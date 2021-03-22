package com.perrys.RequestHandlers;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.perrys.GatewayResponse;
import com.perrys.RequestObjects.ConversationRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetConversationHandler implements RequestHandler<ConversationRequest, GatewayResponse> {
    private AmazonDynamoDBClient client;
    private String DB_MESSAGES_TABLE_NAME = "Messages";
    private Regions REGION = Regions.EU_WEST_1;

    @Override
    public GatewayResponse handleRequest(ConversationRequest conversationRequest, Context context)
    {
        // Create response object
        GatewayResponse response;

        try {
            // Initialise DynamoDB client
            client = new AmazonDynamoDBClient();
            client.setRegion(Region.getRegion(REGION));

            // Cannot use literals in a filter expression
            // Use hashmap to map them
            Map<String, AttributeValue> expressionAttributeValues =
                    new HashMap<String, AttributeValue>();
            expressionAttributeValues.put(":id1", new AttributeValue().withS(conversationRequest.getUserId1()));
            expressionAttributeValues.put(":id2", new AttributeValue().withS(conversationRequest.getUserId2()));

            // Build filter expression
            String filterExpression = "(senderUserId = :id1 AND recipientUserId = :id2)"
                    + " OR (senderUserId = :id2 AND recipientUserId = :id1)";

            // Execute scan request and retrieve a list of DB items
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName(DB_MESSAGES_TABLE_NAME)
                    .withFilterExpression(filterExpression)
                    .withExpressionAttributeValues(expressionAttributeValues);
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
        } catch (IllegalArgumentException e) {
            response = new GatewayResponse("There was an error in the input", 400);
        } catch (AmazonDynamoDBException e) {
            if(e.getMessage().contains("ValidationException") || e.getMessage().contains("invalid value")) {
                response = new GatewayResponse("There was an error in the input", 400);
            } else {
                response = new GatewayResponse("There was an error accessing the database", 500);
            }
        } catch (Exception e) {
            response = new GatewayResponse("There was an error accessing the database", 500);
        }

        return response;
    }
}
