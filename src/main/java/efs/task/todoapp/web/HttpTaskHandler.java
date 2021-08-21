package efs.task.todoapp.web;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import efs.task.todoapp.ToDoApplication;
import efs.task.todoapp.repository.TaskEntity;
import efs.task.todoapp.service.*;
import efs.task.todoapp.service.exceptions.BadRequestException;
import efs.task.todoapp.service.exceptions.NotRequiredUserException;
import efs.task.todoapp.service.exceptions.NotRightsToTaskException;
import efs.task.todoapp.service.exceptions.TaskNotFoundException;

import static efs.task.todoapp.web.ResponseCodes.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.logging.Logger;


public class HttpTaskHandler implements HttpHandler {
    private static final Logger LOGGER = Logger.getLogger(ToDoApplication.class.getName());
    private static final String idPattern =
            "/todo/task/[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}";
    private static final String base64Pattern = "^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$";
    private static final Gson gson = new Gson();
    private final ToDoService service;
    private String auth = null;

    public HttpTaskHandler(ToDoService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String uri = exchange.getRequestURI().toString();
            String response = "";
            auth = exchange.getRequestHeaders().getFirst("auth");

            if (uri.matches("/todo/task")) {
                switch (exchange.getRequestMethod()) {
                    case "POST":
                        response = postHandle(exchange);
                        exchange.sendResponseHeaders(CREATED.getResponseCode(), response.length());
                        break;
                    case "GET":
                        response = getHandle();
                        exchange.sendResponseHeaders(OK.getResponseCode(), response.length());
                        break;
                    default:
                        exchange.sendResponseHeaders(BAD_REQUEST.getResponseCode(), 0);
                }
            } else if (uri.matches(idPattern)) {
                switch (exchange.getRequestMethod()) {
                    case "GET":
                        response = getWithIdHandle(exchange);
                        exchange.sendResponseHeaders(OK.getResponseCode(), response.length());
                        break;
                    case "DELETE":
                        deleteHandle(exchange);
                        exchange.sendResponseHeaders(OK.getResponseCode(), 0);
                        break;
                    case "PUT":
                        response = putHandle(exchange);
                        exchange.sendResponseHeaders(OK.getResponseCode(), response.length());
                        break;
                    default:
                        exchange.sendResponseHeaders(BAD_REQUEST.getResponseCode(), 0);
                }
            } else {
                exchange.sendResponseHeaders(BAD_REQUEST.getResponseCode(), 0);
            }

            if (!response.isEmpty()) {
                OutputStream responseBody = exchange.getResponseBody();
                responseBody.write(response.getBytes());
                responseBody.close();
                exchange.close();
            }

        } catch (JsonSyntaxException | IllegalArgumentException | IOException | BadRequestException e) {
            exchange.sendResponseHeaders(BAD_REQUEST.getResponseCode(), 0);
        } catch (NotRequiredUserException e) {
            exchange.sendResponseHeaders(UNAUTHORIZED.getResponseCode(), 0);
        } catch (NotRightsToTaskException e) {
            exchange.sendResponseHeaders(FORBIDDEN.getResponseCode(), 0);
        } catch (TaskNotFoundException e) {
            exchange.sendResponseHeaders(NOT_FOUND.getResponseCode(), 0);
        } catch (Exception e) {
            LOGGER.warning("Unhandled exception");
            exchange.sendResponseHeaders(BAD_REQUEST.getResponseCode(), 0);
        } finally {
            exchange.close();
        }
    }

    public String postHandle(HttpExchange exchange) throws NotRequiredUserException, IOException, BadRequestException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        List<String> userData = handleTaskHeader(auth);
        TaskEntity task = gson.fromJson(requestBody, TaskEntity.class);

        if (userData.isEmpty() || service.isTaskValid(task)) {
            throw new BadRequestException("Auth or userData are not valid!");
        }
        if (service.isUserFromHeaderValid(userData)) {
            throw new NotRequiredUserException("User not found or user's password is incorrect!");
        }
        UUID taskId = service.saveTask(task, userData);
        return "{ \"id\": \"" + taskId.toString() + "\"}";
    }

    public String getHandle() throws NotRequiredUserException, BadRequestException {
        List<String> userData = handleTaskHeader(auth);
        if (userData.isEmpty()) {
            throw new BadRequestException("Auth or userData are not valid!");
        } else if (this.service.isUserFromHeaderValid(userData)) {
            throw new NotRequiredUserException("User not found or user's password is incorrect!");
        } else {
            List<TaskEntity> listOfTasks = this.service.getTasksList(userData.get(0));
            StringBuilder response = new StringBuilder("[");

            for (TaskEntity listOfTask : listOfTasks) {
                response.append("{\"id\":\"").append(listOfTask.getId()).append("\",");
                response.append("\"description\":\"").append(listOfTask.getDescription()).append("\"");
                if (listOfTask.getDue() != null)
                    response.append(",").append("\"due\":\"").append(listOfTask.getDue()).append("\"");
                response.append("}");
                response.append(",");
            }
            response.deleteCharAt(response.length() - 1);
            response.append("]");
            return response.toString();
        }
    }

    public String getWithIdHandle(HttpExchange exchange) throws NotRequiredUserException,
            BadRequestException, TaskNotFoundException, NotRightsToTaskException {
        String path = exchange.getRequestURI().toString();
        List<String> userData = handleTaskHeader(auth);
        String id = path.split("/")[3];
        if (userData.isEmpty()) {
            throw new BadRequestException("User Data is empty!");
        } else if (this.service.isUserFromHeaderValid(userData)) {
            throw new NotRequiredUserException("User not found or user's password is incorrect!");
        } else {
            List<TaskEntity> listOfTasks = this.service.getTasksList(userData.get(0));
            TaskEntity task = service.getTask(id);

            if (task == null) {
                throw new TaskNotFoundException("Task doesn't exists!");
            }

            if (!listOfTasks.contains(task)) {
                throw new NotRightsToTaskException("You haven't got rights to this task!");
            }
            String response = "" + "{\"id\":\"" + task.getId() + "\"," +
                    "\"description\":\"" + task.getDescription() + "\"";
            if (task.getDue() != null) {
                response += ",\"due\":\"" + task.getDue() + "\"";
            }
            response += "}";

            return response;
        }
    }

    public String putHandle(HttpExchange exchange) throws IOException, BadRequestException,
            NotRequiredUserException, TaskNotFoundException, NotRightsToTaskException {
        String path = exchange.getRequestURI().toString();
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        List<String> userData = handleTaskHeader(auth);
        TaskEntity task = gson.fromJson(requestBody, TaskEntity.class);
        String id = path.split("/")[3];

        if (userData.isEmpty() || service.isTaskValid(task)) {
            throw new BadRequestException("User Data is empty!");
        } else if (this.service.isUserFromHeaderValid(userData)) {
            throw new NotRequiredUserException("User not found or user's password is incorrect!");
        } else {
            List<TaskEntity> listOfTasks = this.service.getTasksList(userData.get(0));
            TaskEntity oldTask = service.getTask(id);

            if (oldTask == null) {
                throw new TaskNotFoundException("Task doesn't exists!");
            }

            if (!listOfTasks.contains(oldTask)) {
                throw new NotRightsToTaskException("You haven't got rights to this task!");
            }

            TaskEntity newTask = service.updateTask(task, userData, UUID.fromString(id));

            String response = "" + "{\"id\":\"" + newTask.getId() + "\"," +
                    "\"description\":\"" + newTask.getDescription() + "\"";
            if (newTask.getDue() != null) {
                response += ",\"due\":\"" + newTask.getDue() + "\"";
            }
            response += "}";
            return response;
        }
    }

    public void deleteHandle(HttpExchange exchange) throws BadRequestException,
            NotRequiredUserException, TaskNotFoundException, NotRightsToTaskException {
        String path = exchange.getRequestURI().toString();
        List<String> userData = handleTaskHeader(auth);
        String id = path.split("/")[3];
        if (userData.isEmpty()) {
            throw new BadRequestException("User Data is empty!");
        } else if (this.service.isUserFromHeaderValid(userData)) {
            throw new NotRequiredUserException("User not found or user's password is incorrect!");
        } else {
            List<TaskEntity> listOfTasks = this.service.getTasksList(userData.get(0));
            TaskEntity task = service.getTask(id);

            if (task == null) {
                throw new TaskNotFoundException("Task doesn't exists!");
            }

            if (!listOfTasks.contains(task)) {
                throw new NotRightsToTaskException("You haven't got rights to this task!");
            }

            service.deleteTask(UUID.fromString(id));

        }
    }

    public List<String> handleTaskHeader(String auth) {

        try {
            if (auth == null) {
                return Collections.emptyList();
            } else {
                var decoder = Base64.getDecoder();
                String[] userData = auth.split(":");
                if (userData.length != 2) {
                    return Collections.emptyList();
                } else if (Arrays.asList(userData).contains("null")) {
                    return Collections.emptyList();
                } else {

                    for (String s : userData) {
                        if (s == null || !s.matches(base64Pattern)) {
                            return Collections.emptyList();
                        }

                    }
                    var decodedUsername = new String(decoder.decode(userData[0]));
                    var decodedPassword = new String(decoder.decode(userData[1]));

                    List<String> userDecodedData = new ArrayList<>();
                    userDecodedData.add(decodedUsername);
                    userDecodedData.add(decodedPassword);

                    return userDecodedData;
                }
            }
        } catch (Exception e) {
            return Collections.emptyList();
        }

    }
}