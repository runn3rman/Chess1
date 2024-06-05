package service;

import dataaccess.AuthTokenDaoInterface;
import dataaccess.UserDaoInterface;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class LoginService {
    private final UserDaoInterface userDao;
    private final AuthTokenDaoInterface authTokenDao;

    public LoginService(UserDaoInterface userDao, AuthTokenDaoInterface authTokenDao) {
        this.userDao = userDao;
        this.authTokenDao = authTokenDao;
    }

    public AuthData login(String username, String password) throws DataAccessException {
        UserData user = userDao.getUser(username);
        if (user != null) {
            System.out.println("Stored hashed password: " + user.password());
            System.out.println("Checking against: " + password);
            if (BCrypt.checkpw(password, user.password())) {
                String authToken = UUID.randomUUID().toString();
                AuthData authData = new AuthData(authToken, username);
                authTokenDao.insertAuthToken(authData);
                return authData;
            } else {
                System.out.println("Password does not match");
            }
        } else {
            System.out.println("User not found");
        }
        throw new DataAccessException("Invalid username or password");
    }

}
