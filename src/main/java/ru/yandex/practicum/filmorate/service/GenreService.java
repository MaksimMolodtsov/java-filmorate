package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.dal.GenreDbStorage;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    public void validateFilmGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) return;
        String filterGenres = film.getGenres().stream()
                .filter(Objects::nonNull)
                .map(Genre::getId)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        List<Genre> dbGenres = getGenreByIdDb(filterGenres);

        Set<Long> filmGenreIds = film.getGenres().stream()
                .filter(Objects::nonNull)
                .map(Genre::getId)
                .collect(Collectors.toSet());

        Set<Long> dbGenreIds = dbGenres.stream()
                .filter(Objects::nonNull)
                .map(Genre::getId)
                .collect(Collectors.toSet());

        if (!Objects.equals(filmGenreIds, dbGenreIds)) {
            throw new NotFoundException("Жанры не найдены");
        }

    }

    private List<Genre> getGenreByIdDb(String idList) {
        if (idList == null || !idList.matches("[\\d,\\s]*"))
            throw new IllegalArgumentException("Неверный список id жанров");
        return genreDbStorage.getGenreByIdDb(idList);
    }

}
