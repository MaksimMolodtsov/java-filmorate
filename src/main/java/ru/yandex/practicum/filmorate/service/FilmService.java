package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Collection<Film> allFilms() {
        return filmStorage.allFilms();
    }

    public Film addFilm(Film film) {
        if (film.getReleaseDate() == null) {
            log.warn("Ошибка валидации: Должна быть указана дата релиза фильма {}", film);
            throw new ValidationException("Должна быть указана дата релиза фильма");
        } else if (film.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28))) {
            log.warn("Ошибка валидации: Дата релиза должна быть позже 28.12.1895 для {}", film);
            throw new ValidationException("Дата релиза должна быть позже 28.12.1895");
        }
        if (film.getDuration() == null) {
            log.warn("Ошибка валидации: Должны быть указана продолжительность фильма для {}", film);
            throw new ValidationException("Должны быть указана продолжительность фильма");
        } else if (film.getDuration().isNegative()) {
            log.warn("Ошибка валидации: Продолжительность не может быть отрицательным числом для {}", film);
            throw new ValidationException("Продолжительность не может быть отрицательным числом");
        }
        log.debug("Добавлен фильм {}", film);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film newFilm) {
        if (newFilm.getId() == null) {
            log.warn("Ошибка валидации: Id должен быть указан для {}", newFilm);
            throw new ValidationException("Id должен быть указан");
        }
        Film oldFilm = filmStorage.getFilmById(newFilm.getId());
        if (newFilm.getName() == null) {
            oldFilm.setName(oldFilm.getName());
        } else {
            oldFilm.setName(newFilm.getName());
        }
        if (newFilm.getDescription() == null) {
            oldFilm.setDescription(oldFilm.getDescription());
        } else {
            oldFilm.setDescription(newFilm.getDescription());
        }
        if (newFilm.getReleaseDate() == null) {
            oldFilm.setReleaseDate(oldFilm.getReleaseDate());
        } else if (newFilm.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28))) {
            log.warn("Ошибка валидации: Дата выхода фильма должна быть позже 28.12.1895 для {}", newFilm);
            throw new ValidationException("Дата релиза должна быть позже 28.12.1895");
        } else {
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
        }
        if (newFilm.getDuration() == null) {
            oldFilm.setDuration(oldFilm.getDuration());
        } else if (newFilm.getDuration().isNegative()) {
            log.warn("Ошибка валидации: Продолжительность фильма не может быть отрицательным числом для {}", newFilm);
            throw new ValidationException("Продолжительность не может быть отрицательным числом");
        } else {
            oldFilm.setDuration(newFilm.getDuration());
        }
        log.debug("Обновлен фильм {}", oldFilm);
        return filmStorage.updateFilm(oldFilm);
    }

    public Film getFilmById(Long id) {
        if (id == null) {
            log.warn("Ошибка валидации: Id должен быть указан для запрашиваемого фильма");
            throw new ValidationException("Id должен быть указан");
    }
        return filmStorage.getFilmById(id);
    }

    public Film deleteFilmById(Long id) {
        if (id == null) {
            log.warn("Ошибка валидации: Id должен быть указан для удаляемого фильма");
            throw new ValidationException("Id должен быть указан");
        }
        Film film = filmStorage.deleteFilmById(id);
        log.debug("Deleted film {}", film);
        return film;
    }

    //Likes

    public void addLike(Long filmId, Long userId) {
        User user = userStorage.getUserById(userId);
        Film film = filmStorage.getFilmById(filmId);
        film.getLikes().add(user.getId());
        log.debug("The user {} added like to the film {}", user, film);
    }

    public void removeLike(Long filmId, Long userId) {
        User user = userStorage.getUserById(userId);
        Film film = filmStorage.getFilmById(filmId);
        film.getLikes().remove(user.getId());
        log.debug("The user {} deleted the like of the film {}", user, film);
    }

    public List<Film> getPopularFilms(Integer count) {
        if (count == null || count < 0) throw new IllegalArgumentException("Count should be a positive");
        return filmStorage.allFilms().stream()
                .sorted((film1, film2) -> film2.getLikes().size() - film1.getLikes().size())
                .limit(count)
                .toList();
    }
}