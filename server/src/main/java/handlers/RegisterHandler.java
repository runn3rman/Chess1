package handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import model.ErrorResponse;
import model.RegisterRequest;
import model.UserData;
import service.RegisterService;
import spark.Request;
import spark.Response;

public class RegisterHandler {
    private final RegisterService registerService = new RegisterService();
    private final Gson gson = new Gson();

    public Object handleRequest(Request req, Response res) {
        RegisterRequest registerRequest = gson.fromJson(req.body(), RegisterRequest.class);
        // Validate the request fields
        if (registerRequest.getUsername() == null || registerRequest.getPassword() == null || registerRequest.getEmail() == null) {
            res.status(400); // Bad Request status
            return gson.toJson(new ErrorResponse("Error: bad request"));
        }

        try {
            UserData newUser = new UserData(
                    registerRequest.getUsername(),
                    registerRequest.getPassword(),
                    registerRequest.getEmail()
            );
            AuthData authData = registerService.register(newUser);

            res.status(200); // OK status
            return gson.toJson(authData);
        } catch (DataAccessException e) {
            if (e.getMessage().equals("Username is already taken")) {
                res.status(403); // Forbidden status
                return gson.toJson(new ErrorResponse("Error: already taken"));
            } else {
                res.status(400); // Bad Request status
                return gson.toJson(new ErrorResponse("Error: bad request"));
            }
        } catch (Exception e) {
            res.status(500); // Internal Server Error status
            return gson.toJson(new ErrorResponse("Internal server error"));
        }
    }
}
