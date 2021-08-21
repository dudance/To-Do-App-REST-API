package efs.task.todoapp.service.exceptions;

public class NotRequiredUserException extends Exception {
    public NotRequiredUserException(String errorMessage) {
        super(errorMessage);
    }
}
