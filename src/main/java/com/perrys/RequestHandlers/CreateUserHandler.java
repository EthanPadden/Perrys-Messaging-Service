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
        this.initDynamoDbClient();
        persistData(userRequest);
        GatewayResponse personResponse = new GatewayResponse("UserRequest created", 200);
        return personResponse;
    }

    private PutItemOutcome persistData(UserRequest userRequest)
            throws ConditionalCheckFailedException
    {
        // Get the table from the DB object
        Table table = dynamoDB.getTable(DYNAMODB_TABLE_NAME);

        // Generate a random UUID as a string
        String uuid = UUID.randomUUID().toString(); // TODO: move to empty constructor and set as member variable?

        try {
            System.out.println("Creating user in database...");
            PutItemOutcome outcome = table
                    .putItem(new Item().withPrimaryKey("userId", uuid).withString("username", userRequest.getUsername()));
            System.out.println("PutItem succeeded:\n" + outcome.getPutItemResult());
            return outcome;
        } catch (Exception e) {
            System.err.println("Unable to add item: " + userRequest.getUsername());
            System.err.println(e.getMessage());
            return null; // TODO: throw custom exception here and handle response in handleRequest - set status code accordingly
        }
    }

    private void initDynamoDbClient()
    {
        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        client.setRegion(Region.getRegion(REGION));
        this.dynamoDB = new DynamoDB(client);
    }
}
