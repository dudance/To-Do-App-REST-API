package efs.task.todoapp.web;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import efs.task.todoapp.ToDoApplication;
import efs.task.todoapp.repository.UserEntity;
import efs.task.todoapp.service.exceptions.BadRequestException;
import efs.task.todoapp.service.ToDoService;
import efs.task.todoapp.service.exceptions.UserExistsException;
import static efs.task.todoapp.web.ResponseCodes.*;
import java.io.IOException;
import java.util.logging.Logger;


public class HttpUserHandler implements HttpHandler {
    private static final Logger LOGGER = Logger.getLogger(ToDoApplication.class.getName());
    private final ToDoService service;

    public HttpUserHandler(ToDoService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Gson gson = new Gson();
        try {
            String responseBody = new String(exchange.getRequestBody().readAllBytes());
            UserEntity user = gson.fromJson(responseBody, UserEntity.class);
            service.saveUser(user);
            exchange.sendResponseHeaders(CREATED.getResponseCode(), 0);
        } catch (JsonSyntaxException | IllegalArgumentException | IOException | BadRequestException e) {
            exchange.sendResponseHeaders(BAD_REQUEST.getResponseCode(), 0);
        } catch (UserExistsException e) {
            exchange.sendResponseHeaders(CONFLICT.getResponseCode(), 0);
        } catch (Exception e) {
            LOGGER.warning("Unhandled exception");
        }
        finally {
            exchange.close();
        }

    }
}
