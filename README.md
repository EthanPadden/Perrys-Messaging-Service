# Perrys-Messaging-Service
### Steps before using code
* The unit tests require an AWS access key ID and secret key 
* Replace the values for AWS_ACCESS_KEY_ID and AWS_SECRET_KEY in Credentials.java to those generated in the IAM console
* Alternatively: remove the integration tests (no other code requires this)
````
public class Credentials {
    public static String AWS_ACCESS_KEY_ID = "YOUR_ACCESS_KEY_ID";
    public static String AWS_SECRET_KEY = "YOUR_SECRET_KEY";
}
````

### Using the API
* The API is deployed at: https://3c0seyrda9.execute-api.eu-west-1.amazonaws.com/Production
*POST   /users/createuser*
* Parameters: 
    * username - string
*  Returns:
````
{
    "body": {
        "userId": STRING,
        "username": STRING
    },
    "statusCode": INT
}
````
The userId returned is generated by the Lambda function and can be used to referenced that user

*GET   /users/getusers*
* Parameters: (none)
*  Returns:
````
[
    {
        "userId": STRING,
        "username": STRING
    },
    ...etc
]
````
Returns all users in the database

*POST   /messages/createmessage*
* Parameters: 
    * senderUserId - string
    * recipientUserId - string
    * body - string
*  Returns:
````
{
    "body": {
        "senderUserId": STRING,
        "recipientUserId": STRING,
        "body": STRING,
        "messageId": STRING,
        "lastUpdated": LONG
    },
    "statusCode": INT
}
````
The messageId returned is generated by the Lambda function and can be used to referenced that user
The lastUpdated is the timestamp generated by the Lambda function that shows when the message was last changed
POSSIBLE IMPROVEMENT: For both the senderUserId and recipientUserId, check do users exist in the database with those userIds. If not, return an error message.

*GET   /messages/getmessages*
* Parameters: (none)
*  Returns:
````
[
    {
        "messageId": STRING,
        "lastUpdated": STRING,
        "body": STRING,
        "recipientUserId": STRING,
        "senderUserId": STRING
    },
    ...etc
]
````
Returns all messages in the database
POSSIBLE IMPROVEMENT: Return the timestamps as numbers rather than strings

*GET   /messages/getconversations*
* Parameters:
    * userId1- string
    * userId2- string
*  Returns:
````
{
    "body": STRING, // JSON array string with similar structure to getmessages response
    "statusCode": INT
}
````
Returns all messages in the database that were sent between the users with the input userIds.
POSSIBLE IMPROVEMENT:
* I did not manage to configure the response properly using the API gateway
* In some of the other endpoints, this was done using the Lambda proxy
* However, this endpoint does not use this - resulting in the body being returned as a string of JSON

*POST   /messages/updatemessage*
* Parameters: 
    * messageId - string
    * body - string
*  Returns:
````
{
    "body": {
        "body": STRING,
        "messageId": STRING,
        "lastUpdated": LONG
    },
    "statusCode": INT
}
````
The lastUpdated is the timestamp generated by the Lambda function that shows when the message was last changed
The body is the updated body of the message
POSSIBLE IMPROVEMENT:
* Currently, this does not check that a message with the corresponding messageId exists - it will return as above but will not make changes to the database
* An improvement would be to return an error message if this happens

*POST   /messages/deletemessage*
* Parameters: 
    * messageId - string
*  Returns:
````
{
    "body": {
        "messageId": STRING,
        "lastUpdated": 0
    },
    "statusCode": INT
}
````
The lastUpdated is the timestamp generated by the Lambda function that shows when the message was last changed
POSSIBLE IMPROVEMENT:
* Currently, this does not check that a message with the corresponding messageId exists - it will return as above but will not make changes to the database
* An improvement would be to return an error message if this happens
* Also, there is no need for lastUpdated to be returned (it will always return 0 for this endpoint)

## Overall Structure
### Database Structure
Table: Users
* userId - primary key, string
* username - string

Table: Messages
* messageId - primary key, string
* body - string
* recipientUserId - string
* senderUserId - string
* lastUpdated - number

**Issues and improvements**
* This solution is not actually scalable, for example:
    * The conversations handler gets all the messages between 2 users
    * It does this by scanning the database, filtering to the messages where
      (recipientUserId = userId1 AND senderUserId = userId2) OR (recipientUserId = userId2 AND senderUserId = userId1) 
      However, this operation would take a long time when the table contains a lot of messages
    * **A fix that I did not manage to implement in time would be to have database indexes on the senderUserId and recipientUserID fields. The lastUpdated could be the sort key, keepint the messages sorted by time so that they would not have to be sorted programatically by the client.**

### API structure
* Each API endpoint is a resource in the AWS API Gateway
* There is a Lambda function for each, which corresponds to an IAM role, which in turn has an inline policy for access to the resource.
* The code is uploaded to the S3 service as a .jar file and added from there to the Lambda functions.
