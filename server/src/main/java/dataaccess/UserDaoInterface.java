package dataaccess;

import model.UserData;

public interface UserDaoInterface {
    void clearUsers();
    UserData getUser(String username);
    void insertUser(UserData user) throws DataAccessException;
    // Define other necessary methods here
}
