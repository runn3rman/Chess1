package dataaccess;

import model.UserData;

import java.sql.SQLException;
import java.util.HashMap;

public class MemoryUserDao implements UserDaoInterface {
    private static final HashMap<String, UserData> USERS = new HashMap<>();


    @Override
    public void clearUsers() {
        USERS.clear();
    }

    @Override
    public UserData getUser(String username) {
        return USERS.get(username);
    }

    @Override
    public void insertUser(UserData user) throws DataAccessException {
        if (USERS.containsKey(user.username())) {
            throw new DataAccessException("User already exists");
        }
        USERS.put(user.username(), user);
    }



    //
}



