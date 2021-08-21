package efs.task.todoapp.service.exceptions;

public class NotRightsToTaskException extends Exception {
    public NotRightsToTaskException(String errorMessage) {
        super(errorMessage);
    }
}
