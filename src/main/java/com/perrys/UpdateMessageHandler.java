package com.perrys;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.sql.Timestamp;
import java.util.UUID;

public class UpdateMessageHandler implements RequestHandler<UpdateMessage, GatewayResponse> {
    private DynamoDB dynamoDB;
    private String DYNAMODB_TABLE_NAME = "Messages";
    private Regions REGION = Regions.EU_WEST_1;

    @Override
    public GatewayResponse handleRequest(UpdateMessage message, Context context)
    {
        this.initDynamoDbClient();
        updateData(message.getMessageId(), message.getBody());
        GatewayResponse personResponse = new GatewayResponse("Message updated", 200);
        return personResponse;
    }

    private UpdateItemOutcome updateData(String messageId, String newMessageBody)
    {
        // Get the table from the DB object
        Table table = dynamoDB.getTable(DYNAMODB_TABLE_NAME);

        String updateExpn = "set body = :b";
        ValueMap valueMap = new ValueMap().withString(":b", newMessageBody);
        UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                .withPrimaryKey("messageId", messageId)
                .withUpdateExpression(updateExpn)
                .withValueMap(valueMap)
                .withReturnValues(ReturnValue.UPDATED_NEW);

        try {
            System.out.println("Updating the item...");
            UpdateItemOutcome outcome = table.updateItem(updateItemSpec);
            System.out.println("UpdateItem succeeded:\n" + outcome.getItem().toJSONPretty());
            return outcome;
        } catch (Exception e) {
            System.err.println("Unable to update item");
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
