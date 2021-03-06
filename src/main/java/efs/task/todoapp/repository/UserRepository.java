package efs.task.todoapp.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UserRepository implements Repository<String, UserEntity> {

    private final Map<String, UserEntity> usersMap;

    public UserRepository() {
        usersMap = new HashMap<>();
    }

    @Override
    public String save(UserEntity userEntity) {
        UserEntity result = usersMap.putIfAbsent(userEntity.getUsername(), userEntity);
        if (result == null) {
            return userEntity.getUsername();
        } else {
            return null;
        }
    }

    @Override
    public UserEntity query(String s) {
        return usersMap.getOrDefault(s, null);
    }

    @Override
    public List<UserEntity> query(Predicate<UserEntity> condition) {
        return usersMap.values().stream().filter(condition).collect(Collectors.toList());
    }

    @Override
    public UserEntity update(String s, UserEntity userEntity) {
        if (usersMap.containsKey(s)) {
            usersMap.put(s, userEntity);
            return userEntity;
        } else {
            return null;
        }
    }

    @Override
    public boolean delete(String s) {
        if (usersMap.containsKey(s)) {
            usersMap.remove(s);
            return true;
        } else {
            return false;
        }
    }


}
