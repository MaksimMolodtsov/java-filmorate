package ru.yandex.practicum.filmorate.storage.film.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.dal.mappers.FilmRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.Objects;

@RequiredArgsConstructor
@Repository
@Primary
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbc;
    private final FilmRowMapper mapper;

    @Override
    public Collection<Film> allFilms() {
        String query = """
                    SELECT
                    f.film_id AS film_id,
                    f.name AS name,
                    f.description AS description,
                    f.release_date AS release_date,
                    f.duration AS duration,
                    f.rating_id AS rating_id,
                    r.name AS rating_name,
                    ARRAY_AGG(DISTINCT l.user_id) AS likes,
                    CAST(
                    JSON_ARRAYAGG(
                    DISTINCT JSON_OBJECT(
                    'id': g.genre_id,
                    'name': g.name
                    )
                    ) FILTER (WHERE g.genre_id IS NOT NULL) AS VARCHAR
                    ) AS genres
                    FROM films AS f
                    LEFT JOIN likes AS l ON f.film_id = l.film_id
                    LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id
                    LEFT JOIN genres AS g ON g.genre_id = fg.genre_id
                    LEFT JOIN mpa_rating AS r ON f.rating_id = r.rating_id
                    GROUP BY f.film_id;
                    """;
        return jdbc.query(query, mapper);
    }

    @Override
    public Film addFilm(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String queryAddGenres = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        String query = "INSERT INTO films (name, description, release_date, duration, rating_id) VALUES (?,?,?,?,?);";
        try {
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(query, new String[]{"film_id"});
                ps.setString(1, film.getName());
                ps.setString(2, film.getDescription());
                ps.setDate(3, Date.valueOf(film.getReleaseDate()));
                ps.setLong(4, film.getDuration().toSeconds());
                ps.setLong(5, film.getMpa().getId());
                return ps;
            }, keyHolder);
        } catch (DuplicateKeyException e) {
            throw new ValidationException("В базе данных уже содержится такой id");
        }
        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        for (Genre genre : film.getGenres()) {
            if (genre.getId() != null) {
                jdbc.update(queryAddGenres, film.getId(), genre.getId());
            }
        }
        return film;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        getFilmById(newFilm.getId());
        String query = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? " +
                "WHERE film_id = ?;";
        String queryRemoveGenres = "DELETE FROM film_genres WHERE film_id = ?;";
        String queryAddGenres = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        int rowsUpdated = jdbc.update(query, newFilm.getName(), newFilm.getDescription(), newFilm.getReleaseDate(),
                newFilm.getDuration().toSeconds(), newFilm.getMpa().getId(), newFilm.getId());
        if (rowsUpdated == 0) {
            throw new RuntimeException("Не удалось обновить данные");
        }
        int rowsUpdatedRemoveGenres = jdbc.update(queryRemoveGenres, newFilm.getId());
        if (rowsUpdatedRemoveGenres == 0) {
            throw new RuntimeException("Не удалось удалить жанры фильма");
        }
        for (Genre genre : newFilm.getGenres()) {
            if (genre.getId() != null) {
                int rowsUpdatedAddGenres = jdbc.update(queryAddGenres, newFilm.getId(), genre.getId());
                if (rowsUpdatedAddGenres == 0) {
                    throw new RuntimeException("Не удалось добавить жанры фильма");
                }
            }
        }
        return newFilm;
    }

    @Override
    public Film getFilmById(Long id) {
        try {
            String query = """
                    SELECT
                    f.film_id AS film_id,
                    f.name AS name,
                    f.description AS description,
                    f.release_date AS release_date,
                    f.duration AS duration,
                    f.rating_id AS rating_id,
                    r.name AS rating_name,
                    ARRAY_AGG(DISTINCT l.user_id) AS likes,
                    CAST(
                    JSON_ARRAYAGG(
                    DISTINCT JSON_OBJECT(
                    'id': g.genre_id,
                    'name': g.name
                    )
                    ) FILTER (WHERE g.genre_id IS NOT NULL) AS VARCHAR
                    ) AS genres
                    FROM films AS f
                    LEFT JOIN likes AS l ON f.film_id = l.film_id
                    LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id
                    LEFT JOIN genres AS g ON g.genre_id = fg.genre_id
                    LEFT JOIN mpa_rating AS r ON f.rating_id = r.rating_id
                    WHERE f.film_id = ?
                    GROUP BY f.film_id;
                    """;
            return jdbc.queryForObject(query, mapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("Фильм %d не найден", id));
        }
    }

    @Override
    public Film deleteFilmById(Long id) {
        Film film = getFilmById(id);
        String query = "DELETE FROM films WHERE film_id = ?;";
        jdbc.update(query, id);
        return film;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String query = "INSERT INTO likes (film_id, user_id) VALUES (?, ?);";
        jdbc.update(query, filmId, userId);

    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String query = "DELETE FROM likes WHERE film_id = ? AND user_id = ?;";
        jdbc.update(query, filmId, userId);
    }

    @Override
    public Collection<Film> getPopularFilms(Integer count) {
        String query = """
                    SELECT
                    f.film_id AS film_id,
                    f.name AS name,
                    f.description AS description,
                    f.release_date AS release_date,
                    f.duration AS duration,
                    f.rating_id AS rating_id,
                    r.name AS rating_name,
                    ARRAY_AGG(DISTINCT l.user_id) AS likes,
                    CAST(
                    JSON_ARRAYAGG(
                    DISTINCT JSON_OBJECT(
                    'id': g.genre_id,
                    'name': g.name
                    )
                    ) FILTER (WHERE g.genre_id IS NOT NULL) AS VARCHAR
                    ) AS genres
                    FROM films AS f
                    LEFT JOIN likes AS l ON f.film_id = l.film_id
                    LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id
                    LEFT JOIN genres AS g ON g.genre_id = fg.genre_id
                    LEFT JOIN mpa_rating AS r ON f.rating_id = r.rating_id
                    GROUP BY f.film_id
                    ORDER BY COUNT(DISTINCT l.user_id) DESC
                    LIMIT ?;
                    """;
        return jdbc.query(query, mapper, count);
    }

}
