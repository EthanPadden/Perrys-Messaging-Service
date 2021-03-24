package com.perrys.RequestHandlers;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.perrys.DBObjects.Message;
import com.perrys.ErrorMessages;
import com.perrys.GatewayResponse;

import java.sql.Timestamp;

public class UpdateMessageHandler implements RequestHandler<Message, GatewayResponse> {
    private DynamoDB dynamoDB;
    private String DYNAMODB_TABLE_NAME = "Messages";
    private Regions REGION = Regions.EU_WEST_1;

    @Override
    public GatewayResponse handleRequest(Message message, Context context)
    {
        // Create response object
        GatewayResponse response;

        try {
            // Validate input
            if(message.getMessageId() == null) throw new IllegalArgumentException();
            if(message.getMessageId().compareTo("") == 0) throw new IllegalArgumentException();
            if(message.getBody() == null) throw new IllegalArgumentException();
            if(message.getBody().compareTo("") == 0) throw new IllegalArgumentException();

            // Initialise DynamoDB client
            AmazonDynamoDBClient client = new AmazonDynamoDBClient();
            client.setRegion(Region.getRegion(REGION));
            this.dynamoDB = new DynamoDB(client);

            // Get the table from the DB object
            Table table = dynamoDB.getTable(DYNAMODB_TABLE_NAME);


            // Get current timestamp
            // TODO: move to empty constructor and set as member variable?
            Timestamp timestampObj = new Timestamp(System.currentTimeMillis());
            long timestamp = timestampObj.getTime();

            // Cannot use literals in update expression
            // Use value map to map values
            String updateExpn = "set body = :b, lastUpdated = :t";
            ValueMap valueMap = new ValueMap()
                    .withString(":b", message.getBody())
                    .withNumber(":t", timestamp);

            // Build updateItemSpec
            UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                    .withPrimaryKey("messageId", message.getMessageId())
                    .withUpdateExpression(updateExpn)
                    .withValueMap(valueMap)
                    .withReturnValues(ReturnValue.UPDATED_NEW);

            // Update item in database
            table.updateItem(updateItemSpec);

            // Set new timestamp of input message
            message.setLastUpdated(timestamp);

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
