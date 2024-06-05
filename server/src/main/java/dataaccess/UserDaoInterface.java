package dataaccess;

import model.UserData;

import java.sql.SQLException;

public interface UserDaoInterface {

    void clearUsers();
    UserData getUser(String username) throws DataAccessException;
    void insertUser(UserData user) throws DataAccessException;
    // Define other necessary methods here
}
