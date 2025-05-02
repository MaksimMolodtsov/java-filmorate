package ru.yandex.practicum.filmorate.storage.film.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.dal.mappers.MpaRowMapper;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class MpaDbStorage {

    private final JdbcTemplate jdbc;
    private final MpaRowMapper mapper;

    public List<Mpa> allRatings() {
        String query = "SELECT rating_id, rating FROM mpa_rating;";
        return jdbc.query(query, mapper);
    }

    public Mpa getRatingById(Long id) {
        try {
            String query = "SELECT rating_id, rating FROM mpa_rating WHERE rating_id = ?";
            return jdbc.queryForObject(query, mapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("Рейтинг %d не найден", id));
        }
    }
}