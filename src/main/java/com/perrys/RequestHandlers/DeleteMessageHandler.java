package com.perrys.RequestHandlers;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.perrys.DBObjects.Message;
import com.perrys.ErrorMessages;
import com.perrys.GatewayResponse;

public class DeleteMessageHandler implements RequestHandler<Message, GatewayResponse> {
    private DynamoDB dynamoDB;
    private String DYNAMODB_TABLE_NAME = "Messages";
    private Regions REGION = Regions.EU_WEST_1;

    @Override
    public GatewayResponse handleRequest(Message message, Context context)
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

            // Build deleteItemSpec
            DeleteItemSpec deleteItemSpec = new DeleteItemSpec()
                    .withPrimaryKey(new PrimaryKey("messageId", message.getMessageId()));

            // Delete item from database
            table.deleteItem(deleteItemSpec);

            response = new GatewayResponse(message, 200);
        } catch (IllegalArgumentException e) {
            response = new GatewayResponse(ErrorMessages.MESSAGE_INVALID_INPUT, 400);
        }  catch (AmazonDynamoDBException e) {
            if(e.getMessage().contains("ValidationException") || e.getMessage().contains("invalid value")) {
                response = new GatewayResponse(ErrorMessages.MESSAGE_INVALID_INPUT, 400);
            } else {
                response = new GatewayResponse(ErrorMessages.MESSAGE_ERROR_DB_ACCESS, 500);
            }
        } catch (Exception e) {
            response = new GatewayResponse(ErrorMessages.MESSAGE_ERROR_DB_ACCESS, 500);
        }

        return response;
    }
}
