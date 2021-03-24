
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.perrys.ErrorMessages;
import com.perrys.GatewayResponse;
import com.perrys.RequestHandlers.CreateUserHandler;
import com.perrys.RequestHandlers.GetUsersHandler;
import com.perrys.DBObjects.User;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class UserTests {
    private static AWSCredentials awsCredentials;
    private static AmazonDynamoDBClient client;
    private static String DB_USERS_TABLE_NAME = "Users";

    @Test
    @BeforeAll
    public static void setupTest()
    {
        // Set up credentials for interacting with database
        awsCredentials = new BasicAWSCredentials(
                Credentials.AWS_ACCESS_KEY_ID,
                Credentials.AWS_SECRET_KEY
        );
        client = new AmazonDynamoDBClient(awsCredentials);
        Regions REGION = Regions.EU_WEST_1;
        client.setRegion(Region.getRegion(REGION));

        // Set system properties
        System.setProperty("aws.accessKeyId", awsCredentials.getAWSAccessKeyId());
        System.setProperty("aws.secretKey", awsCredentials.getAWSSecretKey());
    }

    @Test
    public void createUserSuccess()
    {
        // Create new user object to insert into the database
        User userRequest = new User();
        userRequest.setUsername("Test user");

        // Make request
        CreateUserHandler createUserHandler = new CreateUserHandler();
        GatewayResponse response = createUserHandler.handleRequest(userRequest, null);

        // Verify status code
        Assert.assertEquals(new Integer(200), response.getStatusCode());

        // Verify username returned
        User userResponse = (User) response.getBody();
        Assert.assertEquals(userRequest.getUsername(), userResponse.getUsername());

        // Verify user exists in database
        verifyUserExistsInDatabase(userResponse, true);
    }

    @Test
    public void createUserInvalidUsername()
    {
        // Create user objects
        User userRequestUsernameNull = new User();
        userRequestUsernameNull.setUsername(null);
        User userRequestUsernameEmpty = new User();
        userRequestUsernameEmpty.setUsername("");

        // Make requests and verify responses
        CreateUserHandler createUserHandler = new CreateUserHandler();

        GatewayResponse response = createUserHandler.handleRequest(userRequestUsernameNull, null);
        Assert.assertEquals(new Integer(400), response.getStatusCode());
        Assert.assertEquals(ErrorMessages.MESSAGE_INVALID_INPUT, response.getBody());

        response = createUserHandler.handleRequest(userRequestUsernameEmpty, null);
        Assert.assertEquals(new Integer(400), response.getStatusCode());
        Assert.assertEquals(ErrorMessages.MESSAGE_INVALID_INPUT, response.getBody());
    }

    /**
     * NOTE: Test can only pass if users exist in the database
     * Keep sample data in table for testing purposes
     */
    @Test
    public void getUsersSuccess()
    {
        // Make request
        GetUsersHandler getUsersHandler = new GetUsersHandler();
        GatewayResponse response = getUsersHandler.handleRequest(null, null);

        // Verify status code
        Assert.assertEquals(new Integer(200), response.getStatusCode());

        // Can only be true if there are users in the database
        JsonArray jsonArray = (JsonArray) response.getBody();
        Assert.assertNotEquals(0, jsonArray.size());
    }

    private void verifyUserExistsInDatabase(User user, boolean userShouldExist) {
        /** Searching database to verify user added */
        // Execute scan request and retrieve a list of DB items
        ScanRequest scanRequest = new ScanRequest().withTableName(DB_USERS_TABLE_NAME);
        ScanResult scanResult = client.scan(scanRequest);
        List<Map<String, AttributeValue>> items = scanResult.getItems();

        boolean userExists = false;
        for (Map<String, AttributeValue> item : items) {
            try {
                String userId = item.get("userId").getS();
                String username = item.get("username").getS();

                if(userId.compareTo(user.getUserId()) == 0) {
                    userExists = true;

                    // Verify fields
                    Assert.assertEquals(user.getUsername(), username);
                }
            } catch (NullPointerException e) {
                // In this case, the message has been entered incorrectly into the database
                // i.e. Some fields are not filled out
                // We do not return malformed messages
            }
        }
        Assert.assertTrue(userExists == userShouldExist);
    }
}
