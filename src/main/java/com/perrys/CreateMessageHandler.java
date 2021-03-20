package com.perrys;

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

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

public class CreateMessageHandler implements RequestHandler<Message, GatewayResponse> {
    private DynamoDB dynamoDB;
    private String DYNAMODB_TABLE_NAME = "Messages";
    private Regions REGION = Regions.EU_WEST_1;

    @Override
    public GatewayResponse handleRequest(Message message, Context context)
    {
        this.initDynamoDbClient();
        persistData(message);
        GatewayResponse personResponse = new GatewayResponse("Message created", 200);
        return personResponse;
    }

    private PutItemOutcome persistData(Message messageRequest)
            throws ConditionalCheckFailedException
    {
        // Get the table from the DB object
        Table table = dynamoDB.getTable(DYNAMODB_TABLE_NAME);

        // Generate a random UUID as a string
        String uuid = UUID.randomUUID().toString(); // TODO: move to empty constructor and set as member variable?

        // Get current timestamp
        // TODO: move to empty constructor and set as member variable?
        Timestamp timestampObj = new Timestamp(System.currentTimeMillis());
//        String timestampStr = Long.toString(timestampObj.getTime());
        long timestamp = timestampObj.getTime();
        try {
            System.out.println("Creating message in database...");
            PutItemOutcome outcome = table
                    .putItem(new Item().withPrimaryKey("messageId", uuid)
                            .withString("senderUserId", messageRequest.getSenderUserId())
                            .withString("recipientUserId", messageRequest.getRecipientUserId())
                            .withNumber("timestamp", timestamp)
                            .withString("body", messageRequest.getBody()));
            System.out.println("PutItem succeeded:\n" + outcome.getPutItemResult());
            return outcome;
        } catch (Exception e) {
            System.err.println("Unable to add message");
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
