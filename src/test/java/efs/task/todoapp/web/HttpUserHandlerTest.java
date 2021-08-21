package efs.task.todoapp.web;

import com.google.gson.Gson;
import efs.task.todoapp.repository.UserEntity;
import efs.task.todoapp.util.ToDoServerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.assertj.core.api.Assertions.assertThat;
import static efs.task.todoapp.web.ResponseCodes.*;


@ExtendWith(ToDoServerExtension.class)
class HttpUserHandlerTest {
    private HttpClient client;
    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() {
        client = HttpClient.newHttpClient();
    }

    @Test
    @Timeout(1)
    void saveUser_shouldCreateNewUserAndReturnCREATEDCode() throws IOException, InterruptedException {
        String body = gson.toJson(new UserEntity("username", "password"));
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/todo/" + "user"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        var httpResponse = client.send(httpRequest, ofString());

        assertThat(httpResponse.statusCode()).isEqualTo(CREATED.getResponseCode());
    }

    @Test
    @Timeout(1)
    void saveUser_shouldReturnBAD_REQUESTCode_whenRequestBodyIsInvalid() throws IOException, InterruptedException {
        String body = gson.toJson(new UserEntity(null, "password"));

        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/todo/" + "user"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        var httpResponse = client.send(httpRequest, ofString());

        assertThat(httpResponse.statusCode()).isEqualTo(BAD_REQUEST.getResponseCode());
    }

    @Test
    @Timeout(1)
    void saveUser_shouldReturnUNAUTHORIZEDCode_whenUserAddedTwoTimes() throws IOException, InterruptedException {

        String body = gson.toJson(new UserEntity("username", "password"));

        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/todo/" + "user"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        var firstHttpResponse = client.send(httpRequest, ofString());
        var secondHttpResponse = client.send(httpRequest, ofString());

        assertThat(firstHttpResponse.statusCode()).isEqualTo(CREATED.getResponseCode());
        assertThat(secondHttpResponse.statusCode()).isEqualTo(CONFLICT.getResponseCode());
    }
}