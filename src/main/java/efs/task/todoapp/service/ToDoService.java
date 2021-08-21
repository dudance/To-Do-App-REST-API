package efs.task.todoapp.service;

import efs.task.todoapp.repository.*;
import efs.task.todoapp.service.exceptions.BadRequestException;
import efs.task.todoapp.service.exceptions.UserExistsException;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class ToDoService {
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public ToDoService(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    public boolean isUserExists(UserEntity userEntity) {
        return userRepository.query(userEntity.getUsername()) != null;
    }

    public void saveUser(UserEntity userEntity) throws BadRequestException, UserExistsException {
        if (!isUserDataValid(userEntity)) {
            throw new BadRequestException("User data is not valid");
        } else if (isUserExists(userEntity)) {
            throw new UserExistsException("User " + userEntity.getUsername() + " exists");
        } else {
            userRepository.save(userEntity);
        }
    }

    public UUID saveTask(TaskEntity task, List<String> userData) {

        UUID idTask = UUID.randomUUID();
        while (taskRepository.query(idTask) != null) {
            idTask = UUID.randomUUID();
        }
        task.setId(idTask);
        task.setOwner(userData.get(0));
        taskRepository.save(task);
        return idTask;
    }

    public boolean isTaskValid(TaskEntity task) throws BadRequestException {
        if (task != null && task.getDescription() != null && !task.getDescription().equals("")) {
            if (task.getDue() != null) {
                try {
                    java.time.format.DateTimeFormatter.ISO_DATE.parse(task.getDue());
                } catch (Exception e) {
                    throw new BadRequestException(e.getMessage());
                }
            }
            return false;
        } else {
            return true;
        }
    }

    public List<TaskEntity> getTasksList(String userId) {
        return taskRepository.query(userTasks(userId));
    }

    public static Predicate<TaskEntity> userTasks(String userId) {
        return p -> p.getOwner().equals(userId);
    }

    public boolean isUserFromHeaderValid(List<String> userData) throws BadRequestException {

        if (userData.get(0) != null && !(userData.get(0)).equals("") && userData.get(1) != null &&
                !(userData.get(1)).equals("") && !(userData.get(1)).equals(" ") && !(userData.get(0)).equals(" ")) {
            UserEntity user = this.userRepository.query(userData.get(0));
            return user == null || !user.getPassword().equals(userData.get(1));
        } else {
            throw new BadRequestException("username is invalid");
        }
    }

    public boolean isUserDataValid(UserEntity user) {
        return user != null && user.getUsername() != null && user.getPassword() != null &&
                !user.getPassword().isEmpty() && !user.getUsername().isEmpty();
    }

    public TaskEntity getTask(String uuid) {
        return taskRepository.query(UUID.fromString(uuid));
    }

    public void deleteTask(UUID uuid) {
        taskRepository.delete(uuid);
    }

    public TaskEntity updateTask(TaskEntity task, List<String> userData, UUID uuid) {
        TaskEntity newTask = taskRepository.update(uuid, task);
        task.setId(uuid);
        task.setOwner(userData.get(0));
        return newTask;
    }

}
