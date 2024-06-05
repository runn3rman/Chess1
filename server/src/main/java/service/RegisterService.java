package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class RegisterService {
    private UserDaoInterface userDao = new SqlUserDao();
    private AuthTokenDaoInterface authTokenDao = new SqlAuthTokenDao();


    public AuthData register(UserData newUser) throws DataAccessException {
        // Check if user already exists
        UserData existingUser = userDao.getUser(newUser.username());
        if (existingUser != null) {
            throw new DataAccessException("Username is already taken");
        }

        // Hash the user's password
        String hashedPassword = BCrypt.hashpw(newUser.password(), BCrypt.gensalt());
        System.out.println("Hashed Password: " + hashedPassword); // Log the hashed password for verification

        // Create a new UserData object with the hashed password
        UserData userWithHashedPassword = new UserData(newUser.username(), hashedPassword, newUser.email());

        // If user does not exist, use the DAO to insert the new user with hashed password
        userDao.insertUser(userWithHashedPassword);

        // Generate an authToken for the user
        String authToken = generateAuthToken();

        // Store the authToken associated with the user
        AuthData authData = new AuthData(authToken, newUser.username());
        authTokenDao.insertAuthToken(authData);

        return authData;
    }

    private String generateAuthToken() {
        // Generate a unique authToken using UUID
        return UUID.randomUUID().toString();
    }
}

