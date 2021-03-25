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
