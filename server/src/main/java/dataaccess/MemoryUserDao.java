package dataaccess;

import model.UserData;

import java.util.HashMap;

public class MemoryUserDao implements UserDaoInterface {
    private static final HashMap<String, UserData> Users = new HashMap<>();

    @Override
    public void clearUsers() {
        Users.clear();
    }

    @Override
    public UserData getUser(String username) {
        return Users.get(username);
    }

    @Override
    public void insertUser(UserData user) throws DataAccessException {
        if (Users.containsKey(user.username())) {
            throw new DataAccessException("User already exists");
        }
        Users.put(user.username(), user);
    }



    //
}



