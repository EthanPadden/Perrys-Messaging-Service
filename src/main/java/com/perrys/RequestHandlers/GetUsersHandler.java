package com.perrys.RequestHandlers;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.perrys.ErrorMessages;
import com.perrys.GatewayResponse;

import java.util.List;
import java.util.Map;

public class GetUsersHandler implements RequestHandler<Object, GatewayResponse> {
    private AmazonDynamoDBClient client;
    private String DB_USERS_TABLE_NAME = "Users";
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

            // Execute scan request and retrieve a list of DB items
            ScanRequest scanRequest = new ScanRequest().withTableName(DB_USERS_TABLE_NAME);
            ScanResult scanResult = client.scan(scanRequest);
            List<Map<String, AttributeValue>> items = scanResult.getItems();

            // Create JSON array for response body
            JsonArray jsonUsersList = new JsonArray();
            for (Map<String, AttributeValue> item : items) {
                try {
                    String userId = item.get("userId").getS();
                    String username = item.get("username").getS();
                    JsonObject jsonUser = new JsonObject();
                    jsonUser.addProperty("userId", userId);
                    jsonUser.addProperty("username", username);
                    jsonUsersList.add(jsonUser);
                } catch (NullPointerException e) {
                    // In this case, the user has been entered incorrectly into the database
                    // i.e. Some fields are not filled out
                    // We do not return malformed users
                }
            }

            response = new GatewayResponse(jsonUsersList, 200);
        } catch (Exception e) {
            response = new GatewayResponse(ErrorMessages.MESSAGE_ERROR_DB_ACCESS, 500);
        }

        return response;
    }
}
