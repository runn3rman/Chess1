package dataaccess;

import model.UserData;

import java.sql.SQLException;

public interface UserDaoInterface {
    void addUser(String username, String password, String email) throws SQLException, DataAccessException;

    String getPassword(String username) throws SQLException, DataAccessException;

    boolean userExists(String username) throws SQLException, DataAccessException;

    void clearUsers();
    UserData getUser(String username) throws DataAccessException;
    void insertUser(UserData user) throws DataAccessException;
    // Define other necessary methods here
}
