package handlers;

import com.google.gson.Gson;
import model.AuthData;
import model.AuthResponse;
import model.ErrorResponse;
import model.LoginRequest;
import service.LoginService;
import spark.Request;
import spark.Response;

public class LoginHandler {
    private LoginService loginService;
    private Gson gson = new Gson();

    public LoginHandler(LoginService loginService) {
        this.loginService = loginService;
    }

    public Object handleRequest(Request req, Response res) {
        try {
            LoginRequest loginRequest = gson.fromJson(req.body(), LoginRequest.class);
            AuthData authData = loginService.login(loginRequest.getUsername(), loginRequest.getPassword());

            res.status(200); // OK
            res.type("application/json"); // Ensure response type is set to application/json
            return gson.toJson(new AuthResponse(authData.username(), authData.authToken()));
        } catch (Exception e) {
            res.status(e.getMessage().equals("Invalid username or password") ? 401 : 500);
            res.type("application/json"); // Set Content-Type as application/json
            String errorMessage = e.getMessage().equals("Invalid username or password") ? "Error: unauthorized" : "Error: " + e.getMessage();
            ErrorResponse errorResponse = new ErrorResponse(errorMessage);
            return gson.toJson(errorResponse); // Use the errorResponse object for serialization
        }
    }
}
