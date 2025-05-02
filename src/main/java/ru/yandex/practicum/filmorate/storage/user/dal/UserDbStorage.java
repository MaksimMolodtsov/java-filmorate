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
        return user;
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
                    " birthday, ARRAY_AGG(DISTINCT f1.friend_id) AS followers, ARRAY_AGG(DISTINCT f2.user_id) AS " +
                    "friends FROM users AS u " +
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
