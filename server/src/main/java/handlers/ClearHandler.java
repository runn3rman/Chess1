package handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.ErrorResponse;
import service.ClearService;
import spark.Request;
import spark.Response;

public class ClearHandler {
    private ClearService clearService;
    private Gson gson = new Gson();

    public ClearHandler(ClearService clearService) {
        this.clearService = clearService;
    }

    public Object handleRequest(Request req, Response res) {
        try {
            // Call clearAllData to clear the database
            clearService.clearAllData();

            res.status(200); // OK status
            return "{}"; // Empty JSON object as a success response
        } catch (DataAccessException e) {
            res.status(500); // Internal Server Error
            return gson.toJson(new ErrorResponse("Error clearing the database: " + e.getMessage()));
        }
    }
}
