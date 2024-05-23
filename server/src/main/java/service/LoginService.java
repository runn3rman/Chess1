package service;

import dataaccess.AuthTokenDaoInterface;
import dataaccess.UserDaoInterface;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class LoginService {
    private UserDaoInterface userDao;
    private AuthTokenDaoInterface authTokenDao;

    public LoginService(UserDaoInterface userDao, AuthTokenDaoInterface authTokenDao) {
        this.userDao = userDao;
        this.authTokenDao = authTokenDao;
    }

    public AuthData login(String username, String password) throws Exception {
        UserData user = userDao.getUser(username);

        // Verify the password with the hashed password stored in the database
        if (user != null) {
            System.out.println("Stored Hashed Password: " + user.password()); // Log the stored hashed password
            if (BCrypt.checkpw(password, user.password())) {
                // Generate a new auth token for the session
                String authToken = UUID.randomUUID().toString();
                AuthData authData = new AuthData(authToken, username);

                // Insert the new auth token into the database
                authTokenDao.insertAuthToken(authData);
                return authData;
            }
        }
        // Throw an exception if the username doesn't exist or the password doesn't match
        throw new Exception("Invalid username or password");
    }
}

