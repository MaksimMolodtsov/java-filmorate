package ru.yandex.practicum.filmorate.storage.user.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.user.dal.mappers.UserRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.Collection;

@RequiredArgsConstructor
@Repository
@Primary
@Slf4j
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbc;
    private final UserRowMapper mapper;

    @Override
    public Collection<User> allUsers() {
        String query = "SELECT * FROM users;";
        return jdbc.query(query, mapper);
    }

    @Override
    public User addUser(User user) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        String query = "INSERT INTO users (email, login, name, birthday) VALUES (?,?,?,?);";
        try {
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(query, new String[]{"user_id"});
                ps.setString(1, user.getEmail());
                ps.setString(2, user.getLogin());
                ps.setString(3, user.getName());
                if (user.getBirthday() != null) {
                    ps.setDate(4, Date.valueOf(user.getBirthday()));
                } else {
                    ps.setNull(4, Types.DATE);
                }
                return ps;
            }, keyHolder);
        } catch (DuplicateKeyException e) {
            throw new ValidationException("В базе данных уже содержится такой id");
        } catch (DataIntegrityViolationException ex) {
            throw new NotFoundException("Ошибка данных пользователя");
        }
        user.setId(keyHolder.getKeyAs(Long.class));
        return getUserById(user.getId());
    }

    @Override
    public User updateUser(User newUser) {
        getUserById(newUser.getId());
        String query = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?;";
        int rowsUpdated = jdbc.update(query, newUser.getEmail(), newUser.getLogin(), newUser.getName(),
                newUser.getBirthday(), newUser.getId());
        if (rowsUpdated == 0) {
            throw new RuntimeException("Не удалось обновить данные");
        }
        return getUserById(newUser.getId());
    }

    @Override
    public User getUserById(Long id) {
        try {
            String query = "SELECT * FROM users WHERE user_id = ?;";
            return jdbc.queryForObject(query, mapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("Пользователь %d не найден", id));
        }
    }

    @Override
    public User deleteUserById(Long id) {
        User user = getUserById(id);
        String query = "DELETE FROM users WHERE user_id = ?;";
        jdbc.update(query, id);
        return user;
    }

    @Override
    public void addFriend(Long id1, Long id2) {
        getUserById(id1);
        getUserById(id2);
        String query = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?);";
        jdbc.update(query, id1, id2);
    }

    @Override
    public void removeFriend(Long id1, Long id2) {
        getUserById(id1);
        getUserById(id2);
        String query = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?;";
        jdbc.update(query, id1, id2);
    }
}
