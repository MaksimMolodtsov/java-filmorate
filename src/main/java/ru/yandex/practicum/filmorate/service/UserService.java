package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public Collection<User> allUsers() {
        return userStorage.allUsers();
    }

    public User updateUser(User newUser) {
        User oldUser = validateUserForUpdate(newUser);
        log.debug("Обновлен пользователь {}", oldUser);
        return userStorage.updateUser(oldUser);
    }

    public User addUser(User user) {
        if (user == null) throw new IllegalArgumentException("Пустой объект");
        validateUser(user);
        log.debug("Добавлен пользователь {}", user);
        return userStorage.addUser(user);
    }

    public User getUserById(Long id) {
        if (id == null) {
            log.warn("Ошибка валидации: Id должен быть указан для запрашиваемого пользователя");
            throw new ValidationException("Id должен быть указан");
        }
        return userStorage.getUserById(id);
    }

    public User deleteUserById(Long id) {
        if (id == null) {
            log.warn("Ошибка валидации: Id должен быть указан для удаляемого пользователя");
            throw new ValidationException("Id должен быть указан");
        }
        User user = userStorage.deleteUserById(id);
        log.debug("Deleted user {}", user);
        return user;
    }

    //Friends

    public void addFriend(Long idOfFirstFriend, Long idOfSecondFriend) {
        User firstUser = getUserById(idOfFirstFriend);
        User secondUser = getUserById(idOfSecondFriend);
        userStorage.addFriend(idOfFirstFriend, idOfSecondFriend);
        log.debug("{} added {} in friends", firstUser, secondUser);
    }

    public void removeFriend(Long idOfFirstFriend, Long idOfSecondFriend) {
        User firstUser = getUserById(idOfFirstFriend);
        User secondUser = getUserById(idOfSecondFriend);
        userStorage.removeFriend(idOfFirstFriend, idOfSecondFriend);
        log.debug("{} delete {} from friends", firstUser, secondUser);
    }

    public Set<User> friendsOfUser(Long id) {
        User user = userStorage.getUserById(id);
        if (user.getFriends().isEmpty()) return Set.of();
        Set<Long> friendIds = new HashSet<>(user.getFriends());
        Collection<User> friendsCollection = userStorage.getUsersByIds(friendIds);
        return friendsCollection.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Set<User> commonFriends(Long idOfFirstFriend, Long idOfSecondFriend) {
        userStorage.getUserById(idOfFirstFriend);
        userStorage.getUserById(idOfSecondFriend);
        Set<User> commonFriends = new HashSet<>(friendsOfUser(idOfFirstFriend));
        commonFriends.retainAll(friendsOfUser(idOfSecondFriend));
        return commonFriends;
    }

    //Validate

    public void validateUser(User user) {
        if (user.getEmail() == null) {
            log.warn("Ошибка валидации: e-mail должен быть указан для {}", user);
            throw new ValidationException("e-mail должен быть указан для");
        } else {
            for (User u : allUsers()) {
                if (u.getEmail().equals(user.getEmail())) {
                    log.warn("Этот e-mail уже используется для {}", u);
                    throw new ValidationException("Этот e-mail уже используется");
                }
            }
        }
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday() == null) {
            log.warn("Ошибка валидации: Необходимо указать дату рождения для {}", user);
            throw new ValidationException("Необходимо указать дату рождения");
        } else if (user.getBirthday().isAfter(LocalDateTime.now().toLocalDate())) {
            log.warn("Ошибка валидации: Указана дата рождения в будущем для {}", user);
            throw new ValidationException("Указана дата рождения в будущем");
        }
    }

    public User validateUserForUpdate(User newUser) {
        if (newUser == null) throw new IllegalArgumentException("Пустой объект");
        if (newUser.getId() == null) {
            log.warn("Ошибка валидации: Id должен быть указан для {}", newUser);
            throw new ValidationException("Id должен быть указан");
        }
        User oldUser = userStorage.getUserById(newUser.getId());
        if (newUser.getEmail() == null) {
            oldUser.setEmail(oldUser.getEmail());
        } else {
            oldUser.setEmail(newUser.getEmail());
        }
        if (newUser.getLogin() == null) {
            oldUser.setLogin(oldUser.getLogin());
        } else {
            oldUser.setLogin(newUser.getLogin());
        }
        if (newUser.getName() == null || newUser.getName().isBlank()) {
            oldUser.setName(oldUser.getName());
        } else {
            oldUser.setName(newUser.getName());
        }
        if (newUser.getBirthday() == null) {
            oldUser.setBirthday(oldUser.getBirthday());
        } else if (newUser.getBirthday().isAfter(LocalDateTime.now().toLocalDate())) {
            log.warn("Ошибка валидации: Указана неверная дата рождения для {}", newUser);
            throw new ValidationException("Указана дата рождения в будущем");
        } else {
            oldUser.setBirthday(newUser.getBirthday());
        }
        return oldUser;
    }

}