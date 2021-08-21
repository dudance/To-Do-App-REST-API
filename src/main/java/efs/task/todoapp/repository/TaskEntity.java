package efs.task.todoapp.repository;

import java.util.UUID;

public class TaskEntity {
    private UUID id;
    private final String description;
    private final String due;
    private String owner;


    public TaskEntity(String description, String due) {
        this.description = description;
        this.due = due;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getDue() {
        return due;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
