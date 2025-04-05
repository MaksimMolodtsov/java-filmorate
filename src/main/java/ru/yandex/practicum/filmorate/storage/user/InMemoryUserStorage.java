package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> allUsers() {
        return users.values();
    }

    @Override
    public User addUser(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User newUser) {
       User oldUser = users.get(newUser.getId());
       oldUser.setEmail(newUser.getEmail());
       oldUser.setLogin(newUser.getLogin());
       oldUser.setName(newUser.getName());
       oldUser.setBirthday(newUser.getBirthday());
       return oldUser;
    }

    @Override
    public User getUserById(Long id) {
        User user = users.get(id);
        if (user == null) throw new NotFoundException("Пользователь c id = " + id + " не найден");
        return user;
    }

    @Override
    public User deleteUserById(Long id) {
        User user = users.get(id);
        if (user == null) throw new NotFoundException("Пользователь c id = " + id + " не найден");
        users.remove(id);
        return user;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}