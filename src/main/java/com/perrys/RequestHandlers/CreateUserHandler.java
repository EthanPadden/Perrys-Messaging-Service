package com.perrys.RequestHandlers;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.perrys.GatewayResponse;
import com.perrys.RequestObjects.UserRequest;

import java.util.UUID;

public class CreateUserHandler implements RequestHandler<UserRequest, GatewayResponse> {
    private DynamoDB dynamoDB;
    private String DYNAMODB_TABLE_NAME = "Users";
    private Regions REGION = Regions.EU_WEST_1; // TODO: Update with correct region

    @Override
    public GatewayResponse handleRequest(UserRequest userRequest, Context context)
    {
        // Create response object
        GatewayResponse response;

        try {
            // Initialise DynamoDB client
            AmazonDynamoDBClient client = new AmazonDynamoDBClient();
            client.setRegion(Region.getRegion(REGION));
            this.dynamoDB = new DynamoDB(client);

            // Get the table from the DB object
            Table table = dynamoDB.getTable(DYNAMODB_TABLE_NAME);

            // Generate a random UUID as a string
            String uuid = UUID.randomUUID().toString();

            // Create DB item object
            Item newUserDBItem = new Item()
                    .withPrimaryKey("userId", uuid)
                    .withString("username", userRequest.getUsername());

            // Put item into table
            table.putItem(newUserDBItem);

            response = new GatewayResponse("User created", 200);
        } catch (Exception e) {
            response = new GatewayResponse("There was an error accessing the database: ", 500);
        }

        return response;
    }
}
