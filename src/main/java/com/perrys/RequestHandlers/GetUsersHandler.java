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
import com.google.gson.JsonObject;
import com.perrys.GatewayResponse;
import com.perrys.ResponseObjects.UserResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GetUsersHandler implements RequestHandler<Object, GatewayResponse> {
    private DynamoDB dynamoDB;
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
            this.dynamoDB = new DynamoDB(client);

            // Execute scan request and retrieve a list of DB items
            ScanRequest scanRequest = new ScanRequest().withTableName(DB_USERS_TABLE_NAME);
            ScanResult scanResult = client.scan(scanRequest);
            List<Map<String, AttributeValue>> items = scanResult.getItems();

            // Populate arraylist of UserResponse objects to send in HTTP response
            JsonObject jsonUsersList = new JsonObject();
            for (int i = 0; i < items.size(); i++) {
                String userId = items.get(i).get("userId").toString();
                String username = items.get(i).get("username").toString();
                JsonObject jsonUser = new JsonObject();
                jsonUser.addProperty("userId", userId);
                jsonUser.addProperty("username", username);
                jsonUsersList.add(Integer.toString(i), jsonUser);
            }

            response = new GatewayResponse(jsonUsersList.toString(), 200);
        } catch (Exception e) {
            response = new GatewayResponse("There was an error accessing the database", 500);
        }

        return response;
    }
}
