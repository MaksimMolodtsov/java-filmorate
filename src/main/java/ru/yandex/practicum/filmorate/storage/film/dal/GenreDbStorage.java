package ru.yandex.practicum.filmorate.storage.film.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.dal.mappers.GenreRowMapper;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class GenreDbStorage {

    private final JdbcTemplate jdbc;
    private final GenreRowMapper mapper;

    public List<Genre> allGenres() {
        String query = "SELECT genre_id, name FROM genres;";
        return jdbc.query(query, mapper);
    }

    public Genre getGenreById(Long id) {
        try {
            String query = "SELECT genre_id, name FROM genres WHERE genre_id = ?";
            return jdbc.queryForObject(query, mapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("Жанр %d не найден", id));
        }
    }

    public List<Genre> getGenreByIdDb(String idList) {
        String queryForReplace = "SELECT genre_id, name FROM genres WHERE genre_id IN ( id_List )";
        String query = queryForReplace.replace("id_List", idList);
        try {
            return jdbc.query(query, mapper);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Жанр не найден");
        }
    }
}
