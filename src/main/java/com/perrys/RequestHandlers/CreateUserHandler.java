package com.perrys.RequestHandlers;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.perrys.ErrorMessages;
import com.perrys.GatewayResponse;
import com.perrys.DBObjects.User;

import java.util.UUID;

public class CreateUserHandler implements RequestHandler<User, GatewayResponse> {
    private DynamoDB dynamoDB;
    private String DYNAMODB_TABLE_NAME = "Users";
    private Regions REGION = Regions.EU_WEST_1; // TODO: Update with correct region

    @Override
    public GatewayResponse handleRequest(User user, Context context)
    {
        // Create response object
        GatewayResponse response;

        try {
            // Validate input
            if(user.getUsername() == null) throw new IllegalArgumentException();
            if(user.getUsername().compareTo("") == 0) throw new IllegalArgumentException();

            // Initialise DynamoDB client
            AmazonDynamoDBClient client = new AmazonDynamoDBClient();
            client.setRegion(Region.getRegion(REGION));
            this.dynamoDB = new DynamoDB(client);

            // Get the table from the DB object
            Table table = dynamoDB.getTable(DYNAMODB_TABLE_NAME);

            // Generate a random UUID as a string
            String uuid = UUID.randomUUID().toString();

            // Set the ID of the input object
            user.setUserId(uuid);

            // Create DB item object
            Item newUserDBItem = new Item()
                    .withPrimaryKey("userId", uuid)
                    .withString("username", user.getUsername());

            // Put item into table
            table.putItem(newUserDBItem);

            // Return the newly created user
            response = new GatewayResponse(user, 200);
        } catch (IllegalArgumentException e) {
            response = new GatewayResponse(ErrorMessages.MESSAGE_INVALID_INPUT, 400);
        } catch (Exception e) {
            response = new GatewayResponse(ErrorMessages.MESSAGE_ERROR_DB_ACCESS, 500);
        }

        return response;
    }
}
