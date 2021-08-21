package efs.task.todoapp.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TaskRepository implements Repository<UUID, TaskEntity> {

    private final Map<UUID, TaskEntity> tasksMap;

    public TaskRepository() {
        tasksMap = new HashMap<>();
    }

    @Override
    public UUID save(TaskEntity taskEntity) {
        TaskEntity result = tasksMap.putIfAbsent(taskEntity.getId(), taskEntity);
        if (result == null) {
            return taskEntity.getId();
        } else {
            return null;
        }
    }

    @Override
    public TaskEntity query(UUID uuid) {
        return tasksMap.getOrDefault(uuid, null);
    }

    @Override
    public List<TaskEntity> query(Predicate<TaskEntity> condition) {
        return tasksMap.values().stream().filter(condition).collect(Collectors.toList());
    }

    @Override
    public TaskEntity update(UUID uuid, TaskEntity taskEntity) {
        if (tasksMap.containsKey(uuid)) {
            tasksMap.put(uuid, taskEntity);
            return taskEntity;
        } else {
            return null;
        }
    }

    @Override
    public boolean delete(UUID uuid) {
        if (tasksMap.containsKey(uuid)) {
            tasksMap.remove(uuid);
            return true;
        } else {
            return false;
        }
    }
}
