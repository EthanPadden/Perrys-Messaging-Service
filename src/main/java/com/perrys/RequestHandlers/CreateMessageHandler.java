package com.perrys.RequestHandlers;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.perrys.GatewayResponse;
import com.perrys.RequestObjects.Message;

import java.sql.Timestamp;
import java.util.UUID;

public class CreateMessageHandler implements RequestHandler<Message, GatewayResponse> {
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

            // Generate a random UUID as a string
            String uuid = UUID.randomUUID().toString();

            // Get current timestamp
            // TODO: move to empty constructor and set as member variable?
            Timestamp timestampObj = new Timestamp(System.currentTimeMillis());
            long timestamp = timestampObj.getTime();

            Item newMessageDBItem = new Item()
                    .withPrimaryKey("messageId", uuid)
                    .withString("senderUserId", message.getSenderUserId())
                    .withString("recipientUserId", message.getRecipientUserId())
                    .withNumber("timestamp", timestamp)
                    .withString("body", message.getBody());

            // Put item into table
            table.putItem(newMessageDBItem);

            response = new GatewayResponse("Message created", 200);

        } catch (Exception e) {
            response = new GatewayResponse("There was an error accessing the database: ", 500);
        }

        return response;
    }


}
