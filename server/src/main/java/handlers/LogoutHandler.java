package handlers;

import com.google.gson.Gson;
import dataaccess.AuthTokenDaoInterface;
import model.ErrorResponse;
import service.LogoutService;
import spark.Request;
import spark.Response;

public class LogoutHandler {
    private LogoutService logoutService;
    private final AuthTokenDaoInterface authTokenDao;
    private Gson gson = new Gson();

    public LogoutHandler(LogoutService logoutService, AuthTokenDaoInterface authTokenDao) {
        this.logoutService = logoutService;
        this.authTokenDao = authTokenDao;
    }

    public Object handleRequest(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");
            if (authToken == null || authToken.isEmpty() || !authTokenDao.isValidAuthToken(authToken)) {
                res.status(401); // Unauthorized
                return gson.toJson(new ErrorResponse("Error: unauthorized"));
            }

            logoutService.logout(authToken);

            res.status(200); // OK
            return "{}"; // Empty JSON object as a success response
        } catch (Exception e) {
            res.status(500); // Internal Server Error
            // Ensure the response type is set to application/json for error messages
            res.type("application/json");
            // Return a JSON object containing the error message
            return gson.toJson(new ErrorResponse(e.getMessage()));
        }
    }
}
