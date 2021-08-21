package efs.task.todoapp.service.exceptions;

public class UserExistsException extends Exception{
    public UserExistsException(String errorMessage) {
        super(errorMessage);
    }
}
