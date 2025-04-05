package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;
import java.util.Collection;

public interface UserStorage {

    Collection<User> allUsers();

    User addUser(User user);

    User updateUser(User newUser);

    User getUserById(Long id);

    User deleteUserById(Long id);
}