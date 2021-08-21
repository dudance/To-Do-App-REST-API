package efs.task.todoapp.web;

import com.google.gson.Gson;
import efs.task.todoapp.repository.TaskEntity;
import efs.task.todoapp.repository.UserEntity;
import efs.task.todoapp.util.ToDoServerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static efs.task.todoapp.web.ResponseCodes.*;

@ExtendWith(ToDoServerExtension.class)
class HttpTaskHandlerTest {

    private HttpClient httpClient;
    private final Gson gson = new Gson();
    public static final String TODO_APP_PATH = "http://localhost:8080/todo/";
    private HttpResponse<String> httpResponseUser;
    private final String login = "login";
    private final String password = "password";

    private static String getEncodedData(String login) {
        Base64.Encoder encoder = Base64.getEncoder();
        var encodedLogin = encoder.encodeToString(login.getBytes());
        var encodedPassword = encoder.encodeToString("password".getBytes());
        return encodedLogin + ":" + encodedPassword;
    }


    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        httpClient = HttpClient.newHttpClient();

        String sampleBodyUser = gson.toJson(new UserEntity(login, password));
        HttpRequest httpRequestUser = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "user"))
                .POST(HttpRequest.BodyPublishers.ofString(sampleBodyUser))
                .build();

        httpResponseUser = httpClient.send(httpRequestUser, HttpResponse.BodyHandlers.ofString());
    }


    @ParameterizedTest(name = "{index}: username={0},password={1}")
    @CsvSource({"valid,testpassword", "name,valid", "aa,bb", "a,b", "valid123,valid123"})
    @Timeout(1)
    void saveTask_invalidPassword_shouldReturnUNAUTHORIZEDCode(String name, String password) throws IOException, InterruptedException {

        var encoder = Base64.getEncoder();
        String encodedUsername = new String(encoder.encode(name.getBytes(StandardCharsets.UTF_8)));
        String encodedPassword = new String(encoder.encode(password.getBytes(StandardCharsets.UTF_8)));

        String sampleBodyTask = gson.toJson(new TaskEntity("kup mleko", "2021-06-30"));

        var httpRequestTask = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task"))
                .POST(HttpRequest.BodyPublishers.ofString(sampleBodyTask))
                .header("auth", encodedUsername + ":" + encodedPassword)
                .build();

        HttpResponse<String> httpResponseTask = httpClient.send(httpRequestTask, HttpResponse.BodyHandlers.ofString());

        assertThat(httpResponseUser.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTask.statusCode()).as("Response status code").isEqualTo(UNAUTHORIZED.getResponseCode());
    }

    @Test
    @Timeout(1)
    void saveTask_missingDescription_shouldReturnBAD_REQUESTCode() throws IOException, InterruptedException {

        String sampleBodyTask = gson.toJson(new TaskEntity(null, "2021-06-30"));

        var httpRequestTask = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task"))
                .POST(HttpRequest.BodyPublishers.ofString(sampleBodyTask))
                .header("auth", "bmFtZQ==:dGVzdHBhc3N3b3Jk")
                .build();

        HttpResponse<String> httpResponseTask = httpClient.send(httpRequestTask, HttpResponse.BodyHandlers.ofString());

        assertThat(httpResponseUser.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTask.statusCode()).as("Response status code").isEqualTo(BAD_REQUEST.getResponseCode());
    }

    @Test
    @Timeout(1)
    void saveTask_shouldReturnCREATEDCode() throws IOException, InterruptedException {

        String sampleBodyTask = gson.toJson(new TaskEntity("Kup mleko", "2021-06-30"));

        var httpRequestTask = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task"))
                .POST(HttpRequest.BodyPublishers.ofString(sampleBodyTask))
                .header("auth", getEncodedData(login))
                .build();

        HttpResponse<String> httpResponseTask = httpClient.send(httpRequestTask, HttpResponse.BodyHandlers.ofString());

        assertThat(httpResponseUser.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTask.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
    }

    @Test
    @Timeout(1)
    void getTask_shouldReturnCREATEDCode() throws IOException, InterruptedException {

        String sampleBodyTask = gson.toJson(new TaskEntity("Kup mleko", "2021-06-30"));

        var httpRequestTask = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task"))
                .POST(HttpRequest.BodyPublishers.ofString(sampleBodyTask))
                .header("auth", getEncodedData(login))
                .build();

        var httpRequestTaskGET = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task"))
                .GET()
                .header("auth", getEncodedData(login))
                .build();

        HttpResponse<String> httpResponseTaskPOST = httpClient.send(httpRequestTask, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> httpResponseTaskGET = httpClient.send(httpRequestTaskGET, HttpResponse.BodyHandlers.ofString());

        assertThat(httpResponseUser.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskPOST.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskGET.statusCode()).as("Response status code").isEqualTo(OK.getResponseCode());
    }

    @Test
    @Timeout(1)
    void getTask_missingHeader_shouldReturnBAD_REQUESTCode() throws IOException, InterruptedException {

        String sampleBodyTask = gson.toJson(new TaskEntity("Kup mleko", "2021-06-30"));

        var httpRequestTask = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task"))
                .POST(HttpRequest.BodyPublishers.ofString(sampleBodyTask))
                .header("auth", getEncodedData(login))
                .build();

        var httpRequestTaskGET = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task"))
                .GET()
                .header("auth", "incorrect header")
                .build();

        HttpResponse<String> httpResponseTaskPOST = httpClient.send(httpRequestTask, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> httpResponseTaskGET = httpClient.send(httpRequestTaskGET, HttpResponse.BodyHandlers.ofString());

        assertThat(httpResponseUser.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskPOST.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskGET.statusCode()).as("Response status code").isEqualTo(BAD_REQUEST.getResponseCode());
    }

    @Test
    @Timeout(1)
    void getTask_missingUser_shouldReturnUNAUTHORIZEDCode() throws IOException, InterruptedException {

        String sampleBodyTask = gson.toJson(new TaskEntity("Kup mleko", "2021-06-30"));

        var httpRequestTask = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task"))
                .POST(HttpRequest.BodyPublishers.ofString(sampleBodyTask))
                .header("auth", getEncodedData(login))
                .build();

        var httpRequestTaskGET = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task"))
                .GET()
                .header("auth", getEncodedData("incorrect login"))
                .build();

        HttpResponse<String> httpResponseTaskPOST = httpClient.send(httpRequestTask, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> httpResponseTaskGET = httpClient.send(httpRequestTaskGET, HttpResponse.BodyHandlers.ofString());

        assertThat(httpResponseUser.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskPOST.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskGET.statusCode()).as("Response status code").isEqualTo(UNAUTHORIZED.getResponseCode());
    }

    @Test
    @Timeout(1)
    void getTaskId_shouldReturnOKCode() throws IOException, InterruptedException {

        String sampleBodyTask = gson.toJson(new TaskEntity("Kup mleko", "2021-06-30"));

        var httpRequestTask = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task"))
                .POST(HttpRequest.BodyPublishers.ofString(sampleBodyTask))
                .header("auth", getEncodedData(login))
                .build();

        HttpResponse<String> httpResponseTaskPOST = httpClient.send(httpRequestTask, HttpResponse.BodyHandlers.ofString());

        Properties properties = gson.fromJson(httpResponseTaskPOST.body(), Properties.class);
        String id = properties.getProperty("id");

        var httpRequestTaskGET = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task/" + id))
                .GET()
                .header("auth", getEncodedData(login))
                .build();


        HttpResponse<String> httpResponseTaskGET = httpClient.send(httpRequestTaskGET, HttpResponse.BodyHandlers.ofString());

        assertThat(httpResponseUser.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskPOST.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskGET.statusCode()).as("Response status code").isEqualTo(OK.getResponseCode());
    }

    @Test
    @Timeout(1)
    void getTaskId_missingHeader_shouldReturnBAD_REQUESTCode() throws IOException, InterruptedException {

        String sampleBodyTask = gson.toJson(new TaskEntity("Kup mleko", "2021-06-30"));

        var httpRequestTask = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task"))
                .POST(HttpRequest.BodyPublishers.ofString(sampleBodyTask))
                .header("auth", getEncodedData(login))
                .build();

        HttpResponse<String> httpResponseTaskPOST = httpClient.send(httpRequestTask, HttpResponse.BodyHandlers.ofString());

        Properties properties = gson.fromJson(httpResponseTaskPOST.body(), Properties.class);
        String id = properties.getProperty("id");

        var httpRequestTaskGET = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task/" + id))
                .GET()
                .header("incorrectHeader", getEncodedData(login))
                .build();


        HttpResponse<String> httpResponseTaskGET = httpClient.send(httpRequestTaskGET, HttpResponse.BodyHandlers.ofString());

        assertThat(httpResponseUser.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskPOST.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskGET.statusCode()).as("Response status code").isEqualTo(BAD_REQUEST.getResponseCode());
    }

    @Test
    @Timeout(1)
    void getTaskId_missingUser_shouldReturnUNAUTHORIZEDCode() throws IOException, InterruptedException {

        String sampleBodyTask = gson.toJson(new TaskEntity("Kup mleko", "2021-06-30"));

        var httpRequestTask = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task"))
                .POST(HttpRequest.BodyPublishers.ofString(sampleBodyTask))
                .header("auth", getEncodedData(login))
                .build();

        HttpResponse<String> httpResponseTaskPOST = httpClient.send(httpRequestTask, HttpResponse.BodyHandlers.ofString());

        Properties properties = gson.fromJson(httpResponseTaskPOST.body(), Properties.class);
        String id = properties.getProperty("id");

        var httpRequestTaskGET = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task/" + id))
                .GET()
                .header("auth", getEncodedData("incorrectLogin"))
                .build();


        HttpResponse<String> httpResponseTaskGET = httpClient.send(httpRequestTaskGET, HttpResponse.BodyHandlers.ofString());

        assertThat(httpResponseUser.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskPOST.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskGET.statusCode()).as("Response status code").isEqualTo(UNAUTHORIZED.getResponseCode());
    }

    @Test
    @Timeout(1)
    void getTaskId_invalidTaskOwner_shouldReturnFORBIDDENCode() throws IOException, InterruptedException {

        String sampleBodyTask = gson.toJson(new TaskEntity("Kup mleko", "2021-06-30"));


        String secondUser = gson.toJson(new UserEntity("secondLogin", password));

        HttpRequest httpRequestSecondUser = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "user"))
                .POST(HttpRequest.BodyPublishers.ofString(secondUser))
                .build();

        var httpResponseSecondUser = httpClient.send(httpRequestSecondUser, HttpResponse.BodyHandlers.ofString());

        var httpRequestTask = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task"))
                .POST(HttpRequest.BodyPublishers.ofString(sampleBodyTask))
                .header("auth", getEncodedData("secondLogin"))
                .build();

        HttpResponse<String> httpResponseTaskPOST = httpClient.send(httpRequestTask, HttpResponse.BodyHandlers.ofString());

        Properties properties = gson.fromJson(httpResponseTaskPOST.body(), Properties.class);
        String id = properties.getProperty("id");

        var httpRequestTaskGET = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task/" + id))
                .GET()
                .header("auth", getEncodedData(login))
                .build();


        HttpResponse<String> httpResponseTaskGET = httpClient.send(httpRequestTaskGET, HttpResponse.BodyHandlers.ofString());

        assertThat(httpResponseUser.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseSecondUser.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskPOST.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskGET.statusCode()).as("Response status code").isEqualTo(FORBIDDEN.getResponseCode());
    }

    @Test
    @Timeout(1)
    void getTaskId_taskNotFound_shouldReturnNOT_FOUNDCode() throws IOException, InterruptedException {

        UUID idTask = UUID.randomUUID();

        var httpRequestTaskGET = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task/" + idTask))
                .GET()
                .header("auth", getEncodedData(login))
                .build();


        HttpResponse<String> httpResponseTaskGET = httpClient.send(httpRequestTaskGET, HttpResponse.BodyHandlers.ofString());

        assertThat(httpResponseUser.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskGET.statusCode()).as("Response status code").isEqualTo(NOT_FOUND.getResponseCode());
    }


    ////////////////////////////////
    @Test
    @Timeout(1)
    void putTaskId_shouldReturnOKCode() throws IOException, InterruptedException {

        String sampleBodyTask = gson.toJson(new TaskEntity("Kup mleko", "2021-06-30"));
        String newTask = gson.toJson(new TaskEntity("Kup chleb", "2021-07-10"));

        var httpRequestTask = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task"))
                .POST(HttpRequest.BodyPublishers.ofString(sampleBodyTask))
                .header("auth", getEncodedData(login))
                .build();

        HttpResponse<String> httpResponseTaskPOST = httpClient.send(httpRequestTask, HttpResponse.BodyHandlers.ofString());

        Properties properties = gson.fromJson(httpResponseTaskPOST.body(), Properties.class);
        String id = properties.getProperty("id");

        var httpRequestTaskPUT = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task/" + id))
                .PUT(HttpRequest.BodyPublishers.ofString(newTask))
                .header("auth", getEncodedData(login))
                .build();


        HttpResponse<String> httpResponseTaskPUT = httpClient.send(httpRequestTaskPUT, HttpResponse.BodyHandlers.ofString());

        assertThat(httpResponseUser.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskPOST.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskPUT.statusCode()).as("Response status code").isEqualTo(OK.getResponseCode());
    }

    @Test
    @Timeout(1)
    void putTaskId_missingHeader_shouldReturnBAD_REQUESTCode() throws IOException, InterruptedException {

        String sampleBodyTask = gson.toJson(new TaskEntity("Kup mleko", "2021-06-30"));
        String newTask = gson.toJson(new TaskEntity("Kup chleb", "2021-07-10"));

        var httpRequestTask = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task"))
                .POST(HttpRequest.BodyPublishers.ofString(sampleBodyTask))
                .header("auth", getEncodedData(login))
                .build();

        HttpResponse<String> httpResponseTaskPOST = httpClient.send(httpRequestTask, HttpResponse.BodyHandlers.ofString());

        Properties properties = gson.fromJson(httpResponseTaskPOST.body(), Properties.class);
        String id = properties.getProperty("id");

        var httpRequestTaskPUT = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task/" + id))
                .PUT(HttpRequest.BodyPublishers.ofString(newTask))
                .header("incorrectHeader", getEncodedData(login))
                .build();


        HttpResponse<String> httpResponseTaskPUT = httpClient.send(httpRequestTaskPUT, HttpResponse.BodyHandlers.ofString());

        assertThat(httpResponseUser.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskPOST.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskPUT.statusCode()).as("Response status code").isEqualTo(BAD_REQUEST.getResponseCode());
    }

    @Test
    @Timeout(1)
    void putTaskId_missingUser_shouldReturnUNAUTHORIZEDCode() throws IOException, InterruptedException {

        String sampleBodyTask = gson.toJson(new TaskEntity("Kup mleko", "2021-06-30"));
        String newTask = gson.toJson(new TaskEntity("Kup chleb", "2021-07-10"));

        var httpRequestTask = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task"))
                .POST(HttpRequest.BodyPublishers.ofString(sampleBodyTask))
                .header("auth", getEncodedData(login))
                .build();

        HttpResponse<String> httpResponseTaskPOST = httpClient.send(httpRequestTask, HttpResponse.BodyHandlers.ofString());

        Properties properties = gson.fromJson(httpResponseTaskPOST.body(), Properties.class);
        String id = properties.getProperty("id");

        var httpRequestTaskPUT = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task/" + id))
                .PUT(HttpRequest.BodyPublishers.ofString(newTask))
                .header("auth", getEncodedData("incorrectLogin"))
                .build();


        HttpResponse<String> httpResponseTaskPUT = httpClient.send(httpRequestTaskPUT, HttpResponse.BodyHandlers.ofString());

        assertThat(httpResponseUser.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskPOST.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskPUT.statusCode()).as("Response status code").isEqualTo(UNAUTHORIZED.getResponseCode());
    }

    @Test
    @Timeout(1)
    void putTaskId_invalidTaskOwner_shouldReturnFORBIDDENCode() throws IOException, InterruptedException {

        String sampleBodyTask = gson.toJson(new TaskEntity("Kup mleko", "2021-06-30"));
        String newTask = gson.toJson(new TaskEntity("Kup chleb", "2021-07-10"));

        String secondUser = gson.toJson(new UserEntity("secondLogin", password));

        HttpRequest httpRequestSecondUser = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "user"))
                .POST(HttpRequest.BodyPublishers.ofString(secondUser))
                .build();

        var httpResponseSecondUser = httpClient.send(httpRequestSecondUser, HttpResponse.BodyHandlers.ofString());

        var httpRequestTask = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task"))
                .POST(HttpRequest.BodyPublishers.ofString(sampleBodyTask))
                .header("auth", getEncodedData("secondLogin"))
                .build();

        HttpResponse<String> httpResponseTaskPOST = httpClient.send(httpRequestTask, HttpResponse.BodyHandlers.ofString());

        Properties properties = gson.fromJson(httpResponseTaskPOST.body(), Properties.class);
        String id = properties.getProperty("id");

        var httpRequestTaskPUT = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task/" + id))
                .PUT(HttpRequest.BodyPublishers.ofString(newTask))
                .header("auth", getEncodedData(login))
                .build();


        HttpResponse<String> httpResponseTaskPUT = httpClient.send(httpRequestTaskPUT, HttpResponse.BodyHandlers.ofString());

        assertThat(httpResponseUser.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseSecondUser.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskPOST.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskPUT.statusCode()).as("Response status code").isEqualTo(FORBIDDEN.getResponseCode());
    }

    @Test
    @Timeout(1)
    void putTaskId_taskNotFound_shouldReturnNOT_FOUNDCode() throws IOException, InterruptedException {

        String newTask = gson.toJson(new TaskEntity("Kup chleb", "2021-07-10"));
        UUID idTask = UUID.randomUUID();

        var httpRequestTaskPUT = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task/" + idTask))
                .PUT(HttpRequest.BodyPublishers.ofString(newTask))
                .header("auth", getEncodedData(login))
                .build();


        HttpResponse<String> httpResponseTaskPUT = httpClient.send(httpRequestTaskPUT, HttpResponse.BodyHandlers.ofString());

        assertThat(httpResponseUser.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskPUT.statusCode()).as("Response status code").isEqualTo(NOT_FOUND.getResponseCode());
    }

    ////////////////////////////////
    @Test
    @Timeout(1)
    void deleteTaskId_shouldReturnOKCode() throws IOException, InterruptedException {

        String sampleBodyTask = gson.toJson(new TaskEntity("Kup mleko", "2021-06-30"));

        var httpRequestTask = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task"))
                .POST(HttpRequest.BodyPublishers.ofString(sampleBodyTask))
                .header("auth", getEncodedData(login))
                .build();

        HttpResponse<String> httpResponseTaskPOST = httpClient.send(httpRequestTask, HttpResponse.BodyHandlers.ofString());

        Properties properties = gson.fromJson(httpResponseTaskPOST.body(), Properties.class);
        String id = properties.getProperty("id");

        var httpRequestTaskDELETE = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task/" + id))
                .DELETE()
                .header("auth", getEncodedData(login))
                .build();


        HttpResponse<String> httpResponseTaskDELETE = httpClient.send(httpRequestTaskDELETE, HttpResponse.BodyHandlers.ofString());

        assertThat(httpResponseUser.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskPOST.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskDELETE.statusCode()).as("Response status code").isEqualTo(OK.getResponseCode());
    }

    @Test
    @Timeout(1)
    void deleteTaskId_missingHeader_shouldReturnBAD_REQUESTCode() throws IOException, InterruptedException {

        String sampleBodyTask = gson.toJson(new TaskEntity("Kup mleko", "2021-06-30"));

        var httpRequestTask = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task"))
                .POST(HttpRequest.BodyPublishers.ofString(sampleBodyTask))
                .header("auth", getEncodedData(login))
                .build();

        HttpResponse<String> httpResponseTaskPOST = httpClient.send(httpRequestTask, HttpResponse.BodyHandlers.ofString());

        Properties properties = gson.fromJson(httpResponseTaskPOST.body(), Properties.class);
        String id = properties.getProperty("id");

        var httpRequestTaskDELETE = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task/" + id))
                .DELETE()
                .header("incorrectHeader", getEncodedData(login))
                .build();


        HttpResponse<String> httpResponseTaskDELETE = httpClient.send(httpRequestTaskDELETE, HttpResponse.BodyHandlers.ofString());

        assertThat(httpResponseUser.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskPOST.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskDELETE.statusCode()).as("Response status code").isEqualTo(BAD_REQUEST.getResponseCode());
    }

    @Test
    @Timeout(1)
    void deleteTaskId_missingUser_shouldReturnUNAUTHORIZEDCode() throws IOException, InterruptedException {

        String sampleBodyTask = gson.toJson(new TaskEntity("Kup mleko", "2021-06-30"));

        var httpRequestTask = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task"))
                .POST(HttpRequest.BodyPublishers.ofString(sampleBodyTask))
                .header("auth", getEncodedData(login))
                .build();

        HttpResponse<String> httpResponseTaskPOST = httpClient.send(httpRequestTask, HttpResponse.BodyHandlers.ofString());

        Properties properties = gson.fromJson(httpResponseTaskPOST.body(), Properties.class);
        String id = properties.getProperty("id");

        var httpRequestTaskDELETE = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task/" + id))
                .DELETE()
                .header("auth", getEncodedData("incorrectLogin"))
                .build();


        HttpResponse<String> httpResponseTaskDELETE = httpClient.send(httpRequestTaskDELETE, HttpResponse.BodyHandlers.ofString());

        assertThat(httpResponseUser.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskPOST.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskDELETE.statusCode()).as("Response status code").isEqualTo(UNAUTHORIZED.getResponseCode());
    }

    @Test
    @Timeout(1)
    void deleteTaskId_invalidTaskOwner_shouldReturnFORBIDDENCode() throws IOException, InterruptedException {

        String sampleBodyTask = gson.toJson(new TaskEntity("Kup mleko", "2021-06-30"));

        String secondUser = gson.toJson(new UserEntity("secondLogin", password));

        HttpRequest httpRequestSecondUser = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "user"))
                .POST(HttpRequest.BodyPublishers.ofString(secondUser))
                .build();

        var httpResponseSecondUser = httpClient.send(httpRequestSecondUser, HttpResponse.BodyHandlers.ofString());

        var httpRequestTask = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task"))
                .POST(HttpRequest.BodyPublishers.ofString(sampleBodyTask))
                .header("auth", getEncodedData("secondLogin"))
                .build();

        HttpResponse<String> httpResponseTaskPOST = httpClient.send(httpRequestTask, HttpResponse.BodyHandlers.ofString());

        Properties properties = gson.fromJson(httpResponseTaskPOST.body(), Properties.class);
        String id = properties.getProperty("id");

        var httpRequestTaskDELETE = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task/" + id))
                .DELETE()
                .header("auth", getEncodedData(login))
                .build();


        HttpResponse<String> httpResponseTaskDELETE = httpClient.send(httpRequestTaskDELETE, HttpResponse.BodyHandlers.ofString());

        assertThat(httpResponseUser.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseSecondUser.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskPOST.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskDELETE.statusCode()).as("Response status code").isEqualTo(FORBIDDEN.getResponseCode());
    }

    @Test
    @Timeout(1)
    void deleteTaskId_taskNotFound_shouldReturnNOT_FOUNDCode() throws IOException, InterruptedException {

        UUID idTask = UUID.randomUUID();

        var httpRequestTaskDELETE = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task/" + idTask))
                .DELETE()
                .header("auth", getEncodedData(login))
                .build();

        HttpResponse<String> httpResponseTaskDELETE = httpClient.send(httpRequestTaskDELETE, HttpResponse.BodyHandlers.ofString());

        assertThat(httpResponseUser.statusCode()).as("Response status code").isEqualTo(CREATED.getResponseCode());
        assertThat(httpResponseTaskDELETE.statusCode()).as("Response status code").isEqualTo(NOT_FOUND.getResponseCode());
    }

}