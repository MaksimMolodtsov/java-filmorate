package ru.yandex.practicum.filmorate.storage.film.dal.mappers;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MpaRowMapper implements RowMapper<Mpa> {

    @Override
    public Mpa mapRow(ResultSet rs, int rowNum) throws SQLException {
        Mpa rating = new Mpa();
        rating.setId(rs.getLong("rating_id"));
        rating.setTitle(rs.getString("rating"));
        return rating;
    }

}
