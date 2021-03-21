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
            String filterExpression = "senderUserId = :id1"
                    + " OR senderUserId = :id2"
                    + " OR recipientUserId = :id1"
                    + " OR recipientUserId = :id2";

            // Execute scan request and retrieve a list of DB items
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName(DB_MESSAGES_TABLE_NAME)
                    .withFilterExpression(filterExpression)
                    .withExpressionAttributeValues(expressionAttributeValues);
            ScanResult scanResult = client.scan(scanRequest);
            List<Map<String, AttributeValue>> items = scanResult.getItems();

            // Create JSON object for response body
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
