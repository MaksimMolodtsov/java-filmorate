package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.dal.GenreDbStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreDbStorage genreDbStorage;

    public Collection<Genre> allGenres() {
        return genreDbStorage.allGenres();
    }

    public Genre getGenreById(Long id) {
        if (id == null) {
            log.warn("Ошибка валидации: Id должен быть указан для необходимого жанра");
            throw new ValidationException("Id должен быть указан");
        }
        return genreDbStorage.getGenreById(id);
    }
}
