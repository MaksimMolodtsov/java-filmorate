package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import java.util.Collection;

public interface FilmStorage {

    Collection<Film> allFilms();

    Film addFilm(Film film);

    Film updateFilm(Film newFilm);

    Film getFilmById(Long id);

    Film deleteFilmById(Long id);

}