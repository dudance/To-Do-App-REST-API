package efs.task.todoapp.web;

import com.sun.net.httpserver.HttpServer;
import efs.task.todoapp.repository.TaskRepository;
import efs.task.todoapp.repository.UserRepository;
import efs.task.todoapp.service.ToDoService;

import java.io.IOException;
import java.net.InetSocketAddress;

public class WebServerFactory {
    public static HttpServer createServer() {
        ToDoService service = new ToDoService(new UserRepository(), new TaskRepository());
        InetSocketAddress address = new InetSocketAddress("localhost", 8080);
        try {
            HttpServer httpServer = HttpServer.create(address, 0);
            httpServer.createContext("/todo/user", new HttpUserHandler(service));
            httpServer.createContext("/todo/task", new HttpTaskHandler(service));
            return httpServer;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
