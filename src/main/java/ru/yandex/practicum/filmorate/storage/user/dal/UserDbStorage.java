package ru.yandex.practicum.filmorate.storage.user.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.user.dal.mappers.UserRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;

@RequiredArgsConstructor
@Repository
@Primary
@Slf4j
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbc;
    private final UserRowMapper mapper;

    @Override
    public Collection<User> allUsers() {
        String query = "SELECT u.user_id AS id, u.email AS email, u.login AS login, u.name AS name, u.birthday AS " +
                "birthday, ARRAY_AGG(DISTINCT f1.friend_id) AS followers, ARRAY_AGG(DISTINCT f2.user_id) AS " +
                "friends FROM users AS u " +
                "LEFT JOIN friends AS f1 ON u.user_id = f1.user_id " +
                "LEFT JOIN friends AS f2 ON u.user_id = f2.friend_id " +
                "GROUP BY u.user_id;";
        return jdbc.query(query, mapper);
    }

    @Override
    public User addUser(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String query = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?);";
        try {
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(query, new String[]{"user_id"});
                ps.setString(1, user.getEmail());
                ps.setString(2, user.getLogin());
                ps.setString(3, user.getName());
                ps.setDate(4, Date.valueOf(user.getBirthday()));
                return ps;
            }, keyHolder);
        } catch (DuplicateKeyException e) {
            throw new ValidationException("В базе данных уже содержится такой id");
        }
        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
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
        return newUser;
    }

    @Override
    public User getUserById(Long id) {
        try {
            String query = "SELECT u.user_id AS id, u.email AS email, u.login AS login, u.name AS name, u.birthday AS" +
                    " birthday, ARRAY_AGG(DISTINCT f1.friend_id) AS friends, ARRAY_AGG(DISTINCT f2.user_id) AS " +
                    "followers FROM users AS u " +
                    "LEFT JOIN friends AS f1 ON u.user_id = f1.user_id " +
                    "LEFT JOIN friends AS f2 ON u.user_id = f2.friend_id " +
                    "WHERE u.user_id = ? " +
                    "GROUP BY u.user_id;";
            return jdbc.queryForObject(query, mapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    @Override
    public User deleteUserById(Long id) {
        User user;
        String queryGet = "SELECT u.user_id AS id, u.email AS email, u.login AS login, u.name AS name, u.birthday AS " +
                "birthday, NULL AS followers, NULL AS friends " +
                "FROM users AS u WHERE u.user_id = ?;";
        String queryDelete = "DELETE FROM users WHERE user_id = ?;";
        try {
            user = jdbc.queryForObject(queryGet, mapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь не найден");
        }
        jdbc.update(queryDelete, id);
        return user;
    }

    @Override
    public void addFriend(Long id1, Long id2) {
        String queryGet = "SELECT u.user_id AS id, u.email AS email, u.login AS login, u.name AS name, " +
                "u.birthday AS birthday, " +
                "NULL AS friends, NULL AS followers " +
                "FROM users AS u WHERE u.user_id = ?;";
        try {
            jdbc.queryForObject(queryGet, mapper, id1);
            jdbc.queryForObject(queryGet, mapper, id2);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь не найден");
        }
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

    @Override
    public Collection<User> getUsersByIds(Collection<Long> ids) {
        if (ids.isEmpty()) return Set.of();
        String idsForQuery = String.join(",", ids.stream().map(id -> "?").toList());
        String queryForReplace = "SELECT " +
                "u.user_id AS id, u.email AS email, u.login AS login, u.name AS name, u.birthday AS birthday, " +
                "ARRAY_AGG(DISTINCT f1.friend_id) AS friends, ARRAY_AGG(DISTINCT f2.user_id) AS followers " +
                "FROM users AS u " +
                "LEFT JOIN friends AS f1 ON u.user_id = f1.user_id " +
                "LEFT JOIN friends AS f2 ON u.user_id = f2.friend_id " +
                "WHERE u.user_id IN (IDS) " +
                "GROUP BY u.user_id;";
        String query = queryForReplace.replace("IDS", idsForQuery);
        return jdbc.query(query, mapper, ids.toArray());
    }

}