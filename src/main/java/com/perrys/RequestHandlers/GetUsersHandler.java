package com.perrys.RequestHandlers;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.Attribute;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.JsonObject;
import com.perrys.GatewayResponse;
import org.apache.http.client.CredentialsProvider;

import java.util.List;
import java.util.Map;

public class GetUsersHandler implements RequestHandler<Object, GatewayResponse> {
    private DynamoDB dynamoDB;
    private AmazonDynamoDBClient client;
    private String DYNAMODB_TABLE_NAME = "Users";
    private Regions REGION = Regions.EU_WEST_1;

    @Override
    public GatewayResponse handleRequest(Object o, Context context)
    {
        this.initDynamoDbClient();
        Table table = dynamoDB.getTable("Users");
        ScanRequest scanRequest = new ScanRequest().withTableName("Users");
        ScanResult scanResult = client.scan(scanRequest);
        List<Map<String, AttributeValue>> items = scanResult.getItems();
        JsonObject jsonUsersList = new JsonObject();
        for (int i = 0; i < items.size(); i++) {
            String userId = items.get(i).get("userId").toString();
            String username = items.get(i).get("username").toString();
            JsonObject jsonUser = new JsonObject();
            jsonUser.addProperty("userId", userId);
            jsonUser.addProperty("username", username);
            jsonUsersList.add(Integer.toString(i), jsonUser);
        }

        GatewayResponse response = new GatewayResponse(jsonUsersList.toString(), 200);
        return response;
    }

    private void initDynamoDbClient()
    {
        client = new AmazonDynamoDBClient();
        client.setRegion(Region.getRegion(REGION));
        this.dynamoDB = new DynamoDB(client);
    }
}
