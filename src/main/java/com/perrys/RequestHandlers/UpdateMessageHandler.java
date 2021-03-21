package com.perrys.RequestHandlers;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.perrys.GatewayResponse;
import com.perrys.RequestObjects.UpdateMessageRequest;

public class UpdateMessageHandler implements RequestHandler<UpdateMessageRequest, GatewayResponse> {
    private DynamoDB dynamoDB;
    private String DYNAMODB_TABLE_NAME = "Messages";
    private Regions REGION = Regions.EU_WEST_1;

    @Override
    public GatewayResponse handleRequest(UpdateMessageRequest message, Context context)
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

            // Cannot use literals in update expression
            // Use value map to map values
            String updateExpn = "set body = :b";
            ValueMap valueMap = new ValueMap().withString(":b", message.getBody());

            // Build updateItemSpsc
            UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                    .withPrimaryKey("messageId", message.getMessageId())
                    .withUpdateExpression(updateExpn)
                    .withValueMap(valueMap)
                    .withReturnValues(ReturnValue.UPDATED_NEW);

            // Update item in database
            table.updateItem(updateItemSpec);

            response = new GatewayResponse("Message updated", 200);
        } catch (IllegalArgumentException e) {
            response = new GatewayResponse("There was an error in the input", 400);
        } catch (Exception e) {
            response = new GatewayResponse("There was an error accessing the database", 500);
        }

        return response;
    }
}
