package service;

import dataaccess.GameDaoInterface;
import model.GameData;

import java.util.List;

public class GameService {
    private GameDaoInterface gameDao;

    public GameService(GameDaoInterface gameDao) {
        this.gameDao = gameDao;
    }

    public List<GameData> listGames() {

        return gameDao.listGames();
    }
}
