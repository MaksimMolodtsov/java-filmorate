package ru.yandex.practicum.filmorate.storage.film.dal.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import com.fasterxml.jackson.core.type.TypeReference;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.dal.MpaDbStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FilmRowMapper implements RowMapper<Film> {

    MpaDbStorage mpaDbStorage;

    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getLong("film_id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        film.setReleaseDate(resultSet.getDate("release_date").toLocalDate());
        film.setDuration(Duration.ofSeconds(resultSet.getObject("duration", Long.class)));

        Long rating = resultSet.getObject("rating_id", Long.class);
        if (rating != null) {
            Mpa mpa = new Mpa();
            mpa.setId(rating);
            mpa.setName(resultSet.getString("rating_name"));
            film.setMpa(mpa);
        }

        film.setLikes(doLongSet(resultSet.getArray("likes")));

        String genres = resultSet.getString("genres");
        if (genres != null && !genres.isBlank()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                Set<Genre> filmGenres = objectMapper.readValue(genres, new TypeReference<>() {
                });
                film.setGenres(filmGenres);
            } catch (JsonProcessingException ignored) {

            }
        }
        return film;
    }

    private Set<Long> doLongSet(java.sql.Array array) throws SQLException {
        if (array == null) return new HashSet<>();
        Object[] objectArray = (Object[]) array.getArray();
        return Arrays.stream(objectArray)
                .filter(Objects::nonNull)
                .map(o -> ((Number) o).longValue())
                .collect(Collectors.toSet());
    }
}