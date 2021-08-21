package efs.task.todoapp.service.exceptions;

public class TaskNotFoundException extends Exception{
    public TaskNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
