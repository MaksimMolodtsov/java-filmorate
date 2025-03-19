package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> allUsers() {
        return users.values();
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        if (user.getEmail() == null) {
            log.warn("Ошибка валидации: Имейл должен быть указан для {}", user);
            throw new ValidationException("Имейл должен быть указан");
        } else {
            for (User u : allUsers()) {
                if (u.getEmail().equals(user.getEmail())) {
                    log.warn("Этот имейл уже используется для {}", u);
                    throw new ValidationException("Этот имейл уже используется");
                }
            }
        }
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
        try {
            if (user.getBirthday().isAfter(LocalDateTime.now().toLocalDate()) ) {
                log.warn("Ошибка валидации: Указана дата рождения в будущем для {}", user);
                throw new ValidationException("Указана дата рождения в будущем");
            }
        } catch (NullPointerException e) {
            log.warn("Ошибка валидации: Необходимо указать дату рождения для {}", user);
            throw new ValidationException("Необходимо указать дату рождения");
        }
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Добавлен пользователь {}", user);
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

    @PutMapping
    public User updateUser(@Valid @RequestBody User newUser) {
        if (newUser.getId() == null) {
            log.warn("Ошибка валидации: Id должен быть указан для {}", newUser);
            throw new ValidationException("Id должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
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
            log.info("Обновлен пользователь {}", oldUser);
            return oldUser;
        }
        log.warn("Ошибка валидации: Пользователь c id = {} не найден", newUser.getId());
        throw new ValidationException("Пользователь с id = " + newUser.getId() + " не найден");
    }
}