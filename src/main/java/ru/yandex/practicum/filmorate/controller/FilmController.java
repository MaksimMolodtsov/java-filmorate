package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> allFilms() {
        return films.values();
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        try {
            if (film.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28))) {
                log.warn("Ошибка валидации: Дата релиза должна быть позже 28.12.1895 для {}", film);
                throw new ValidationException("Дата релиза должна быть позже 28.12.1895");
            }
        } catch (NullPointerException e) {
            log.warn("Ошибка валидации: Должна быть указана дата релиза фильма для {}", film);
            throw new ValidationException("Должна быть указана дата релиза фильма");
        }
        try {
            if (film.getDuration().isNegative()) {
                log.warn("Ошибка валидации: Продолжительность не может быть отрицательным числом для {}", film);
                throw new ValidationException("Продолжительность не может быть отрицательным числом");
            }
        } catch (NullPointerException e) {
            log.warn("Ошибка валидации: Должны быть указана продолжительность фильма для {}", film);
            throw new ValidationException("Должны быть указана продолжительность фильма");
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Добавлен фильм {}", film);
        return film;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            log.warn("Ошибка валидации: Id должен быть указан для {}", newFilm);
            throw new ValidationException("Id должен быть указан");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
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
            log.info("Обновлен фильм {}", oldFilm);
            return oldFilm;
        }
        log.warn("Ошибка валидации: Фильм не найден для id = {}", newFilm.getId());
        throw new ValidationException("Фильм с id = " + newFilm.getId() + " не найден");
    }
}