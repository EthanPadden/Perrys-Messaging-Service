import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.perrys.ErrorMessages;
import com.perrys.DBObjects.Message;
import com.perrys.GatewayResponse;
import com.perrys.RequestHandlers.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.Alphanumeric.class)public class MessageTests {
    @Test
    public void CRUDMessageSuccess()
    {
        /** Create message test */
        // Set AWS credentials
        System.setProperty("aws.accessKeyId", Credentials.AWS_ACCESS_KEY_ID);
        System.setProperty("aws.secretKey", Credentials.AWS_SECRET_KEY);

        // Create new message object to insert into the database
        Message messageRequest = new Message();
        messageRequest.setSenderUserId("123-abc");
        messageRequest.setRecipientUserId("456-def");
        messageRequest.setBody("Hello");

        // Make request
        CreateMessageHandler createMessageHandler = new CreateMessageHandler();
        GatewayResponse response = createMessageHandler.handleRequest(messageRequest, null);

        // Verify status code
        Assert.assertEquals(new Integer(200), response.getStatusCode());

        // Verify message properties returned
        Message messageResponse = (Message) response.getBody();
        Assert.assertEquals(messageRequest.getSenderUserId(), messageResponse.getSenderUserId());
        Assert.assertEquals(messageRequest.getRecipientUserId(), messageResponse.getRecipientUserId());
        Assert.assertEquals(messageRequest.getBody(), messageResponse.getBody());

        /** Get messages test */
        GetMessagesHandler getMessagesHandler = new GetMessagesHandler();
        response = getMessagesHandler.handleRequest(null, null);

        Assert.assertEquals(new Integer(200), response.getStatusCode());

        JsonArray jsonArray = (JsonArray) response.getBody();
        Assert.assertNotEquals(0, jsonArray.size());

        // Verify message is added
        // TODO: compare fields
        boolean messageExists = false;
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (jsonObject.get("messageId").getAsString().compareTo(messageResponse.getMessageId()) == 0) {
                messageExists = true;
                break;
            }
        }
        Assert.assertTrue(messageExists);

        /** Update message test */
        UpdateMessageHandler updateMessageHandler = new UpdateMessageHandler();

        // Update message with new body
        messageRequest.setBody("New body");
        messageRequest.setMessageId(messageResponse.getMessageId());
        // Get current timestamp from message
        long currentTS = messageRequest.getLastUpdated();

        // Make request
        response = updateMessageHandler.handleRequest(messageRequest, null);

        Assert.assertEquals(new Integer(200), response.getStatusCode());
        // Verify message properties returned
        messageResponse = (Message) response.getBody();
        Assert.assertEquals(messageRequest.getMessageId(), messageResponse.getMessageId());
        Assert.assertEquals(messageRequest.getSenderUserId(), messageResponse.getSenderUserId());
        Assert.assertEquals(messageRequest.getRecipientUserId(), messageResponse.getRecipientUserId());
        Assert.assertEquals(messageRequest.getBody(), messageResponse.getBody());
        Assert.assertTrue(messageResponse.getLastUpdated() > currentTS);

        /** Delete message test */
        DeleteMessageHandler deleteMessageHandler = new DeleteMessageHandler();

        // Make request
        response = deleteMessageHandler.handleRequest(messageRequest, null);

        Assert.assertEquals(new Integer(200), response.getStatusCode());

        // Verify message properties returned
        messageResponse = (Message) response.getBody();
        Assert.assertEquals(messageRequest.getMessageId(), messageResponse.getMessageId());

        response = getMessagesHandler.handleRequest(null, null);

        Assert.assertEquals(new Integer(200), response.getStatusCode());

        // Can only be true if there are messages in the database
        jsonArray = (JsonArray) response.getBody();

        // Verify message is added
        // TODO: compare fields
        messageExists = false;
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (jsonObject.get("messageId").getAsString().compareTo(messageRequest.getMessageId()) == 0) {
                messageExists = true;
                break;
            }
        }
        Assert.assertFalse(messageExists);
    }

    @Test
    public void createMessageInvalid() {
        /** Create message test */
        // Set AWS credentials
        System.setProperty("aws.accessKeyId", Credentials.AWS_ACCESS_KEY_ID);
        System.setProperty("aws.secretKey", Credentials.AWS_SECRET_KEY);

        // Create new message object to insert into the database
        Message messageNoSender = new Message();
        messageNoSender.setRecipientUserId("456-def");
        messageNoSender.setBody("Hello");

        Message messageNoRecipient = new Message();
        messageNoRecipient.setSenderUserId("123-abc");
        messageNoRecipient.setBody("Hello");

        Message messageNoBody = new Message();
        messageNoBody.setSenderUserId("123-abc");
        messageNoBody.setRecipientUserId("456-def");


        // Make request
        CreateMessageHandler createMessageHandler = new CreateMessageHandler();
        GatewayResponse responseNoSender = createMessageHandler.handleRequest(messageNoSender, null);
        GatewayResponse responseNoRecipient = createMessageHandler.handleRequest(messageNoRecipient, null);
        GatewayResponse responseNoBody = createMessageHandler.handleRequest(messageNoBody, null);

        // Verify status code
        Assert.assertEquals(new Integer(400), responseNoSender.getStatusCode());
        Assert.assertEquals(new Integer(400), responseNoRecipient.getStatusCode());
        Assert.assertEquals(new Integer(400), responseNoBody.getStatusCode());

        // Verify message properties returned
        Assert.assertEquals(ErrorMessages.MESSAGE_INVALID_INPUT, responseNoSender.getBody());
        Assert.assertEquals(ErrorMessages.MESSAGE_INVALID_INPUT, responseNoRecipient.getBody());
        Assert.assertEquals(ErrorMessages.MESSAGE_INVALID_INPUT, responseNoBody.getBody());
    }

    @Test
    public void updateMessageInvalid() {
        /** Create message */
        // Set AWS credentials
        System.setProperty("aws.accessKeyId", Credentials.AWS_ACCESS_KEY_ID);
        System.setProperty("aws.secretKey", Credentials.AWS_SECRET_KEY);

        // Create new message object to insert into the database
        Message messageRequest = new Message();
        messageRequest.setSenderUserId("123-abc");
        messageRequest.setRecipientUserId("456-def");
        messageRequest.setBody("Hello");

        // Make request
        CreateMessageHandler createMessageHandler = new CreateMessageHandler();
        GatewayResponse response = createMessageHandler.handleRequest(messageRequest, null);
        String messageId = ((Message)response.getBody()).getMessageId();

        // Create new message object to insert into the database
        Message messageNoId = new Message();
        messageNoId.setBody("Hello");

        Message messageWrongId = new Message();
        messageWrongId.setMessageId("Not an ID");
        messageWrongId.setBody("New body");

        Message messageNoBody = new Message();
        messageNoBody.setMessageId(messageId);


        // Make request
        UpdateMessageHandler updateMessageHandler = new UpdateMessageHandler();
        GatewayResponse responseNoSender = createMessageHandler.handleRequest(messageNoId, null);
        GatewayResponse responseNoRecipient = createMessageHandler.handleRequest(messageWrongId, null);
        GatewayResponse responseNoBody = createMessageHandler.handleRequest(messageNoBody, null);

        // Verify status code
        Assert.assertEquals(new Integer(400), responseNoSender.getStatusCode());
        Assert.assertEquals(new Integer(400), responseNoRecipient.getStatusCode());
        Assert.assertEquals(new Integer(400), responseNoBody.getStatusCode());

        // Verify message properties returned
        Assert.assertEquals(ErrorMessages.MESSAGE_INVALID_INPUT, responseNoSender.getBody());
        Assert.assertEquals(ErrorMessages.MESSAGE_INVALID_INPUT, responseNoRecipient.getBody());
        Assert.assertEquals(ErrorMessages.MESSAGE_INVALID_INPUT, responseNoBody.getBody());
    }
}
