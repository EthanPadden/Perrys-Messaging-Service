
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.perrys.Constants;
import com.perrys.GatewayResponse;
import com.perrys.RequestHandlers.CreateUserHandler;
import com.perrys.RequestHandlers.GetUsersHandler;
import com.perrys.DBObjects.User;
import org.junit.Assert;
import org.junit.jupiter.api.Test;


/**
 * Unit test for simple App.
 */
public class UserRequestTests {
    @Test
    public void createUserSuccess()
    {
        // Set AWS credentials
        System.setProperty("aws.accessKeyId", Credentials.AWS_ACCESS_KEY_ID);
        System.setProperty("aws.secretKey", Credentials.AWS_SECRET_KEY);

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

        // Make request to get users
        GetUsersHandler getUsersHandler = new GetUsersHandler();
        response = getUsersHandler.handleRequest(null, null);

        // Verify status code
        Assert.assertEquals(new Integer(200), response.getStatusCode());

        // Verify user is added
        JsonArray jsonArray = (JsonArray) response.getBody();
        boolean userExists = false;
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (jsonObject.get("username").getAsString().compareTo(userRequest.getUsername()) == 0) {
                userExists = true;
                break;
            }
        }
        Assert.assertTrue(userExists);

    }

    @Test
    public void createUserInvalidUsername()
    {
        System.setProperty("aws.accessKeyId", Credentials.AWS_ACCESS_KEY_ID);
        System.setProperty("aws.secretKey", Credentials.AWS_SECRET_KEY);

        CreateUserHandler createUserHandler = new CreateUserHandler();

        User userRequestUsernameNull = new User();
        userRequestUsernameNull.setUsername(null);
        User userRequestUsernameEmpty = new User();
        userRequestUsernameEmpty.setUsername("");

        GatewayResponse response = createUserHandler.handleRequest(userRequestUsernameNull, null);

        Assert.assertEquals(new Integer(400), response.getStatusCode());
        Assert.assertEquals(Constants.MESSAGE_INVALID_INPUT, response.getBody());

        response = createUserHandler.handleRequest(userRequestUsernameEmpty, null);

        Assert.assertEquals(new Integer(400), response.getStatusCode());
        Assert.assertEquals(Constants.MESSAGE_INVALID_INPUT, response.getBody());
    }

    @Test
    public void getUsersSuccess()
    {
        System.setProperty("aws.accessKeyId", Credentials.AWS_ACCESS_KEY_ID);
        System.setProperty("aws.secretKey", Credentials.AWS_SECRET_KEY);

        GetUsersHandler getUsersHandler = new GetUsersHandler();
//
        GatewayResponse response = getUsersHandler.handleRequest(null, null);
//
        Assert.assertEquals(new Integer(200), response.getStatusCode());

        // Can only be true if there are users in the database
        JsonArray jsonArray = (JsonArray) response.getBody();
        Assert.assertNotEquals(0, jsonArray.size());
//        Assert.assertEquals(Constants.MESSAGE_USER_CREATED, response.getBody());
    }
}
