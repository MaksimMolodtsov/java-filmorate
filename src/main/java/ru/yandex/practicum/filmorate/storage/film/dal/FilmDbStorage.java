package ru.yandex.practicum.filmorate.storage.film.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
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
import java.util.List;

@RequiredArgsConstructor
@Repository
@Primary
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbc;
    private final FilmRowMapper mapper;

    @Override
    public Collection<Film> allFilms() {
        String query = "SELECT * FROM films;";
        return jdbc.query(query, mapper);
    }

    @Override
    public Film addFilm(Film film) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
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
        film.setId(keyHolder.getKeyAs(Long.class));
        for (Genre genre : film.getGenres()) {
            if (genre.getId() != null) {
                int rowsUpdatedAddGenres = jdbc.update(queryAddGenres, film.getId(), genre.getId());
                if (rowsUpdatedAddGenres == 0) {
                    throw new RuntimeException("Не удалось добавить жанры фильма");
                }
            }
        }
        return film;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        String query = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? " +
                "WHERE film_id = ?;";
        String queryRemoveGenres = "DELETE FROM film_genres WHERE film_id = ?;";
        String queryAddGenres = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        getFilmById(newFilm.getId());
        Long newDuration = (newFilm.getDuration() == null) ? null : newFilm.getDuration().toSeconds();
        Long newRating = (newFilm.getMpa() == null) ? null : newFilm.getMpa().getId();
        int rowsUpdated = jdbc.update(query, newFilm.getName(), newFilm.getDescription(), newFilm.getReleaseDate(),
                newDuration, newRating, newFilm.getId());
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
        return getFilmById(newFilm.getId());
    }

    @Override
    public Film getFilmById(Long id) {
        try {
            String query = "SELECT * FROM films WHERE film_id = ?;";
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
        try {
            jdbc.update(query, filmId, userId);
        } catch (DuplicateKeyException ignored) {}
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String query = "DELETE FROM likes WHERE film_id = ? AND user_id = ?;";
        jdbc.update(query, filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(Integer count) {
        return allFilms().stream()
                .sorted((f1, f2) -> f2.getLikes().size() - f1.getLikes().size())
                .limit(count)
                .toList();
    }

}
