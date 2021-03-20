package com.perrys;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class DeleteMessageHandler implements RequestHandler<String, GatewayResponse> {
    private DynamoDB dynamoDB;
    private String DYNAMODB_TABLE_NAME = "Messages";
    private Regions REGION = Regions.EU_WEST_1;

    @Override
    public GatewayResponse handleRequest(String messageId, Context context)
    {
        this.initDynamoDbClient();
        GatewayResponse personResponse = new GatewayResponse("Message updated", 200);
        deleteData(messageId);
        return personResponse;
    }

    private DeleteItemOutcome deleteData(String messageId)
    {
        // Get the table from the DB object
        Table table = dynamoDB.getTable(DYNAMODB_TABLE_NAME);

        DeleteItemSpec deleteItemSpec = new DeleteItemSpec()
                .withPrimaryKey(new PrimaryKey("messageId", messageId));

        try {
            System.out.println("Attempting a conditional delete...");
            DeleteItemOutcome deleteItemOutcome = table.deleteItem(deleteItemSpec);
            System.out.println("DeleteItem succeeded");
            return deleteItemOutcome;
        }
        catch (Exception e) {
            System.err.println("Unable to delete item");
            System.err.println(e.getMessage());
            return null;
        }
    }

    private void initDynamoDbClient()
    {
        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        client.setRegion(Region.getRegion(REGION));
        this.dynamoDB = new DynamoDB(client);
    }
}
