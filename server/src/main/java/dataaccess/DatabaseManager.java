package dataaccess;

import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    private static final String DATABASE_NAME;
    private static final String USER;
    private static final String PASSWORD;
    private static final String CONNECTION_URL;

    /*
     * Load the database information from the db.properties file.
     */
    static {
        try {
            try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
                if (propStream == null) throw new Exception("Unable to load db.properties");
                Properties props = new Properties();
                props.load(propStream);
                DATABASE_NAME = props.getProperty("db.name");
                USER = props.getProperty("db.user");
                PASSWORD = props.getProperty("db.password");

                var host = props.getProperty("db.host");
                var port = Integer.parseInt(props.getProperty("db.port"));
                CONNECTION_URL = String.format("jdbc:mysql://%s:%d", host, port);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to process db.properties. " + ex.getMessage());
        }
    }

    /**
     * Creates the database if it does not already exist.
     */
    public static void createDatabase() throws DataAccessException {
        try {
            var statement = "CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME;
            var conn = DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD);
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    /**
     * Create a connection to the database and sets the catalog based upon the
     * properties specified in db.properties. Connections to the database should
     * be short-lived, and you must close the connection when you are done with it.
     * The easiest way to do that is with a try-with-resource block.
     * <br/>
     * <code>
     * try (var conn = DbInfo.getConnection(databaseName)) {
     * // execute SQL statements.
     * }
     * </code>
     */
    public static Connection getConnection() throws DataAccessException {
        try {
            var conn = DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD);
            conn.setCatalog(DATABASE_NAME);
            return conn;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    /**
     * Initializes the database by creating the necessary tables if they do not already exist.
     */
    public static void initializeDatabase() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "username VARCHAR(255) NOT NULL UNIQUE," +
                    "password VARCHAR(255) NOT NULL" +
                    ")";

            String createGamesTable = "CREATE TABLE IF NOT EXISTS games (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "game_name VARCHAR(255) NOT NULL," +
                    "white_username VARCHAR(255)," +
                    "black_username VARCHAR(255)," +
                    "state TEXT NOT NULL," +
                    "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")";
            //String dropAuthTokensTable = "DROP TABLE IF EXISTS auth_tokens";
            String createAuthTokensTable = "CREATE TABLE IF NOT EXISTS auth_tokens (" +
                    "token VARCHAR(255) PRIMARY KEY," +
                    "username VARCHAR(255) NOT NULL" +
                    ")";

            stmt.execute(createUsersTable);
            stmt.execute(createGamesTable);
            //stmt.execute(dropAuthTokensTable);
            stmt.execute(createAuthTokensTable);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
